//? if fabric {
package uk.co.duelmonster.minersadvantage.client;

import com.mojang.blaze3d.vertex.PoseStack;
import java.lang.reflect.Proxy;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

/**
 * Fabric client-side initialization entry point.
 *
 * This class is invoked by Fabric on the client side only (as declared in fabric.mod.json).
 * It provides a hook for client-only systems:
 * - Keybinding registration (M3)
 * - Client tick listener setup (M3)
 * - Config screen factory registration (M6)
 *
 * Server-side initialization happens in ModEntry (the main Fabric ModInitializer).
 */
public final class FabricClientEntrypoint implements ClientModInitializer {

    @Override
    /**
     * onInitializeClient exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public void onInitializeClient() {
        // Why this exists: M3/M4: Register keybindings, payload types, and wire client tick input loop (future-you will thank present-you).
        ClientInputHandler.registerKeybindings();
        FabricNetworkEvents.registerPayloadTypes();
        ClientTickEvents.END_CLIENT_TICK.register(minecraft -> ClientInputHandler.tick());
        registerOutlineRenderHook();
    }

    private static void registerOutlineRenderHook() {
        try {
            Class<?> worldRenderEventsClass = findWorldRenderEventsClass();
            Object beforeBlockOutlineEvent = worldRenderEventsClass.getField("BEFORE_BLOCK_OUTLINE").get(null);
            Class<?> listenerInterface = Class.forName(worldRenderEventsClass.getName() + "$BeforeBlockOutline");
            Object listener = Proxy.newProxyInstance(
                FabricClientEntrypoint.class.getClassLoader(),
                new Class<?>[]{listenerInterface},
                (proxy, method, args) -> {
                    Object context = args[0];
                    Object consumers = context.getClass().getMethod("consumers").invoke(context);
                    PoseStack matrices = (PoseStack) context.getClass().getMethod("matrices").invoke(context);
                    if (consumers == null || matrices == null) {
                        return true;
                    }

                    Minecraft minecraft = Minecraft.getInstance();
                    Vec3 cameraPos = minecraft.gameRenderer.getMainCamera().position();
                    ShapePreviewRenderer.renderHeldPreview(
                        ClientInputHandler.getInputState(),
                        matrices,
                        cameraPos.x,
                        cameraPos.y,
                        cameraPos.z
                    );
                    return true;
                }
            );

            @SuppressWarnings("unchecked")
            Event<Object> event = (Event<Object>) beforeBlockOutlineEvent;
            event.register(listener);
        } catch (ReflectiveOperationException exception) {
            System.err.println("[MinersAdvantage] Failed to register Fabric outline render hook; continuing without preview outline hook.");
            exception.printStackTrace();
        }
    }

    private static Class<?> findWorldRenderEventsClass() throws ClassNotFoundException {
        try {
            return Class.forName("net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents");
        } catch (ClassNotFoundException ignored) {
            return Class.forName("net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents");
        }
    }

}
//?} else {
/*
// This class is Fabric-only (client-side initialization).
*/ //?}
