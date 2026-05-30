package uk.co.duelmonster.minersadvantage.agent;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import uk.co.duelmonster.minersadvantage.common.config.CommonConfig;
import uk.co.duelmonster.minersadvantage.common.config.ExcavationConfig;
import uk.co.duelmonster.minersadvantage.common.config.IlluminationConfig;
import uk.co.duelmonster.minersadvantage.common.config.MAServerRootConfig;
import uk.co.duelmonster.minersadvantage.common.config.VeinationConfig;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;
import uk.co.duelmonster.minersadvantage.common.Functions;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeContext;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeDimensions;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeRegistry;
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
    private final CommonConfig commonConfig;
    private final IlluminationConfig illuminationConfig;
    private final boolean mineVeins;
    private final VeinationRuntimeService veinationRuntime;
    private final VeinationConfig veinationConfig;
    private final ItemStack veinationTriggerTool;
    private boolean carvedAnyBlock = false;
    private int carvedMinX;
    private int carvedMinY;
    private int carvedMinZ;
    private int carvedMaxX;
    private int carvedMaxY;
    private int carvedMaxZ;
    private final Set<BlockPos> allowedShapePositions;
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
        this(player, origin, originState, config, commonConfig, horizontalRadius, verticalRadius, veinationRuntime, veinationConfig, veinationTriggerTool, null);
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
        ItemStack veinationTriggerTool,
        IlluminationConfig illuminationConfig
    ) {
        this(
            player,
            origin,
            originState,
            config,
            commonConfig,
            horizontalRadius,
            verticalRadius,
            veinationRuntime,
            veinationConfig,
            veinationTriggerTool,
            illuminationConfig,
            0,
            player == null ? null : player.getDirection()
        );
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
        ItemStack veinationTriggerTool,
        IlluminationConfig illuminationConfig,
        int selectedShapeIndex,
        net.minecraft.core.Direction hitFace
    ) {
        super(player);
        this.origin = origin;
        this.originState = originState == null ? Blocks.AIR.defaultBlockState() : originState;
        this.config = config == null ? MAServerRootConfig.defaults().excavation() : config;
        this.commonConfig = commonConfig == null ? new CommonConfig() : commonConfig;
        this.illuminationConfig = illuminationConfig;
        this.horizontalRadius = Math.max(0, horizontalRadius);
        this.verticalRadius = Math.max(0, verticalRadius);
        int globalBlocksPerTick = Math.max(1, this.commonConfig.blocksPerTick());
        this.blocksPerTick = Math.max(1, Math.min(globalBlocksPerTick, this.config.processesPerTick()));
        this.blockLimit = Math.max(1, this.commonConfig.blockLimit());
        this.mineVeins = this.commonConfig.mineVeins();
        this.veinationRuntime = veinationRuntime;
        this.veinationConfig = veinationConfig;
        this.veinationTriggerTool = veinationTriggerTool == null ? ItemStack.EMPTY : veinationTriggerTool.copy();

        this.allowedShapePositions = MAShapeRegistry.byIndex(FeatureId.EXCAVATION, selectedShapeIndex)
            .map(shapeDefinition -> {
                MAShapeDimensions.Dimensions dimensions = MAShapeDimensions.excavationFromRadii(this.horizontalRadius, this.verticalRadius);
                MAShapeContext context = new MAShapeContext(
                    world,
                    player,
                    origin,
                    hitFace == null ? player.getDirection() : hitFace,
                    player.getDirection(),
                    dimensions.width(),
                    dimensions.height(),
                    dimensions.depth(),
                    this.blockLimit
                );
                Set<BlockPos> computed = shapeDefinition.compute(context);
                if (!computed.contains(origin)) {
                    computed = new java.util.LinkedHashSet<>(computed);
                    computed.add(origin.immutable());
                }
                return computed;
            })
            .orElse(null);

        String originBlockId = BuiltInRegistries.BLOCK.getKey(this.originState.getBlock()).toString();
        if (this.config.isBlacklisted(originBlockId)) {
            return;
        }

        resetCarvedBounds();

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
                recordCarvedBlock(pos);
                maybeFanOutVeinationFromConnectedOre(pos, state);
                processed++;
                count++;
                enqueueNeighbors(pos);
            }
        }

        if (queue.isEmpty() || processed >= blockLimit) {
            maybeQueueIllumination();
            return finish(queue.isEmpty() ? "excavation queue exhausted" : "excavation block limit reached");
        }
        return false;
    }

    private void maybeQueueIllumination() {
        if (!commonConfig.autoIlluminate() || illuminationConfig == null || !illuminationConfig.enabled()) {
            return;
        }

        if (!carvedAnyBlock) {
            return;
        }

        AgentManager manager = AgentManager.get();
        if (!manager.hasAgentType(player, IlluminationAgent.class)) {
            manager.addAgent(player, new IlluminationAgent(player, carvedArea(), illuminationConfig, commonConfig));
        }
    }

    private void resetCarvedBounds() {
        carvedAnyBlock = false;
        carvedMinX = Integer.MAX_VALUE;
        carvedMinY = Integer.MAX_VALUE;
        carvedMinZ = Integer.MAX_VALUE;
        carvedMaxX = Integer.MIN_VALUE;
        carvedMaxY = Integer.MIN_VALUE;
        carvedMaxZ = Integer.MIN_VALUE;
    }

    private void recordCarvedBlock(BlockPos pos) {
        carvedAnyBlock = true;
        carvedMinX = Math.min(carvedMinX, pos.getX());
        carvedMinY = Math.min(carvedMinY, pos.getY());
        carvedMinZ = Math.min(carvedMinZ, pos.getZ());
        carvedMaxX = Math.max(carvedMaxX, pos.getX());
        carvedMaxY = Math.max(carvedMaxY, pos.getY());
        carvedMaxZ = Math.max(carvedMaxZ, pos.getZ());
    }

    private AABB carvedArea() {
        return new AABB(carvedMinX, carvedMinY, carvedMinZ, carvedMaxX, carvedMaxY, carvedMaxZ);
    }

    private void enqueueNeighbors(BlockPos pos) {
        queue.addAll(Functions.connectedNeighbors(pos));
    }

    private boolean isWithinConfiguredRadius(BlockPos pos) {
        if (allowedShapePositions != null) {
            return allowedShapePositions.contains(pos);
        }
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

    private void maybeFanOutVeinationFromConnectedOre(BlockPos brokenPos, BlockState brokenState) {
        if (maybeFanOutVeination(brokenPos, brokenState, mineVeins, commonConfig, veinationRuntime, veinationConfig, veinationTriggerTool)) {
            return;
        }

        for (BlockPos neighbor : Functions.connectedNeighbors(brokenPos)) {
            BlockState neighborState = world.getBlockState(neighbor);
            if (neighborState.isAir()) {
                continue;
            }

            if (maybeFanOutVeination(neighbor, neighborState, mineVeins, commonConfig, veinationRuntime, veinationConfig, veinationTriggerTool)) {
                return;
            }
        }
    }
}
