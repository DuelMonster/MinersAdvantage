package uk.co.duelmonster.minersadvantage.agent;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedList;
import java.util.Queue;

/**
 * CultivationAgent: tills and plants farmland in a radius.
 */
public class CultivationAgent extends Agent {
    private final BlockPos origin;
    private final int radius;
    private final Queue<BlockPos> queue = new LinkedList<>();
    private int blocksPerTick = 8;
    private int processed = 0;
    private int blockLimit = 64;

    public CultivationAgent(ServerPlayer player, BlockPos origin, int radius) {
        super(player);
        this.origin = origin;
        this.radius = radius;
        queue.add(origin);
    }

    @Override
    public boolean tick() {
        int count = 0;
        while (!queue.isEmpty() && count < blocksPerTick && processed < blockLimit) {
            BlockPos pos = queue.poll();
            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            if (block == Blocks.DIRT || block == Blocks.GRASS_BLOCK) {
                world.setBlockAndUpdate(pos, Blocks.FARMLAND.defaultBlockState());
                processed++;
                count++;
                // Add neighbors in a 3x3 area
                for (int dx = -1; dx <= 1; dx++)
                    for (int dz = -1; dz <= 1; dz++)
                        queue.add(pos.offset(dx, 0, dz));
            }
        }
        if (queue.isEmpty() || processed >= blockLimit) {
            return finish(queue.isEmpty() ? "cultivation queue exhausted" : "cultivation block limit reached");
        }
        return false;
    }
}