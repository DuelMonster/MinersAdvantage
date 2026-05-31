package uk.co.duelmonster.minersadvantage.agent;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import uk.co.duelmonster.minersadvantage.common.config.CommonConfig;
import uk.co.duelmonster.minersadvantage.common.config.MAServerRootConfig;
import uk.co.duelmonster.minersadvantage.common.config.ShaftanationConfig;
import uk.co.duelmonster.minersadvantage.common.config.VeinationConfig;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;
import uk.co.duelmonster.minersadvantage.common.log.LogUtils;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeContext;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeDimensions;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeIds;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeRegistry;
import uk.co.duelmonster.minersadvantage.common.shape.builtin.shaft.ShaftFloorGeometry;
import uk.co.duelmonster.minersadvantage.common.services.utility.VeinationRuntimeService;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Shaft-digging worker that carves a directional tunnel and optionally places torches afterward.
 */
public class ShaftanationAgent extends Agent {
    private static final int MAX_TORCH_LIGHT_WAIT_TICKS = 40;

    private final BlockPos origin;
    private final Direction direction;
    private final ShaftanationConfig config;
    private final Queue<BlockPos> queue = new LinkedList<>();
    private final int targetDepth;
    private final int shaftWidth;
    private final int shaftHeight;
    private final int blocksPerTick;
    private final int blockLimit;
    private final boolean autoIlluminate;
    private final int torchLowestLightLevel;
    private final boolean mineVeins;
    private final CommonConfig commonConfig;
    private final VeinationRuntimeService veinationRuntime;
    private final VeinationConfig veinationConfig;
    private final ItemStack veinationTriggerTool;
    private int dug = 0;
    private int torchPlacements = 0;
    private int torchLightWaitTicks = 0;

    /**
     * Why this exists: TorchJob keeps this path readable and less mysterious when debugging edge-case chaos.
     * Translation: future-us gets answers faster and fewer 2 AM surprises.
     */
    record TorchJob(BlockPos pos, Direction facing, BlockPos lightCheckPos) {}

    /**
     * TorchGeometry precomputes tiny offset bundles so torch placement math stays boring and predictable.
     */
    record TorchGeometry(int offsetX, int offsetY, int offsetZ, int lightCheckOffsetY) {}

    /**
     * TorchPlacementDecision is the tiny referee that says place now, wait for light, or skip entirely.
     */
    private enum TorchPlacementDecision {
        PLACE,
        WAIT_FOR_LIGHT,
        DISCARD
    }
    private final Deque<TorchJob> torchQueue = new LinkedList<>();

    /**
     * Convenience constructor using depth and default config values.
     */
    public ShaftanationAgent(ServerPlayer player, BlockPos origin, int depth) {
        this(
            player,
            origin,
            player.getDirection(),
            new ShaftanationConfig(true, Math.max(1, depth), 8),
            new CommonConfig(),
            MAServerRootConfig.defaults().illumination().lowestLightLevel()
        );
    }

    /**
     * Convenience constructor using player facing direction.
     */
    public ShaftanationAgent(ServerPlayer player, BlockPos origin, ShaftanationConfig config, CommonConfig commonConfig) {
        this(player, origin, player.getDirection(), config, commonConfig, MAServerRootConfig.defaults().illumination().lowestLightLevel());
    }

    /**
     * Constructor with explicit direction and default torch-light threshold.
     */
    public ShaftanationAgent(ServerPlayer player, BlockPos origin, Direction direction, ShaftanationConfig config, CommonConfig commonConfig) {
        this(player, origin, direction, config, commonConfig, MAServerRootConfig.defaults().illumination().lowestLightLevel());
    }

    /**
     * Constructor with explicit torch-light threshold.
     */
    public ShaftanationAgent(ServerPlayer player, BlockPos origin, Direction direction, ShaftanationConfig config, CommonConfig commonConfig, int torchLowestLightLevel) {
        this(player, origin, direction, config, commonConfig, torchLowestLightLevel, null, null);
    }

