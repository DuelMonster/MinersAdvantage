package uk.co.duelmonster.minersadvantage.client;

import java.lang.reflect.Method;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import uk.co.duelmonster.minersadvantage.common.MinersAdvantageCore;
import uk.co.duelmonster.minersadvantage.common.network.AbortWorkersPacket;
import uk.co.duelmonster.minersadvantage.common.network.ComponentTogglePacket;
import uk.co.duelmonster.minersadvantage.common.network.FeatureDispatchPacket;
import uk.co.duelmonster.minersadvantage.common.network.PlayerStateSyncPacket;
import uk.co.duelmonster.minersadvantage.common.network.SupremeVantagePacket;

public final class FabricNetworkEvents {
    private static boolean payloadTypesRegistered;
    private static boolean serverHandlersRegistered;
    private static MinersAdvantageCore core;

    private FabricNetworkEvents() {
    }

    public static void initialize(MinersAdvantageCore minersAdvantageCore) {
        core = minersAdvantageCore;
    }

    public static void registerPayloadTypes() {
        if (payloadTypesRegistered) {
            return;
        }
        registerPlayToServer(ComponentTogglePacket.TYPE, ComponentTogglePacket.STREAM_CODEC);
        registerPlayToServer(AbortWorkersPacket.TYPE, AbortWorkersPacket.STREAM_CODEC);
        registerPlayToServer(PlayerStateSyncPacket.TYPE, PlayerStateSyncPacket.STREAM_CODEC);
        registerPlayToServer(FeatureDispatchPacket.TYPE, FeatureDispatchPacket.STREAM_CODEC);
        registerPlayToServer(SupremeVantagePacket.TYPE, SupremeVantagePacket.STREAM_CODEC);
        payloadTypesRegistered = true;
    }

    private static <T extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> void registerPlayToServer(net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<T> type, Object codec) {
        try {
            Method registryMethod;
            try {
                registryMethod = PayloadTypeRegistry.class.getMethod("playC2S");
            } catch (NoSuchMethodException missingModernApi) {
                registryMethod = PayloadTypeRegistry.class.getMethod("serverboundPlay");
            }
            Object registry = registryMethod.invoke(null);
            Method registerMethod = null;
            for (Method candidate : registry.getClass().getMethods()) {
                if (candidate.getName().equals("register") && candidate.getParameterCount() == 2) {
                    registerMethod = candidate;
                    break;
                }
            }
            if (registerMethod == null) {
                throw new NoSuchMethodException("Payload registry register(type, codec) method not found");
            }
            registerMethod.invoke(registry, type, codec);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to register Fabric payload types", exception);
        }
    }

    public static void registerServerHandlers() {
        if (serverHandlersRegistered) {
            return;
        }
        ServerPlayNetworking.registerGlobalReceiver(ComponentTogglePacket.TYPE, (payload, context) -> context.server().execute(() -> core.handleComponentTogglePacket(payload)));
        ServerPlayNetworking.registerGlobalReceiver(AbortWorkersPacket.TYPE, (payload, context) -> context.server().execute(() -> core.handleAbortPacket(payload)));
        ServerPlayNetworking.registerGlobalReceiver(PlayerStateSyncPacket.TYPE, (payload, context) -> context.server().execute(() -> core.handlePlayerStateSyncPacket(payload)));
        ServerPlayNetworking.registerGlobalReceiver(FeatureDispatchPacket.TYPE, (payload, context) -> context.server().execute(() -> core.handleFeatureDispatchPacket(payload)));
        ServerPlayNetworking.registerGlobalReceiver(SupremeVantagePacket.TYPE, (payload, context) -> context.server().execute(() -> core.handleSupremeVantagePacket(payload)));
        serverHandlersRegistered = true;
    }
}
