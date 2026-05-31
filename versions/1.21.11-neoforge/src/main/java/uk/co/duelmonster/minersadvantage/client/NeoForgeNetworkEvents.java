package uk.co.duelmonster.minersadvantage.client;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import uk.co.duelmonster.minersadvantage.common.MinersAdvantageCore;
import uk.co.duelmonster.minersadvantage.common.network.AbortWorkersPacket;
import uk.co.duelmonster.minersadvantage.common.network.ComponentTogglePacket;
import uk.co.duelmonster.minersadvantage.common.network.FeatureDispatchPacket;
import uk.co.duelmonster.minersadvantage.common.network.PlayerStateSyncPacket;
import uk.co.duelmonster.minersadvantage.common.network.SupremeVantagePacket;

@EventBusSubscriber(modid = "minersadvantage")
/**
 * NeoForgeNetworkEvents keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class NeoForgeNetworkEvents {
    private static MinersAdvantageCore core;

    /**
     * NeoForgeNetworkEvents exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private NeoForgeNetworkEvents() {
    }

    /**
     * initialize exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public static void initialize(MinersAdvantageCore minersAdvantageCore) {
        core = minersAdvantageCore;
    }

    @SubscribeEvent
    /**
     * onRegisterPayloadHandlers exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(ComponentTogglePacket.TYPE, ComponentTogglePacket.STREAM_CODEC, NeoForgeNetworkEvents::handleComponentTogglePacket);
        registrar.playToServer(AbortWorkersPacket.TYPE, AbortWorkersPacket.STREAM_CODEC, NeoForgeNetworkEvents::handleAbortWorkersPacket);
        registrar.playToServer(PlayerStateSyncPacket.TYPE, PlayerStateSyncPacket.STREAM_CODEC, NeoForgeNetworkEvents::handlePlayerStateSyncPacket);
        registrar.playToServer(FeatureDispatchPacket.TYPE, FeatureDispatchPacket.STREAM_CODEC, NeoForgeNetworkEvents::handleFeatureDispatchPacket);
        registrar.playToServer(SupremeVantagePacket.TYPE, SupremeVantagePacket.STREAM_CODEC, NeoForgeNetworkEvents::handleSupremeVantagePacket);
    }

    /**
     * handleComponentTogglePacket exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private static void handleComponentTogglePacket(ComponentTogglePacket payload, IPayloadContext context) {
        core.handleComponentTogglePacket(payload);
    }

    /**
     * handleAbortWorkersPacket exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private static void handleAbortWorkersPacket(AbortWorkersPacket payload, IPayloadContext context) {
        core.handleAbortPacket(payload);
    }

    /**
     * handlePlayerStateSyncPacket exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private static void handlePlayerStateSyncPacket(PlayerStateSyncPacket payload, IPayloadContext context) {
        core.handlePlayerStateSyncPacket(payload);
    }

    /**
     * handleFeatureDispatchPacket exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private static void handleFeatureDispatchPacket(FeatureDispatchPacket payload, IPayloadContext context) {
        core.handleFeatureDispatchPacket(payload);
    }

    /**
     * handleSupremeVantagePacket exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private static void handleSupremeVantagePacket(SupremeVantagePacket payload, IPayloadContext context) {
        core.handleSupremeVantagePacket(payload);
    }
}
