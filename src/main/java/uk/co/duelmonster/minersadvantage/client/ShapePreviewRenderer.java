package uk.co.duelmonster.minersadvantage.client;

import java.util.Objects;
import java.util.LinkedHashSet;
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
    private static final double OUTLINE_INFLATE = 0.005d;

    private static final RenderType LINES_NORMAL = RenderTypes.lines();
    private static final RenderType LINES_TRANSLUCENT_NO_DEPTH_TEST = createLinesTranslucentNoDepthTestRenderType();

    private static final OutlineCache CACHE = new OutlineCache();

    private static final class OutlineCache {
        FeatureId feature;
        BlockPos origin;
        int shapeIndex;
        int width;
        int height;
        int depth;
        VoxelShape combinedShape;
        long updatedAt;

        boolean isValid(FeatureId feature, BlockPos origin, int shapeIndex, int width, int height, int depth) {
            return this.feature == feature
                && Objects.equals(this.origin, origin)
                && this.shapeIndex == shapeIndex
                && this.width == width
                && this.height == height
                && this.depth == depth
                && this.combinedShape != null
                && (System.currentTimeMillis() - this.updatedAt) < 250L;
        }

        void store(FeatureId feature, BlockPos origin, int shapeIndex, int width, int height, int depth, VoxelShape combinedShape) {
            this.feature = feature;
            this.origin = origin;
            this.shapeIndex = shapeIndex;
            this.width = width;
            this.height = height;
            this.depth = depth;
            this.combinedShape = combinedShape;
            this.updatedAt = System.currentTimeMillis();
        }
    }

    private ShapePreviewRenderer() {
    }

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

    public static void renderHeldPreview(ClientInputState state, PoseStack poseStack, double cameraX, double cameraY, double cameraZ) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }
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

        if (!excavationPreview && !shaftPreview && !ventilationPreview) {
            return;
        }

        MAShapeBootstrap.ensureInitialized();
        Player player = minecraft.player;
        BlockPos origin = blockHit.getBlockPos();
        Direction hitFace = blockHit.getDirection();

        if (excavationPreview) {
            var excavation = MAServerRootConfig.defaults().excavation();
            MAShapeDimensions.Dimensions dimensions = MAShapeDimensions.excavationFromConfig(
                excavation.width(),
                excavation.height(),
                excavation.depth()
            );
            MAShapeContext context = new MAShapeContext(
                minecraft.level,
                player,
                origin,
                hitFace,
                player.getDirection(),
                dimensions.width(),
                dimensions.height(),
                dimensions.depth(),
                MAX_PREVIEW_BLOCKS
            );
            MAShapeRegistry.byIndex(FeatureId.EXCAVATION, state.selectedExcavationShapeIndex())
                .ifPresent(shape -> renderOutline(
                    FeatureId.EXCAVATION,
                    shape.compute(context),
                    origin,
                    state.selectedExcavationShapeIndex(),
                    dimensions,
                    poseStack,
                    cameraX,
                    cameraY,
                    cameraZ
                ));
        }

        if (shaftPreview) {
            var shaft = MAServerRootConfig.defaults().shaftanation();
            MAShapeDimensions.Dimensions dimensions = MAShapeDimensions.shaftFromConfig(shaft.width(), shaft.height(), shaft.depth());
            MAShapeContext context = new MAShapeContext(
                minecraft.level,
                player,
                origin,
                hitFace,
                player.getDirection(),
                dimensions.width(),
                dimensions.height(),
                dimensions.depth(),
                MAX_PREVIEW_BLOCKS
            );
            MAShapeRegistry.byIndex(FeatureId.SHAFTANATION, state.selectedShaftanationShapeIndex())
                .ifPresent(shape -> renderOutline(
                    FeatureId.SHAFTANATION,
                    shape.compute(context),
                    origin,
                    state.selectedShaftanationShapeIndex(),
                    dimensions,
                    poseStack,
                    cameraX,
                    cameraY,
                    cameraZ
                ));
        }

        if (ventilationPreview) {
            var ventilation = MAServerRootConfig.defaults().ventilation();
            int ventDepth = Math.max(1, ventilation.height());
            Direction ventDirection = hitFace == Direction.UP ? Direction.DOWN : Direction.UP;
            Set<BlockPos> positions = new LinkedHashSet<>();
            for (int depth = 0; depth < ventDepth && positions.size() < MAX_PREVIEW_BLOCKS; depth++) {
                positions.add(origin.relative(ventDirection, depth).immutable());
            }

            renderOutline(
                FeatureId.VENTILATION,
                positions,
                origin,
                0,
                new MAShapeDimensions.Dimensions(1, ventDepth, 1),
                poseStack,
                cameraX,
                cameraY,
                cameraZ
            );
        }
    }

    private static void renderOutline(
        FeatureId feature,
        Set<BlockPos> positions,
        BlockPos origin,
        int shapeIndex,
        MAShapeDimensions.Dimensions dimensions,
        PoseStack poseStack,
        double cameraX,
        double cameraY,
        double cameraZ
    ) {
        if (positions.isEmpty()) {
            return;
        }

        VoxelShape combinedShape;
        if (CACHE.isValid(feature, origin, shapeIndex, dimensions.width(), dimensions.height(), dimensions.depth())) {
            combinedShape = CACHE.combinedShape;
        } else {
            combinedShape = combineToVoxelShape(positions, origin);
            CACHE.store(feature, origin.immutable(), shapeIndex, dimensions.width(), dimensions.height(), dimensions.depth(), combinedShape);
        }

        if (combinedShape.isEmpty()) {
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

    private static VoxelShape combineToVoxelShape(Set<BlockPos> positions, BlockPos origin) {
        VoxelShape combinedShape = Shapes.empty();

        for (BlockPos position : positions) {
            BlockPos relative = position.subtract(origin);
            AABB inflatedBox = new AABB(
                relative.getX(),
                relative.getY(),
                relative.getZ(),
                relative.getX() + 1,
                relative.getY() + 1,
                relative.getZ() + 1
            ).inflate(OUTLINE_INFLATE);

            combinedShape = Shapes.join(combinedShape, Shapes.create(inflatedBox), BooleanOp.OR);
        }

        return combinedShape.optimize();
    }
}