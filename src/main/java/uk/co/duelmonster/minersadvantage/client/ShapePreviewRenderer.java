package uk.co.duelmonster.minersadvantage.client;

import java.util.Objects;
import java.util.HashSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
//? if mc1 {
import com.mojang.blaze3d.platform.DepthTestFunction;
//?} else {
/*
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.platform.CompareOp;
*/ //?}
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
//? if mc26 {
/*
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.OutputTarget;
*/ //?}
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import java.util.function.Supplier;
import uk.co.duelmonster.minersadvantage.common.config.ClientConfig;
import uk.co.duelmonster.minersadvantage.common.config.MAConfig_Base;
import uk.co.duelmonster.minersadvantage.common.config.MAServerRootConfig;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;
import uk.co.duelmonster.minersadvantage.common.log.LogUtils;
import uk.co.duelmonster.minersadvantage.common.services.input.ClientInputService.ClientInputState;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeBootstrap;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeContext;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeDefinition;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeDimensions;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapePrecomputeCache;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeRegistry;

/**
 * ShapePreviewRenderer renders lightweight held-key shape previews on the client.
 *
 * Uses a two-pass rendering approach matching LiteMiner:
 *   Pass 1: translucent lines with NO_DEPTH_TEST so occluded bounds show through blocks.
 *   Pass 2: opaque lines with normal depth testing for crisp foreground edges.
 */
public final class ShapePreviewRenderer {
  private static final int SHAPELESS_BUILD_STEPS_PER_FRAME = 24;
  private static final double OUTLINE_INFLATE = 0.005d;
  private static final int OUTLINE_OPTIMIZE_MAX_BLOCKS = 96;
  private static final int OUTLINE_CACHE_MAX_ENTRIES = 64;
  private static final String SHAPE_ID_SHAPELESS = "minersadvantage:shapeless";
  private static final MAShapeDefinition VENTILATION_PREVIEW_SHAPE = new MAShapeDefinition(
      "minersadvantage:ventilation_preview",
      "Ventilation Preview",
      FeatureId.VENTILATION,
      context -> {
        Set<BlockPos> positions = new LinkedHashSet<>();
        int ventDepth = Math.max(1, context.height());
        Direction ventDirection = context.hitFace() == Direction.UP ? Direction.DOWN : Direction.UP;
        for (int depth = 0; depth < ventDepth; depth++) {
          positions.add(context.origin().relative(ventDirection, depth).immutable());
        }
        return positions;
      });
  private static final long DIAGNOSTIC_SKIP_LOG_INTERVAL_NANOS = 2_000_000_000L;
  private static final long DIAGNOSTIC_ACTIVE_LOG_INTERVAL_NANOS = 500_000_000L;
  private static final double DIAGNOSTIC_SLOW_COMPUTE_MS = 6.0d;
  private static final double DIAGNOSTIC_SLOW_RENDER_MS = 4.0d;
  private static long lastMissingWorldLogNanos;
  private static long lastMissingHitResultLogNanos;
  private static long lastInactivePreviewLogNanos;
  private static long lastActivePreviewLogNanos;
  private static volatile java.lang.reflect.Method heldKeyMethod;
  private static volatile boolean heldKeyMethodInitialized;
  private static volatile String heldKeyMethodSource;

  private static final RenderType LINES_NORMAL = RenderTypes.lines();
  private static final RenderType LINES_TRANSLUCENT_NO_DEPTH_TEST = createLinesTranslucentNoDepthTestRenderType();

  private static final OutlineCache CACHE = new OutlineCache();

  private record OutlineKey(
      FeatureId feature,
      int shapeIndex,
      int width,
      int height,
      int depth,
      BlockPos origin,
      Direction hitFace,
      Direction playerFacing) {
  }

  private static final class ShapelessOutlineBuild {
    final VoxelShape[] blockShapes;
    int nextIndex;

