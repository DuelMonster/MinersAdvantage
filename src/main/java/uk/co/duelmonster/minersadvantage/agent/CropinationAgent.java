package uk.co.duelmonster.minersadvantage.agent;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import uk.co.duelmonster.minersadvantage.common.config.CommonConfig;
import uk.co.duelmonster.minersadvantage.common.config.CropinationConfig;
import uk.co.duelmonster.minersadvantage.common.config.MAServerRootConfig;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Cropination worker that scans a farm patch, harvests mature crops, and replants immediately.
 * Goal: keep farms productive without requiring players to do endless right-click cardio.
 */
public class CropinationAgent extends Agent {
    private static final int FARM_PLOT_RADIUS = 4;

    private final BlockPos origin;
    private final int minX;
    private final int maxX;
    private final int minZ;
    private final int maxZ;
    private final boolean hasWaterSource;
    private final boolean allowNoWaterFallback;
    private final Queue<BlockPos> queue = new LinkedList<>();
    private final int blocksPerTick;
    private final boolean harvestSeeds;

    /**
     * Convenience constructor using default server and common configs.
     */
    public CropinationAgent(ServerPlayer player, BlockPos origin, int radius) {
        this(player, origin, radius, MAServerRootConfig.defaults().cropination(), new CommonConfig());
    }

    /**
     * Build cropination scan bounds from nearest water source, with optional nether-wart fallback.
     */
    public CropinationAgent(ServerPlayer player, BlockPos origin, int radius, CropinationConfig config, CommonConfig commonConfig) {
        super(player);
        CropinationConfig effectiveConfig = config == null ? MAServerRootConfig.defaults().cropination() : config;
        this.origin = origin;
        BlockState originState = world.getBlockState(origin);
        this.allowNoWaterFallback = isWaterIndependentCrop(originState);

        BlockPos waterSource = findClosestWaterSource(origin.below(), FARM_PLOT_RADIUS);
        this.hasWaterSource = waterSource != null;
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (hasWaterSource) {
            // Hydrated patch mode: center scan bounds around the discovered water source.
            this.minX = waterSource.getX() - FARM_PLOT_RADIUS;
            this.maxX = waterSource.getX() + FARM_PLOT_RADIUS;
            this.minZ = waterSource.getZ() - FARM_PLOT_RADIUS;
            this.maxZ = waterSource.getZ() + FARM_PLOT_RADIUS;
            seedQueueForPatch();
        } else if (allowNoWaterFallback) {
            // Water-independent crops (like nether wart) still deserve automation.
            this.minX = origin.getX() - FARM_PLOT_RADIUS;
            this.maxX = origin.getX() + FARM_PLOT_RADIUS;
            this.minZ = origin.getZ() - FARM_PLOT_RADIUS;
            this.maxZ = origin.getZ() + FARM_PLOT_RADIUS;
            seedQueueForPatch();
        } else {
            this.minX = origin.getX();
            this.maxX = origin.getX();
            this.minZ = origin.getZ();
            this.maxZ = origin.getZ();
        }

        int globalBlocksPerTick = commonConfig == null ? 1 : Math.max(1, commonConfig.blocksPerTick());
        this.blocksPerTick = globalBlocksPerTick;
        this.harvestSeeds = effectiveConfig.harvestSeeds();
    }

    /**
     * Per-tick farm processing loop with bounded work budget.
     */
    @Override
    /**
     * t ic k exists so this path stays predictable and easier to debug when things get weird.
     */
    public boolean tick() {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (!hasWaterSource && !allowNoWaterFallback) {
            return finish("no nearby water source");
        }

        int count = 0;
        // Process queued crop positions until tick budget is consumed.
        while (!queue.isEmpty() && count < blocksPerTick) {
            BlockPos pos = queue.poll();
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (pos == null || !withinFarmPatch(pos)) {
                continue;
            }

            BlockState state = world.getBlockState(pos);
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (!isCropLike(state)) {
                continue;
            }

            count++;

            Block block = state.getBlock();
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (isMatureCrop(state)) {
                harvestAndReplant(pos, state, block);
            }
        }
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (queue.isEmpty()) {
            return finish("crop queue exhausted");
        }
        return false;
    }

