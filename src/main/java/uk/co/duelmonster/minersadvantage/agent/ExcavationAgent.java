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
import uk.co.duelmonster.minersadvantage.common.log.LogUtils;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeContext;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeDimensions;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeDefinition;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapePrecomputeCache;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeRegistry;
import uk.co.duelmonster.minersadvantage.common.shape.builtin.excavation.ExcavationFaceGeometry;
import uk.co.duelmonster.minersadvantage.common.services.utility.VeinationRuntimeService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
    private final boolean useOrderedShapeQueue;
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
        this.mineVeins = this.commonConfig.mineVeins();
        this.veinationRuntime = veinationRuntime;
        this.veinationConfig = veinationConfig;
        this.veinationTriggerTool = veinationTriggerTool == null ? ItemStack.EMPTY : veinationTriggerTool.copy();

        var selectedShape = MAShapeRegistry.byIndex(FeatureId.EXCAVATION, selectedShapeIndex);
        net.minecraft.core.Direction effectiveHitFace = hitFace == null ? player.getDirection() : hitFace;
        LogUtils.logDebug(
            "Excavation break trigger player={} selectedIndex={} shapeId={} shapeName={} hitFace={}",
            player.getScoreboardName(),
            selectedShapeIndex,
            selectedShape.map(MAShapeDefinition::id).orElse("none"),
            selectedShape.map(MAShapeDefinition::displayName).orElse("none"),
            hitFace == null ? "null" : hitFace
        );
        this.allowedShapePositions = selectedShape
            .map(shapeDefinition -> {
                MAShapeDimensions.Dimensions dimensions = MAShapeDimensions.excavationFromConfig(this.width, this.height, this.depth);
                MAShapeContext context = new MAShapeContext(
                    world,
                    player,
                    origin,
                    this.originState,
                    effectiveHitFace,
                    player.getDirection(),
                    dimensions.width(),
                    dimensions.height(),
                    dimensions.depth()
                );
                Set<BlockPos> computed = MAShapePrecomputeCache.compute(shapeDefinition, context);
                computed = clampToConfiguredExcavationBounds(
                    computed,
                    origin,
                    effectiveHitFace,
                    player.getDirection(),
                    dimensions.width(),
                    dimensions.height(),
                    dimensions.depth()
                );
                // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                if (!computed.contains(origin)) {
                    computed = new java.util.LinkedHashSet<>(computed);
                    computed.add(origin.immutable());
                }
                return computed;
            })
            .orElse(null);
        this.useOrderedShapeQueue = this.allowedShapePositions != null;
        if (selectedShape.isPresent()) {
            MAShapeDefinition shape = selectedShape.get();
            LogUtils.logDebug(
                "Excavation shape resolved player={} selectedIndex={} shapeId={} shapeName={} hitFace={} width={} height={} depth={} computedPositions={}",
                player.getScoreboardName(),
                selectedShapeIndex,
                shape.id(),
                shape.displayName(),
                hitFace == null ? "null" : hitFace,
                this.width,
                this.height,
                this.depth,
                this.allowedShapePositions == null ? 0 : this.allowedShapePositions.size()
            );
        } else {
            LogUtils.logDebug(
                "Excavation shape resolution failed player={} selectedIndex={} reason=no-shape-registered",
                player.getScoreboardName(),
                selectedShapeIndex
            );
        }

        String originBlockId = BuiltInRegistries.BLOCK.getKey(this.originState.getBlock()).toString();
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (this.config.isBlacklisted(originBlockId)) {
            return;
        }

        resetCarvedBounds();

        if (useOrderedShapeQueue) {
            queue.addAll(
                orderExcavationPositions(
                    allowedShapePositions,
                    origin,
                    effectiveHitFace,
                    player.getDirection(),
                    this.width,
                    this.height,
                    this.depth
                )
            );
            if (queue.isEmpty()) {
                queue.add(origin);
            }
        } else {
            queue.add(origin);
        }
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
        while (!queue.isEmpty() && count < blocksPerTick) {
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
            if (!useOrderedShapeQueue && pos.equals(origin) && state.getBlock() == Blocks.AIR) {
                enqueueNeighbors(pos);
                continue;
            }

            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (state.getBlock() != Blocks.AIR && isTargetState(state)) {
                world.destroyBlock(pos, true, player);
                maybeFanOutVeinationFromConnectedOre(pos, state);
                recordCarvedBlock(pos);
                processed++;
                count++;
                if (!useOrderedShapeQueue) {
                    enqueueNeighbors(pos);
                }
            }
        }

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (queue.isEmpty()) {
            maybeQueueIllumination();
            return finish("excavation queue exhausted");
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
     * Intersect computed shape positions with the configured excavation volume envelope.
     */
    private static Set<BlockPos> clampToConfiguredExcavationBounds(
        Set<BlockPos> computed,
        BlockPos origin,
        net.minecraft.core.Direction hitFace,
        net.minecraft.core.Direction playerFacing,
        int width,
        int height,
        int depth
    ) {
        java.util.LinkedHashSet<BlockPos> bounded = new java.util.LinkedHashSet<>();
        if (computed == null || computed.isEmpty()) {
            return bounded;
        }

        Set<BlockPos> envelope = MAShapePrecomputeCache.excavationEnvelopeAt(
            origin,
            width,
            height,
            depth,
            hitFace,
            playerFacing
        );

        for (BlockPos pos : computed) {
            if (envelope.contains(pos)) {
                bounded.add(pos.immutable());
            }
        }
        return bounded;
    }

    private record ExcavationLocal(int depth, int width, int height) {
    }

    private static List<BlockPos> orderExcavationPositions(
        Set<BlockPos> positions,
        BlockPos origin,
        net.minecraft.core.Direction hitFace,
        net.minecraft.core.Direction playerFacing,
        int width,
        int height,
        int depth
    ) {
        if (positions == null || positions.isEmpty()) {
            return List.of();
        }

        ExcavationFaceGeometry.FaceDirection face = ExcavationFaceGeometry.fromMinecraftDirection(hitFace);
        ExcavationFaceGeometry.FaceDirection facing = ExcavationFaceGeometry.fromMinecraftDirection(playerFacing);
        ExcavationFaceGeometry.IntRange widthRange = ExcavationFaceGeometry.rightBiasedCenteredRange(width);
        ExcavationFaceGeometry.IntRange heightRange = ExcavationFaceGeometry.rightBiasedCenteredRange(height);

        Map<BlockPos, ExcavationLocal> localByPos = new HashMap<>(positions.size());
        for (int d = 0; d < depth; d++) {
            for (int h = heightRange.min(); h <= heightRange.max(); h++) {
                for (int w = widthRange.min(); w <= widthRange.max(); w++) {
                    int[] offset = ExcavationFaceGeometry.offsetFor(face, facing, d, w, h);
                    BlockPos absolute = origin.offset(offset[0], offset[1], offset[2]).immutable();
                    if (positions.contains(absolute)) {
                        localByPos.put(absolute, new ExcavationLocal(d, w, h));
                    }
                }
            }
        }

        Map<Long, Integer> spiralIndex = clockwiseSpiralIndex(
            widthRange.min(),
            widthRange.max(),
            heightRange.min(),
            heightRange.max(),
            0,
            0
        );

        ArrayList<BlockPos> ordered = new ArrayList<>(positions.size());
        ArrayList<BlockPos> overflow = new ArrayList<>();
        for (BlockPos pos : positions) {
            if (localByPos.containsKey(pos)) {
                ordered.add(pos.immutable());
            } else {
                overflow.add(pos.immutable());
            }
        }

        ordered.sort(
            Comparator
                .comparingInt((BlockPos pos) -> localByPos.get(pos).depth())
                .thenComparingInt(pos -> spiralIndex.getOrDefault(pairKey(localByPos.get(pos).width(), localByPos.get(pos).height()), Integer.MAX_VALUE))
                .thenComparingInt(BlockPos::getY)
                .thenComparingInt(BlockPos::getX)
                .thenComparingInt(BlockPos::getZ)
        );
        ordered.addAll(overflow);
        return ordered;
    }

    private static Map<Long, Integer> clockwiseSpiralIndex(
        int minW,
        int maxW,
        int minH,
        int maxH,
        int startW,
        int startH
    ) {
        HashMap<Long, Integer> index = new HashMap<>();
        int total = Math.max(0, (maxW - minW + 1) * (maxH - minH + 1));
        if (total == 0) {
            return index;
        }

        int[][] directions = new int[][] {
            { 1, 0 },
            { 0, -1 },
            { -1, 0 },
            { 0, 1 }
        };

        int w = Math.max(minW, Math.min(maxW, startW));
        int h = Math.max(minH, Math.min(maxH, startH));
        int dirIndex = 0;
        int stepLength = 1;

        addSpiralPoint(index, minW, maxW, minH, maxH, w, h);
        while (index.size() < total) {
            for (int side = 0; side < 2; side++) {
                int[] direction = directions[dirIndex % directions.length];
                for (int step = 0; step < stepLength; step++) {
                    w += direction[0];
                    h += direction[1];
                    addSpiralPoint(index, minW, maxW, minH, maxH, w, h);
                    if (index.size() >= total) {
                        return index;
                    }
                }
                dirIndex++;
            }
            stepLength++;
        }

        return index;
    }

    private static void addSpiralPoint(
        Map<Long, Integer> index,
        int minW,
        int maxW,
        int minH,
        int maxH,
        int w,
        int h
    ) {
        if (w < minW || w > maxW || h < minH || h > maxH) {
            return;
        }
        long key = pairKey(w, h);
        index.putIfAbsent(key, index.size());
    }

    private static long pairKey(int a, int b) {
        return (((long) a) << 32) ^ (b & 0xffffffffL);
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
