package uk.co.duelmonster.minersadvantage.client;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import uk.co.duelmonster.minersadvantage.common.services.input.ClientInputService;

/**
 * NeoForge client-side event handlers.
 *
 * Registers key mappings and polls them on the client tick.
 */
@EventBusSubscriber(modid = "minersadvantage", value = Dist.CLIENT)
public final class NeoForgeClientEvents {
    private static final Map<KeyBindings.ClientAction, KeyMapping> KEY_MAPPINGS = new EnumMap<>(KeyBindings.ClientAction.class);
    private static ClientInputService.ClientInputState inputState = ClientInputService.ClientInputState.defaults();

    private NeoForgeClientEvents() {
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        for (KeyBindings.KeyBindingSpec spec : KeyBindings.all()) {
            String translationKey = "key.minersadvantage." + spec.action().name().toLowerCase();
            InputConstants.Key key = parseKeyToken(spec.defaultKey());
            KeyMapping keyMapping = new KeyMapping(
                translationKey,
                key.getType(),
                key.getValue(),
                KeyMapping.Category.MISC
            );
            event.register(keyMapping);
            KEY_MAPPINGS.put(spec.action(), keyMapping);
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        List<KeyBindings.ClientAction> pressed = new ArrayList<>();
        for (Map.Entry<KeyBindings.ClientAction, KeyMapping> entry : KEY_MAPPINGS.entrySet()) {
            if (entry.getValue().consumeClick()) {
                pressed.add(entry.getKey());
            }
        }

        ClientInputService.ClientInputResult result = new ClientInputService().process(
            inputState,
            new HashSet<>(pressed),
            false
        );
        inputState = result.state();
    }

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
}
