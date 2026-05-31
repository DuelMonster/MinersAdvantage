package uk.co.duelmonster.minersadvantage.client;

import java.util.Objects;
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
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShapeRenderer;
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
import uk.co.duelmonster.minersadvantage.common.services.input.ClientInputService.ClientInputState;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeBootstrap;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeContext;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeDimensions;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeRegistry;

/**
 * ShapePreviewRenderer renders lightweight held-key shape previews on the client.
 *
 * Uses a two-pass rendering approach matching LiteMiner:
 *   Pass 1: translucent lines with NO_DEPTH_TEST so occluded bounds show through blocks.
 *   Pass 2: opaque lines with normal depth testing for crisp foreground edges.
 */
public final class ShapePreviewRenderer {
    private static final int MAX_PREVIEW_BLOCKS = 256;
    private static final int SHAPELESS_PREVIEW_MAX_BLOCKS = 96;
    private static final int SHAPELESS_BUILD_STEPS_PER_FRAME = 24;
    private static final double OUTLINE_INFLATE = 0.005d;
    private static final int OUTLINE_OPTIMIZE_MAX_BLOCKS = 96;
    private static final String SHAPE_ID_SHAPELESS = "minersadvantage:shapeless";

    private static final RenderType LINES_NORMAL = RenderTypes.lines();
    private static final RenderType LINES_TRANSLUCENT_NO_DEPTH_TEST = createLinesTranslucentNoDepthTestRenderType();

    private static final OutlineCache CACHE = new OutlineCache();

