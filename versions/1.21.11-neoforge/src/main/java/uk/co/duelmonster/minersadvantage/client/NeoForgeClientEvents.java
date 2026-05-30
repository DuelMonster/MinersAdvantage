package uk.co.duelmonster.minersadvantage.client;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import uk.co.duelmonster.minersadvantage.common.config.MAClientRootConfig;
import uk.co.duelmonster.minersadvantage.common.config.MAServerRootConfig;
import uk.co.duelmonster.minersadvantage.common.network.AbortWorkersPacket;
import uk.co.duelmonster.minersadvantage.common.network.ComponentTogglePacket;
import uk.co.duelmonster.minersadvantage.common.network.PlayerStateSyncPacket;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;
import uk.co.duelmonster.minersadvantage.common.services.input.ClientInputService;

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
    private static final Map<KeyBindings.ClientAction, KeyMapping> KEY_MAPPINGS = new EnumMap<>(KeyBindings.ClientAction.class);
    private static ClientInputService.ClientInputState inputState = ClientInputService.ClientInputState.defaults();
    private static ClientInputService.ClientInputState lastSyncedState = ClientInputService.ClientInputState.defaults();

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
    /**
     * onClientTick exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public static void onClientTick(ClientTickEvent.Post event) {
        List<KeyBindings.ClientAction> pressed = new ArrayList<>();
        for (Map.Entry<KeyBindings.ClientAction, KeyMapping> entry : KEY_MAPPINGS.entrySet()) {
            boolean active = switch (entry.getKey()) {
                case EXCAVATION_MODE_TOGGLE, SHAFT_VENT_TOGGLE -> entry.getValue().isDown();
                default -> entry.getValue().consumeClick();
            };
            if (active) {
                pressed.add(entry.getKey());
            }
        }

        Set<KeyBindings.ClientAction> pressedSet = new HashSet<>(pressed);

        ClientInputService.ClientInputResult result = new ClientInputService().process(
            inputState,
            pressedSet,
            false
        );
        inputState = result.state();

        for (ComponentTogglePacket packet : result.togglePackets()) {
            ClientPacketDistributor.sendToServer(packet);
        }

        if (result.abortRequested()) {
            long playerId = 0L;
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null) {
                playerId = minecraft.player.getUUID().getLeastSignificantBits();
            }
            ClientPacketDistributor.sendToServer(new AbortWorkersPacket(playerId, "client:keybind"));
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
            && inputState.featureEnabled().getOrDefault(FeatureId.EXCAVATION, false);
        boolean shaftActive = inputState.shaftVentToggled()
            && inputState.featureEnabled().getOrDefault(FeatureId.SHAFTANATION, false);

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
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            playerId = minecraft.player.getUUID().getLeastSignificantBits();
        }
        ClientPacketDistributor.sendToServer(new PlayerStateSyncPacket(
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

    /**
     * parseKeyToken exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
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
}



