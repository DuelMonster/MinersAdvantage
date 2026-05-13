package uk.co.duelmonster.minersadvantage.agent;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedList;
import java.util.Queue;

/**
 * VeinationAgent: breaks all connected ore blocks (vein mining).
 */
public class VeinationAgent extends Agent {
    private final BlockPos origin;
    private final Block originBlock;
    private final Queue<BlockPos> queue = new LinkedList<>();
    private int blocksPerTick = 8;
    private int processed = 0;
    private int blockLimit = 64;

    public VeinationAgent(ServerPlayer player, BlockPos origin) {
        super(player);
        this.origin = origin;
        BlockState state = world.getBlockState(origin);
        this.originBlock = state.getBlock();
        queue.add(origin);
    }

    @Override
    public boolean tick() {
        int count = 0;
        while (!queue.isEmpty() && count < blocksPerTick && processed < blockLimit) {
            BlockPos pos = queue.poll();
            BlockState state = world.getBlockState(pos);
            if (isOre(state)) {
                world.destroyBlock(pos, true, player);
                processed++;
                count++;
                // Add neighbors in a 3x3x3 cube
                for (int dx = -1; dx <= 1; dx++)
                    for (int dy = -1; dy <= 1; dy++)
                        for (int dz = -1; dz <= 1; dz++)
                            queue.add(pos.offset(dx, dy, dz));
            }
        }
        if (queue.isEmpty() || processed >= blockLimit) {
            return finish(queue.isEmpty() ? "vein queue exhausted" : "vein block limit reached");
        }
        return false;
    }

    private boolean isOre(BlockState state) {
        return state.is(BlockTags.COAL_ORES)
            || state.is(BlockTags.IRON_ORES)
            || state.is(BlockTags.COPPER_ORES)
            || state.is(BlockTags.GOLD_ORES)
            || state.is(BlockTags.REDSTONE_ORES)
            || state.is(BlockTags.EMERALD_ORES)
            || state.is(BlockTags.LAPIS_ORES)
            || state.is(BlockTags.DIAMOND_ORES)
            || state.is(Blocks.ANCIENT_DEBRIS);
    }
}