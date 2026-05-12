package uk.co.duelmonster.minersadvantage.common.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;

/**
 * ComponentTogglePacket keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public record ComponentTogglePacket(
    FeatureId feature,
    boolean enabled
) implements CustomPacketPayload {
    public static final Type<ComponentTogglePacket> TYPE = createType();
    public static final StreamCodec<RegistryFriendlyByteBuf, ComponentTogglePacket> STREAM_CODEC = createStreamCodec();

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
    private static Type<ComponentTogglePacket> createType() {
        try {
            return payloadId("component_toggle");
        } catch (Throwable throwable) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    /**
     * createStreamCodec exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private static StreamCodec<RegistryFriendlyByteBuf, ComponentTogglePacket> createStreamCodec() {
        try {
            StreamCodec<RegistryFriendlyByteBuf, FeatureId> featureCodec =
                (StreamCodec<RegistryFriendlyByteBuf, FeatureId>) (StreamCodec<?, FeatureId>) ByteBufCodecs.STRING_UTF8.map(FeatureId::valueOf, FeatureId::name);
            return StreamCodec.composite(
                featureCodec,
                ComponentTogglePacket::feature,
                ByteBufCodecs.BOOL,
                ComponentTogglePacket::enabled,
                ComponentTogglePacket::new
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
    private static Type<ComponentTogglePacket> payloadId(String path) {
        return NetworkPayloadReflection.payloadId(path, "Unable to create payload id for component_toggle");
    }

}