    ShapelessOutlineBuild(VoxelShape[] blockShapes, int nextIndex) {
      this.blockShapes = blockShapes;
      this.nextIndex = nextIndex;
    }
  }

  private static final class CachedOutline {
    VoxelShape combinedShape;
    ShapelessOutlineBuild pendingShapelessBuild;

    CachedOutline(VoxelShape combinedShape, ShapelessOutlineBuild pendingShapelessBuild) {
      this.combinedShape = combinedShape;
      this.pendingShapelessBuild = pendingShapelessBuild;
    }
  }

  private static final class OutlineCache {
    private final Map<OutlineKey, CachedOutline> shapesByKey = new HashMap<>();

    /**
     * Retrieve cached shape for a fully-qualified preview key.
     */
    CachedOutline get(
        FeatureId feature,
        int shapeIndex,
        int width,
        int height,
        int depth,
        BlockPos origin,
        Direction hitFace,
        Direction playerFacing) {
      return shapesByKey.get(new OutlineKey(feature, shapeIndex, width, height, depth, origin, hitFace, playerFacing));
    }

    /**
     * Store freshly computed shape for the current origin and preview key.
     */
    void put(
        FeatureId feature,
        int shapeIndex,
        int width,
        int height,
        int depth,
        BlockPos origin,
        Direction hitFace,
        Direction playerFacing,
        CachedOutline outline) {
      if (shapesByKey.size() >= OUTLINE_CACHE_MAX_ENTRIES) {
        LogUtils.logDebug("Preview cache capacity reached entries={} limit={} clearing cache", shapesByKey.size(),
            OUTLINE_CACHE_MAX_ENTRIES);
        shapesByKey.clear();
      }
      shapesByKey.put(new OutlineKey(feature, shapeIndex, width, height, depth, origin, hitFace, playerFacing),
          outline);
    }
  }

  /**
   * Utility class only.
   */
  private ShapePreviewRenderer() {
  }

  /**
   * Build no-depth translucent line render type used for occluded preview edges.
   */
  private static RenderType createLinesTranslucentNoDepthTestRenderType() {
    //? if mc1 {
    RenderPipeline.Snippet snippet = RenderPipeline
        .builder(RenderPipelines.MATRICES_FOG_SNIPPET, RenderPipelines.GLOBALS_SNIPPET)
        .withVertexShader("core/rendertype_lines")
        .withFragmentShader("core/rendertype_lines")
        .withBlend(BlendFunction.TRANSLUCENT)
        .withCull(false)
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .withVertexFormat(DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH, VertexFormat.Mode.LINES)
        .buildSnippet();

    RenderPipeline pipeline = RenderPipeline.builder(snippet)
        .withLocation("pipeline/minersadvantage_lines_translucent_no_depth")
        .build();

    RenderSetup setup = RenderSetup.builder(pipeline)
        .useLightmap()
        .createRenderSetup();

    return RenderType.create("minersadvantage_lines_translucent_no_depth_test", setup);
    //?} else {
    /*
    RenderPipeline.Builder builder = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET, RenderPipelines.GLOBALS_SNIPPET)
      .withVertexShader("core/rendertype_lines")
      .withFragmentShader("core/rendertype_lines")
      .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
      .withCull(false);
    builder = applyMc26LineVertexFormat(builder);
    RenderPipeline.Snippet snippet = builder
      .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
      .buildSnippet();
    
    RenderPipeline pipeline = RenderPipeline.builder(snippet)
        .withLocation("pipeline/minersadvantage_lines_translucent_no_depth")
        .build();
    
    RenderSetup setup = RenderSetup.builder(pipeline)
        .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
        .setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
        .createRenderSetup();
    
    return RenderType.create("minersadvantage_lines_translucent_no_depth_test", setup);
    */ //?}
  }

