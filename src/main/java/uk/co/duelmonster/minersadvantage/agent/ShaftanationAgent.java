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
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapePrecomputeCache;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeRegistry;
import uk.co.duelmonster.minersadvantage.common.shape.builtin.ShapeGeometryUtils;
import uk.co.duelmonster.minersadvantage.common.shape.builtin.shaft.ShaftFloorGeometry;
import uk.co.duelmonster.minersadvantage.common.services.utility.VeinationRuntimeService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

/**
 * Shaft-digging worker that carves a directional tunnel and optionally places torches afterward.
 */
public class ShaftanationAgent extends Agent {
  private static final int MAX_TORCH_LIGHT_WAIT_TICKS = 40;

  private record ShaftTarget(BlockPos pos, int depth) {
  }

  private final BlockPos origin;
  private final Direction direction;
  private final ShaftanationConfig config;
  private final Queue<ShaftTarget> queue = new LinkedList<>();
  private final int targetDepth;
  private final int shaftWidth;
  private final int shaftHeight;
  private final int blocksPerTick;
  private final boolean autoIlluminate;
  private final int torchLowestLightLevel;
  private final boolean mineVeins;
  private final CommonConfig commonConfig;
  private final VeinationRuntimeService veinationRuntime;
  private final VeinationConfig veinationConfig;
  private ItemStack veinationTriggerTool;
  private int dug = 0;
  private int torchPlacements = 0;
  private int torchLightWaitTicks = 0;
  private int activeShaftLayer = Integer.MIN_VALUE;
  private boolean activeShaftLayerHasNonAir = false;
  private boolean shaftEmptyLayerAborted = false;

  /**
   * Why this exists: TorchJob keeps this path readable and less mysterious when debugging edge-case chaos.
   * Translation: future-us gets answers faster and fewer 2 AM surprises.
   */
  record TorchJob(BlockPos pos, Direction facing, BlockPos lightCheckPos) {
  }

