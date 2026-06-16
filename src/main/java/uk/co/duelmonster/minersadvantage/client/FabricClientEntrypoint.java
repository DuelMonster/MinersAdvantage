//? if fabric {
package uk.co.duelmonster.minersadvantage.client;

import com.mojang.blaze3d.vertex.PoseStack;
import java.lang.reflect.Proxy;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import uk.co.duelmonster.minersadvantage.common.log.LogUtils;

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
  private static long lastOutlineCallbackLogNanos;
  private static long lastOutlineCallbackSkipLogNanos;

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

  /**
   * r eg is te ro ut li ne re nd er ho ok exists so this path stays predictable and easier to debug when things get weird.
   */
  private static void registerOutlineRenderHook() {
    // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
    try {
      Class<?> worldRenderEventsClass = findWorldRenderEventsClass();
      Object beforeBlockOutlineEvent = worldRenderEventsClass.getField("BEFORE_BLOCK_OUTLINE").get(null);
      Class<?> listenerInterface = Class.forName(worldRenderEventsClass.getName() + "$BeforeBlockOutline");
      Object listener = Proxy.newProxyInstance(
          FabricClientEntrypoint.class.getClassLoader(),
          new Class<?>[] { listenerInterface },
          (proxy, method, args) -> {
            // Context moved from WorldRenderContext to LevelRenderContext in newer Fabric API.
            Object context = args[0];
            PoseStack matrices = extractPoseStack(context);
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (matrices == null) {
              long now = System.nanoTime();
              if (now - lastOutlineCallbackSkipLogNanos >= 2_000_000_000L) {
                lastOutlineCallbackSkipLogNanos = now;
                LogUtils.logDebug("Fabric outline callback skipped reason=null-pose-stack contextType={}",
                    context == null ? "null" : context.getClass().getName());
              }
              return true;
            }

            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null || minecraft.level == null || minecraft.gameRenderer == null) {
              long now = System.nanoTime();
              if (now - lastOutlineCallbackSkipLogNanos >= 2_000_000_000L) {
                lastOutlineCallbackSkipLogNanos = now;
                LogUtils.logDebug(
                    "Fabric outline callback skipped reason=missing-client-state playerPresent={} levelPresent={} gameRendererPresent={}",
                    minecraft.player != null,
                    minecraft.level != null,
                    minecraft.gameRenderer != null);
              }
              return true;
            }
            long now = System.nanoTime();
            if (now - lastOutlineCallbackLogNanos >= 1_000_000_000L) {
              lastOutlineCallbackLogNanos = now;
              Vec3 cameraPos = ClientRuntimeCompat.getCameraPosition(minecraft);
              LogUtils.logDebug("Fabric outline callback active camera={} hitResultType={}",
                  cameraPos,
                  minecraft.hitResult == null ? "null" : minecraft.hitResult.getClass().getSimpleName());
            }
            Vec3 cameraPos = ClientRuntimeCompat.getCameraPosition(minecraft);
            ShapePreviewRenderer.renderHeldPreview(
                ClientInputHandler.getInputState(),
                matrices,
                cameraPos.x,
                cameraPos.y,
                cameraPos.z);
            return true;
          });

      @SuppressWarnings("unchecked")
      Event<Object> event = (Event<Object>) beforeBlockOutlineEvent;
      event.register(listener);
      LogUtils.logDebug("Fabric outline render hook registered eventClass={} listenerInterface={}",
          worldRenderEventsClass.getName(),
          listenerInterface.getName());
    } catch (ReflectiveOperationException exception) {
      System.err.println(
          "[MinersAdvantage] Failed to register Fabric outline render hook; continuing without preview outline hook.");
      LogUtils.logError("Fabric outline render hook registration failed", exception);
      exception.printStackTrace();
    }
  }

  /**
   * Find the world-render events class across API package variants without forcing a hard dependency.
   */
  private static Class<?> findWorldRenderEventsClass() throws ClassNotFoundException {
    // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
    try {
      return Class.forName("net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents");
    } catch (ClassNotFoundException ignored) {
    }

    try {
      return Class.forName("net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents");
    } catch (ClassNotFoundException ignored) {
      return Class.forName("net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents");
    }
  }

  /**
   * Extract pose stack from either legacy WorldRenderContext or new LevelRenderContext.
   */
  private static PoseStack extractPoseStack(Object context) throws ReflectiveOperationException {
    for (java.lang.reflect.Method method : context.getClass().getMethods()) {
      if (method.getParameterCount() != 0 || !PoseStack.class.isAssignableFrom(method.getReturnType())) {
        continue;
      }

      Object value = method.invoke(context);
      if (value instanceof PoseStack poseStack) {
        return poseStack;
      }
    }

    throw new NoSuchMethodException("No zero-arg PoseStack accessor on " + context.getClass().getName());
  }

}
//?} else {
/*
// This class is Fabric-only (client-side initialization).
*/ //?}