  //? if mc26 {
  /*
  private static RenderPipeline.Builder applyMc26LineVertexFormat(RenderPipeline.Builder builder) {
    try {
      Method withVertexBinding = builder.getClass().getMethod("withVertexBinding", int.class, VertexFormat.class);
      Object boundBuilder = withVertexBinding.invoke(builder, 0, DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH);
      Class<?> primitiveTopologyClass = Class.forName("com.mojang.blaze3d.PrimitiveTopology");
      @SuppressWarnings({ "unchecked", "rawtypes" })
      Object lines = Enum.valueOf((Class<? extends Enum>) primitiveTopologyClass.asSubclass(Enum.class), "LINES");
      Method withPrimitiveTopology = builder.getClass().getMethod("withPrimitiveTopology", primitiveTopologyClass);
      Object result = withPrimitiveTopology.invoke(boundBuilder, lines);
      if (result instanceof RenderPipeline.Builder typedBuilder) {
        return typedBuilder;
      }
    } catch (ReflectiveOperationException ignored) {
    }
  
    try {
      Class<?> modeClass = Class.forName("com.mojang.blaze3d.vertex.VertexFormat$Mode");
      @SuppressWarnings({ "unchecked", "rawtypes" })
      Object lines = Enum.valueOf((Class<? extends Enum>) modeClass.asSubclass(Enum.class), "LINES");
      Method withVertexFormat = builder.getClass().getMethod("withVertexFormat", VertexFormat.class, modeClass);
      Object result = withVertexFormat.invoke(builder, DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH, lines);
      if (result instanceof RenderPipeline.Builder typedBuilder) {
        return typedBuilder;
      }
    } catch (ReflectiveOperationException ignored) {
    }
  
    return builder;
  }
  */ //?}

