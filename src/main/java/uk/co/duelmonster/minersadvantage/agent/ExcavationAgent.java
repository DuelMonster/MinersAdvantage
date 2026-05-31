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
 * Excavation worker that carves a bounded region (or selected shape) and can fan out veination.
 */
public class ExcavationAgent extends Agent {
    private final BlockPos origin;
    private final BlockState originState;
    private final ExcavationConfig config;
    private final int width;
    private final int height;
    private final int depth;
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

    /**
     * Convenience constructor deriving dimensions from a radius value.
     */
    public ExcavationAgent(ServerPlayer player, BlockPos origin, int radius) {
        this(
            player,
            origin,
            player.level().getBlockState(origin),
            MAServerRootConfig.defaults().excavation(),
            new CommonConfig(),
            (Math.max(1, radius) * 2) + 1,
            (Math.max(1, radius) * 2) + 1,
            Math.max(1, radius)
        );
    }

    /**
     * Convenience constructor with explicit origin block identity.
     */
    public ExcavationAgent(ServerPlayer player, BlockPos origin, int radius, Block originBlock) {
        this(
            player,
            origin,
            originBlock.defaultBlockState(),
            MAServerRootConfig.defaults().excavation(),
            new CommonConfig(),
            (Math.max(1, radius) * 2) + 1,
            (Math.max(1, radius) * 2) + 1,
            Math.max(1, radius)
        );
    }

    /**
     * Constructor with explicit config and dimensions.
     */
    public ExcavationAgent(
        ServerPlayer player,
        BlockPos origin,
        BlockState originState,
        ExcavationConfig config,
        CommonConfig commonConfig,
        int width,
        int height,
        int depth
    ) {
        this(player, origin, originState, config, commonConfig, width, height, depth, null, null);
    }

    /**
     * Constructor with optional veination runtime wiring.
     */
    public ExcavationAgent(
        ServerPlayer player,
        BlockPos origin,
        BlockState originState,
        ExcavationConfig config,
        CommonConfig commonConfig,
        int width,
        int height,
        int depth,
        VeinationRuntimeService veinationRuntime,
        VeinationConfig veinationConfig
    ) {
        this(player, origin, originState, config, commonConfig, width, height, depth, veinationRuntime, veinationConfig, ItemStack.EMPTY);
    }

    /**
     * Constructor with explicit veination trigger tool.
     */
    public ExcavationAgent(
        ServerPlayer player,
        BlockPos origin,
        BlockState originState,
        ExcavationConfig config,
        CommonConfig commonConfig,
        int width,
        int height,
        int depth,
        VeinationRuntimeService veinationRuntime,
        VeinationConfig veinationConfig,
        ItemStack veinationTriggerTool
    ) {
        this(player, origin, originState, config, commonConfig, width, height, depth, veinationRuntime, veinationConfig, veinationTriggerTool, null);
    }

