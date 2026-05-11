//? if fabric {
package uk.co.duelmonster.minersadvantage.client;

import java.util.HashSet;
import java.util.List;
import java.lang.reflect.Method;
import java.util.Set;
import uk.co.duelmonster.minersadvantage.common.config.ServerOverridesConfig;
import uk.co.duelmonster.minersadvantage.common.config.SyncedClientConfig;
import uk.co.duelmonster.minersadvantage.common.network.AbortWorkersPacket;
import uk.co.duelmonster.minersadvantage.common.network.ComponentTogglePacket;
import uk.co.duelmonster.minersadvantage.common.network.PlayerStateSyncPacket;
import uk.co.duelmonster.minersadvantage.common.services.input.ClientInputService;

//? if fabric {
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
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
        // Map token names to Minecraft key format
        String mcKeyName = switch (token) {
            case "KP_1" -> "key.keyboard.keypad.1";
            case "KP_2" -> "key.keyboard.keypad.2";
            case "KP_3" -> "key.keyboard.keypad.3";
            case "KP_4" -> "key.keyboard.keypad.4";
            case "KP_5" -> "key.keyboard.keypad.5";
            case "KP_6" -> "key.keyboard.keypad.6";
            case "KP_7" -> "key.keyboard.keypad.7";
            case "KP_8" -> "key.keyboard.keypad.8";
            case "DELETE" -> "key.keyboard.delete";
            case "GRAVE" -> "key.keyboard.grave.accent";
            case "TAB" -> "key.keyboard.tab";
            case "LEFT_ALT" -> "key.keyboard.left.alt";
            case "V" -> "key.keyboard.v";
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

    //? if fabric {
    /**
     * Register keybindings on Fabric client startup.
     * Called from FabricClientEntrypoint.onInitializeClient().
     */
    public static void registerKeybindings() {
        for (KeyBindings.KeyBindingSpec spec : KeyBindings.all()) {
            String translationKey = "key.minersadvantage." + spec.action().name().toLowerCase();

            InputConstants.Key key = parseKeyToken(spec.defaultKey());
            KeyMapping keyMapping = new KeyMapping(
                translationKey,
                key.getType(),
                key.getValue(),
                KeyMapping.Category.MISC
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
            if (entry.getValue().consumeClick()) {
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
        // Collect currently pressed keybindings
        List<KeyBindings.ClientAction> pressed = getPressedActions();
        Set<KeyBindings.ClientAction> pressedSet = new HashSet<>(pressed);

        // Process input state machine
        // Hold-mode remains the default here until a dedicated local toggle setting is introduced.
        boolean excavationToggleMode = false;
        ClientInputService.ClientInputResult result = new ClientInputService().process(inputState, pressedSet, excavationToggleMode);

        // Update state
        inputState = result.state();

        // Send component toggle packets to the server.
        for (ComponentTogglePacket packet : result.togglePackets()) {
            ClientPlayNetworking.send(packet);
        }

        if (result.abortRequested()) {
            long playerId = 0L;
            Minecraft playerClient = Minecraft.getInstance();
            if (playerClient.player != null) {
                playerId = playerClient.player.getUUID().getLeastSignificantBits();
            }
            ClientPlayNetworking.send(new AbortWorkersPacket(playerId, "client:keybind"));
        }

        if (!pressedSet.isEmpty() && (result.shouldSyncConfig() || result.shouldSyncVariables())) {
            long playerId = 0L;
            Minecraft playerClient = Minecraft.getInstance();
            if (playerClient.player != null) {
                playerId = playerClient.player.getUUID().getLeastSignificantBits();
            }
            SyncedClientConfig defaults = SyncedClientConfig.defaults();
            ClientPlayNetworking.send(new PlayerStateSyncPacket(
                playerId,
                defaults,
                defaults,
                new ServerOverridesConfig()
            ));
        }
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



