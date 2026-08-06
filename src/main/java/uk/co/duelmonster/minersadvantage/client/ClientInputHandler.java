//? if fabric {
package uk.co.duelmonster.minersadvantage.client;

import java.util.Set;
import uk.co.duelmonster.minersadvantage.common.network.AbortWorkersPacket;
import uk.co.duelmonster.minersadvantage.common.network.ComponentTogglePacket;
import uk.co.duelmonster.minersadvantage.common.network.IlluminationActionPacket;
import uk.co.duelmonster.minersadvantage.common.network.SupremeVantagePacket;
import uk.co.duelmonster.minersadvantage.common.services.input.ClientInputService;
import uk.co.duelmonster.minersadvantage.common.services.utility.SupremeVantageService;

//? if fabric {
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import uk.co.duelmonster.minersadvantage.common.config.MAClientRootConfig;
import uk.co.duelmonster.minersadvantage.common.config.MAConfig_Base;
import uk.co.duelmonster.minersadvantage.common.config.MAServerRootConfig;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;
import uk.co.duelmonster.minersadvantage.common.log.LogUtils;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeBootstrap;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeRegistry;
//?} else {
/*
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
// import net.minecraft.client.KeyMapping;
*/ //?}

/**
 * Client input tick handler for Fabric.
     *
     * Called each client tick to:
     * 1. Register keybindings (Fabric-only)
     * 2. Poll keybindings for pressed actions
     * 3. Update client input state via ClientInputService.process()
     * 4. Send component toggle packets to server
     * 5. Send illumination and abort actions
     */
public final class ClientInputHandler {
  private static ClientInputService.ClientInputState inputState = ClientInputService.ClientInputState.defaults();
  private static ClientInputService.ClientInputState lastSyncedState = ClientInputService.ClientInputState.defaults();
  private static MAClientRootConfig lastSyncedClientConfig;
  private static MAServerRootConfig lastSyncedServerConfig;
  private static final SupremeVantageService supremeVantageService = new SupremeVantageService();
  private static SupremeVantageService.ClientState supremeVantageState = SupremeVantageService.ClientState.defaults();
  private static String lastObservedScreenClassName;
  private static final KeyMapping.Category KEY_CATEGORY = ClientActionInputSupport.createKeyCategory();
  //? if fabric {
  private static java.util.Map<KeyBindings.ClientAction, KeyMapping> keyMappings = new java.util.EnumMap<>(
      KeyBindings.ClientAction.class);
  //?} else {
  /*
  // NeoForge uses RegisterKeyMappingsEvent for registration
  */ //?}

  /**
   * ClientInputHandler exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  private ClientInputHandler() {
  }

  /**
   * Convert token key names (KP_1, DELETE, F12, etc.) to Minecraft format.
   * Returns InputConstants.Key that can be used to construct KeyMapping.
   */
  private static InputConstants.Key parseKeyToken(String token) {
    // Map token names to Minecraft key format (future-you will thank present-you).
    return ClientActionInputSupport.parseKeyToken(token);
  }

