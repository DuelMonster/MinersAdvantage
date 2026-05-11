package uk.co.duelmonster.minersadvantage.client;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import uk.co.duelmonster.minersadvantage.common.MinersAdvantageCore;
import uk.co.duelmonster.minersadvantage.common.network.AbortWorkersPacket;
import uk.co.duelmonster.minersadvantage.common.network.ComponentTogglePacket;

@EventBusSubscriber(modid = "minersadvantage")
public final class NeoForgeNetworkEvents {
    private static MinersAdvantageCore core;

    private NeoForgeNetworkEvents() {
    }

    public static void initialize(MinersAdvantageCore minersAdvantageCore) {
        core = minersAdvantageCore;
    }

    @SubscribeEvent
    public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(ComponentTogglePacket.TYPE, ComponentTogglePacket.STREAM_CODEC, NeoForgeNetworkEvents::handleComponentTogglePacket);
        registrar.playToServer(AbortWorkersPacket.TYPE, AbortWorkersPacket.STREAM_CODEC, NeoForgeNetworkEvents::handleAbortWorkersPacket);
    }

    private static void handleComponentTogglePacket(ComponentTogglePacket payload, IPayloadContext context) {
        core.handleComponentTogglePacket(payload);
    }

    private static void handleAbortWorkersPacket(AbortWorkersPacket payload, IPayloadContext context) {
        core.handleAbortPacket(payload);
    }
}