    private record OutlineKey(
        FeatureId feature,
        int shapeIndex,
        int width,
        int height,
        int depth,
        Direction hitFace,
        Direction playerFacing
    ) {
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
        BlockPos origin;
        private final Map<OutlineKey, CachedOutline> shapesByKey = new HashMap<>();

        /**
         * Reset cached outlines when the targeted origin block changes.
         */
        void prepareForOrigin(BlockPos currentOrigin) {
            BlockPos immutableOrigin = currentOrigin.immutable();
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (Objects.equals(this.origin, immutableOrigin)) {
                return;
            }
            this.origin = immutableOrigin;
            this.shapesByKey.clear();
        }

        /**
         * Retrieve cached shape for a fully-qualified preview key.
         */
        CachedOutline get(
            FeatureId feature,
            int shapeIndex,
            int width,
            int height,
            int depth,
            Direction hitFace,
            Direction playerFacing
        ) {
            return shapesByKey.get(new OutlineKey(feature, shapeIndex, width, height, depth, hitFace, playerFacing));
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
            Direction hitFace,
            Direction playerFacing,
            CachedOutline outline
        ) {
            shapesByKey.put(new OutlineKey(feature, shapeIndex, width, height, depth, hitFace, playerFacing), outline);
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
        RenderPipeline.Snippet snippet = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET, RenderPipelines.GLOBALS_SNIPPET)
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
        RenderPipeline.Snippet snippet = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET, RenderPipelines.GLOBALS_SNIPPET)
            .withVertexShader("core/rendertype_lines")
            .withFragmentShader("core/rendertype_lines")
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withCull(false)
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH, VertexFormat.Mode.LINES)
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

    /**
     * Render active held-key preview outlines for excavation/shaft/ventilation contexts.
     */
    public static void renderHeldPreview(ClientInputState state, PoseStack poseStack, double cameraX, double cameraY, double cameraZ) {
        Minecraft minecraft = Minecraft.getInstance();
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (!(minecraft.hitResult instanceof BlockHitResult blockHit)) {
            return;
        }

        boolean excavationPreview =
            state.excavationToggled()
                && state.featureEnabled().getOrDefault(FeatureId.EXCAVATION, false);
        boolean shaftVentPreview =
            state.shaftVentToggled()
                && state.featureEnabled().getOrDefault(FeatureId.SHAFTANATION, false);
        boolean shaftPreview = shaftVentPreview && blockHit.getDirection().getAxis().isHorizontal();
        boolean ventilationPreview = shaftVentPreview && blockHit.getDirection().getAxis().isVertical();

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (!excavationPreview && !shaftPreview && !ventilationPreview) {
            return;
        }

        MAShapeBootstrap.ensureInitialized();
        Player player = minecraft.player;
        BlockPos origin = blockHit.getBlockPos();
        Direction hitFace = blockHit.getDirection();
        Direction playerFacing = player.getDirection();
        CACHE.prepareForOrigin(origin);

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (excavationPreview) {
            var excavation = MAServerRootConfig.defaults().excavation();
            MAShapeDimensions.Dimensions dimensions = MAShapeDimensions.excavationFromConfig(
                excavation.width(),
                excavation.height(),
                excavation.depth()
            );
            MAShapeRegistry.byIndex(FeatureId.EXCAVATION, state.selectedExcavationShapeIndex())
                .ifPresent(shape -> {
                    int previewBlockLimit = SHAPE_ID_SHAPELESS.equals(shape.id()) ? SHAPELESS_PREVIEW_MAX_BLOCKS : MAX_PREVIEW_BLOCKS;
                    MAShapeContext context = new MAShapeContext(
                        minecraft.level,
                        player,
                        origin,
                        minecraft.level.getBlockState(origin),
                        hitFace,
                        playerFacing,
                        dimensions.width(),
                        dimensions.height(),
                        dimensions.depth(),
                        previewBlockLimit
                    );
                    renderOutline(
                        FeatureId.EXCAVATION,
                        shape.id(),
                        origin,
                        state.selectedExcavationShapeIndex(),
                        dimensions,
                        hitFace,
                        playerFacing,
                        () -> shape.compute(context),
                        poseStack,
                        cameraX,
                        cameraY,
                        cameraZ
                    );
                });
        }

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (shaftPreview) {
            var shaft = MAServerRootConfig.defaults().shaftanation();
            MAShapeDimensions.Dimensions dimensions = MAShapeDimensions.shaftFromConfig(shaft.width(), shaft.height(), shaft.depth());
            MAShapeContext context = new MAShapeContext(
                minecraft.level,
                player,
                origin,
                minecraft.level.getBlockState(origin),
                hitFace,
                playerFacing,
                dimensions.width(),
                dimensions.height(),
                dimensions.depth(),
                MAX_PREVIEW_BLOCKS
            );
            MAShapeRegistry.byIndex(FeatureId.SHAFTANATION, state.selectedShaftanationShapeIndex())
                .ifPresent(shape -> renderOutline(
                    FeatureId.SHAFTANATION,
                    shape.id(),
                    origin,
                    state.selectedShaftanationShapeIndex(),
                    dimensions,
                    hitFace,
                    playerFacing,
                    () -> shape.compute(context),
                    poseStack,
                    cameraX,
                    cameraY,
                    cameraZ
                ));
        }

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (ventilationPreview) {
            var ventilation = MAServerRootConfig.defaults().ventilation();
            int ventDepth = Math.max(1, ventilation.height());
            Direction ventDirection = hitFace == Direction.UP ? Direction.DOWN : Direction.UP;
            renderOutline(
                FeatureId.VENTILATION,
                "minersadvantage:ventilation",
                origin,
                0,
                new MAShapeDimensions.Dimensions(1, ventDepth, 1),
                hitFace,
                playerFacing,
                () -> {
                    Set<BlockPos> positions = new LinkedHashSet<>();
                    // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                    for (int depth = 0; depth < ventDepth && positions.size() < MAX_PREVIEW_BLOCKS; depth++) {
                        positions.add(origin.relative(ventDirection, depth).immutable());
                    }
                    return positions;
                },
                poseStack,
                cameraX,
                cameraY,
                cameraZ
            );
        }
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
        double cameraZ
    ) {
        CachedOutline outline = CACHE.get(
            feature,
            shapeIndex,
            dimensions.width(),
            dimensions.height(),
            dimensions.depth(),
            hitFace,
            playerFacing
        );

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (outline == null) {
            Set<BlockPos> positions = positionsSupplier.get();
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (positions.isEmpty()) {
                return;
            }
            if (SHAPE_ID_SHAPELESS.equals(shapeId)) {
                outline = createIncrementalShapelessOutline(positions, origin);
            } else {
                boolean shouldOptimize = positions.size() <= OUTLINE_OPTIMIZE_MAX_BLOCKS;
                VoxelShape combinedShape = combineToVoxelShape(positions, origin, shouldOptimize);
                outline = new CachedOutline(combinedShape, null);
            }
            CACHE.put(
                feature,
                shapeIndex,
                dimensions.width(),
                dimensions.height(),
                dimensions.depth(),
                hitFace,
                playerFacing,
                outline
            );
        }

        // Continue incremental shapeless merge on the render thread with a small per-frame budget.
        advanceShapelessBuild(outline, SHAPELESS_BUILD_STEPS_PER_FRAME);

        VoxelShape combinedShape = outline.combinedShape;
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (combinedShape == null || combinedShape.isEmpty()) {
            return;
        }

        // Use the game's own render buffer source directly, just like LiteMiner does.
        // The event-provided consumers cannot support custom RenderTypes with custom pipelines.
        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        float lineWidth = Minecraft.getInstance().getWindow().getAppropriateLineWidth();
        ClientConfig clientConfig = MAConfig_Base.getClientRootConfig().client();
        int outlineForegroundColor = clientConfig.outlineForegroundColor();
        int outlineSeeThroughColor = clientConfig.outlineSeeThroughColor();

        poseStack.pushPose();
        poseStack.translate(origin.getX() - cameraX, origin.getY() - cameraY, origin.getZ() - cameraZ);

        // Pass 1: translucent, NO_DEPTH_TEST -- occluded bounds visible through blocks
        VertexConsumer translucentBuilder = buffers.getBuffer(LINES_TRANSLUCENT_NO_DEPTH_TEST);
        ShapeRenderer.renderShape(poseStack, translucentBuilder, combinedShape, 0.0d, 0.0d, 0.0d, outlineSeeThroughColor, lineWidth);

        // Pass 2: opaque, normal depth test -- foreground edges
        VertexConsumer opaqueBuilder = buffers.getBuffer(LINES_NORMAL);
        ShapeRenderer.renderShape(poseStack, opaqueBuilder, combinedShape, 0.0d, 0.0d, 0.0d, outlineForegroundColor, lineWidth);

        buffers.endBatch(LINES_TRANSLUCENT_NO_DEPTH_TEST);
        buffers.endBatch(LINES_NORMAL);

        poseStack.popPose();
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
                relativeZ + 1
            ).inflate(OUTLINE_INFLATE);

            combinedShape = Shapes.join(combinedShape, Shapes.create(inflatedBox), BooleanOp.OR);
        }

        // Large irregular previews (especially shapeless) spend disproportionate time in optimize() for little visual gain.
        if (!optimize) {
            return combinedShape;
        }

        return combinedShape.optimize();
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
                relativeZ + 1
            ).inflate(OUTLINE_INFLATE);
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