  /**
   * Render active held-key preview outlines for excavation/shaft/ventilation contexts.
   */
  public static void renderHeldPreview(ClientInputState state, PoseStack poseStack, double cameraX, double cameraY,
      double cameraZ) {
    long frameStartNanos = System.nanoTime();
    Minecraft minecraft = Minecraft.getInstance();
    // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
    if (minecraft.level == null || minecraft.player == null) {
      logRateLimitedSkip("missing-world-or-player", lastMissingWorldLogNanos, frameStartNanos,
          "Preview skipped reason=missing-world-or-player levelPresent={} playerPresent={}",
          minecraft.level != null,
          minecraft.player != null);
      lastMissingWorldLogNanos = frameStartNanos;
      return;
    }
    // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
    if (!(minecraft.hitResult instanceof BlockHitResult blockHit)) {
      logRateLimitedSkip("missing-block-hit", lastMissingHitResultLogNanos, frameStartNanos,
          "Preview skipped reason=missing-block-hit hitResultType={}",
          minecraft.hitResult == null ? "null" : minecraft.hitResult.getClass().getSimpleName());
      lastMissingHitResultLogNanos = frameStartNanos;
      return;
    }

    boolean excavationKeyHeld = isActionKeyHeld(KeyBindings.ClientAction.EXCAVATION_MODE_TOGGLE);
    boolean shaftVentKeyHeld = isActionKeyHeld(KeyBindings.ClientAction.SHAFT_VENT_TOGGLE);

    boolean excavationPreview = excavationKeyHeld
        && state.excavationToggled()
        && state.featureEnabled().getOrDefault(FeatureId.EXCAVATION, false);
    boolean shaftVentPreview = shaftVentKeyHeld
        && state.shaftVentToggled()
        && state.featureEnabled().getOrDefault(FeatureId.SHAFTANATION, false);
    boolean shaftPreview = shaftVentPreview && blockHit.getDirection().getAxis().isHorizontal();
    boolean ventilationPreview = shaftVentPreview && blockHit.getDirection().getAxis().isVertical();

    // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
    if (!excavationPreview && !shaftPreview && !ventilationPreview) {
      logRateLimitedSkip("inactive-preview", lastInactivePreviewLogNanos, frameStartNanos,
          "Preview inactive held[excavation={},shaftVent={}] toggled[excavation={},shaftVent={}] featureEnabled[excavation={},shaftanation={}] hitFace={}",
          excavationKeyHeld,
          shaftVentKeyHeld,
          state.excavationToggled(),
          state.shaftVentToggled(),
          state.featureEnabled().getOrDefault(FeatureId.EXCAVATION, false),
          state.featureEnabled().getOrDefault(FeatureId.SHAFTANATION, false),
          blockHit.getDirection());
      lastInactivePreviewLogNanos = frameStartNanos;
      return;
    }

    MAShapeBootstrap.ensureInitialized();
    Player player = minecraft.player;
    var syncedConfig = MAConfig_Base.getGlobalConfig();
    BlockPos origin = blockHit.getBlockPos();
    Direction hitFace = blockHit.getDirection();
    Direction playerFacing = player.getDirection();

    if (shouldLogAtInterval(lastActivePreviewLogNanos, frameStartNanos, DIAGNOSTIC_ACTIVE_LOG_INTERVAL_NANOS)) {
      lastActivePreviewLogNanos = frameStartNanos;
      LogUtils.logDebug(
          "Preview active origin={} hitFace={} playerFacing={} camera=({},{},{}) excavationPreview={} shaftPreview={} ventilationPreview={} selectedShapes[excavation={},shaft={}] held[excavation={},shaftVent={}]",
          origin,
          hitFace,
          playerFacing,
          String.format(java.util.Locale.ROOT, "%.3f", cameraX),
          String.format(java.util.Locale.ROOT, "%.3f", cameraY),
          String.format(java.util.Locale.ROOT, "%.3f", cameraZ),
          excavationPreview,
          shaftPreview,
          ventilationPreview,
          state.selectedExcavationShapeIndex(),
          state.selectedShaftanationShapeIndex(),
          excavationKeyHeld,
          shaftVentKeyHeld);
    }

    // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
    if (excavationPreview) {
      var excavation = syncedConfig.excavation();
      MAShapeDimensions.Dimensions dimensions = MAShapeDimensions.excavationFromConfig(
          excavation.width(),
          excavation.height(),
          excavation.depth());
      MAShapeRegistry.byIndex(FeatureId.EXCAVATION, state.selectedExcavationShapeIndex())
          .ifPresent(shape -> {
            MAShapeContext context = new MAShapeContext(
                minecraft.level,
                player,
                origin,
                minecraft.level.getBlockState(origin),
                hitFace,
                playerFacing,
                dimensions.width(),
                dimensions.height(),
                dimensions.depth());
            renderOutline(
                FeatureId.EXCAVATION,
                shape.id(),
                origin,
                state.selectedExcavationShapeIndex(),
                dimensions,
                hitFace,
                playerFacing,
                () -> MAShapePrecomputeCache.compute(shape, context),
                poseStack,
                cameraX,
                cameraY,
                cameraZ);
          });
    }

    // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
    if (shaftPreview) {
      var shaft = syncedConfig.shaftanation();
      MAShapeDimensions.Dimensions dimensions = MAShapeDimensions.shaftFromConfig(shaft.width(), shaft.height(),
          shaft.depth());
      MAShapeContext context = new MAShapeContext(
          minecraft.level,
          player,
          origin,
          minecraft.level.getBlockState(origin),
          hitFace,
          playerFacing,
          dimensions.width(),
          dimensions.height(),
          dimensions.depth());
      MAShapeRegistry.byIndex(FeatureId.SHAFTANATION, state.selectedShaftanationShapeIndex())
          .ifPresent(shape -> renderOutline(
              FeatureId.SHAFTANATION,
              shape.id(),
              origin,
              state.selectedShaftanationShapeIndex(),
              dimensions,
              hitFace,
              playerFacing,
              () -> MAShapePrecomputeCache.compute(shape, context),
              poseStack,
              cameraX,
              cameraY,
              cameraZ));
    }

    // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
    if (ventilationPreview) {
      var ventilation = syncedConfig.ventilation();
      int ventDepth = Math.max(1, ventilation.height());
      MAShapeContext context = new MAShapeContext(
          minecraft.level,
          player,
          origin,
          minecraft.level.getBlockState(origin),
          hitFace,
          playerFacing,
          1,
          ventDepth,
          1);
      renderOutline(
          FeatureId.VENTILATION,
          "minersadvantage:ventilation",
          origin,
          0,
          new MAShapeDimensions.Dimensions(1, ventDepth, 1),
          hitFace,
          playerFacing,
          () -> MAShapePrecomputeCache.compute(VENTILATION_PREVIEW_SHAPE, context),
          poseStack,
          cameraX,
          cameraY,
          cameraZ);
    }

    logSlowOperation("renderHeldPreview.total", frameStartNanos, DIAGNOSTIC_SLOW_RENDER_MS,
        "origin={} hitFace={} playerFacing={} excavationPreview={} shaftPreview={} ventilationPreview={}",
        origin,
        hitFace,
        playerFacing,
        excavationPreview,
        shaftPreview,
        ventilationPreview);
  }

