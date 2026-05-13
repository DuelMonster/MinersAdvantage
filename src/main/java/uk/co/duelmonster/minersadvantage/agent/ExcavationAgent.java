package uk.co.duelmonster.minersadvantage.agent;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Modernized ExcavationAgent: actually breaks blocks in the world, production-wired.
 */
public class ExcavationAgent extends Agent {
    private final BlockPos origin;
    private final Block originBlock;
    private final int radius;
    private final Queue<BlockPos> queue = new LinkedList<>();
    private int blocksPerTick = 5;
    private int processed = 0;
    private int blockLimit = 64;

    public ExcavationAgent(ServerPlayer player, BlockPos origin, int radius) {
        this(player, origin, radius, player.level().getBlockState(origin).getBlock());
    }

    public ExcavationAgent(ServerPlayer player, BlockPos origin, int radius, Block originBlock) {
        super(player);
        this.origin = origin;
        this.originBlock = originBlock;
        this.radius = radius;
        queue.add(origin);
    }

    @Override
    public boolean tick() {
        int count = 0;
        while (!queue.isEmpty() && count < blocksPerTick && processed < blockLimit) {
            BlockPos pos = queue.poll();
            BlockState state = world.getBlockState(pos);
            if (pos.equals(origin) && state.getBlock() == Blocks.AIR) {
                queue.add(pos.above());
                queue.add(pos.below());
                queue.add(pos.north());
                queue.add(pos.south());
                queue.add(pos.east());
                queue.add(pos.west());
                continue;
            }
            if (state.getBlock() == originBlock && state.getBlock() != Blocks.AIR) {
                world.destroyBlock(pos, true, player);
                processed++;
                count++;
                // Add neighbors
                for (BlockPos n : BlockPos.betweenClosedStream(pos.offset(-1,0,0), pos.offset(1,0,0)).map(BlockPos::immutable).toList())
                    if (!n.equals(pos)) queue.add(n);
                for (BlockPos n : BlockPos.betweenClosedStream(pos.offset(0,-1,0), pos.offset(0,1,0)).map(BlockPos::immutable).toList())
                    if (!n.equals(pos)) queue.add(n);
                for (BlockPos n : BlockPos.betweenClosedStream(pos.offset(0,0,-1), pos.offset(0,0,1)).map(BlockPos::immutable).toList())
                    if (!n.equals(pos)) queue.add(n);
            }
        }
        if (queue.isEmpty() || processed >= blockLimit) {
            return finish(queue.isEmpty() ? "excavation queue exhausted" : "excavation block limit reached");
        }
        return false;
    }
}
