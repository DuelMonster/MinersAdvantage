package uk.co.duelmonster.minersadvantage.common.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * SupremeVantagePacket keeps this part of MinersAdvantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public record SupremeVantagePacket(
    long playerId,
    String code
) implements CustomPacketPayload {
    public static final Type<SupremeVantagePacket> TYPE = createType();
    public static final StreamCodec<RegistryFriendlyByteBuf, SupremeVantagePacket> STREAM_CODEC = createStreamCodec();

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
    private static Type<SupremeVantagePacket> createType() {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        try {
            return payloadId("supreme_vantage");
        } catch (Throwable throwable) {
            return null;
        }
    }

    /**
     * createStreamCodec exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private static StreamCodec<RegistryFriendlyByteBuf, SupremeVantagePacket> createStreamCodec() {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        try {
            return StreamCodec.composite(
                ByteBufCodecs.VAR_LONG,
                SupremeVantagePacket::playerId,
                ByteBufCodecs.STRING_UTF8,
                SupremeVantagePacket::code,
                SupremeVantagePacket::new
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
    private static Type<SupremeVantagePacket> payloadId(String path) {
        return NetworkPayloadReflection.payloadId(path, "Unable to create payload id for supreme_vantage");
    }

}
