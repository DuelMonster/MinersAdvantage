package uk.co.duelmonster.minersadvantage.agent;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedList;
import java.util.Queue;

/**
 * PathanationAgent: creates a path (dirt path blocks) in a line from the player.
 */
public class PathanationAgent extends Agent {
    private final BlockPos origin;
    private final int length;
    private final Queue<BlockPos> queue = new LinkedList<>();
    private int placed = 0;
    private int blocksPerTick = 4;

    public PathanationAgent(ServerPlayer player, BlockPos origin, int length) {
        super(player);
        this.origin = origin;
        this.length = length;
        for (int i = 1; i <= length; i++) {
            queue.add(origin.offset(0, 0, i));
        }
    }

    @Override
    public boolean tick() {
        int count = 0;
        while (!queue.isEmpty() && count < blocksPerTick) {
            BlockPos pos = queue.poll();
            BlockState state = world.getBlockState(pos);
            if (state.getBlock() == Blocks.DIRT || state.getBlock() == Blocks.GRASS_BLOCK) {
                world.setBlockAndUpdate(pos, Blocks.DIRT_PATH.defaultBlockState());
                placed++;
                count++;
            }
        }
        if (queue.isEmpty() || placed >= length) {
            return finish(queue.isEmpty() ? "path queue exhausted" : "path target reached");
        }
        return false;
    }
}