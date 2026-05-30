package uk.co.duelmonster.minersadvantage.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import uk.co.duelmonster.minersadvantage.client.ClientPreviewStateBridge;
import uk.co.duelmonster.minersadvantage.client.ShapePreviewRenderer;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Inject(method = "renderHitOutline", at = @At("TAIL"))
    private static void minersadvantage$renderShapeOutline(
        PoseStack poseStack,
        VertexConsumer vertexConsumer,
        Entity entity,
        double cameraX,
        double cameraY,
        double cameraZ,
        BlockPos blockPos,
        BlockState blockState,
        CallbackInfo ci
    ) {
        ShapePreviewRenderer.renderHeldPreview(
            ClientPreviewStateBridge.currentState(),
            poseStack,
            vertexConsumer,
            cameraX,
            cameraY,
            cameraZ
        );
    }
}
