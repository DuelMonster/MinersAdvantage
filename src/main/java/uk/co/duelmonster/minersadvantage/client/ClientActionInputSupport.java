package uk.co.duelmonster.minersadvantage.client;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import uk.co.duelmonster.minersadvantage.common.config.MAClientRootConfig;
import uk.co.duelmonster.minersadvantage.common.config.MAServerRootConfig;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;
import uk.co.duelmonster.minersadvantage.common.network.PlayerStateSyncPacket;
import uk.co.duelmonster.minersadvantage.common.services.input.ClientInputService;

public final class ClientActionInputSupport {
    private ClientActionInputSupport() {
    }

    public static Set<KeyBindings.ClientAction> collectPressedActions(Map<KeyBindings.ClientAction, KeyMapping> keyMappings) {
        Set<KeyBindings.ClientAction> pressed = new HashSet<>();
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

    public static long resolveLocalPlayerId() {
        long playerId = 0L;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            playerId = minecraft.player.getUUID().getLeastSignificantBits();
        }
        return playerId;
    }

    public static boolean hasActivationStateChanged(
        ClientInputService.ClientInputState previousState,
        ClientInputService.ClientInputState currentState
    ) {
        return previousState.excavationToggled() != currentState.excavationToggled()
            || previousState.shaftVentToggled() != currentState.shaftVentToggled()
            || previousState.selectedExcavationShapeIndex() != currentState.selectedExcavationShapeIndex()
            || previousState.selectedShaftanationShapeIndex() != currentState.selectedShaftanationShapeIndex();
    }

    public static Set<KeyBindings.ClientAction> collectScrollActions(
        ClientInputService.ClientInputState inputState,
        double scrollY
    ) {
        boolean excavationActive = inputState.excavationToggled()
            && inputState.featureEnabled().getOrDefault(FeatureId.EXCAVATION, false);
        boolean shaftActive = inputState.shaftVentToggled()
            && inputState.featureEnabled().getOrDefault(FeatureId.SHAFTANATION, false);
        if (!excavationActive && !shaftActive) {
            return Set.of();
        }

        Set<KeyBindings.ClientAction> actions = new HashSet<>();
        if (excavationActive) {
            actions.add(scrollY > 0.0d ? KeyBindings.ClientAction.EXCAVATION_SHAPE_PREV : KeyBindings.ClientAction.EXCAVATION_SHAPE_NEXT);
        }
        if (shaftActive) {
            actions.add(scrollY > 0.0d ? KeyBindings.ClientAction.SHAFTANATION_SHAPE_PREV : KeyBindings.ClientAction.SHAFTANATION_SHAPE_NEXT);
        }
        return actions;
    }

    public static InputConstants.Key parseKeyToken(String token) {
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

    public static KeyMapping createKeyMapping(String translationKey, InputConstants.Key key, KeyMapping.Category category) {
        return new KeyMapping(
            translationKey,
            key.getType(),
            key.getValue(),
            category
        );
    }

    public static PlayerStateSyncPacket createPlayerStateSyncPacket(ClientInputService.ClientInputState state) {
        return new PlayerStateSyncPacket(
            resolveLocalPlayerId(),
            MAClientRootConfig.defaults(),
            MAServerRootConfig.defaults(),
            state.excavationToggled(),
            state.shaftVentToggled(),
            state.selectedExcavationShapeIndex(),
            state.selectedShaftanationShapeIndex()
        );
    }
}