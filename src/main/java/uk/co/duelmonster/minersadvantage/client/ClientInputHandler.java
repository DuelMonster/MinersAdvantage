//? if fabric {
package uk.co.duelmonster.minersadvantage.client;

import java.util.HashSet;
import java.util.List;
import java.lang.reflect.Method;
import java.util.Set;
import uk.co.duelmonster.minersadvantage.common.network.ComponentTogglePacket;
import uk.co.duelmonster.minersadvantage.common.services.input.ClientInputService;

//? if fabric {
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
//?} else {
/*
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.minecraft.client.KeyMapping;
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
    // NeoForge registration happens via RegisterKeyMappingsEvent in NeoForgeClientEvents
    // TODO: Add NeoForge keybinding polling
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
        boolean excavationToggleMode = false; // TODO: Read from config
        ClientInputService.ClientInputResult result = new ClientInputService().process(inputState, pressedSet, excavationToggleMode);

        // Update state
        inputState = result.state();

        // TODO: Send component toggle packets (M4 - networking transport layer)
        // for (ComponentTogglePacket packet : result.togglePackets()) {
        //     ClientPlayNetworking.send(..., packet);
        // }

        // TODO: Send illumination and abort packets if needed (M4)
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
