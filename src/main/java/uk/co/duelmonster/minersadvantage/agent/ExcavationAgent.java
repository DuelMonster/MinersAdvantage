package uk.co.duelmonster.minersadvantage.agent;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import uk.co.duelmonster.minersadvantage.common.config.CommonConfig;
import uk.co.duelmonster.minersadvantage.common.config.ExcavationConfig;
import uk.co.duelmonster.minersadvantage.common.config.MAServerRootConfig;
import uk.co.duelmonster.minersadvantage.common.config.VeinationConfig;
import uk.co.duelmonster.minersadvantage.common.registry.RegistryPredicates;
import uk.co.duelmonster.minersadvantage.common.services.utility.VeinationRuntimeService;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * Modernized ExcavationAgent: actually breaks blocks in the world, production-wired.
 */
public class ExcavationAgent extends Agent {
    private final BlockPos origin;
    private final BlockState originState;
    private final ExcavationConfig config;
    private final int horizontalRadius;
    private final int verticalRadius;
    private final Queue<BlockPos> queue = new LinkedList<>();
    private final Set<BlockPos> visited = new HashSet<>();
    private final int blocksPerTick;
    private final int blockLimit;
    private final boolean mineVeins;
    private final VeinationRuntimeService veinationRuntime;
    private final VeinationConfig veinationConfig;
    private final ItemStack veinationTriggerTool;
    private int processed = 0;

    public ExcavationAgent(ServerPlayer player, BlockPos origin, int radius) {
        this(
            player,
            origin,
            player.level().getBlockState(origin),
            MAServerRootConfig.defaults().excavation(),
            new CommonConfig(),
            radius,
            radius
        );
    }

    public ExcavationAgent(ServerPlayer player, BlockPos origin, int radius, Block originBlock) {
        this(
            player,
            origin,
            originBlock.defaultBlockState(),
            MAServerRootConfig.defaults().excavation(),
            new CommonConfig(),
            radius,
            radius
        );
    }

    public ExcavationAgent(
        ServerPlayer player,
        BlockPos origin,
        BlockState originState,
        ExcavationConfig config,
        CommonConfig commonConfig,
        int horizontalRadius,
        int verticalRadius
    ) {
        this(player, origin, originState, config, commonConfig, horizontalRadius, verticalRadius, null, null);
    }

    public ExcavationAgent(
        ServerPlayer player,
        BlockPos origin,
        BlockState originState,
        ExcavationConfig config,
        CommonConfig commonConfig,
        int horizontalRadius,
        int verticalRadius,
        VeinationRuntimeService veinationRuntime,
        VeinationConfig veinationConfig
    ) {
        this(player, origin, originState, config, commonConfig, horizontalRadius, verticalRadius, veinationRuntime, veinationConfig, ItemStack.EMPTY);
    }

    public ExcavationAgent(
        ServerPlayer player,
        BlockPos origin,
        BlockState originState,
        ExcavationConfig config,
        CommonConfig commonConfig,
        int horizontalRadius,
        int verticalRadius,
        VeinationRuntimeService veinationRuntime,
        VeinationConfig veinationConfig,
        ItemStack veinationTriggerTool
    ) {
        super(player);
        this.origin = origin;
        this.originState = originState == null ? Blocks.AIR.defaultBlockState() : originState;
        this.config = config == null ? MAServerRootConfig.defaults().excavation() : config;
        this.horizontalRadius = Math.max(0, horizontalRadius);
        this.verticalRadius = Math.max(0, verticalRadius);
        int globalBlocksPerTick = commonConfig == null ? 1 : Math.max(1, commonConfig.blocksPerTick());
        this.blocksPerTick = Math.max(1, Math.min(globalBlocksPerTick, this.config.processesPerTick()));
        this.blockLimit = commonConfig == null ? 64 : Math.max(1, commonConfig.blockLimit());
        this.mineVeins = commonConfig == null || commonConfig.mineVeins();
        this.veinationRuntime = veinationRuntime;
        this.veinationConfig = veinationConfig;
        this.veinationTriggerTool = veinationTriggerTool == null ? ItemStack.EMPTY : veinationTriggerTool.copy();

        String originBlockId = BuiltInRegistries.BLOCK.getKey(this.originState.getBlock()).toString();
        if (this.config.isBlacklisted(originBlockId)) {
            return;
        }

        queue.add(origin);
    }

    @Override
    public boolean tick() {
        int count = 0;
        while (!queue.isEmpty() && count < blocksPerTick && processed < blockLimit) {
            BlockPos pos = queue.poll();
            if (pos == null || !visited.add(pos)) {
                continue;
            }

            if (!isWithinConfiguredRadius(pos)) {
                continue;
            }

            BlockState state = world.getBlockState(pos);
            if (pos.equals(origin) && state.getBlock() == Blocks.AIR) {
                enqueueNeighbors(pos);
                continue;
            }

            if (state.getBlock() != Blocks.AIR && isTargetState(state)) {
                world.destroyBlock(pos, true, player);
                maybeFanOutVeination(pos, state);
                processed++;
                count++;
                enqueueNeighbors(pos);
            }
        }

        if (queue.isEmpty() || processed >= blockLimit) {
            return finish(queue.isEmpty() ? "excavation queue exhausted" : "excavation block limit reached");
        }
        return false;
    }

    private void enqueueNeighbors(BlockPos pos) {
        queue.add(pos.above().immutable());
        queue.add(pos.below().immutable());
        queue.add(pos.north().immutable());
        queue.add(pos.south().immutable());
        queue.add(pos.east().immutable());
        queue.add(pos.west().immutable());
    }

    private boolean isWithinConfiguredRadius(BlockPos pos) {
        int dx = Math.abs(pos.getX() - origin.getX());
        int dy = Math.abs(pos.getY() - origin.getY());
        int dz = Math.abs(pos.getZ() - origin.getZ());
        return dx <= horizontalRadius && dz <= horizontalRadius && dy <= verticalRadius;
    }

    private boolean isTargetState(BlockState state) {
        String blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
        if (config.isBlacklisted(blockId)) {
            return false;
        }

        if (config.ignoreBlockVariants()) {
            return state.getBlock() == originState.getBlock();
        }
        return state.equals(originState);
    }

    private void maybeFanOutVeination(BlockPos pos, BlockState brokenState) {
        if (!mineVeins || veinationRuntime == null || veinationConfig == null || !veinationConfig.enabled()) {
            return;
        }

        ItemStack toolStack = veinationTriggerTool.isEmpty() ? player.getMainHandItem() : veinationTriggerTool;

        if (!RegistryPredicates.isPickaxeTool(toolStack)) {
            return;
        }

        if (!veinationRuntime.isPickaxeAllowed(world, veinationConfig, toolStack)) {
            return;
        }

        if (!veinationRuntime.isOreAllowed(veinationConfig, brokenState)) {
            return;
        }

        AgentManager agentManager = AgentManager.get();
        veinationRuntime.registerDropAnchor(player, pos, veinationConfig);
        if (!agentManager.hasAgentType(player, VeinationAgent.class)) {
            agentManager.addAgent(player, new VeinationAgent(player, pos, brokenState, veinationRuntime, veinationConfig));
        }
    }
}
