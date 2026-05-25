package uk.co.duelmonster.minersadvantage.agent;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import uk.co.duelmonster.minersadvantage.common.config.CommonConfig;
import uk.co.duelmonster.minersadvantage.common.config.CultivationConfig;
import uk.co.duelmonster.minersadvantage.common.config.MAServerRootConfig;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * CultivationAgent: tills and plants farmland in a radius.
 */
public class CultivationAgent extends Agent {
    private final BlockPos origin;
    private final int radius;
    private final Queue<BlockPos> queue = new LinkedList<>();
    private final Set<BlockPos> visited = new HashSet<>();
    private final int blocksPerTick;
    private final int blockLimit;
    private int processed = 0;

    public CultivationAgent(ServerPlayer player, BlockPos origin, int radius) {
        this(player, origin, radius, MAServerRootConfig.defaults().cultivation(), new CommonConfig());
    }

    public CultivationAgent(ServerPlayer player, BlockPos origin, int radius, CultivationConfig config, CommonConfig commonConfig) {
        super(player);
        this.origin = origin;
        int configuredRadius = config == null ? Math.max(1, radius) : Math.max(1, config.hydrationDistance());
        this.radius = configuredRadius;
        int globalBlocksPerTick = commonConfig == null ? 1 : Math.max(1, commonConfig.blocksPerTick());
        this.blocksPerTick = globalBlocksPerTick;
        this.blockLimit = commonConfig == null ? 64 : Math.max(1, commonConfig.blockLimit());
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

    private boolean withinRadius(BlockPos pos) {
        int dx = Math.abs(pos.getX() - origin.getX());
        int dz = Math.abs(pos.getZ() - origin.getZ());
        return dx <= radius && dz <= radius;
    }
}