    /**
     * Constructor with optional illumination config and default shape context.
     */
    public ExcavationAgent(
        ServerPlayer player,
        BlockPos origin,
        BlockState originState,
        ExcavationConfig config,
        CommonConfig commonConfig,
        int width,
        int height,
        int depth,
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
            width,
            height,
            depth,
            veinationRuntime,
            veinationConfig,
            veinationTriggerTool,
            illuminationConfig,
            0,
            player == null ? null : player.getDirection()
        );
    }

    /**
     * Full constructor that resolves shape-limited region and seeds traversal queue.
     */
    public ExcavationAgent(
        ServerPlayer player,
        BlockPos origin,
        BlockState originState,
        ExcavationConfig config,
        CommonConfig commonConfig,
        int width,
        int height,
        int depth,
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
        this.width = Math.max(1, width);
        this.height = Math.max(1, height);
        this.depth = Math.max(1, depth);
        int globalBlocksPerTick = Math.max(1, this.commonConfig.blocksPerTick());
        this.blocksPerTick = Math.max(1, Math.min(globalBlocksPerTick, this.config.processesPerTick()));
        this.blockLimit = Math.max(1, this.commonConfig.blockLimit());
        this.mineVeins = this.commonConfig.mineVeins();
        this.veinationRuntime = veinationRuntime;
        this.veinationConfig = veinationConfig;
        this.veinationTriggerTool = veinationTriggerTool == null ? ItemStack.EMPTY : veinationTriggerTool.copy();

        this.allowedShapePositions = MAShapeRegistry.byIndex(FeatureId.EXCAVATION, selectedShapeIndex)
            .map(shapeDefinition -> {
                MAShapeDimensions.Dimensions dimensions = MAShapeDimensions.excavationFromConfig(this.width, this.height, this.depth);
                MAShapeContext context = new MAShapeContext(
                    world,
                    player,
                    origin,
                    this.originState,
                    hitFace == null ? player.getDirection() : hitFace,
                    player.getDirection(),
                    dimensions.width(),
                    dimensions.height(),
                    dimensions.depth(),
                    this.blockLimit
                );
                Set<BlockPos> computed = shapeDefinition.compute(context);
                // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                if (!computed.contains(origin)) {
                    computed = new java.util.LinkedHashSet<>(computed);
                    computed.add(origin.immutable());
                }
                return computed;
            })
            .orElse(null);

        String originBlockId = BuiltInRegistries.BLOCK.getKey(this.originState.getBlock()).toString();
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (this.config.isBlacklisted(originBlockId)) {
            return;
        }

        resetCarvedBounds();

        queue.add(origin);
    }

    /**
     * Per-tick excavation loop with radius/shape guards and optional veination fan-out.
     */
    @Override
    /**
     * t ic k exists so this path stays predictable and easier to debug when things get weird.
     */
    public boolean tick() {
        int count = 0;
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        while (!queue.isEmpty() && count < blocksPerTick && processed < blockLimit) {
            BlockPos pos = queue.poll();
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (pos == null || !visited.add(pos)) {
                continue;
            }

            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (!isWithinConfiguredRadius(pos)) {
                continue;
            }

            BlockState state = world.getBlockState(pos);
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (pos.equals(origin) && state.getBlock() == Blocks.AIR) {
                enqueueNeighbors(pos);
                continue;
            }

            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (state.getBlock() != Blocks.AIR && isTargetState(state)) {
                world.destroyBlock(pos, true, player);
                recordCarvedBlock(pos);
                maybeFanOutVeinationFromConnectedOre(pos, state);
                processed++;
                count++;
                enqueueNeighbors(pos);
            }
        }

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (queue.isEmpty() || processed >= blockLimit) {
            maybeQueueIllumination();
            return finish(queue.isEmpty() ? "excavation queue exhausted" : "excavation block limit reached");
        }
        return false;
    }

    /**
     * Optionally enqueue illumination agent for carved area after excavation completes.
     */
    private void maybeQueueIllumination() {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (!commonConfig.autoIlluminate() || illuminationConfig == null || !illuminationConfig.enabled()) {
            return;
        }

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (!carvedAnyBlock) {
            return;
        }

        AgentManager manager = AgentManager.get();
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (!manager.hasAgentType(player, IlluminationAgent.class)) {
            manager.addAgent(player, new IlluminationAgent(player, carvedArea(), illuminationConfig, commonConfig));
        }
    }

    /**
     * Reset carved bounds accumulator.
     */
    private void resetCarvedBounds() {
        carvedAnyBlock = false;
        carvedMinX = Integer.MAX_VALUE;
        carvedMinY = Integer.MAX_VALUE;
        carvedMinZ = Integer.MAX_VALUE;
        carvedMaxX = Integer.MIN_VALUE;
        carvedMaxY = Integer.MIN_VALUE;
        carvedMaxZ = Integer.MIN_VALUE;
    }

    /**
     * Expand carved bounds accumulator with one carved block.
     */
    private void recordCarvedBlock(BlockPos pos) {
        carvedAnyBlock = true;
        carvedMinX = Math.min(carvedMinX, pos.getX());
        carvedMinY = Math.min(carvedMinY, pos.getY());
        carvedMinZ = Math.min(carvedMinZ, pos.getZ());
        carvedMaxX = Math.max(carvedMaxX, pos.getX());
        carvedMaxY = Math.max(carvedMaxY, pos.getY());
        carvedMaxZ = Math.max(carvedMaxZ, pos.getZ());
    }

    /**
     * Build carved-region AABB for follow-up illumination.
     */
    private AABB carvedArea() {
        return new AABB(carvedMinX, carvedMinY, carvedMinZ, carvedMaxX, carvedMaxY, carvedMaxZ);
    }

    /**
     * Queue connected neighbor positions for traversal.
     */
    private void enqueueNeighbors(BlockPos pos) {
        queue.addAll(Functions.connectedNeighbors(pos));
    }

    /**
     * Check whether position is within selected shape or fallback width/height/depth bounds.
     */
    private boolean isWithinConfiguredRadius(BlockPos pos) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (allowedShapePositions != null) {
            return allowedShapePositions.contains(pos);
        }
        int dx = Math.abs(pos.getX() - origin.getX());
        int dy = Math.abs(pos.getY() - origin.getY());
        int dz = Math.abs(pos.getZ() - origin.getZ());
        int halfWidth = width / 2;
        int halfHeight = height / 2;
        int halfDepth = depth / 2;
        return dx <= halfWidth && dz <= halfDepth && dy <= halfHeight;
    }

    /**
     * Check whether block state is a valid excavation target under current matching rules.
     */
    private boolean isTargetState(BlockState state) {
        String blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (config.isBlacklisted(blockId)) {
            return false;
        }

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (config.ignoreBlockVariants()) {
            return state.getBlock() == originState.getBlock();
        }
        return state.equals(originState);
    }

    /**
     * Try veination on broken block, then on immediate connected neighbors.
     */
    private void maybeFanOutVeinationFromConnectedOre(BlockPos brokenPos, BlockState brokenState) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (maybeFanOutVeination(brokenPos, brokenState, mineVeins, commonConfig, veinationRuntime, veinationConfig, veinationTriggerTool)) {
            return;
        }

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        for (BlockPos neighbor : Functions.connectedNeighbors(brokenPos)) {
            BlockState neighborState = world.getBlockState(neighbor);
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (neighborState.isAir()) {
                continue;
            }

            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (maybeFanOutVeination(neighbor, neighborState, mineVeins, commonConfig, veinationRuntime, veinationConfig, veinationTriggerTool)) {
                return;
            }
        }
    }
}
