package uk.co.duelmonster.minersadvantage.agent;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedList;
import java.util.Queue;

/**
 * VentilationAgent: digs a horizontal tunnel for ventilation.
 */
public class VentilationAgent extends Agent {
    private final BlockPos origin;
    private final int length;
    private final Queue<BlockPos> queue = new LinkedList<>();
    private int dug = 0;
    private int blocksPerTick = 8;

    public VentilationAgent(ServerPlayer player, BlockPos origin, int length) {
        super(player);
        this.origin = origin;
        this.length = length;
        for (int i = 1; i <= length; i++) {
            queue.add(origin.offset(i, 0, 0));
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
        if (queue.isEmpty() || dug >= length) {
            return finish(queue.isEmpty() ? "ventilation queue exhausted" : "ventilation target reached");
        }
        return false;
    }
}