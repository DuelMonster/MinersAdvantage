package uk.co.duelmonster.minersadvantage.agent;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * Modernized LumbinationAgent: fells trees by breaking connected logs and leaves.
 */
public class LumbinationAgent extends Agent {
    private final BlockPos origin;
    private final Block originBlock;
    private final Queue<BlockPos> queue = new LinkedList<>();
    private final Set<BlockPos> visited = new HashSet<>();
    private int blocksPerTick = 8;
    private int processed = 0;
    private int blockLimit = 128;

    public LumbinationAgent(ServerPlayer player, BlockPos origin) {
        this(player, origin, player.level().getBlockState(origin).getBlock());
    }

    public LumbinationAgent(ServerPlayer player, BlockPos origin, Block originBlock) {
        super(player);
        this.origin = origin;
        this.originBlock = originBlock;
        queue.add(origin);
    }

    @Override
    public boolean tick() {
        int count = 0;
        while (!queue.isEmpty() && count < blocksPerTick && processed < blockLimit) {
            BlockPos pos = queue.poll();
            if (!visited.add(pos)) continue;
            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            if (pos.equals(origin) && block == Blocks.AIR && isLog(originBlock)) {
                for (int dx = -1; dx <= 1; dx++)
                    for (int dy = -1; dy <= 1; dy++)
                        for (int dz = -1; dz <= 1; dz++)
                            if (!(dx == 0 && dy == 0 && dz == 0))
                                queue.add(pos.offset(dx, dy, dz));
                continue;
            }
            if (isLog(block)) {
                world.destroyBlock(pos, true, player);
                processed++;
                count++;
                // Add neighbors in a 3x3x3 cube
                for (int dx = -1; dx <= 1; dx++)
                    for (int dy = -1; dy <= 1; dy++)
                        for (int dz = -1; dz <= 1; dz++)
                            queue.add(pos.offset(dx, dy, dz));
            } else if (isLeaf(block)) {
                world.destroyBlock(pos, true, player);
                processed++;
                count++;
            }
        }
        if (queue.isEmpty() || processed >= blockLimit) {
            return finish(queue.isEmpty() ? "tree traversal exhausted" : "tree block limit reached");
        }
        return false;
    }

    private boolean isLog(Block block) {
        return block.defaultBlockState().is(BlockTags.LOGS);
    }
    private boolean isLeaf(Block block) {
        return block.defaultBlockState().is(BlockTags.LEAVES) || block.defaultBlockState().is(BlockTags.WART_BLOCKS);
    }
}
