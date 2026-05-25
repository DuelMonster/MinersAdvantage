package uk.co.duelmonster.minersadvantage.common.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * IlluminationActionPacket carries an explicit client illumination request to the server.
 */
public record IlluminationActionPacket(
    int blockX,
    int blockY,
    int blockZ,
    boolean area
) implements CustomPacketPayload {
    public static final Type<IlluminationActionPacket> TYPE = createType();
    public static final StreamCodec<RegistryFriendlyByteBuf, IlluminationActionPacket> STREAM_CODEC = createStreamCodec();

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private static Type<IlluminationActionPacket> createType() {
        try {
            return payloadId("illumination_action");
        } catch (Throwable throwable) {
            return null;
        }
    }

    private static StreamCodec<RegistryFriendlyByteBuf, IlluminationActionPacket> createStreamCodec() {
        try {
            return StreamCodec.composite(
                ByteBufCodecs.VAR_INT,
                IlluminationActionPacket::blockX,
                ByteBufCodecs.VAR_INT,
                IlluminationActionPacket::blockY,
                ByteBufCodecs.VAR_INT,
                IlluminationActionPacket::blockZ,
                ByteBufCodecs.BOOL,
                IlluminationActionPacket::area,
                IlluminationActionPacket::new
            );
        } catch (Throwable throwable) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static Type<IlluminationActionPacket> payloadId(String path) {
        return NetworkPayloadReflection.payloadId(path, "Unable to create payload id for illumination_action");
    }
}