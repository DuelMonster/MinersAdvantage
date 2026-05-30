package uk.co.duelmonster.minersadvantage.client;

import java.util.Set;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
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
    private static final int MAX_PREVIEW_OUTLINES = 96;
    private static final int OUTLINE_COLOR = 0xF240D9C0;
    private static final float OUTLINE_WIDTH = 1.0f;

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
                MAX_PREVIEW_OUTLINES
            );
            MAShapeRegistry.byIndex(FeatureId.EXCAVATION, state.selectedExcavationShapeIndex())
                .ifPresent(shape -> renderOutline(shape.compute(context), poseStack, vertexConsumer, cameraX, cameraY, cameraZ));
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
                MAX_PREVIEW_OUTLINES
            );
            MAShapeRegistry.byIndex(FeatureId.SHAFTANATION, state.selectedShaftanationShapeIndex())
                .ifPresent(shape -> renderOutline(shape.compute(context), poseStack, vertexConsumer, cameraX, cameraY, cameraZ));
        }
    }

    private static void renderOutline(
        Set<BlockPos> positions,
        PoseStack poseStack,
        VertexConsumer vertexConsumer,
        double cameraX,
        double cameraY,
        double cameraZ
    ) {
        if (positions.isEmpty()) {
            return;
        }

        int stride = Math.max(1, (int) Math.ceil((double) positions.size() / (double) MAX_PREVIEW_OUTLINES));
        int index = 0;
        for (BlockPos pos : positions) {
            if (index % stride == 0) {
                ShapeRenderer.renderShape(
                    poseStack,
                    vertexConsumer,
                    Shapes.box(
                        pos.getX() - cameraX,
                        pos.getY() - cameraY,
                        pos.getZ() - cameraZ,
                        pos.getX() + 1.0d - cameraX,
                        pos.getY() + 1.0d - cameraY,
                        pos.getZ() + 1.0d - cameraZ
                    ),
                    0.0d,
                    0.0d,
                    0.0d,
                    OUTLINE_COLOR,
                    OUTLINE_WIDTH
                );
            }
            index++;
        }
    }
}
