//? if fabric {
package uk.co.duelmonster.minersadvantage.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
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

                    Object linesRenderType = resolveLinesRenderType();
                    Method getBufferMethod = Arrays.stream(consumers.getClass().getMethods())
                        .filter(candidate -> candidate.getName().equals("getBuffer") && candidate.getParameterCount() == 1)
                        .findFirst()
                        .orElseThrow(() -> new NoSuchMethodException("Could not find getBuffer(renderType) on " + consumers.getClass().getName()));
                    VertexConsumer vertexConsumer = (VertexConsumer) getBufferMethod.invoke(consumers, linesRenderType);

                    Minecraft minecraft = Minecraft.getInstance();
                    Vec3 cameraPos = minecraft.gameRenderer.getMainCamera().position();
                    ShapePreviewRenderer.renderHeldPreview(
                        ClientInputHandler.getInputState(),
                        matrices,
                        vertexConsumer,
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

    private static Object resolveLinesRenderType() throws ReflectiveOperationException {
        try {
            Class<?> renderTypeClass = Class.forName("net.minecraft.client.renderer.RenderType");
            return renderTypeClass.getMethod("lines").invoke(null);
        } catch (ClassNotFoundException ignored) {
            try {
                Class<?> renderTypesClass = Class.forName("net.minecraft.client.renderer.RenderTypes");
                return renderTypesClass.getMethod("lines").invoke(null);
            } catch (ClassNotFoundException ignoredAgain) {
                Class<?> renderTypesClass = Class.forName("net.minecraft.client.renderer.rendertype.RenderTypes");
                return renderTypesClass.getMethod("lines").invoke(null);
            }
        }
    }
}
//?} else {
/*
// This class is Fabric-only (client-side initialization).
*/ //?}
