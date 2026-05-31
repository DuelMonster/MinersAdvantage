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
    public static Set<KeyBindings.ClientAction> collectPressedActions(Map<KeyBindings.ClientAction, KeyMapping> keyMappings) {
        Set<KeyBindings.ClientAction> pressed = new HashSet<>();
        // Toggle-style actions stay active while held; others consume click so one tap equals one action.
        for (Map.Entry<KeyBindings.ClientAction, KeyMapping> entry : keyMappings.entrySet()) {
            boolean active = switch (entry.getKey()) {
                case EXCAVATION_MODE_TOGGLE, SHAFT_VENT_TOGGLE -> entry.getValue().isDown();
                default -> entry.getValue().consumeClick();
            };
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (active) {
                pressed.add(entry.getKey());
            }
        }
        return pressed;
    }

    /**
     * Resolve local player id for packets; falls back to zero when player is not yet available.
     */
    public static long resolveLocalPlayerId() {
        long playerId = 0L;
        Minecraft minecraft = Minecraft.getInstance();
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
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
        ClientInputService.ClientInputState currentState
    ) {
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
        double scrollY
    ) {
        boolean excavationActive = inputState.excavationToggled()
            && inputState.featureEnabled().getOrDefault(FeatureId.EXCAVATION, false);
        boolean shaftActive = inputState.shaftVentToggled()
            && inputState.featureEnabled().getOrDefault(FeatureId.SHAFTANATION, false);
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (!excavationActive && !shaftActive) {
            return Set.of();
        }

        Set<KeyBindings.ClientAction> actions = new HashSet<>();
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (excavationActive) {
            actions.add(scrollY > 0.0d ? KeyBindings.ClientAction.EXCAVATION_SHAPE_PREV : KeyBindings.ClientAction.EXCAVATION_SHAPE_NEXT);
        }
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (shaftActive) {
            actions.add(scrollY > 0.0d ? KeyBindings.ClientAction.SHAFTANATION_SHAPE_PREV : KeyBindings.ClientAction.SHAFTANATION_SHAPE_NEXT);
        }
        return actions;
    }

    /**
     * Convert config key tokens into Minecraft key identifiers.
     */
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

    /**
     * Build a key mapping consistently across loaders so call sites stay tiny and predictable.
     */
    public static KeyMapping createKeyMapping(String translationKey, InputConstants.Key key, KeyMapping.Category category) {
        return new KeyMapping(
            translationKey,
            key.getType(),
            key.getValue(),
            category
        );
    }

    /**
     * Build client->server state sync packet using local defaults and current input toggles.
     */
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