  private static void renderOutline(
      FeatureId feature,
      String shapeId,
      BlockPos origin,
      int shapeIndex,
      MAShapeDimensions.Dimensions dimensions,
      Direction hitFace,
      Direction playerFacing,
      Supplier<Set<BlockPos>> positionsSupplier,
      PoseStack poseStack,
      double cameraX,
      double cameraY,
      double cameraZ) {
    boolean cacheable = true;
    BlockPos cacheOrigin = SHAPE_ID_SHAPELESS.equals(shapeId) ? origin.immutable() : null;
    CachedOutline outline = cacheable
        ? CACHE.get(
            feature,
            shapeIndex,
            dimensions.width(),
            dimensions.height(),
            dimensions.depth(),
            cacheOrigin,
            hitFace,
            playerFacing)
        : null;
    boolean cacheHit = outline != null;
    long outlineStartNanos = System.nanoTime();

    // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
    if (outline == null) {
      long positionsStartNanos = System.nanoTime();
      Set<BlockPos> positions = positionsSupplier.get();
      logSlowOperation(
          "preview.positionsSupplier",
          positionsStartNanos,
          DIAGNOSTIC_SLOW_COMPUTE_MS,
          "feature={} shapeId={} origin={} shapeIndex={} dimensions={}x{}x{} hitFace={} playerFacing={} cacheable={}",
          feature,
          shapeId,
          origin,
          shapeIndex,
          dimensions.width(),
          dimensions.height(),
          dimensions.depth(),
          hitFace,
          playerFacing,
          cacheable);
      // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
      if (positions.isEmpty()) {
        LogUtils.logDebug(
            "Preview outline empty feature={} shapeId={} origin={} shapeIndex={} dimensions={}x{}x{} hitFace={} playerFacing={} cacheable={}",
            feature,
            shapeId,
            origin,
            shapeIndex,
            dimensions.width(),
            dimensions.height(),
            dimensions.depth(),
            hitFace,
            playerFacing,
            cacheable);
        return;
      }
      LogUtils.logDebug(
          "Preview outline cache miss feature={} shapeId={} origin={} shapeIndex={} dimensions={}x{}x{} hitFace={} playerFacing={} positions={} cacheable={}",
          feature,
          shapeId,
          origin,
          shapeIndex,
          dimensions.width(),
          dimensions.height(),
          dimensions.depth(),
          hitFace,
          playerFacing,
          positions.size(),
          cacheable);
      if (SHAPE_ID_SHAPELESS.equals(shapeId)) {
        outline = createIncrementalShapelessOutline(positions, origin);
      } else {
        boolean shouldOptimize = positions.size() <= OUTLINE_OPTIMIZE_MAX_BLOCKS;
        long combineStartNanos = System.nanoTime();
        VoxelShape combinedShape = combineToVoxelShape(positions, origin, shouldOptimize);
        logSlowOperation(
            "preview.combineToVoxelShape",
            combineStartNanos,
            DIAGNOSTIC_SLOW_COMPUTE_MS,
            "feature={} shapeId={} origin={} positions={} optimize={}",
            feature,
            shapeId,
            origin,
            positions.size(),
            shouldOptimize);
        outline = new CachedOutline(combinedShape, null);
      }
      if (cacheable) {
        CACHE.put(
            feature,
            shapeIndex,
            dimensions.width(),
            dimensions.height(),
            dimensions.depth(),
            cacheOrigin,
            hitFace,
            playerFacing,
            outline);
      }
    } else if (shouldLogAtInterval(lastActivePreviewLogNanos, System.nanoTime(),
        DIAGNOSTIC_ACTIVE_LOG_INTERVAL_NANOS)) {
      LogUtils.logDebug(
          "Preview outline cache hit feature={} shapeId={} origin={} shapeIndex={} dimensions={}x{}x{} hitFace={} playerFacing={}",
          feature,
          shapeId,
          origin,
          shapeIndex,
          dimensions.width(),
          dimensions.height(),
          dimensions.depth(),
          hitFace,
          playerFacing);
    }

    // Continue incremental shapeless merge on the render thread with a small per-frame budget.
    long shapelessAdvanceStartNanos = System.nanoTime();
    advanceShapelessBuild(outline, SHAPELESS_BUILD_STEPS_PER_FRAME);
    logSlowOperation(
        "preview.advanceShapelessBuild",
        shapelessAdvanceStartNanos,
        DIAGNOSTIC_SLOW_COMPUTE_MS,
        "feature={} shapeId={} origin={} cacheHit={}",
        feature,
        shapeId,
        origin,
        cacheHit);

    VoxelShape combinedShape = outline.combinedShape;
    // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
    if (combinedShape == null || combinedShape.isEmpty()) {
      LogUtils.logDebug("Preview outline combined shape empty feature={} shapeId={} origin={} cacheHit={}",
          feature,
          shapeId,
          origin,
          cacheHit);
      return;
    }

    // Use the game's own render buffer source directly, just like LiteMiner does.
    // The event-provided consumers cannot support custom RenderTypes with custom pipelines.
    Object buffers = ClientRuntimeCompat.getBufferSource(Minecraft.getInstance());
    if (buffers == null) {
      return;
    }
    float lineWidth = Minecraft.getInstance().getWindow().getAppropriateLineWidth();
    ClientConfig clientConfig = MAConfig_Base.getClientRootConfig().client();
    int outlineForegroundColor = clientConfig.outlineForegroundColor();
    int outlineSeeThroughColor = clientConfig.outlineSeeThroughColor();
    long drawStartNanos = System.nanoTime();

    poseStack.pushPose();
    poseStack.translate(origin.getX() - cameraX, origin.getY() - cameraY, origin.getZ() - cameraZ);

    // Pass 1: translucent, NO_DEPTH_TEST -- occluded bounds visible through blocks
    Object translucentBuilder = ClientRuntimeCompat.getBuffer(buffers, LINES_TRANSLUCENT_NO_DEPTH_TEST);
    ClientRuntimeCompat.renderShape(poseStack, translucentBuilder, combinedShape, outlineSeeThroughColor, lineWidth);

    // Pass 2: opaque, normal depth test -- foreground edges
    Object opaqueBuilder = ClientRuntimeCompat.getBuffer(buffers, LINES_NORMAL);
    ClientRuntimeCompat.renderShape(poseStack, opaqueBuilder, combinedShape, outlineForegroundColor, lineWidth);

    ClientRuntimeCompat.endBatch(buffers, LINES_TRANSLUCENT_NO_DEPTH_TEST);
    ClientRuntimeCompat.endBatch(buffers, LINES_NORMAL);

    poseStack.popPose();

    logSlowOperation(
        "preview.drawAndFlush",
        drawStartNanos,
        DIAGNOSTIC_SLOW_RENDER_MS,
        "feature={} shapeId={} origin={} cacheHit={} lineWidth={} foregroundColor={} seeThroughColor={}",
        feature,
        shapeId,
        origin,
        cacheHit,
        String.format(java.util.Locale.ROOT, "%.3f", lineWidth),
        outlineForegroundColor,
        outlineSeeThroughColor);
    logSlowOperation(
        "preview.renderOutline.total",
        outlineStartNanos,
        DIAGNOSTIC_SLOW_RENDER_MS,
        "feature={} shapeId={} origin={} cacheHit={} dimensions={}x{}x{}",
        feature,
        shapeId,
        origin,
        cacheHit,
        dimensions.width(),
        dimensions.height(),
        dimensions.depth());
  }