    /**
     * Check whether position belongs to this agent's active farm patch at origin Y level.
     */
    private boolean withinFarmPatch(BlockPos pos) {
        return pos.getY() == origin.getY()
            && pos.getX() >= minX
            && pos.getX() <= maxX
            && pos.getZ() >= minZ
            && pos.getZ() <= maxZ;
    }

    /**
     * Seed processing queue with every block position in the selected patch.
     */
    private void seedQueueForPatch() {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        for (int x = minX; x <= maxX; x++) {
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            for (int z = minZ; z <= maxZ; z++) {
                queue.add(new BlockPos(x, origin.getY(), z));
            }
        }
    }

    /**
     * Break mature crop and immediately replant base-age state.
     */
    private void harvestAndReplant(BlockPos pos, BlockState state, Block block) {
        boolean broken = player.gameMode.destroyBlock(pos);
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (!broken) {
            return;
        }

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (!harvestSeeds) {
            removeSeedDropsNear(pos, resolveSeedItem(block));
        }

        world.setBlock(pos, replantedState(state, block), 3);
    }

    /**
     * Compute replanted state for known crop types, falling back to block default when needed.
     */
    private BlockState replantedState(BlockState state, Block block) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (block instanceof CropBlock cropBlock) {
            return cropBlock.getStateForAge(0);
        }
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (block instanceof NetherWartBlock && state.hasProperty(NetherWartBlock.AGE)) {
            return state.setValue(NetherWartBlock.AGE, 0);
        }
        return block.defaultBlockState();
    }

    /**
     * Determine whether crop state is mature enough to harvest.
     */
    private boolean isMatureCrop(BlockState state) {
        Block block = state.getBlock();
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (block instanceof CropBlock cropBlock) {
            return cropBlock.isMaxAge(state);
        }
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (block instanceof NetherWartBlock && state.hasProperty(NetherWartBlock.AGE)) {
            return state.getValue(NetherWartBlock.AGE) >= NetherWartBlock.MAX_AGE;
        }
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        for (var property : state.getProperties()) {
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (property instanceof IntegerProperty integerProperty && "age".equals(integerProperty.getName())) {
                Integer value = state.getValue(integerProperty);
                return value != null && value >= integerProperty.getPossibleValues().stream().mapToInt(Integer::intValue).max().orElse(0);
            }
        }
        return false;
    }

    /**
     * Determine whether block state behaves like a harvestable crop.
     */
    private boolean isCropLike(BlockState state) {
        Block block = state.getBlock();
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (block instanceof CropBlock || block instanceof NetherWartBlock) {
            return true;
        }
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        for (var property : state.getProperties()) {
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (property instanceof IntegerProperty integerProperty && "age".equals(integerProperty.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Identify crops that do not require nearby water to be valid for automation.
     */
    private boolean isWaterIndependentCrop(BlockState state) {
        return state.getBlock() instanceof NetherWartBlock;
    }

    /**
     * Resolve seed item for drop-filter logic when harvestSeeds is disabled.
     */
    private Item resolveSeedItem(Block block) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (block instanceof NetherWartBlock) {
            return Items.NETHER_WART;
        }
        Item blockItem = block.asItem();
        return blockItem == null ? Items.AIR : blockItem;
    }

    /**
     * Remove nearby seed drops when configured, so harvest output can exclude seed returns.
     */
    private void removeSeedDropsNear(BlockPos pos, Item seedItem) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (seedItem == null || seedItem == Items.AIR) {
            return;
        }
        AABB box = new AABB(pos).inflate(0.9D);
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        for (ItemEntity entity : world.getEntitiesOfClass(ItemEntity.class, box)) {
            ItemStack stack = entity.getItem();
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (!stack.isEmpty() && stack.getItem() == seedItem) {
                entity.discard();
            }
        }
    }

    /**
     * Find nearest water source in expanding square rings around start position.
     */
    private BlockPos findClosestWaterSource(BlockPos start, int maxDistance) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        for (int offset = 1; offset <= maxDistance; offset++) {
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            for (int x = start.getX() - offset; x <= start.getX() + offset; x++) {
                // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                for (int z = start.getZ() - offset; z <= start.getZ() + offset; z++) {
                    BlockPos candidate = new BlockPos(x, start.getY(), z);
                    BlockState state = world.getBlockState(candidate);
                    // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                    if (state.getFluidState().is(Fluids.WATER)
                        || (state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED))) {
                        return candidate;
                    }
                }
            }
        }
        return null;
    }
}