    /**
     * Constructor with optional veination runtime wiring.
     */
    public ShaftanationAgent(
        ServerPlayer player,
        BlockPos origin,
        Direction direction,
        ShaftanationConfig config,
        CommonConfig commonConfig,
        int torchLowestLightLevel,
        VeinationRuntimeService veinationRuntime,
        VeinationConfig veinationConfig
    ) {
        this(player, origin, direction, config, commonConfig, torchLowestLightLevel, veinationRuntime, veinationConfig, ItemStack.EMPTY);
    }

    /**
     * Constructor with explicit veination trigger tool.
     */
    public ShaftanationAgent(
        ServerPlayer player,
        BlockPos origin,
        Direction direction,
        ShaftanationConfig config,
        CommonConfig commonConfig,
        int torchLowestLightLevel,
        VeinationRuntimeService veinationRuntime,
        VeinationConfig veinationConfig,
        ItemStack veinationTriggerTool
    ) {
        this(
            player,
            origin,
            direction,
            config,
            commonConfig,
            torchLowestLightLevel,
            veinationRuntime,
            veinationConfig,
            veinationTriggerTool,
            0,
            direction == null ? player.getDirection().getOpposite() : direction.getOpposite()
        );
    }

    /**
     * Full constructor that builds shaft queue from selected shape or fallback cuboid geometry.
     */
    public ShaftanationAgent(
        ServerPlayer player,
        BlockPos origin,
        Direction direction,
        ShaftanationConfig config,
        CommonConfig commonConfig,
        int torchLowestLightLevel,
        VeinationRuntimeService veinationRuntime,
        VeinationConfig veinationConfig,
        ItemStack veinationTriggerTool,
        int selectedShapeIndex,
        Direction hitFace
    ) {
        super(player);
        this.origin = origin;
        this.direction = direction != null && direction.getAxis().isHorizontal() ? direction : player.getDirection();
        this.config = config == null ? MAServerRootConfig.defaults().shaftanation() : config;
        this.targetDepth = Math.max(1, this.config.depth());
        this.shaftWidth = Math.max(1, this.config.width());
        this.shaftHeight = Math.max(1, this.config.height());

        int globalBlocksPerTick = commonConfig == null ? 1 : Math.max(1, commonConfig.blocksPerTick());
        this.blocksPerTick = Math.max(1, Math.min(globalBlocksPerTick, this.config.processesPerTick()));
        this.blockLimit = commonConfig == null ? 64 : Math.max(1, commonConfig.blockLimit());
        this.autoIlluminate = commonConfig == null || commonConfig.autoIlluminate();
        this.torchLowestLightLevel = Math.max(0, torchLowestLightLevel);
        this.mineVeins = commonConfig == null || commonConfig.mineVeins();
        this.commonConfig = commonConfig;
        this.veinationRuntime = veinationRuntime;
        this.veinationConfig = veinationConfig;
        this.veinationTriggerTool = veinationTriggerTool == null ? ItemStack.EMPTY : veinationTriggerTool.copy();

        int floorY = ShaftFloorGeometry.resolveFloorY(origin.getY(), player.blockPosition().getY(), this.shaftHeight);
        var selectedShape = MAShapeRegistry.byIndex(FeatureId.SHAFTANATION, selectedShapeIndex);
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (selectedShape.isPresent()) {
            MAShapeDimensions.Dimensions dimensions = MAShapeDimensions.shaftFromConfig(this.shaftWidth, this.shaftHeight, this.targetDepth);
            MAShapeContext context = new MAShapeContext(
                world,
                player,
                origin,
                world.getBlockState(origin),
                hitFace == null ? this.direction.getOpposite() : hitFace,
                this.direction,
                dimensions.width(),
                dimensions.height(),
                dimensions.depth(),
                this.blockLimit
            );
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            for (BlockPos shapePos : selectedShape.get().compute(context)) {
                queue.add(shapePos.immutable());
            }

            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (autoIlluminate && MAShapeIds.SHAFTANATION_SHAFT.equals(selectedShape.get().id())) {
                int halfWidth = shaftWidth / 2;
                BlockPos floorOrigin = new BlockPos(origin.getX(), floorY, origin.getZ());
                // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                for (int depth = 1; depth < targetDepth; depth++) {
                    addTorchTargets(floorOrigin.relative(this.direction, depth), halfWidth);
                }
            }
            return;
        }

