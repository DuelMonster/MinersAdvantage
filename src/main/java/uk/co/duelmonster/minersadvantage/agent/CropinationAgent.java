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
 * CropinationAgent: harvests all mature crops in a radius.
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

    public CropinationAgent(ServerPlayer player, BlockPos origin, int radius) {
        this(player, origin, radius, MAServerRootConfig.defaults().cropination(), new CommonConfig());
    }

    public CropinationAgent(ServerPlayer player, BlockPos origin, int radius, CropinationConfig config, CommonConfig commonConfig) {
        super(player);
        CropinationConfig effectiveConfig = config == null ? MAServerRootConfig.defaults().cropination() : config;
        this.origin = origin;
        BlockState originState = world.getBlockState(origin);
        this.allowNoWaterFallback = isWaterIndependentCrop(originState);

        BlockPos waterSource = findClosestWaterSource(origin.below(), FARM_PLOT_RADIUS);
        this.hasWaterSource = waterSource != null;
        if (hasWaterSource) {
            this.minX = waterSource.getX() - FARM_PLOT_RADIUS;
            this.maxX = waterSource.getX() + FARM_PLOT_RADIUS;
            this.minZ = waterSource.getZ() - FARM_PLOT_RADIUS;
            this.maxZ = waterSource.getZ() + FARM_PLOT_RADIUS;
            seedQueueForPatch();
        } else if (allowNoWaterFallback) {
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

    @Override
    public boolean tick() {
        if (!hasWaterSource && !allowNoWaterFallback) {
            return finish("no nearby water source");
        }

        int count = 0;
        while (!queue.isEmpty() && count < blocksPerTick) {
            BlockPos pos = queue.poll();
            if (pos == null || !withinFarmPatch(pos)) {
                continue;
            }

            BlockState state = world.getBlockState(pos);
            if (!isCropLike(state)) {
                continue;
            }

            count++;

            Block block = state.getBlock();
            if (isMatureCrop(state)) {
                harvestAndReplant(pos, state, block);
            }
        }
        if (queue.isEmpty()) {
            return finish("crop queue exhausted");
        }
        return false;
    }

    private boolean withinFarmPatch(BlockPos pos) {
        return pos.getY() == origin.getY()
            && pos.getX() >= minX
            && pos.getX() <= maxX
            && pos.getZ() >= minZ
            && pos.getZ() <= maxZ;
    }

    private void seedQueueForPatch() {
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                queue.add(new BlockPos(x, origin.getY(), z));
            }
        }
    }

    private void harvestAndReplant(BlockPos pos, BlockState state, Block block) {
        boolean broken = player.gameMode.destroyBlock(pos);
        if (!broken) {
            return;
        }

        if (!harvestSeeds) {
            removeSeedDropsNear(pos, resolveSeedItem(block));
        }

        world.setBlock(pos, replantedState(state, block), 3);
    }

    private BlockState replantedState(BlockState state, Block block) {
        if (block instanceof CropBlock cropBlock) {
            return cropBlock.getStateForAge(0);
        }
        if (block instanceof NetherWartBlock && state.hasProperty(NetherWartBlock.AGE)) {
            return state.setValue(NetherWartBlock.AGE, 0);
        }
        return block.defaultBlockState();
    }

    private boolean isMatureCrop(BlockState state) {
        Block block = state.getBlock();
        if (block instanceof CropBlock cropBlock) {
            return cropBlock.isMaxAge(state);
        }
        if (block instanceof NetherWartBlock && state.hasProperty(NetherWartBlock.AGE)) {
            return state.getValue(NetherWartBlock.AGE) >= NetherWartBlock.MAX_AGE;
        }
        for (var property : state.getProperties()) {
            if (property instanceof IntegerProperty integerProperty && "age".equals(integerProperty.getName())) {
                Integer value = state.getValue(integerProperty);
                return value != null && value >= integerProperty.getPossibleValues().stream().mapToInt(Integer::intValue).max().orElse(0);
            }
        }
        return false;
    }

    private boolean isCropLike(BlockState state) {
        Block block = state.getBlock();
        if (block instanceof CropBlock || block instanceof NetherWartBlock) {
            return true;
        }
        for (var property : state.getProperties()) {
            if (property instanceof IntegerProperty integerProperty && "age".equals(integerProperty.getName())) {
                return true;
            }
        }
        return false;
    }

    private boolean isWaterIndependentCrop(BlockState state) {
        return state.getBlock() instanceof NetherWartBlock;
    }

    private Item resolveSeedItem(Block block) {
        if (block instanceof NetherWartBlock) {
            return Items.NETHER_WART;
        }
        Item blockItem = block.asItem();
        return blockItem == null ? Items.AIR : blockItem;
    }

    private void removeSeedDropsNear(BlockPos pos, Item seedItem) {
        if (seedItem == null || seedItem == Items.AIR) {
            return;
        }
        AABB box = new AABB(pos).inflate(0.9D);
        for (ItemEntity entity : world.getEntitiesOfClass(ItemEntity.class, box)) {
            ItemStack stack = entity.getItem();
            if (!stack.isEmpty() && stack.getItem() == seedItem) {
                entity.discard();
            }
        }
    }

    private BlockPos findClosestWaterSource(BlockPos start, int maxDistance) {
        for (int offset = 1; offset <= maxDistance; offset++) {
            for (int x = start.getX() - offset; x <= start.getX() + offset; x++) {
                for (int z = start.getZ() - offset; z <= start.getZ() + offset; z++) {
                    BlockPos candidate = new BlockPos(x, start.getY(), z);
                    BlockState state = world.getBlockState(candidate);
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