  /**
   * Combine per-block AABBs into one optimized voxel outline shape.
   */
  private static VoxelShape combineToVoxelShape(Set<BlockPos> positions, BlockPos origin, boolean optimize) {
    VoxelShape combinedShape = Shapes.empty();
    int originX = origin.getX();
    int originY = origin.getY();
    int originZ = origin.getZ();

    // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
    for (BlockPos position : positions) {
      int relativeX = position.getX() - originX;
      int relativeY = position.getY() - originY;
      int relativeZ = position.getZ() - originZ;
      AABB inflatedBox = new AABB(
          relativeX,
          relativeY,
          relativeZ,
          relativeX + 1,
          relativeY + 1,
          relativeZ + 1).inflate(OUTLINE_INFLATE);

      combinedShape = Shapes.join(combinedShape, Shapes.create(inflatedBox), BooleanOp.OR);
    }

    // Large irregular previews (especially shapeless) spend disproportionate time in optimize() for little visual gain.
    if (!optimize) {
      return combinedShape;
    }

    return combinedShape.optimize();
  }

  private static boolean isActionKeyHeld(KeyBindings.ClientAction action) {
    java.lang.reflect.Method method = resolveHeldKeyMethod();
    if (method == null) {
      return false;
    }

    try {
      Object result = method.invoke(null, action);
      return result instanceof Boolean && (Boolean) result;
    } catch (ReflectiveOperationException exception) {
      LogUtils.logWarn("Preview key-held lookup invocation failed source={} action={} exceptionType={}",
          heldKeyMethodSource,
          action,
          exception.getClass().getSimpleName());
      return false;
    }
  }

