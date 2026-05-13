package uk.co.duelmonster.minersadvantage.agent;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedList;
import java.util.Queue;

/**
 * ShaftanationAgent: digs a vertical shaft down from the origin.
 */
public class ShaftanationAgent extends Agent {
    private final BlockPos origin;
    private final int depth;
    private final Queue<BlockPos> queue = new LinkedList<>();
    private int dug = 0;
    private int blocksPerTick = 8;

    public ShaftanationAgent(ServerPlayer player, BlockPos origin, int depth) {
        super(player);
        this.origin = origin;
        this.depth = depth;
        for (int i = 1; i <= depth; i++) {
            queue.add(origin.below(i));
        }
    }

    @Override
    public boolean tick() {
        int count = 0;
        while (!queue.isEmpty() && count < blocksPerTick) {
            BlockPos pos = queue.poll();
            BlockState state = world.getBlockState(pos);
            if (!state.isAir()) {
                world.destroyBlock(pos, true, player);
                dug++;
                count++;
            }
        }
        if (queue.isEmpty() || dug >= depth) {
            return finish(queue.isEmpty() ? "shaft queue exhausted" : "shaft target reached");
        }
        return false;
    }
}