//? if neoforge {
package uk.co.duelmonster.minersadvantage.platform.neoforge;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import uk.co.duelmonster.minersadvantage.common.MinersAdvantageCore;
import uk.co.duelmonster.minersadvantage.common.network.AbortWorkersPacket;
import uk.co.duelmonster.minersadvantage.common.network.ComponentTogglePacket;
import uk.co.duelmonster.minersadvantage.common.network.FeatureDispatchPacket;
import uk.co.duelmonster.minersadvantage.common.network.IlluminationActionPacket;
import uk.co.duelmonster.minersadvantage.common.network.PlayerStateSyncPacket;
import uk.co.duelmonster.minersadvantage.common.network.SupremeVantagePacket;

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
    registrar.playToServer(ComponentTogglePacket.TYPE, ComponentTogglePacket.STREAM_CODEC,
        NeoForgeNetworkEvents::handleComponentTogglePacket);
    registrar.playToServer(AbortWorkersPacket.TYPE, AbortWorkersPacket.STREAM_CODEC,
        NeoForgeNetworkEvents::handleAbortWorkersPacket);
    registrar.playToServer(PlayerStateSyncPacket.TYPE, PlayerStateSyncPacket.STREAM_CODEC,
        NeoForgeNetworkEvents::handlePlayerStateSyncPacket);
    registrar.playToServer(FeatureDispatchPacket.TYPE, FeatureDispatchPacket.STREAM_CODEC,
        NeoForgeNetworkEvents::handleFeatureDispatchPacket);
    registrar.playToServer(IlluminationActionPacket.TYPE, IlluminationActionPacket.STREAM_CODEC,
        NeoForgeNetworkEvents::handleIlluminationActionPacket);
    registrar.playToServer(SupremeVantagePacket.TYPE, SupremeVantagePacket.STREAM_CODEC,
        NeoForgeNetworkEvents::handleSupremeVantagePacket);
  }

    private static void handleComponentTogglePacket(ComponentTogglePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() != null) {
                core.handleComponentTogglePacket(packet);
            }
        });
    }

    private static void handleAbortWorkersPacket(AbortWorkersPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() != null) {
                core.handleAbortPacket(packet);
            }
        });
    }

    private static void handlePlayerStateSyncPacket(PlayerStateSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() != null) {
                core.handlePlayerStateSyncPacket(packet);
            }
        });
    }

    private static void handleFeatureDispatchPacket(FeatureDispatchPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() != null) {
                core.handleFeatureDispatchPacket(packet);
            }
        });
    }

    private static void handleIlluminationActionPacket(IlluminationActionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                core.handleIlluminationActionPacket(serverPlayer, packet);
            }
        });
    }

    private static void handleSupremeVantagePacket(SupremeVantagePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                core.handleSupremeVantagePacket(serverPlayer, packet);
            }
        });
    }
}
//?} else {
/*
// This class is NeoForge-only.
*/ //?}