        int halfWidth = shaftWidth / 2;
        boolean alongZ = this.direction.getAxis() == Direction.Axis.Z;
        BlockPos floorOrigin = new BlockPos(origin.getX(), floorY, origin.getZ());
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        for (int depth = 0; depth < targetDepth; depth++) {
            BlockPos base = floorOrigin.relative(this.direction, depth);
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            for (int w = -halfWidth; w <= halfWidth; w++) {
                // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                for (int h = 0; h < shaftHeight; h++) {
                    queue.add((alongZ ? base.offset(w, h, 0) : base.offset(0, h, w)).immutable());
                }
            }
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (autoIlluminate && depth > 0) {
                addTorchTargets(base, halfWidth);
            }
        }
    }

    /**
     * Per-tick shaft carving loop plus deferred torch placement pass.
     */
    @Override
    /**
     * t ic k exists so this path stays predictable and easier to debug when things get weird.
     */
    public boolean tick() {
        int count = 0;
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        while (!queue.isEmpty() && count < blocksPerTick && dug < blockLimit) {
            BlockPos pos = queue.poll();
            BlockState state = world.getBlockState(pos);
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (!state.isAir()) {
                world.destroyBlock(pos, true, player);
                maybeFanOutVeination(pos, state, mineVeins, this.commonConfig, veinationRuntime, veinationConfig, veinationTriggerTool);
                dug++;
                count++;
            }
        }

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (queue.isEmpty()) {
            boolean madeProgress = false;
            int scanBudget = torchQueue.size();
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            while (scanBudget-- > 0 && !torchQueue.isEmpty()) {
                TorchJob torchJob = torchQueue.peekFirst();
                TorchPlacementDecision decision = evaluateTorchPlacement(torchJob);
                // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                if (decision == TorchPlacementDecision.PLACE) {
                    torchQueue.pollFirst();
                    // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                    if (playerHasTorches()) {
                        placeTorch(torchJob);
                        madeProgress = true;
                    } else {
                        LogUtils.logDebug("Shaft torch skipped: no torches in inventory player={} pos={}", player.getScoreboardName(), torchJob.pos());
                        torchQueue.clear();
                    }
                    break;
                }
                // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                if (decision == TorchPlacementDecision.DISCARD) {
                    torchQueue.pollFirst();
                    LogUtils.logDebug("Discarded shaft torch job player={} pos={} facing={} reason=unplaceable", player.getScoreboardName(), torchJob.pos(), torchJob.facing());
                    madeProgress = true;
                    continue;
                }

                // Defer bright candidates so darker ones deeper in the queue can be considered this tick.
                torchQueue.addLast(torchQueue.pollFirst());
            }

            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (madeProgress) {
                torchLightWaitTicks = 0;
            } else if (!torchQueue.isEmpty()) {
                torchLightWaitTicks++;
                // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                if (torchLightWaitTicks > MAX_TORCH_LIGHT_WAIT_TICKS) {
                    int dropped = torchQueue.size();
                    TorchJob head = torchQueue.peekFirst();
                    torchQueue.clear();
                    LogUtils.logDebug(
                        "Discarded stalled shaft torch queue player={} headPos={} headFacing={} reason=light_wait_timeout waitedTicks={} threshold={} droppedJobs={}",
                        player.getScoreboardName(),
                        head == null ? null : head.pos(),
                        head == null ? null : head.facing(),
                        torchLightWaitTicks,
                        torchLowestLightLevel,
                        dropped
                    );
                    torchLightWaitTicks = 0;
                }
            }
        }

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if ((queue.isEmpty() && torchQueue.isEmpty()) || dug >= blockLimit) {
            return finish(queue.isEmpty() && torchQueue.isEmpty() ? "shaft queue exhausted" : "shaft target reached");
        }
        return false;
    }

    /**
     * Enqueue torch jobs for configured placement mode at one depth slice.
     */
    private void addTorchTargets(BlockPos base, int halfWidth) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        switch (config.torchPlacement()) {
            case FLOOR -> enqueueTorchJob(floorTorchJob(base));
            case LEFT_WALL -> enqueueTorchJob(wallTorchJob(base, direction, halfWidth, true));
            case RIGHT_WALL -> enqueueTorchJob(wallTorchJob(base, direction, halfWidth, false));
            case BOTH_WALLS -> {
                enqueueTorchJob(wallTorchJob(base, direction, halfWidth, true));
                enqueueTorchJob(wallTorchJob(base, direction, halfWidth, false));
            }
            default -> {
            }
        }
    }

    /**
     * Compute floor torch position/light-check geometry.
     */
    static TorchJob floorTorchJob(BlockPos base) {
        TorchGeometry geometry = floorTorchGeometry();
        BlockPos floorPos = base.offset(geometry.offsetX(), geometry.offsetY(), geometry.offsetZ()).immutable();
        return new TorchJob(floorPos, null, base.above(geometry.lightCheckOffsetY()).immutable());
    }

    /**
     * Compute wall torch position/facing/light-check geometry.
     */
    static TorchJob wallTorchJob(BlockPos base, Direction shaftDirection, int halfWidth, boolean leftWall) {
        TorchGeometry geometry = wallTorchGeometry(shaftDirection.getStepX(), shaftDirection.getStepZ(), halfWidth, leftWall);
        Direction wallDirection = leftWall ? shaftDirection.getCounterClockWise() : shaftDirection.getClockWise();
        return new TorchJob(
            base.offset(geometry.offsetX(), geometry.offsetY(), geometry.offsetZ()).immutable(),
            wallDirection.getOpposite(),
            base.above(geometry.lightCheckOffsetY()).immutable()
        );
    }

    /**
     * Floor torch geometry helper.
     */
    static TorchGeometry floorTorchGeometry() {
        return new TorchGeometry(0, 0, 0, 0);
    }

    /**
     * Wall torch geometry helper relative to shaft direction and side.
     */
    static TorchGeometry wallTorchGeometry(int shaftStepX, int shaftStepZ, int halfWidth, boolean leftWall) {
        int wallStepX = leftWall ? shaftStepZ : -shaftStepZ;
        int wallStepZ = leftWall ? -shaftStepX : shaftStepX;
        return new TorchGeometry(wallStepX * halfWidth, 1, wallStepZ * halfWidth, 0);
    }

    /**
     * Queue torch jobs in far-to-near order for post-carve placement.
     */
    private void enqueueTorchJob(TorchJob job) {
        // Torch jobs are prepended so placement runs from far-to-near after carving completes.
        torchQueue.addFirst(job);
    }

    /**
     * Decide whether a torch job should place now, wait, or be discarded.
     */
    private TorchPlacementDecision evaluateTorchPlacement(TorchJob torchJob) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (!autoIlluminate) {
            return TorchPlacementDecision.DISCARD;
        }
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (config.torchPlacement() == null) {
            return TorchPlacementDecision.DISCARD;
        }

        BlockPos pos = torchJob.pos();
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (!world.isEmptyBlock(pos)) {
            return TorchPlacementDecision.DISCARD;
        }

        int lightLevel = effectiveTorchLight(torchJob.lightCheckPos());
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (lightLevel > torchLowestLightLevel) {
            return TorchPlacementDecision.WAIT_FOR_LIGHT;
        }

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (torchJob.facing() == null) {
            return world.isEmptyBlock(pos.below()) ? TorchPlacementDecision.DISCARD : TorchPlacementDecision.PLACE;
        }

        return world.isEmptyBlock(pos.relative(torchJob.facing().getOpposite()))
            ? TorchPlacementDecision.DISCARD
            : TorchPlacementDecision.PLACE;
    }

    /**
     * Place torch and track placement count on success.
     */
    private void placeTorch(TorchJob torchJob) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (placeTorchWithInventory(torchJob.pos(), torchJob.facing())) {
            torchPlacements++;
        }
    }

    /**
     * Compute effective light at candidate torch location using current and above block levels.
     */
    private int effectiveTorchLight(BlockPos pos) {
        int atTorch = world.getBrightness(LightLayer.BLOCK, pos);
        int aboveTorch = world.getBrightness(LightLayer.BLOCK, pos.above());
        return Math.min(atTorch, aboveTorch);
    }

}
