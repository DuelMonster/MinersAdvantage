package uk.co.duelmonster.minersadvantage.common.network;

import net.minecraft.core.Direction;
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
    boolean area,
    Direction faceDirection) implements CustomPacketPayload {
  public static final Type<IlluminationActionPacket> TYPE = createType();
  public static final StreamCodec<RegistryFriendlyByteBuf, IlluminationActionPacket> STREAM_CODEC = createStreamCodec();

  /**
   * Return the payload type key so networking can route this packet without guesswork.
   */
  @Override
  /**
   * Return the payload type key so networking can route this packet without guesswork.
   */
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  /**
   * c re at et yp e exists so this path stays predictable and easier to debug when things get weird.
   */
  private static Type<IlluminationActionPacket> createType() {
    try {
      return payloadId("illumination_action");
    } catch (Throwable throwable) {
      return null;
    }
  }

  /**
   * c re at es tr ea mc od ec exists so this path stays predictable and easier to debug when things get weird.
   */
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
          ByteBufCodecs.VAR_INT.map(i -> Direction.values()[i], Direction::ordinal),
          IlluminationActionPacket::faceDirection,
          IlluminationActionPacket::new);
    } catch (Throwable throwable) {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  /**
   * p ay lo ad id exists so this path stays predictable and easier to debug when things get weird.
   */
  private static Type<IlluminationActionPacket> payloadId(String path) {
    return NetworkPayloadReflection.payloadId(path, "Unable to create payload id for illumination_action");
  }
}
