package uk.co.duelmonster.minersadvantage.client;

import java.lang.reflect.Method;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import uk.co.duelmonster.minersadvantage.common.MinersAdvantageCore;
import uk.co.duelmonster.minersadvantage.common.network.AbortWorkersPacket;
import uk.co.duelmonster.minersadvantage.common.network.ComponentTogglePacket;

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
            Method registerMethod = registry.getClass().getMethod("register", type.getClass(), codec.getClass());
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
        serverHandlersRegistered = true;
    }
}
