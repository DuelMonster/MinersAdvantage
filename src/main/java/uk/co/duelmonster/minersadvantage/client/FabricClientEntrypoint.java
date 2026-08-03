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
  private static boolean clientFeaturesInitialized;
  private static boolean outlineHookRegistered;

  @Override
  /**
   * onInitializeClient exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public void onInitializeClient() {
    // Key mappings must be registered before GameOptions initialization.
    ClientInputHandler.registerKeybindings();

    // Keep payload type registration in startup path so networking remains valid.
    FabricNetworkEvents.registerPayloadTypes();

    // Defer feature hooks until an actual world exists.
    ClientTickEvents.END_CLIENT_TICK.register(minecraft -> {
      if (!clientFeaturesInitialized) {
        if (minecraft.player == null || minecraft.level == null) {
          return;
        }
        initializeClientFeatures();
      }

      ClientInputHandler.tick();

      if (clientFeaturesInitialized && !outlineHookRegistered) {
        registerOutlineRenderHook();
      }
    });
  }

  private static void initializeClientFeatures() {
    if (clientFeaturesInitialized) {
      return;
    }

    clientFeaturesInitialized = true;
    LogUtils.logInfo("Deferred client feature initialization completed trigger=world-loaded");
  }

  /**
   * r eg is te ro ut li ne re nd er ho ok exists so this path stays predictable and easier to debug when things get weird.
   */
  private static void registerOutlineRenderHook() {
    if (outlineHookRegistered) {
      return;
    }

    try {
      Class<?> worldRenderEventsClass = findWorldRenderEventsClass();
      Object beforeBlockOutlineEvent = worldRenderEventsClass.getField("BEFORE_BLOCK_OUTLINE").get(null);
      Class<?> listenerInterface = Class.forName(worldRenderEventsClass.getName() + "$BeforeBlockOutline");
      Object listener = Proxy.newProxyInstance(
          FabricClientEntrypoint.class.getClassLoader(),
          new Class<?>[] { listenerInterface },
          (proxy, method, args) -> {
            if (method.getDeclaringClass() == Object.class) {
              return switch (method.getName()) {
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == (args != null && args.length > 0 ? args[0] : null);
                case "toString" -> "MinersAdvantageBeforeBlockOutlineListener";
                default -> null;
              };
            }

            if (args == null || args.length == 0) {
              return true;
            }

            if (!"beforeBlockOutline".equals(method.getName())) {
              return true;
            }

            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null || minecraft.level == null || minecraft.gameRenderer == null) {
              return true;
            }

            // Context moved from WorldRenderContext to LevelRenderContext in newer Fabric API.
            Object context = args[0];
            PoseStack matrices;
            try {
              matrices = extractPoseStack(context);
            } catch (ReflectiveOperationException exception) {
              long now = System.nanoTime();
              if (now - lastOutlineCallbackSkipLogNanos >= 2_000_000_000L) {
                lastOutlineCallbackSkipLogNanos = now;
                LogUtils.logDebug(
                    "Fabric outline callback skipped reason=pose-stack-reflection-failed contextType={} exceptionType={}",
                    context == null ? "null" : context.getClass().getName(),
                    exception.getClass().getSimpleName());
              }
              return true;
            }
            if (matrices == null) {
              long now = System.nanoTime();
              if (now - lastOutlineCallbackSkipLogNanos >= 2_000_000_000L) {
                lastOutlineCallbackSkipLogNanos = now;
                LogUtils.logDebug("Fabric outline callback skipped reason=null-pose-stack contextType={}",
                    context == null ? "null" : context.getClass().getName());
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
                context,
                matrices,
                cameraPos.x,
                cameraPos.y,
                cameraPos.z);
            return true;
          });

      @SuppressWarnings("unchecked")
      Event<Object> event = (Event<Object>) beforeBlockOutlineEvent;
      event.register(listener);
      outlineHookRegistered = true;
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
    if (context == null) {
      return null;
    }

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
