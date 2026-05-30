package uk.co.duelmonster.minersadvantage.client;

import java.util.Objects;
import java.util.Set;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import uk.co.duelmonster.minersadvantage.common.config.MAServerRootConfig;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;
import uk.co.duelmonster.minersadvantage.common.services.input.ClientInputService.ClientInputState;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeBootstrap;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeContext;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeDimensions;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeRegistry;

/**
 * ShapePreviewRenderer renders lightweight held-key shape previews on the client.
 */
public final class ShapePreviewRenderer {
    private static final int MAX_PREVIEW_BLOCKS = 256;
    private static final int OUTLINE_FOREGROUND_COLOR = 0xFF40D9C0;
    private static final int OUTLINE_SEE_THROUGH_COLOR = 0x4B40D9C0;
    private static final double OUTLINE_INFLATE = 0.005d;

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

    public static void renderHeldPreview(ClientInputState state, PoseStack poseStack, VertexConsumer vertexConsumer, double cameraX, double cameraY, double cameraZ) {
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
        boolean shaftPreview =
            state.shaftVentToggled()
                && state.featureEnabled().getOrDefault(FeatureId.SHAFTANATION, false);

        if (!excavationPreview && !shaftPreview) {
            return;
        }

        MAShapeBootstrap.ensureInitialized();
        Player player = minecraft.player;
        BlockPos origin = blockHit.getBlockPos();
        Direction hitFace = blockHit.getDirection();

        if (excavationPreview) {
            var excavation = MAServerRootConfig.defaults().excavation();
            MAShapeDimensions.Dimensions dimensions = MAShapeDimensions.excavationFromRadii(excavation.radiusHorizontal(), excavation.radiusVertical());
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
                    vertexConsumer,
                    cameraX,
                    cameraY,
                    cameraZ
                ));
        }

        if (shaftPreview) {
            var shaft = MAServerRootConfig.defaults().shaftanation();
            MAShapeDimensions.Dimensions dimensions = MAShapeDimensions.shaftFromConfig(shaft.shaftWidth(), shaft.shaftHeight(), shaft.maxDepth());
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
                    vertexConsumer,
                    cameraX,
                    cameraY,
                    cameraZ
                ));
        }
    }

    private static void renderOutline(
        FeatureId feature,
        Set<BlockPos> positions,
        BlockPos origin,
        int shapeIndex,
        MAShapeDimensions.Dimensions dimensions,
        PoseStack poseStack,
        VertexConsumer vertexConsumer,
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

        float lineWidth = Minecraft.getInstance().getWindow().getAppropriateLineWidth();
        poseStack.pushPose();
        poseStack.translate(origin.getX() - cameraX, origin.getY() - cameraY, origin.getZ() - cameraZ);

        ShapeRenderer.renderShape(poseStack, vertexConsumer, combinedShape, 0.0d, 0.0d, 0.0d, OUTLINE_SEE_THROUGH_COLOR, lineWidth);
        ShapeRenderer.renderShape(poseStack, vertexConsumer, combinedShape, 0.0d, 0.0d, 0.0d, OUTLINE_FOREGROUND_COLOR, lineWidth);

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
