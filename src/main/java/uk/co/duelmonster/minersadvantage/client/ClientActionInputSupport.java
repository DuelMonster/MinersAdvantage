//~ mc26_3_api
package uk.co.duelmonster.minersadvantage.client;

import com.mojang.blaze3d.platform.InputConstants;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import uk.co.duelmonster.minersadvantage.ModCommon;
import uk.co.duelmonster.minersadvantage.common.config.MAConfig_Base;
import uk.co.duelmonster.minersadvantage.common.config.MAServerRootConfig;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;
import uk.co.duelmonster.minersadvantage.common.network.ComponentTogglePacket;
import uk.co.duelmonster.minersadvantage.common.network.PlayerStateSyncPacket;
import uk.co.duelmonster.minersadvantage.common.services.input.ClientInputService;

/**
 * Shared client-input helper bundle that keeps Fabric and NeoForge input plumbing from duplicating
 * the same key parsing, action collection, and sync-packet prep logic everywhere.
 */
public final class ClientActionInputSupport {
  /**
   * Utility class only; no instances needed unless someone enjoys unnecessary object allocation.
   */
  private ClientActionInputSupport() {
  }

  /**
   * Collect currently pressed actions from active key mappings, respecting hold vs click semantics.
   */
  public static Set<KeyBindings.ClientAction> collectPressedActions(
      Map<KeyBindings.ClientAction, KeyMapping> keyMappings) {
    Set<KeyBindings.ClientAction> pressed = new HashSet<>();
    // Toggle-style actions stay active while held; others consume click so one tap equals one action.
    for (Map.Entry<KeyBindings.ClientAction, KeyMapping> entry : keyMappings.entrySet()) {
      boolean active = switch (entry.getKey()) {
        case EXCAVATION_MODE_TOGGLE, SHAFT_VENT_TOGGLE -> entry.getValue().isDown();
        default -> entry.getValue().consumeClick();
      };
      if (active) {
        pressed.add(entry.getKey());
      }
    }
    return pressed;
  }

  /**
   * Collect the secret-code digits currently held on either top-row number keys or numpad.
   */
  public static Set<Character> collectPressedSupremeDigits() {
    Set<Character> pressed = new HashSet<>();
    Minecraft minecraft = Minecraft.getInstance();
    if (minecraft.getWindow() == null) {
      return pressed;
    }

    com.mojang.blaze3d.platform.Window window = minecraft.getWindow();
    addPressedDigit(window, pressed, '0', InputConstants.KEY_0, InputConstants.KEY_NUMPAD0);
    addPressedDigit(window, pressed, '2', InputConstants.KEY_2, InputConstants.KEY_NUMPAD2);
    addPressedDigit(window, pressed, '7', InputConstants.KEY_7, InputConstants.KEY_NUMPAD7);
    addPressedDigit(window, pressed, '8', InputConstants.KEY_8, InputConstants.KEY_NUMPAD8);
    return pressed;
  }

  private static void addPressedDigit(com.mojang.blaze3d.platform.Window window, Set<Character> pressed, char digit,
      int primaryKey, int keypadKey) {
    if (InputConstants.isKeyDown(primaryKey) || InputConstants.isKeyDown(keypadKey)) {
      pressed.add(digit);
    }
  }

  /**
   * Resolve local player id for packets; falls back to zero when player is not yet available.
   */
  public static long resolveLocalPlayerId() {
    long playerId = 0L;
    Minecraft minecraft = Minecraft.getInstance();
    if (minecraft.player != null) {
      playerId = minecraft.player.getUUID().getLeastSignificantBits();
    }
    return playerId;
  }

  /**
   * Determine whether shape-related activation state changed enough to require sync.
   */
  public static boolean hasActivationStateChanged(
      ClientInputService.ClientInputState previousState,
      ClientInputService.ClientInputState currentState) {
    return previousState.excavationToggled() != currentState.excavationToggled()
        || previousState.shaftVentToggled() != currentState.shaftVentToggled()
        || previousState.selectedExcavationShapeIndex() != currentState.selectedExcavationShapeIndex()
        || previousState.selectedShaftanationShapeIndex() != currentState.selectedShaftanationShapeIndex();
  }

