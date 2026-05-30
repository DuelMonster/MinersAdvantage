package uk.co.duelmonster.minersadvantage.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import uk.co.duelmonster.minersadvantage.client.ClientMouseScrollBridge;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void minersadvantage$handleShapeScroll(long windowPointer, double horizontalScroll, double verticalScroll, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen != null || minecraft.player == null || minecraft.level == null) {
            return;
        }

        if (ClientMouseScrollBridge.onScroll(verticalScroll)) {
            ci.cancel();
        }
    }
}