  /**
   * registerKeyMapping exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  private static void registerKeyMapping(KeyMapping keyMapping) {
    try {
      Class<?> helperClass;
      java.lang.reflect.Method registerMethod;
      try {
        helperClass = Class.forName("net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper");
      } catch (ClassNotFoundException oldApiMissing) {
        helperClass = Class.forName("net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper");
      }

      registerMethod = java.util.Arrays.stream(helperClass.getMethods())
          .filter(method -> java.lang.reflect.Modifier.isStatic(method.getModifiers()))
          .filter(method -> method.getParameterCount() == 1)
          .filter(method -> method.getParameterTypes()[0] == KeyMapping.class)
          .findFirst()
          .orElse(null);
      if (registerMethod == null) {
        throw new NoSuchMethodException("No static KeyMapping registration method on " + helperClass.getName());
      }
      registerMethod.invoke(null, keyMapping);
    } catch (ReflectiveOperationException exception) {
      throw new IllegalStateException("Unable to register key mapping", exception);
    }
  }

  //? if fabric {
  /**
   * Register keybindings on Fabric client startup.
   * Called from FabricClientEntrypoint.onInitializeClient().
   */
  public static void registerKeybindings() {
    for (KeyBindings.KeyBindingSpec spec : KeyBindings.all()) {
      if (spec.defaultKey() == null) {
        continue;
      }

      String translationKey = "key." + uk.co.duelmonster.minersadvantage.ModCommon.MOD_ID + "."
          + spec.action().name().toLowerCase();

      InputConstants.Key key = parseKeyToken(spec.defaultKey());
      KeyMapping keyMapping = ClientActionInputSupport.createKeyMapping(translationKey, key, KEY_CATEGORY);
      registerKeyMapping(keyMapping);
      keyMappings.put(spec.action(), keyMapping);
    }
  }

  /**
   * Collect currently pressed keybindings as a set of actions (Fabric).
   */
  private static Set<KeyBindings.ClientAction> getPressedActions() {
    return ClientActionInputSupport.collectPressedActions(keyMappings);
  }

  /**
   * Return whether the specified custom action keybind is physically held down right now.
   */
  public static boolean isActionKeyHeld(KeyBindings.ClientAction action) {
    if (action == null) {
      return false;
    }

    KeyMapping mapping = keyMappings.get(action);
    return mapping != null && mapping.isDown();
  }
  //?} else {
  /*
  // NeoForge keybinding registration and polling are implemented in NeoForgeClientEvents.
  */ //?}

  //? if fabric {
  /**
   * Called each client tick. Consumes key presses and sends packets.
   */
  public static void tick() {
    Minecraft client = Minecraft.getInstance();
    logScreenTransition(client);
    // Skip feature input processing until an actual world is loaded.
    if (client.player == null || client.level == null) {
      return;
    }

    ensureSyncSnapshotsInitialized();

    // Collect currently pressed keybindings (future-you will thank present-you).
    Set<KeyBindings.ClientAction> pressedSet = getPressedActions();

    // Process input state machine (future-you will thank present-you).
    // Hold-mode remains the default here until a dedicated local toggle setting is introduced. (future-you will thank present-you).
    boolean excavationToggleMode = false;
    ClientInputService.ClientInputState previousState = inputState;
    ClientInputService.ClientInputResult result = new ClientInputService().process(inputState, pressedSet,
        excavationToggleMode);

    // Update state (future-you will thank present-you).
    inputState = result.state();
    showShapeHudIfChangedOrActivated(previousState, inputState);

    SupremeVantageService.ClientUpdate supremeUpdate = supremeVantageService.processClientTick(
        supremeVantageState,
        ClientActionInputSupport.collectPressedSupremeDigits(),
        inputState.excavationToggled(),
        areAllFeaturesEnabled(inputState));
    supremeVantageState = supremeUpdate.state();

    if (supremeUpdate.notifyWorthy()) {
      ClientRuntimeCompat.showOverlayMessage(Minecraft.getInstance(),
          Component.literal("SupremeVantage code accepted"));
    }

    if (supremeUpdate.shouldSendRewardPacket() && !supremeUpdate.packetCode().isEmpty()) {
      long playerId = ClientActionInputSupport.resolveLocalPlayerId();
      if (playerId != 0L) {
        ClientPlayNetworking.send(new SupremeVantagePacket(playerId, supremeUpdate.packetCode()));
      }
    }

    // Send component toggle packets to the server. (future-you will thank present-you).
    for (ComponentTogglePacket packet : result.togglePackets()) {
      ClientPlayNetworking.send(packet);
    }

    if (result.illuminatePlace()) {
      sendIlluminationAction(false);
    }
    if (result.illuminateArea()) {
      sendIlluminationAction(true);
    }

    if (result.abortRequested()) {
      long playerId = ClientActionInputSupport.resolveLocalPlayerId();
      ClientPlayNetworking.send(new AbortWorkersPacket(playerId, "client:keybind"));
    }

    boolean activationStateChanged = ClientActionInputSupport.hasActivationStateChanged(lastSyncedState,
        result.state());
    boolean configStateChanged = !lastSyncedClientConfig.equals(MAConfig_Base.getClientRootConfig())
        || !lastSyncedServerConfig.equals(MAConfig_Base.getServerRootConfig());
    boolean shouldSyncFromInput = activationStateChanged && result.shouldSyncVariables();
    boolean shouldSyncFromConfig = configStateChanged && result.shouldSyncConfig();

    if (shouldSyncFromInput || shouldSyncFromConfig) {
      syncStateToServer(result.state());
    }
  }

