package uk.co.duelmonster.minersadvantage.agent;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
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
    private ItemStack breakTool;

    /**
     * v ei na ti on ag en t exists so this path stays predictable and easier to debug when things get weird.
     */
    public VeinationAgent(ServerPlayer player, BlockPos origin, CommonConfig commonConfig, VeinationRuntimeService runtime, VeinationConfig config) {
        this(player, origin, null, commonConfig, runtime, config, ItemStack.EMPTY);
    }

    /**
     * v ei na ti on ag en t exists so this path stays predictable and easier to debug when things get weird.
     */
    public VeinationAgent(ServerPlayer player, BlockPos origin, BlockState originStateHint, CommonConfig commonConfig, VeinationRuntimeService runtime, VeinationConfig config) {
        this(player, origin, originStateHint, commonConfig, runtime, config, ItemStack.EMPTY);
    }

    /**
     * v ei na ti on ag en t exists so this path stays predictable and easier to debug when things get weird.
     */
    public VeinationAgent(
        ServerPlayer player,
        BlockPos origin,
        BlockState originStateHint,
        CommonConfig commonConfig,
        VeinationRuntimeService runtime,
        VeinationConfig config,
        ItemStack breakTool
    ) {
        super(player);
        this.runtime = runtime;
        this.config = config;
        this.blocksPerTick = commonConfig == null ? 1 : Math.max(1, commonConfig.blocksPerTick());
        this.breakTool = breakTool == null ? ItemStack.EMPTY : breakTool.copy();
        queue.addAll(runtime.discoverVein(world, origin, originStateHint, config));
    }

    @Override
    /**
     * t ic k exists so this path stays predictable and easier to debug when things get weird.
     */
    public boolean tick() {
        int count = 0;
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        while (!queue.isEmpty() && count < blocksPerTick) {
            BlockPos pos = queue.poll();
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (world.getBlockState(pos).isAir()) {
                continue;
            }

            BreakOutcome breakOutcome = breakBlockWithTool(pos, breakTool);
            if (breakOutcome.broken()) {
                breakTool = breakOutcome.toolAfterBreak().copy();
                count++;
            }
        }
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (queue.isEmpty()) {
            return finish("vein queue exhausted");
        }
        return false;
    }
}