  /**
   * TorchGeometry precomputes tiny offset bundles so torch placement math stays boring and predictable.
   */
  record TorchGeometry(int offsetX, int offsetY, int offsetZ, int lightCheckOffsetY) {
  }

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
        MAServerRootConfig.defaults().illumination().lowestLightLevel());
  }

  /**
   * Convenience constructor using player facing direction.
   */
  public ShaftanationAgent(ServerPlayer player, BlockPos origin, ShaftanationConfig config, CommonConfig commonConfig) {
    this(player, origin, player.getDirection(), config, commonConfig,
        MAServerRootConfig.defaults().illumination().lowestLightLevel());
  }

  /**
   * Constructor with explicit direction and default torch-light threshold.
   */
  public ShaftanationAgent(ServerPlayer player, BlockPos origin, Direction direction, ShaftanationConfig config,
      CommonConfig commonConfig) {
    this(player, origin, direction, config, commonConfig,
        MAServerRootConfig.defaults().illumination().lowestLightLevel());
  }

  /**
   * Constructor with explicit torch-light threshold.
   */
  public ShaftanationAgent(ServerPlayer player, BlockPos origin, Direction direction, ShaftanationConfig config,
      CommonConfig commonConfig, int torchLowestLightLevel) {
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
      VeinationConfig veinationConfig) {
    this(player, origin, direction, config, commonConfig, torchLowestLightLevel, veinationRuntime, veinationConfig,
        ItemStack.EMPTY);
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
      ItemStack veinationTriggerTool) {
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
        direction == null ? player.getDirection().getOpposite() : direction.getOpposite());
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
      Direction hitFace) {
    super(player);
    this.origin = origin;
    this.direction = direction != null && direction.getAxis().isHorizontal() ? direction : player.getDirection();
    this.config = config == null ? MAServerRootConfig.defaults().shaftanation() : config;
    this.targetDepth = Math.max(1, this.config.depth());
    this.shaftWidth = Math.max(1, this.config.width());
    this.shaftHeight = Math.max(1, this.config.height());

    int globalBlocksPerTick = commonConfig == null ? 1 : Math.max(1, commonConfig.blocksPerTick());
    this.blocksPerTick = Math.max(1, Math.min(globalBlocksPerTick, this.config.processesPerTick()));
    this.autoIlluminate = commonConfig == null || commonConfig.autoIlluminate();
    this.torchLowestLightLevel = Math.max(0, torchLowestLightLevel);
    this.mineVeins = commonConfig == null || commonConfig.mineVeins();
    this.commonConfig = commonConfig;
    this.veinationRuntime = veinationRuntime;
    this.veinationConfig = veinationConfig;
    this.veinationTriggerTool = veinationTriggerTool == null ? ItemStack.EMPTY : veinationTriggerTool.copy();

    int floorY = ShaftFloorGeometry.resolveFloorY(origin.getY(), player.blockPosition().getY(), this.shaftHeight);
    var selectedShape = MAShapeRegistry.byIndex(FeatureId.SHAFTANATION, selectedShapeIndex);
    if (selectedShape.isPresent()) {
      MAShapeDimensions.Dimensions dimensions = MAShapeDimensions.shaftFromConfig(this.shaftWidth, this.shaftHeight,
          this.targetDepth);
      MAShapeContext context = new MAShapeContext(
          world,
          player,
          origin,
          world.getBlockState(origin),
          hitFace == null ? this.direction.getOpposite() : hitFace,
          this.direction,
          dimensions.width(),
          dimensions.height(),
          dimensions.depth());
      Set<BlockPos> shapePositions = MAShapePrecomputeCache.compute(selectedShape.get(), context);
      if (MAShapeIds.SHAFTANATION_SHAFT.equals(selectedShape.get().id())) {
        shapePositions = clampStraightShaftHeight(shapePositions, floorY, this.shaftHeight);
      }
      Set<BlockPos> queuedPositions = excludeOriginIfPresent(shapePositions, origin);
      queue.addAll(
          orderShaftPositions(
              queuedPositions,
              new BlockPos(origin.getX(), floorY, origin.getZ()),
              this.direction,
              this.shaftWidth,
              this.shaftHeight,
              this.targetDepth,
              0,
              origin.getY() - floorY,
              staircaseLayerRisePerDepth(selectedShape.get().id())));

      if (autoIlluminate) {
        int halfWidth = shaftWidth / 2;
        BlockPos floorOrigin = new BlockPos(origin.getX(), floorY, origin.getZ());
        BlockPos[] floorByDepth = collectDepthFloorBases(queuedPositions, floorOrigin, this.direction,
            this.targetDepth);
        for (int depth = 1; depth < targetDepth; depth++) {
          BlockPos torchBase = floorByDepth[depth];
          if (torchBase == null) {
            continue;
          }
          addTorchTargets(torchBase, halfWidth);
        }
      }
      logQueueHeightSummary("shape", floorY);
      return;
    }

    int halfWidth = shaftWidth / 2;
    boolean alongZ = this.direction.getAxis() == Direction.Axis.Z;
    BlockPos floorOrigin = new BlockPos(origin.getX(), floorY, origin.getZ());
    ArrayList<BlockPos> fallbackPositions = new ArrayList<>();
    for (int depth = 0; depth < targetDepth; depth++) {
      BlockPos base = floorOrigin.relative(this.direction, depth);
      for (int w = -halfWidth; w <= halfWidth; w++) {
        for (int h = 0; h < shaftHeight; h++) {
          fallbackPositions.add((alongZ ? base.offset(w, h, 0) : base.offset(0, h, w)).immutable());
        }
      }
      if (autoIlluminate && depth > 0) {
        addTorchTargets(base, halfWidth);
      }
    }
    queue.addAll(
        orderShaftPositions(
            new java.util.LinkedHashSet<>(fallbackPositions),
            floorOrigin,
            this.direction,
            this.shaftWidth,
            this.shaftHeight,
            this.targetDepth,
            0,
            origin.getY() - floorY,
            0));
    logQueueHeightSummary("fallback", floorY);
  }

  private void logQueueHeightSummary(String source, int floorY) {
    if (queue.isEmpty()) {
      LogUtils.logDebug(
          "Shaft queue summary player={} source={} originY={} floorY={} configuredHeight={} queueSize=0",
          player.getScoreboardName(), source, origin.getY(), floorY, shaftHeight);
      return;
    }

    int minY = Integer.MAX_VALUE;
    int maxY = Integer.MIN_VALUE;
    TreeSet<Integer> localHeights = new TreeSet<>();
    for (ShaftTarget target : queue) {
      int y = target.pos().getY();
      minY = Math.min(minY, y);
      maxY = Math.max(maxY, y);
      localHeights.add(y - floorY);
    }

    LogUtils.logDebug(
        "Shaft queue summary player={} source={} originY={} floorY={} configuredHeight={} minY={} maxY={} uniqueY={} localHeights={} queueSize={}",
        player.getScoreboardName(),
        source,
        origin.getY(),
        floorY,
        shaftHeight,
        minY,
        maxY,
        (maxY - minY + 1),
        localHeights,
        queue.size());
  }

  private record ShaftLocal(int depth, int width, int height) {
  }

  private static List<ShaftTarget> orderShaftPositions(
      Set<BlockPos> positions,
      BlockPos floorOrigin,
      Direction forward,
      int width,
      int height,
      int depth,
      int startWidth,
      int startHeight,
      int layerRisePerDepth) {
    if (positions == null || positions.isEmpty()) {
      return List.of();
    }

    Direction shaftForward = ShapeGeometryUtils.horizontalOrNorth(forward);
    Direction shaftRight = ShapeGeometryUtils.rightFromForward(shaftForward);

    Map<BlockPos, ShaftLocal> localByPos = new HashMap<>(positions.size());
    for (BlockPos pos : positions) {
      int relX = pos.getX() - floorOrigin.getX();
      int relY = pos.getY() - floorOrigin.getY();
      int relZ = pos.getZ() - floorOrigin.getZ();
      int localDepth = relX * shaftForward.getStepX() + relZ * shaftForward.getStepZ();
      int localWidth = relX * shaftRight.getStepX() + relZ * shaftRight.getStepZ();
      int localHeight = normalizedLayerHeight(relY, localDepth, layerRisePerDepth);
      localByPos.put(pos, new ShaftLocal(localDepth, localWidth, localHeight));
    }

    int minWidth = ShapeGeometryUtils.minRightBiasedCenteredOffset(width);
    int maxWidth = ShapeGeometryUtils.maxRightBiasedCenteredOffset(width);
    int minHeight = 0;
    int maxHeight = Math.max(0, height - 1);
    Map<Long, Integer> spiralIndex = clockwiseSpiralIndex(minWidth, maxWidth, minHeight, maxHeight, startWidth,
        startHeight);

    ArrayList<ShaftTarget> ordered = new ArrayList<>(positions.size());
    for (BlockPos pos : positions) {
      ordered.add(new ShaftTarget(pos.immutable(), localByPos.get(pos).depth()));
    }

    ordered.sort(
        Comparator
            .comparingInt((ShaftTarget target) -> clampDepth(target.depth(), depth))
            .thenComparingInt(target -> spiralIndex.getOrDefault(
                pairKey(localByPos.get(target.pos()).width(), localByPos.get(target.pos()).height()),
                Integer.MAX_VALUE))
            .thenComparingInt(target -> target.pos().getY())
            .thenComparingInt(target -> target.pos().getX())
            .thenComparingInt(target -> target.pos().getZ()));
    return ordered;
  }

  private static int normalizedLayerHeight(int relY, int localDepth, int layerRisePerDepth) {
    int baseLayerY = layerRisePerDepth * localDepth;
    if (layerRisePerDepth < 0) {
      return baseLayerY - relY;
    }
    return relY - baseLayerY;
  }

  private static int staircaseLayerRisePerDepth(String shapeId) {
    if (MAShapeIds.SHAFTANATION_STAIRCASE_UP.equals(shapeId)) {
      return 1;
    }
    if (MAShapeIds.SHAFTANATION_STAIRCASE_DOWN.equals(shapeId)) {
      return -1;
    }
    return 0;
  }

  private static BlockPos[] collectDepthFloorBases(Set<BlockPos> positions, BlockPos floorOrigin, Direction forward,
      int maxDepth) {
    BlockPos[] floors = new BlockPos[Math.max(0, maxDepth)];
    if (positions == null || positions.isEmpty() || maxDepth <= 0) {
      return floors;
    }

    Direction shaftForward = ShapeGeometryUtils.horizontalOrNorth(forward);
    int[] minYByDepth = new int[maxDepth];
    Arrays.fill(minYByDepth, Integer.MAX_VALUE);

    for (BlockPos pos : positions) {
      int relX = pos.getX() - floorOrigin.getX();
      int relZ = pos.getZ() - floorOrigin.getZ();
      int depth = relX * shaftForward.getStepX() + relZ * shaftForward.getStepZ();
      if (depth < 0 || depth >= maxDepth) {
        continue;
      }
      if (pos.getY() < minYByDepth[depth]) {
        minYByDepth[depth] = pos.getY();
      }
    }

    for (int depth = 0; depth < maxDepth; depth++) {
      if (minYByDepth[depth] == Integer.MAX_VALUE) {
        continue;
      }
      BlockPos depthBase = floorOrigin.relative(shaftForward, depth);
      floors[depth] = new BlockPos(depthBase.getX(), minYByDepth[depth], depthBase.getZ());
    }

    return floors;
  }

  private static Set<BlockPos> excludeOriginIfPresent(Set<BlockPos> positions, BlockPos origin) {
    if (positions == null || positions.isEmpty() || origin == null) {
      return positions;
    }
    LinkedHashSet<BlockPos> filtered = new LinkedHashSet<>(positions);
    filtered.remove(origin);
    return filtered;
  }

  private static Set<BlockPos> clampStraightShaftHeight(Set<BlockPos> positions, int floorY, int shaftHeight) {
    if (positions == null || positions.isEmpty()) {
      return positions;
    }

    int clampedHeight = Math.max(1, shaftHeight);
    int minY = floorY;
    int maxY = floorY + clampedHeight - 1;
    LinkedHashSet<BlockPos> filtered = new LinkedHashSet<>(positions.size());
    for (BlockPos pos : positions) {
      int y = pos.getY();
      if (y >= minY && y <= maxY) {
        filtered.add(pos);
      }
    }
    return filtered;
  }

  private static int clampDepth(int localDepth, int maxDepth) {
    if (localDepth < 0) {
      return Integer.MAX_VALUE - 1;
    }
    if (localDepth >= maxDepth) {
      return Integer.MAX_VALUE;
    }
    return localDepth;
  }

  private static Map<Long, Integer> clockwiseSpiralIndex(
      int minW,
      int maxW,
      int minH,
      int maxH,
      int startW,
      int startH) {
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
      int h) {
    if (w < minW || w > maxW || h < minH || h > maxH) {
      return;
    }
    index.putIfAbsent(pairKey(w, h), index.size());
  }

  private static long pairKey(int a, int b) {
    return (((long) a) << 32) ^ (b & 0xffffffffL);
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
    while (!queue.isEmpty() && count < blocksPerTick) {
      ShaftTarget target = queue.poll();
      if (target == null) {
        continue;
      }

      if (target.depth() != activeShaftLayer) {
        if (activeShaftLayer != Integer.MIN_VALUE && !activeShaftLayerHasNonAir) {
          shaftEmptyLayerAborted = true;
          queue.clear();
          break;
        }
        activeShaftLayer = target.depth();
        activeShaftLayerHasNonAir = false;
      }

      BlockPos pos = target.pos();
      BlockState state = world.getBlockState(pos);
      if (!state.isAir()) {
        activeShaftLayerHasNonAir = true;
        BreakOutcome breakOutcome = breakBlockWithTool(pos, veinationTriggerTool);
        if (breakOutcome.broken()) {
          veinationTriggerTool = breakOutcome.toolAfterBreak().copy();
          maybeFanOutVeination(pos, state, mineVeins, this.commonConfig, veinationRuntime, veinationConfig,
              veinationTriggerTool);
          dug++;
          count++;
        }
      }
    }

    if (queue.isEmpty()) {
      boolean madeProgress = false;
      int scanBudget = torchQueue.size();
      while (scanBudget-- > 0 && !torchQueue.isEmpty()) {
        TorchJob torchJob = torchQueue.peekFirst();
        TorchPlacementDecision decision = evaluateTorchPlacement(torchJob);
        if (decision == TorchPlacementDecision.PLACE) {
          torchQueue.pollFirst();
          if (playerHasTorches()) {
            placeTorch(torchJob);
            madeProgress = true;
          } else {
            LogUtils.logDebug("Shaft torch skipped: no torches in inventory player={} pos={}",
                player.getScoreboardName(), torchJob.pos());
            torchQueue.clear();
          }
          break;
        }
        if (decision == TorchPlacementDecision.DISCARD) {
          torchQueue.pollFirst();
          LogUtils.logDebug("Discarded shaft torch job player={} pos={} facing={} reason=unplaceable",
              player.getScoreboardName(), torchJob.pos(), torchJob.facing());
          madeProgress = true;
          continue;
        }

        // Defer bright candidates so darker ones deeper in the queue can be considered this tick.
        torchQueue.addLast(torchQueue.pollFirst());
      }

      if (madeProgress) {
        torchLightWaitTicks = 0;
      } else if (!torchQueue.isEmpty()) {
        torchLightWaitTicks++;
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
              dropped);
          torchLightWaitTicks = 0;
        }
      }
    }

    if (queue.isEmpty() && torchQueue.isEmpty()) {
      return finish(shaftEmptyLayerAborted ? "shaft empty layer" : "shaft queue exhausted");
    }
    return false;
  }

  /**
   * Enqueue torch jobs for configured placement mode at one depth slice.
   */
  private void addTorchTargets(BlockPos base, int halfWidth) {
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
    TorchGeometry geometry = wallTorchGeometry(shaftDirection.getStepX(), shaftDirection.getStepZ(), halfWidth,
        leftWall);
    Direction wallDirection = leftWall ? shaftDirection.getCounterClockWise() : shaftDirection.getClockWise();
    return new TorchJob(
        base.offset(geometry.offsetX(), geometry.offsetY(), geometry.offsetZ()).immutable(),
        wallDirection.getOpposite(),
        base.above(geometry.lightCheckOffsetY()).immutable());
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
    if (!autoIlluminate) {
      return TorchPlacementDecision.DISCARD;
    }
    if (config.torchPlacement() == null) {
      return TorchPlacementDecision.DISCARD;
    }

    BlockPos pos = torchJob.pos();
    if (!world.isEmptyBlock(pos)) {
      return TorchPlacementDecision.DISCARD;
    }

    int lightLevel = effectiveTorchLight(torchJob.lightCheckPos());
    if (lightLevel > torchLowestLightLevel) {
      return TorchPlacementDecision.WAIT_FOR_LIGHT;
    }

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