  private static java.lang.reflect.Method resolveHeldKeyMethod() {
    if (heldKeyMethodInitialized) {
      return heldKeyMethod;
    }

    synchronized (ShapePreviewRenderer.class) {
      if (heldKeyMethodInitialized) {
        return heldKeyMethod;
      }

      heldKeyMethod = findHeldKeyMethod("uk.co.duelmonster.minersadvantage.client.ClientInputHandler");
      if (heldKeyMethod == null) {
        heldKeyMethod = findHeldKeyMethod("uk.co.duelmonster.minersadvantage.client.NeoForgeClientEvents");
      }

      heldKeyMethodInitialized = true;
      if (heldKeyMethod != null) {
        LogUtils.logDebug("Preview key-held lookup bound source={}", heldKeyMethodSource);
      } else {
        LogUtils.logWarn("Preview key-held lookup unavailable on all known client input sources");
      }
      return heldKeyMethod;
    }
  }

  private static java.lang.reflect.Method findHeldKeyMethod(String className) {
    try {
      Class<?> sourceClass = Class.forName(className);
      java.lang.reflect.Method method = sourceClass.getMethod("isActionKeyHeld", KeyBindings.ClientAction.class);
      heldKeyMethodSource = className;
      return method;
    } catch (ReflectiveOperationException ignored) {
      return null;
    }
  }