  /**
   * Translate mouse-wheel input into shape-cycle actions for whichever feature toggles are currently active.
   */
  public static Set<KeyBindings.ClientAction> collectScrollActions(
      ClientInputService.ClientInputState inputState,
      double scrollY) {
    boolean excavationActive = inputState.excavationToggled()
        && inputState.featureEnabled().getOrDefault(FeatureId.EXCAVATION, false);
    boolean shaftActive = inputState.shaftVentToggled()
        && inputState.featureEnabled().getOrDefault(FeatureId.SHAFTANATION, false);
    if (!excavationActive && !shaftActive) {
      return Set.of();
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
    return actions;
  }

  /**
   * Convert config key tokens into Minecraft key identifiers.
   */
  private static InputConstants.Key parseKeyToken(String token) {
    String mcKeyName = switch (token) {
      case "KP_1" -> "key.keyboard.keypad.1";
      case "KP_2" -> "key.keyboard.keypad.2";
      case "KP_3" -> "key.keyboard.keypad.3";
      case "KP_4" -> "key.keyboard.keypad.4";
      case "KP_5" -> "key.keyboard.keypad.5";
      case "KP_6" -> "key.keyboard.keypad.6";
      case "KP_7" -> "key.keyboard.keypad.7";
      case "KP_8" -> "key.keyboard.keypad.8";
      case "KP_9" -> "key.keyboard.keypad.9";
      case "KP_0" -> "key.keyboard.keypad.0";
      case "DELETE" -> "key.keyboard.delete";
      case "GRAVE" -> "key.keyboard.grave.accent";
      case "TAB" -> "key.keyboard.tab";
      case "LEFT_ALT" -> "key.keyboard.left.alt";
      case "V" -> "key.keyboard.v";
      case "F11" -> "key.keyboard.f11";
      case "F12" -> "key.keyboard.f12";
      default -> throw new IllegalArgumentException("Unknown key token: " + token);
    };
    return InputConstants.getKey(mcKeyName);
  }

  /**
   * Build a key mapping consistently across loaders so call sites stay tiny and predictable.
   */
  public static KeyMapping createKeyMapping(String translationKey, InputConstants.Key key,
      KeyMapping.Category category) {
    return new KeyMapping(
        translationKey,
        key.getType(),
        key.getValue(),
        category);
  }

  /**
   * Resolve a spec's default key; a null token means the binding is registered but left unbound.
   */
  public static InputConstants.Key resolveDefaultKey(String token) {
    return token == null ? InputConstants.UNKNOWN : parseKeyToken(token);
  }

  /**
   * Build the standard translation key used for a keybinding's display name.
   */
  public static String keyMappingTranslationKey(KeyBindings.ClientAction action) {
    return "key." + ModCommon.MOD_ID + "." + action.name().toLowerCase(Locale.ROOT);
  }

  /**
   * Report keybind-driven feature toggles to the player and, in singleplayer, persist the enabled flag.
   */
  public static void applyFeatureToggles(List<ComponentTogglePacket> togglePackets) {
    if (togglePackets.isEmpty()) {
      return;
    }

    Minecraft minecraft = Minecraft.getInstance();
    // Server config is authoritative in multiplayer, so a client keybind must never rewrite it there.
    boolean persist = minecraft.hasSingleplayerServer()
        && !MAConfig_Base.getClientRootConfig().client().disableKeybindConfigPersistence();

    MAServerRootConfig serverConfig = MAConfig_Base.getServerRootConfig();
    for (ComponentTogglePacket packet : togglePackets) {
      ClientRuntimeCompat.showOverlayMessage(minecraft, describeFeatureToggle(packet));
      if (persist) {
        serverConfig = serverConfig.withFeatureEnabled(packet.feature(), packet.enabled());
      }
    }

    if (persist) {
      MAConfig_Base.setServerRootConfig(serverConfig);
    }
  }

  private static Component describeFeatureToggle(ComponentTogglePacket packet) {
    String name = packet.feature().name();
    String displayName = name.charAt(0) + name.substring(1).toLowerCase(Locale.ROOT);
    return Component.literal(displayName + ": ")
        .append(Component.literal(packet.enabled() ? "Enabled" : "Disabled")
            .withStyle(packet.enabled() ? ChatFormatting.GREEN : ChatFormatting.RED));
  }

  /**
   * Register a dedicated MinersAdvantage keybind category that resolves through the standard key.category lang key.
   */
  public static KeyMapping.Category createKeyCategory() {
    try {
      Method registerCategoryMethod = findCategoryFactoryMethod();
      if (registerCategoryMethod == null) {
        throw new NoSuchMethodException("KeyMapping.Category factory method not found");
      }

      Class<?> parameterType = registerCategoryMethod.getParameterTypes()[0];
      Object categoryId = createCategoryIdentifier(parameterType);
      return (KeyMapping.Category) registerCategoryMethod.invoke(null, categoryId);
    } catch (ReflectiveOperationException exception) {
      throw new IllegalStateException("Unable to create key mapping category", exception);
    }
  }

  private static Object createCategoryIdentifier(Class<?> identifierType) throws ReflectiveOperationException {
    String namespace = ModCommon.MOD_ID;
    String path = "keybinds";

    if (identifierType == String.class) {
      return "key.category." + namespace + "." + path;
    }

    ReflectiveOperationException lastException = null;

    try {
      var constructor = identifierType.getConstructor(String.class, String.class);
      return constructor.newInstance(namespace, path);
    } catch (ReflectiveOperationException exception) {
      lastException = exception;
    }

    try {
      var constructor = identifierType.getConstructor(String.class);
      return constructor.newInstance(namespace + ":" + path);
    } catch (ReflectiveOperationException exception) {
      lastException = exception;
    }

    try {
      var constructor = identifierType.getDeclaredConstructor(String.class, String.class);
      if (constructor.trySetAccessible()) {
        return constructor.newInstance(namespace, path);
      }
    } catch (ReflectiveOperationException exception) {
      lastException = exception;
    }

    try {
      var constructor = identifierType.getDeclaredConstructor(String.class);
      if (constructor.trySetAccessible()) {
        return constructor.newInstance(namespace + ":" + path);
      }
    } catch (ReflectiveOperationException exception) {
      lastException = exception;
    }

    for (String methodName : new String[] { "fromNamespaceAndPath", "create", "of", "parse", "tryParse" }) {
      try {
        Method twoArgFactory = identifierType.getMethod(methodName, String.class, String.class);
        Object value = twoArgFactory.invoke(null, namespace, path);
        if (value != null) {
          return value;
        }
      } catch (NoSuchMethodException ignored) {
        try {
          Method oneArgFactory = identifierType.getMethod(methodName, String.class);
          Object value = oneArgFactory.invoke(null, namespace + ":" + path);
          if (value != null) {
            return value;
          }
        } catch (ReflectiveOperationException innerException) {
          lastException = innerException;
        }
      } catch (ReflectiveOperationException exception) {
        lastException = exception;
      }
    }

    throw new IllegalStateException(
        "Unable to create key category identifier for type " + identifierType.getName(),
        lastException);
  }

  private static Method findCategoryFactoryMethod() {
    for (Method method : KeyMapping.Category.class.getDeclaredMethods()) {
      if (java.lang.reflect.Modifier.isStatic(method.getModifiers())
          && method.getParameterCount() == 1
          && method.getReturnType() == KeyMapping.Category.class) {
        method.setAccessible(true);
        return method;
      }
    }
    return null;
  }

  /**
   * Build client->server state sync packet using local defaults and current input toggles.
   */
  public static PlayerStateSyncPacket createPlayerStateSyncPacket(ClientInputService.ClientInputState state) {
    return new PlayerStateSyncPacket(
        resolveLocalPlayerId(),
        MAConfig_Base.getClientRootConfig(),
        MAServerRootConfig.defaults(),
        state.excavationToggled(),
        state.shaftVentToggled(),
        state.selectedExcavationShapeIndex(),
        state.selectedShaftanationShapeIndex());
  }
}