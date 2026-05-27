package uk.co.duelmonster.minersadvantage.agent;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import uk.co.duelmonster.minersadvantage.common.config.CommonConfig;
import uk.co.duelmonster.minersadvantage.common.config.VeinationConfig;
import uk.co.duelmonster.minersadvantage.common.services.utility.VeinationRuntimeService;

import java.util.LinkedList;
import java.util.Queue;

/**
 * VeinationAgent: breaks all connected ore blocks (vein mining).
 */
public class VeinationAgent extends Agent {
    private final Queue<BlockPos> queue = new LinkedList<>();
    private final VeinationRuntimeService runtime;
    private final VeinationConfig config;
    private final int blocksPerTick;

    public VeinationAgent(ServerPlayer player, BlockPos origin, CommonConfig commonConfig, VeinationRuntimeService runtime, VeinationConfig config) {
        this(player, origin, null, commonConfig, runtime, config);
    }

    public VeinationAgent(ServerPlayer player, BlockPos origin, BlockState originStateHint, CommonConfig commonConfig, VeinationRuntimeService runtime, VeinationConfig config) {
        super(player);
        this.runtime = runtime;
        this.config = config;
        this.blocksPerTick = commonConfig == null ? 1 : Math.max(1, commonConfig.blocksPerTick());
        queue.addAll(runtime.discoverVein(world, origin, originStateHint, config));
    }

    @Override
    public boolean tick() {
        int count = 0;
        while (!queue.isEmpty() && count < blocksPerTick) {
            BlockPos pos = queue.poll();
            if (world.getBlockState(pos).isAir()) {
                continue;
            }

            world.destroyBlock(pos, true, player);
            count++;
        }
        if (queue.isEmpty()) {
            return finish("vein queue exhausted");
        }
        return false;
    }
}