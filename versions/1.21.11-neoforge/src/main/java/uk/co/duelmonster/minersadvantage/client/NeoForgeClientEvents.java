package uk.co.duelmonster.minersadvantage.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.HashSet;
import java.lang.reflect.Method;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import uk.co.duelmonster.minersadvantage.common.network.AbortWorkersPacket;
import uk.co.duelmonster.minersadvantage.common.network.ComponentTogglePacket;
import uk.co.duelmonster.minersadvantage.common.network.SupremeVantagePacket;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeBootstrap;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeRegistry;
import uk.co.duelmonster.minersadvantage.common.services.input.ClientInputService;
import uk.co.duelmonster.minersadvantage.common.services.utility.SupremeVantageService;

/**
 * NeoForge client-side event handlers.
 *
 * Registers key mappings and polls them on the client tick.
 */
@EventBusSubscriber(modid = "minersadvantage", value = Dist.CLIENT)
/**
 * NeoForgeClientEvents keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class NeoForgeClientEvents {
  private static final Map<KeyBindings.ClientAction, KeyMapping> KEY_MAPPINGS = new EnumMap<>(
      KeyBindings.ClientAction.class);
  private static final ClientInputService INPUT_SERVICE = new ClientInputService();
  private static final KeyMapping.Category KEY_CATEGORY = ClientActionInputSupport.createKeyCategory();
  private static ClientInputService.ClientInputState inputState = ClientInputService.ClientInputState.defaults();
  private static ClientInputService.ClientInputState lastSyncedState = ClientInputService.ClientInputState.defaults();
  private static final SupremeVantageService supremeVantageService = new SupremeVantageService();
  private static SupremeVantageService.ClientState supremeVantageState = SupremeVantageService.ClientState.defaults();

  /**
   * NeoForgeClientEvents exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  private NeoForgeClientEvents() {
  }

  @SubscribeEvent
  /**
   * onRegisterKeyMappings exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
    // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
    for (KeyBindings.KeyBindingSpec spec : KeyBindings.all()) {
      if (spec.defaultKey() == null) {
        continue;
      }
      String translationKey = "key.minersadvantage." + spec.action().name().toLowerCase();
      InputConstants.Key key = parseKeyToken(spec.defaultKey());
      KeyMapping keyMapping = ClientActionInputSupport.createKeyMapping(translationKey, key, KEY_CATEGORY);
      event.register(keyMapping);
      KEY_MAPPINGS.put(spec.action(), keyMapping);
    }
  }

  @SubscribeEvent
  /**
   * onClientTick exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public static void onClientTick(ClientTickEvent.Post event) {
    Minecraft client = Minecraft.getInstance();
    if (client.player == null || client.level == null) {
      return;
    }

    Set<KeyBindings.ClientAction> pressedSet = ClientActionInputSupport.collectPressedActions(KEY_MAPPINGS);
    ClientInputService.ClientInputState previousState = inputState;

    ClientInputService.ClientInputResult result = INPUT_SERVICE.process(
        inputState,
        pressedSet,
        false);
    inputState = result.state();
    showShapeHudIfChangedOrActivated(previousState, inputState);

    SupremeVantageService.ClientUpdate supremeUpdate = supremeVantageService.processClientTick(
        supremeVantageState,
        ClientActionInputSupport.collectPressedSupremeDigits(),
        inputState.excavationToggled(),
        areAllFeaturesEnabled(inputState));
    supremeVantageState = supremeUpdate.state();

    if (supremeUpdate.notifyWorthy()) {
      ClientRuntimeCompat.showOverlayMessage(client,
          Component.literal("SupremeVantage code accepted"));
    }

    if (supremeUpdate.shouldSendRewardPacket() && !supremeUpdate.packetCode().isEmpty()) {
      long playerId = ClientActionInputSupport.resolveLocalPlayerId();
      if (playerId != 0L) {
        ClientPacketDistributor.sendToServer(new SupremeVantagePacket(playerId, supremeUpdate.packetCode()));
      }
    }

    // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
    for (ComponentTogglePacket packet : result.togglePackets()) {
      ClientPacketDistributor.sendToServer(packet);
    }

    // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
    if (result.abortRequested()) {
      long playerId = ClientActionInputSupport.resolveLocalPlayerId();
      ClientPacketDistributor.sendToServer(new AbortWorkersPacket(playerId, "client:keybind"));
    }

    boolean activationStateChanged = ClientActionInputSupport.hasActivationStateChanged(lastSyncedState,
        result.state());

    // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
    if (activationStateChanged && (result.shouldSyncConfig() || result.shouldSyncVariables())) {
      syncStateToServer(result.state());
    }
  }

  @SubscribeEvent
  public static void onExtractBlockOutlineRenderState(ExtractBlockOutlineRenderStateEvent event) {
    Minecraft client = Minecraft.getInstance();
    if (client.level == null || client.player == null || ClientRuntimeCompat.getCurrentScreen(client) != null) {
      return;
    }

    double[] cameraPosition = extractCameraCoordinates(event.getCamera());
    event.addCustomRenderer((blockOutlineRenderState, bufferSource, poseStack, translucentPass, levelRenderState) -> {
      ShapePreviewRenderer.renderHeldPreview(
          inputState,
          bufferSource,
          ensurePoseStack(poseStack),
          cameraPosition[0],
          cameraPosition[1],
          cameraPosition[2]);
      return false;
    });
  }

  public static ClientInputService.ClientInputState getInputState() {
    return inputState;
  }

  public static boolean isActionKeyHeld(KeyBindings.ClientAction action) {
    if (action == null) {
      return false;
    }

    KeyMapping mapping = KEY_MAPPINGS.get(action);
    return mapping != null && mapping.isDown();
  }

  /**
   * Human-friendly guardrail: o nm ou se sc ro ll exists so this path stays predictable and easier to debug when things get weird.
   */
  public static boolean onMouseScroll(double scrollY) {
    // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
    if (scrollY == 0.0d) {
      return false;
    }

    boolean excavationActive = isActionKeyHeld(KeyBindings.ClientAction.EXCAVATION_MODE_TOGGLE)
        && inputState.featureEnabled().getOrDefault(FeatureId.EXCAVATION, false);
    boolean shaftActive = isActionKeyHeld(KeyBindings.ClientAction.SHAFT_VENT_TOGGLE)
        && inputState.featureEnabled().getOrDefault(FeatureId.SHAFTANATION, false);

    if (!excavationActive && !shaftActive) {
      return false;
    }

    Set<KeyBindings.ClientAction> actions = new HashSet<>();
    if (excavationActive) {
      actions.add(scrollY > 0.0d ? KeyBindings.ClientAction.EXCAVATION_SHAPE_PREV
          : KeyBindings.ClientAction.EXCAVATION_SHAPE_NEXT);
    }
    if (shaftActive) {
      actions.add(scrollY > 0.0d ? KeyBindings.ClientAction.SHAFTANATION_SHAPE_PREV
          : KeyBindings.ClientAction.SHAFTANATION_SHAPE_NEXT);
    }

    if (actions.isEmpty()) {
      return false;
    }

    ClientInputService.ClientInputState previousState = inputState;

    int nextExcavationShapeIndex = previousState.selectedExcavationShapeIndex();
    int nextShaftanationShapeIndex = previousState.selectedShaftanationShapeIndex();

    if (actions.contains(KeyBindings.ClientAction.EXCAVATION_SHAPE_NEXT)
        || actions.contains(KeyBindings.ClientAction.EXCAVATION_SHAPE_PREV)) {
      int excavationShapeCount = MAShapeRegistry.forFeature(FeatureId.EXCAVATION).size();
      if (excavationShapeCount > 0) {
        if (actions.contains(KeyBindings.ClientAction.EXCAVATION_SHAPE_NEXT)) {
          nextExcavationShapeIndex = Math.floorMod(nextExcavationShapeIndex + 1, excavationShapeCount);
        }
        if (actions.contains(KeyBindings.ClientAction.EXCAVATION_SHAPE_PREV)) {
          nextExcavationShapeIndex = Math.floorMod(nextExcavationShapeIndex - 1, excavationShapeCount);
        }
      }
    }

    if (actions.contains(KeyBindings.ClientAction.SHAFTANATION_SHAPE_NEXT)
        || actions.contains(KeyBindings.ClientAction.SHAFTANATION_SHAPE_PREV)) {
      int shaftShapeCount = MAShapeRegistry.forFeature(FeatureId.SHAFTANATION).size();
      if (shaftShapeCount > 0) {
        if (actions.contains(KeyBindings.ClientAction.SHAFTANATION_SHAPE_NEXT)) {
          nextShaftanationShapeIndex = Math.floorMod(nextShaftanationShapeIndex + 1, shaftShapeCount);
        }
        if (actions.contains(KeyBindings.ClientAction.SHAFTANATION_SHAPE_PREV)) {
          nextShaftanationShapeIndex = Math.floorMod(nextShaftanationShapeIndex - 1, shaftShapeCount);
        }
      }
    }

    inputState = new ClientInputService.ClientInputState(
        previousState.featureEnabled(),
        previousState.excavationToggled(),
        previousState.shaftVentToggled(),
        nextExcavationShapeIndex,
        nextShaftanationShapeIndex);

    if (previousState.selectedExcavationShapeIndex() == inputState.selectedExcavationShapeIndex()
        && previousState.selectedShaftanationShapeIndex() == inputState.selectedShaftanationShapeIndex()) {
      return false;
    }

    showShapeHudIfChangedOrActivated(previousState, inputState);
    syncStateToServer(inputState);
    return true;
  }

  private static void showShapeHudIfChangedOrActivated(ClientInputService.ClientInputState previous,
      ClientInputService.ClientInputState current) {
    boolean excavationChanged = previous.selectedExcavationShapeIndex() != current.selectedExcavationShapeIndex();
    boolean shaftChanged = previous.selectedShaftanationShapeIndex() != current.selectedShaftanationShapeIndex();
    boolean excavationActivated = !previous.excavationToggled() && current.excavationToggled();
    boolean shaftActivated = !previous.shaftVentToggled() && current.shaftVentToggled();
    if (!excavationChanged && !shaftChanged && !excavationActivated && !shaftActivated) {
      return;
    }

    MAShapeBootstrap.ensureInitialized();
    StringBuilder message = new StringBuilder();
    if (excavationChanged || excavationActivated) {
      String excavationName = MAShapeRegistry.byIndex(FeatureId.EXCAVATION, current.selectedExcavationShapeIndex())
          .map(shape -> shape.displayName())
          .orElse("#" + current.selectedExcavationShapeIndex());
      message.append("Excavation Shape: ").append(excavationName);
    }
    if (shaftChanged || shaftActivated) {
      String shaftName = MAShapeRegistry.byIndex(FeatureId.SHAFTANATION, current.selectedShaftanationShapeIndex())
          .map(shape -> shape.displayName())
          .orElse("#" + current.selectedShaftanationShapeIndex());
      if (!message.isEmpty()) {
        message.append(" | ");
      }
      message.append("Shaft Shape: ").append(shaftName);
    }

    Minecraft minecraft = Minecraft.getInstance();
    ClientRuntimeCompat.showOverlayMessage(minecraft, Component.literal(message.toString()));
  }

  /**
   * Human-friendly guardrail: s yn cs ta te to se rv er exists so this path stays predictable and easier to debug when things get weird.
   */
  private static void syncStateToServer(ClientInputService.ClientInputState state) {
    ClientPacketDistributor.sendToServer(ClientActionInputSupport.createPlayerStateSyncPacket(state));
    lastSyncedState = state;
  }

  private static boolean areAllFeaturesEnabled(ClientInputService.ClientInputState state) {
    for (FeatureId feature : FeatureId.values()) {
      if (!state.featureEnabled().getOrDefault(feature, false)) {
        return false;
      }
    }
    return true;
  }

  /**
   * parseKeyToken exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  private static InputConstants.Key parseKeyToken(String token) {
    return ClientActionInputSupport.parseKeyToken(token);
  }

  private static PoseStack ensurePoseStack(PoseStack poseStack) {
    return poseStack == null ? new PoseStack() : poseStack;
  }

  private static double[] extractCameraCoordinates(Object camera) {
    if (camera == null) {
      return new double[] { 0.0d, 0.0d, 0.0d };
    }

    try {
      Method getX = camera.getClass().getMethod("getX");
      Method getY = camera.getClass().getMethod("getY");
      Method getZ = camera.getClass().getMethod("getZ");
      return new double[] {
          ((Number) getX.invoke(camera)).doubleValue(),
          ((Number) getY.invoke(camera)).doubleValue(),
          ((Number) getZ.invoke(camera)).doubleValue()
      };
    } catch (ReflectiveOperationException ignored) {
      // Fall through to position-object based extraction.
    }

    try {
      for (String positionMethodName : new String[] { "getPosition", "getPos", "position" }) {
        try {
          Method positionMethod = camera.getClass().getMethod(positionMethodName);
          Object position = positionMethod.invoke(camera);
          if (position != null) {
            return extractVectorCoordinates(position);
          }
        } catch (NoSuchMethodException ignored) {
          // Try next method name.
        }
      }
    } catch (ReflectiveOperationException ignored) {
      // Use safe fallback below.
    }

    return new double[] { 0.0d, 0.0d, 0.0d };
  }

  private static double[] extractVectorCoordinates(Object vector) {
    try {
      Method xMethod = vector.getClass().getMethod("x");
      Method yMethod = vector.getClass().getMethod("y");
      Method zMethod = vector.getClass().getMethod("z");
      return new double[] {
          ((Number) xMethod.invoke(vector)).doubleValue(),
          ((Number) yMethod.invoke(vector)).doubleValue(),
          ((Number) zMethod.invoke(vector)).doubleValue()
      };
    } catch (ReflectiveOperationException ignored) {
      // Fall through to fields.
    }

    try {
      return new double[] {
          ((Number) vector.getClass().getField("x").get(vector)).doubleValue(),
          ((Number) vector.getClass().getField("y").get(vector)).doubleValue(),
          ((Number) vector.getClass().getField("z").get(vector)).doubleValue()
      };
    } catch (ReflectiveOperationException ignored) {
      return new double[] { 0.0d, 0.0d, 0.0d };
    }
  }
}