  public static ClientInputService.ClientInputState getInputState() {
    return inputState;
  }

  /**
   * o nm ou se sc ro ll exists so this path stays predictable and easier to debug when things get weird.
   */
  public static boolean onMouseScroll(double scrollY) {
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

    Set<KeyBindings.ClientAction> actions = new java.util.HashSet<>();
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

  /**
   * s ho ws ha pe hu di fc ha ng ed or ac ti va te d exists so this path stays predictable and easier to debug when things get weird.
   */
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
   * s yn cs ta te to se rv er exists so this path stays predictable and easier to debug when things get weird.
   */
  private static void syncStateToServer(ClientInputService.ClientInputState state) {
    ClientPlayNetworking.send(ClientActionInputSupport.createPlayerStateSyncPacket(state));
    lastSyncedState = state;
    lastSyncedClientConfig = MAConfig_Base.getClientRootConfig();
    lastSyncedServerConfig = MAConfig_Base.getServerRootConfig();
  }

  private static void ensureSyncSnapshotsInitialized() {
    if (lastSyncedClientConfig == null || lastSyncedServerConfig == null) {
      lastSyncedClientConfig = MAConfig_Base.getClientRootConfig();
      lastSyncedServerConfig = MAConfig_Base.getServerRootConfig();
    }
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
   * Log screen class transitions for credits/exit diagnostics without changing behavior.
   */
  private static void logScreenTransition(Minecraft client) {
    if (client == null) {
      return;
    }

    var currentScreen = ClientRuntimeCompat.getCurrentScreen(client);
    String currentName = currentScreen == null ? null : currentScreen.getClass().getName();
    if (java.util.Objects.equals(lastObservedScreenClassName, currentName)) {
      return;
    }

    String previousName = lastObservedScreenClassName == null ? "null" : lastObservedScreenClassName;
    String nextName = currentName == null ? "null" : currentName;
    lastObservedScreenClassName = currentName;

    String playerName = client.player == null ? "null" : client.player.getScoreboardName();
    String dimension = client.level == null ? "null" : client.level.dimension().toString();
    LogUtils.logDebug(
        "Screen transition previous={} current={} player={} dimension={}",
        previousName,
        nextName,
        playerName,
        dimension);
  }

  /**
   * s en di ll um in at io na ct io n exists so this path stays predictable and easier to debug when things get weird.
   */
  private static void sendIlluminationAction(boolean area) {
    Minecraft client = Minecraft.getInstance();
    if (!(client.hitResult instanceof BlockHitResult blockHit)) {
      return;
    }

    var hitFace = blockHit.getDirection();
    var pos = area ? blockHit.getBlockPos() : blockHit.getBlockPos().relative(hitFace);
    ClientPlayNetworking.send(new IlluminationActionPacket(pos.getX(), pos.getY(), pos.getZ(), area, hitFace));
  }
  //?} else {
  /*
  // NeoForge tick handler is in NeoForgeClientEvents
  */ //?}
}
//?} else {
/*
// This handler is Fabric-only. NeoForge uses ForgeClientInputHandler.
*/ //?}