  private static boolean shouldLogAtInterval(long lastLogNanos, long nowNanos, long intervalNanos) {
    return nowNanos - lastLogNanos >= intervalNanos;
  }

  private static void logRateLimitedSkip(String category, long lastLogNanos, long nowNanos, String format,
      Object... args) {
    if (!shouldLogAtInterval(lastLogNanos, nowNanos, DIAGNOSTIC_SKIP_LOG_INTERVAL_NANOS)) {
      return;
    }
    LogUtils.logDebug("Preview {}: " + format, prependArg(category, args));
  }

  private static void logSlowOperation(String operation, long startNanos, double thresholdMs, String format,
      Object... args) {
    long elapsedNanos = System.nanoTime() - startNanos;
    double elapsedMs = elapsedNanos / 1_000_000.0d;
    if (elapsedMs < thresholdMs) {
      return;
    }
    Object[] messageArgs = prependArg(operation,
        prependArg(String.format(java.util.Locale.ROOT, "%.3f", elapsedMs), args));
    LogUtils.logDebug("Preview slow operation={} elapsedMs={} " + format, messageArgs);
  }

  private static Object[] prependArg(Object value, Object... args) {
    Object[] out = new Object[args.length + 1];
    out[0] = value;
    System.arraycopy(args, 0, out, 1, args.length);
    return out;
  }

  /**
   * Build one voxel box per block position for incremental shapeless merging.
   */
  private static VoxelShape[] createVoxelBoxShapes(Set<BlockPos> positions, BlockPos origin) {
    VoxelShape[] shapes = new VoxelShape[positions.size()];
    int originX = origin.getX();
    int originY = origin.getY();
    int originZ = origin.getZ();
    int index = 0;

    for (BlockPos position : positions) {
      int relativeX = position.getX() - originX;
      int relativeY = position.getY() - originY;
      int relativeZ = position.getZ() - originZ;
      AABB inflatedBox = new AABB(
          relativeX,
          relativeY,
          relativeZ,
          relativeX + 1,
          relativeY + 1,
          relativeZ + 1).inflate(OUTLINE_INFLATE);
      shapes[index++] = Shapes.create(inflatedBox);
    }

    return shapes;
  }

  /**
   * Create a cached outline for shapeless mode using partial first-frame merge and queued incremental work.
   */
  private static CachedOutline createIncrementalShapelessOutline(Set<BlockPos> positions, BlockPos origin) {
    VoxelShape[] blockShapes = createVoxelBoxShapes(positions, origin);
    VoxelShape combined = Shapes.empty();
    int warmupCount = Math.min(SHAPELESS_BUILD_STEPS_PER_FRAME, blockShapes.length);

    for (int i = 0; i < warmupCount; i++) {
      combined = Shapes.join(combined, blockShapes[i], BooleanOp.OR);
    }

    ShapelessOutlineBuild pending = warmupCount < blockShapes.length
        ? new ShapelessOutlineBuild(blockShapes, warmupCount)
        : null;
    return new CachedOutline(combined, pending);
  }

  /**
   * Advance queued shapeless merge work using a bounded per-frame budget.
   */
  private static void advanceShapelessBuild(CachedOutline outline, int budget) {
    ShapelessOutlineBuild pending = outline.pendingShapelessBuild;
    // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
    if (pending == null || budget <= 0) {
      return;
    }

    int endExclusive = Math.min(pending.blockShapes.length, pending.nextIndex + budget);
    VoxelShape combined = outline.combinedShape;

    for (int i = pending.nextIndex; i < endExclusive; i++) {
      combined = Shapes.join(combined, pending.blockShapes[i], BooleanOp.OR);
    }

    outline.combinedShape = combined;
    pending.nextIndex = endExclusive;

    if (pending.nextIndex >= pending.blockShapes.length) {
      outline.pendingShapelessBuild = null;
    }
  }

}