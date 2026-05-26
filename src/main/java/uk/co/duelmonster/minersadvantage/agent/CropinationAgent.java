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
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import uk.co.duelmonster.minersadvantage.common.config.CommonConfig;
import uk.co.duelmonster.minersadvantage.common.config.CropinationConfig;
import uk.co.duelmonster.minersadvantage.common.config.MAServerRootConfig;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * CropinationAgent: harvests all mature crops in a radius.
 */
public class CropinationAgent extends Agent {
    private final BlockPos origin;
    private final int radius;
    private final Queue<BlockPos> queue = new LinkedList<>();
    private final Set<BlockPos> visited = new HashSet<>();
    private final int blocksPerTick;
    private final int blockLimit;
    private final boolean harvestSeeds;
    private int processed = 0;

    public CropinationAgent(ServerPlayer player, BlockPos origin, int radius) {
        this(player, origin, radius, MAServerRootConfig.defaults().cropination(), new CommonConfig());
    }

    public CropinationAgent(ServerPlayer player, BlockPos origin, int radius, CropinationConfig config, CommonConfig commonConfig) {
        super(player);
        CropinationConfig effectiveConfig = config == null ? MAServerRootConfig.defaults().cropination() : config;
        this.origin = origin;
        this.radius = Math.max(1, radius);
        int globalBlocksPerTick = commonConfig == null ? 1 : Math.max(1, commonConfig.blocksPerTick());
        this.blocksPerTick = globalBlocksPerTick;
        this.blockLimit = commonConfig == null ? 64 : Math.max(1, commonConfig.blockLimit());
        this.harvestSeeds = effectiveConfig.harvestSeeds();
        queue.add(origin);
    }

    @Override
    public boolean tick() {
        int count = 0;
        while (!queue.isEmpty() && count < blocksPerTick && processed < blockLimit) {
            BlockPos pos = queue.poll();
            if (pos == null || !visited.add(pos) || !withinRadius(pos)) {
                continue;
            }

            BlockState state = world.getBlockState(pos);
            if (!isCropLike(state)) {
                continue;
            }

            count++;
            // Scan contiguous crop patches even when the clicked crop is immature.
            for (int dx = -1; dx <= 1; dx++)
                for (int dz = -1; dz <= 1; dz++)
                    queue.add(pos.offset(dx, 0, dz));

            Block block = state.getBlock();
            if (isMatureCrop(state)) {
                harvestAndReplant(pos, state, block);
                processed++;
            }
        }
        if (queue.isEmpty() || processed >= blockLimit) {
            return finish(queue.isEmpty() ? "crop queue exhausted" : "crop block limit reached");
        }
        return false;
    }

    private boolean withinRadius(BlockPos pos) {
        int dx = Math.abs(pos.getX() - origin.getX());
        int dz = Math.abs(pos.getZ() - origin.getZ());
        return dx <= radius && dz <= radius;
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
}
