package uk.co.duelmonster.minersadvantage.client;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import uk.co.duelmonster.minersadvantage.common.network.AbortWorkersPacket;
import uk.co.duelmonster.minersadvantage.common.network.ComponentTogglePacket;
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
            KeyMapping keyMapping = ClientActionInputSupport.createKeyMapping(translationKey, key, KeyMapping.Category.MISC);
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
        Set<KeyBindings.ClientAction> pressedSet = ClientActionInputSupport.collectPressedActions(KEY_MAPPINGS);

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
            long playerId = ClientActionInputSupport.resolveLocalPlayerId();
            ClientPacketDistributor.sendToServer(new AbortWorkersPacket(playerId, "client:keybind"));
        }

        boolean activationStateChanged = ClientActionInputSupport.hasActivationStateChanged(lastSyncedState, result.state());

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

        Set<KeyBindings.ClientAction> actions = ClientActionInputSupport.collectScrollActions(inputState, scrollY);
        if (actions.isEmpty()) {
            return false;
        }

        ClientInputService.ClientInputResult scrollResult = new ClientInputService().process(inputState, actions, false);
        inputState = scrollResult.state();
        syncStateToServer(inputState);
        return true;
    }

    private static void syncStateToServer(ClientInputService.ClientInputState state) {
        ClientPacketDistributor.sendToServer(ClientActionInputSupport.createPlayerStateSyncPacket(state));
        lastSyncedState = state;
    }

    /**
     * parseKeyToken exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private static InputConstants.Key parseKeyToken(String token) {
        return ClientActionInputSupport.parseKeyToken(token);
    }
}
