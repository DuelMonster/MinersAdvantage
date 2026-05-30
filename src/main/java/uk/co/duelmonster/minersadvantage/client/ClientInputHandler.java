//? if fabric {
package uk.co.duelmonster.minersadvantage.client;

import java.util.HashSet;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import uk.co.duelmonster.minersadvantage.common.config.MAClientRootConfig;
import uk.co.duelmonster.minersadvantage.common.config.MAServerRootConfig;
import uk.co.duelmonster.minersadvantage.common.network.AbortWorkersPacket;
import uk.co.duelmonster.minersadvantage.common.network.ComponentTogglePacket;
import uk.co.duelmonster.minersadvantage.common.network.IlluminationActionPacket;
import uk.co.duelmonster.minersadvantage.common.network.PlayerStateSyncPacket;
import uk.co.duelmonster.minersadvantage.common.services.input.ClientInputService;

//? if fabric {
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.world.phys.BlockHitResult;
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
    private static final KeyMapping.Category KEY_CATEGORY = createKeyCategory();
    //? if fabric {
    private static java.util.Map<KeyBindings.ClientAction, KeyMapping> keyMappings = new java.util.EnumMap<>(KeyBindings.ClientAction.class);
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
        // Why this exists: Map token names to Minecraft key format (future-you will thank present-you).
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
     * registerKeyMapping exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private static void registerKeyMapping(KeyMapping keyMapping) {
        try {
            Class<?> helperClass;
            Method registerMethod;
            try {
                helperClass = Class.forName("net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper");
                registerMethod = helperClass.getMethod("registerKeyBinding", KeyMapping.class);
            } catch (ClassNotFoundException oldApiMissing) {
                helperClass = Class.forName("net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper");
                registerMethod = helperClass.getMethod("registerKeyMapping", KeyMapping.class);
            }
            registerMethod.invoke(null, keyMapping);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to register key mapping", exception);
        }
    }

    private static KeyMapping.Category createKeyCategory() {
        try {
            Object identifier = createCategoryIdentifier();
            Method registerCategoryMethod = KeyMapping.Category.class.getMethod("register", identifier.getClass());
            return (KeyMapping.Category) registerCategoryMethod.invoke(null, identifier);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to create key mapping category", exception);
        }
    }

    private static Object createCategoryIdentifier() throws ReflectiveOperationException {
        try {
            Class<?> identifierClass = Class.forName("net.minecraft.resources.Identifier");
            Method factory = identifierClass.getMethod("fromNamespaceAndPath", String.class, String.class);
            return factory.invoke(null, uk.co.duelmonster.minersadvantage.ModCommon.MOD_ID, "keybinds");
        } catch (ClassNotFoundException missingIdentifierClass) {
            Class<?> resourceLocationClass = Class.forName("net.minecraft.resources.ResourceLocation");
            Method factory = resourceLocationClass.getMethod("fromNamespaceAndPath", String.class, String.class);
            return factory.invoke(null, uk.co.duelmonster.minersadvantage.ModCommon.MOD_ID, "keybinds");
        }
    }

    //? if fabric {
    /**
     * Register keybindings on Fabric client startup.
     * Called from FabricClientEntrypoint.onInitializeClient().
     */
    public static void registerKeybindings() {
        for (KeyBindings.KeyBindingSpec spec : KeyBindings.all()) {
            String translationKey = "key." + uk.co.duelmonster.minersadvantage.ModCommon.MOD_ID + "." + spec.action().name().toLowerCase();

            InputConstants.Key key = parseKeyToken(spec.defaultKey());
            KeyMapping keyMapping = new KeyMapping(
                translationKey,
                key.getType(),
                key.getValue(),
                KEY_CATEGORY
            );
            registerKeyMapping(keyMapping);
            keyMappings.put(spec.action(), keyMapping);
        }
    }

    /**
     * Collect currently pressed keybindings as a set of actions (Fabric).
     */
    private static List<KeyBindings.ClientAction> getPressedActions() {
        List<KeyBindings.ClientAction> pressed = new java.util.ArrayList<>();
        for (java.util.Map.Entry<KeyBindings.ClientAction, KeyMapping> entry : keyMappings.entrySet()) {
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
    //?} else {
    /*
    // NeoForge keybinding registration and polling are implemented in NeoForgeClientEvents.
    */ //?}

    //? if fabric {
    /**
     * Called each client tick. Consumes key presses and sends packets.
     */
    public static void tick() {
        // Why this exists: Collect currently pressed keybindings (future-you will thank present-you).
        List<KeyBindings.ClientAction> pressed = getPressedActions();
        Set<KeyBindings.ClientAction> pressedSet = new HashSet<>(pressed);

        // Why this exists: Process input state machine (future-you will thank present-you).
        // Why this exists: Hold-mode remains the default here until a dedicated local toggle setting is introduced. (future-you will thank present-you).
        boolean excavationToggleMode = false;
        ClientInputService.ClientInputResult result = new ClientInputService().process(inputState, pressedSet, excavationToggleMode);

        // Why this exists: Update state (future-you will thank present-you).
        inputState = result.state();

        // Why this exists: Send component toggle packets to the server. (future-you will thank present-you).
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
            long playerId = 0L;
            Minecraft playerClient = Minecraft.getInstance();
            if (playerClient.player != null) {
                playerId = playerClient.player.getUUID().getLeastSignificantBits();
            }
            ClientPlayNetworking.send(new AbortWorkersPacket(playerId, "client:keybind"));
        }

        boolean activationStateChanged =
            lastSyncedState.excavationToggled() != result.state().excavationToggled()
                || lastSyncedState.shaftVentToggled() != result.state().shaftVentToggled()
                || lastSyncedState.selectedExcavationShapeIndex() != result.state().selectedExcavationShapeIndex()
                || lastSyncedState.selectedShaftanationShapeIndex() != result.state().selectedShaftanationShapeIndex();

        if (activationStateChanged && (result.shouldSyncConfig() || result.shouldSyncVariables())) {
            syncStateToServer(result.state());
        }
    }

    public static ClientInputService.ClientInputState getInputState() {
        return inputState;
    }

    public static boolean onMouseScroll(double scrollY) {
        if (scrollY == 0.0d) {
            return false;
        }

        boolean excavationActive = inputState.excavationToggled()
            && inputState.featureEnabled().getOrDefault(uk.co.duelmonster.minersadvantage.common.feature.FeatureId.EXCAVATION, false);
        boolean shaftActive = inputState.shaftVentToggled()
            && inputState.featureEnabled().getOrDefault(uk.co.duelmonster.minersadvantage.common.feature.FeatureId.SHAFTANATION, false);

        if (!excavationActive && !shaftActive) {
            return false;
        }

        Set<KeyBindings.ClientAction> actions = new HashSet<>();
        if (excavationActive) {
            actions.add(scrollY > 0.0d ? KeyBindings.ClientAction.EXCAVATION_SHAPE_PREV : KeyBindings.ClientAction.EXCAVATION_SHAPE_NEXT);
        }
        if (shaftActive) {
            actions.add(scrollY > 0.0d ? KeyBindings.ClientAction.SHAFTANATION_SHAPE_PREV : KeyBindings.ClientAction.SHAFTANATION_SHAPE_NEXT);
        }

        ClientInputService.ClientInputResult scrollResult = new ClientInputService().process(inputState, actions, false);
        inputState = scrollResult.state();
        syncStateToServer(inputState);
        return true;
    }

    private static void syncStateToServer(ClientInputService.ClientInputState state) {
        long playerId = 0L;
        Minecraft playerClient = Minecraft.getInstance();
        if (playerClient.player != null) {
            playerId = playerClient.player.getUUID().getLeastSignificantBits();
        }
        ClientPlayNetworking.send(new PlayerStateSyncPacket(
            playerId,
            MAClientRootConfig.defaults(),
            MAServerRootConfig.defaults(),
            state.excavationToggled(),
            state.shaftVentToggled(),
            state.selectedExcavationShapeIndex(),
            state.selectedShaftanationShapeIndex()
        ));
        lastSyncedState = state;
    }

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
