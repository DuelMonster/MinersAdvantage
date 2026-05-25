package uk.co.duelmonster.minersadvantage.agent;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import uk.co.duelmonster.minersadvantage.common.config.CommonConfig;
import uk.co.duelmonster.minersadvantage.common.config.CropinationConfig;
import uk.co.duelmonster.minersadvantage.common.config.MAServerRootConfig;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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
            Block block = state.getBlock();
            if (isMatureCrop(state)) {
                harvestAndReplant(pos, state, block);
                processed++;
                count++;
                // Add neighbors in a 3x3 area
                for (int dx = -1; dx <= 1; dx++)
                    for (int dz = -1; dz <= 1; dz++)
                        queue.add(pos.offset(dx, 0, dz));
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
        List<ItemStack> drops = Block.getDrops(state, (net.minecraft.server.level.ServerLevel) world, pos, null, player, player.getMainHandItem());
        boolean consumedSeedForReplant = false;

        for (ItemStack drop : drops) {
            if (drop == null || drop.isEmpty()) {
                continue;
            }

            ItemStack output = drop.copy();
            boolean isSeedItem = output.getItem() instanceof net.minecraft.world.item.BlockItem;

            if (isSeedItem && !consumedSeedForReplant) {
                output.shrink(1);
                consumedSeedForReplant = true;
            }

            if (output.isEmpty()) {
                continue;
            }

            if (harvestSeeds || !isSeedItem) {
                Block.popResource(world, pos, output);
            }
        }

        world.setBlockAndUpdate(pos, replantedState(state, block));
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
}