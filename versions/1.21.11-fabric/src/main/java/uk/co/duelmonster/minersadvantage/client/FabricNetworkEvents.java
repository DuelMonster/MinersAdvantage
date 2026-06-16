package uk.co.duelmonster.minersadvantage.client;

import java.lang.reflect.Method;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import uk.co.duelmonster.minersadvantage.common.MinersAdvantageCore;
import uk.co.duelmonster.minersadvantage.common.network.AbortWorkersPacket;
import uk.co.duelmonster.minersadvantage.common.network.ComponentTogglePacket;
import uk.co.duelmonster.minersadvantage.common.network.FeatureDispatchPacket;
import uk.co.duelmonster.minersadvantage.common.network.IlluminationActionPacket;
import uk.co.duelmonster.minersadvantage.common.network.PlayerStateSyncPacket;
import uk.co.duelmonster.minersadvantage.common.network.SupremeVantagePacket;

/**
 * FabricNetworkEvents keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class FabricNetworkEvents {
    private static boolean payloadTypesRegistered;
    private static boolean serverHandlersRegistered;
    private static MinersAdvantageCore core;

    /**
     * FabricNetworkEvents exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private FabricNetworkEvents() {
    }

    /**
     * initialize exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public static void initialize(MinersAdvantageCore minersAdvantageCore) {
        core = minersAdvantageCore;
    }

    /**
     * registerPayloadTypes exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public static void registerPayloadTypes() {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (payloadTypesRegistered) {
            return;
        }
        registerPlayToServer(ComponentTogglePacket.TYPE, ComponentTogglePacket.STREAM_CODEC);
        registerPlayToServer(AbortWorkersPacket.TYPE, AbortWorkersPacket.STREAM_CODEC);
        registerPlayToServer(PlayerStateSyncPacket.TYPE, PlayerStateSyncPacket.STREAM_CODEC);
        registerPlayToServer(FeatureDispatchPacket.TYPE, FeatureDispatchPacket.STREAM_CODEC);
        registerPlayToServer(IlluminationActionPacket.TYPE, IlluminationActionPacket.STREAM_CODEC);
        registerPlayToServer(SupremeVantagePacket.TYPE, SupremeVantagePacket.STREAM_CODEC);
        payloadTypesRegistered = true;
    }

    /**
     * registerPlayToServer exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private static <T extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> void registerPlayToServer(net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<T> type, Object codec) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        try {
            if (type == null) {
                throw new IllegalStateException("Cannot register Fabric payload type: packet TYPE is null");
            }
            if (codec == null) {
                throw new IllegalStateException("Cannot register Fabric payload type: packet STREAM_CODEC is null");
            }

            Method registryMethod;
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            try {
                registryMethod = PayloadTypeRegistry.class.getMethod("playC2S");
            } catch (NoSuchMethodException missingModernApi) {
                registryMethod = PayloadTypeRegistry.class.getMethod("serverboundPlay");
            }
            Object registry = registryMethod.invoke(null);
            Method registerMethod = null;
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            for (Method candidate : registry.getClass().getMethods()) {
                // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                if (candidate.getName().equals("register") && candidate.getParameterCount() == 2) {
                    registerMethod = candidate;
                    break;
                }
            }
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (registerMethod == null) {
                throw new NoSuchMethodException("Payload registry register(type, codec) method not found");
            }
            registerMethod.invoke(registry, type, codec);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to register Fabric payload types", exception);
        }
    }

    /**
     * registerServerHandlers exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public static void registerServerHandlers() {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (serverHandlersRegistered) {
            return;
        }
        ServerPlayNetworking.registerGlobalReceiver(ComponentTogglePacket.TYPE, (payload, context) -> context.server().execute(() -> core.handleComponentTogglePacket(payload)));
        ServerPlayNetworking.registerGlobalReceiver(AbortWorkersPacket.TYPE, (payload, context) -> context.server().execute(() -> core.handleAbortPacket(payload)));
        ServerPlayNetworking.registerGlobalReceiver(PlayerStateSyncPacket.TYPE, (payload, context) -> context.server().execute(() -> core.handlePlayerStateSyncPacket(payload)));
        ServerPlayNetworking.registerGlobalReceiver(FeatureDispatchPacket.TYPE, (payload, context) -> context.server().execute(() -> core.handleFeatureDispatchPacket(payload)));
        ServerPlayNetworking.registerGlobalReceiver(IlluminationActionPacket.TYPE, (payload, context) -> context.server().execute(() -> core.handleIlluminationActionPacket(context.player(), payload)));
        ServerPlayNetworking.registerGlobalReceiver(SupremeVantagePacket.TYPE, (payload, context) -> context.server().execute(() -> core.handleSupremeVantagePacket(context.player(), payload)));
        serverHandlersRegistered = true;
    }
}
