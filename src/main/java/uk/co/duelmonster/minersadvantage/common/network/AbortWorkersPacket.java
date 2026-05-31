package uk.co.duelmonster.minersadvantage.common.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * AbortWorkersPacket keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public record AbortWorkersPacket(
    long playerId,
    String source
) implements CustomPacketPayload {
    public static final Type<AbortWorkersPacket> TYPE = createType();
    public static final StreamCodec<RegistryFriendlyByteBuf, AbortWorkersPacket> STREAM_CODEC = createStreamCodec();

    @Override
    /**
     * type exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * createType exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private static Type<AbortWorkersPacket> createType() {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        try {
            return payloadId("abort_workers");
        } catch (Throwable throwable) {
            return null;
        }
    }

    /**
     * createStreamCodec exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private static StreamCodec<RegistryFriendlyByteBuf, AbortWorkersPacket> createStreamCodec() {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        try {
            return StreamCodec.composite(
                ByteBufCodecs.VAR_LONG,
                AbortWorkersPacket::playerId,
                ByteBufCodecs.STRING_UTF8,
                AbortWorkersPacket::source,
                AbortWorkersPacket::new
            );
        } catch (Throwable throwable) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    /**
     * payloadId exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private static Type<AbortWorkersPacket> payloadId(String path) {
        return NetworkPayloadReflection.payloadId(path, "Unable to create payload id for abort_workers");
    }

}
