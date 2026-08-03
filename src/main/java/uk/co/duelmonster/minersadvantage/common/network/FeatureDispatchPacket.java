package uk.co.duelmonster.minersadvantage.common.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;

/**
 * FeatureDispatchPacket keeps this part of MinersAdvantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public record FeatureDispatchPacket(
    FeatureId feature,
    int blockX,
    int blockY,
    int blockZ,
    String blockId,
    String toolId) implements CustomPacketPayload {
  public static final Type<FeatureDispatchPacket> TYPE = createType();
  public static final StreamCodec<RegistryFriendlyByteBuf, FeatureDispatchPacket> STREAM_CODEC = createStreamCodec();

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
  private static Type<FeatureDispatchPacket> createType() {
    try {
      return payloadId("feature_dispatch");
    } catch (Throwable throwable) {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  /**
   * createStreamCodec exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  private static StreamCodec<RegistryFriendlyByteBuf, FeatureDispatchPacket> createStreamCodec() {
    try {
      StreamCodec<RegistryFriendlyByteBuf, FeatureId> featureCodec = (StreamCodec<RegistryFriendlyByteBuf, FeatureId>) (StreamCodec<?, FeatureId>) ByteBufCodecs.STRING_UTF8
          .map(FeatureId::valueOf, FeatureId::name);
      return StreamCodec.composite(
          featureCodec,
          FeatureDispatchPacket::feature,
          ByteBufCodecs.VAR_INT,
          FeatureDispatchPacket::blockX,
          ByteBufCodecs.VAR_INT,
          FeatureDispatchPacket::blockY,
          ByteBufCodecs.VAR_INT,
          FeatureDispatchPacket::blockZ,
          ByteBufCodecs.STRING_UTF8,
          FeatureDispatchPacket::blockId,
          ByteBufCodecs.STRING_UTF8,
          FeatureDispatchPacket::toolId,
          FeatureDispatchPacket::new);
    } catch (Throwable throwable) {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  /**
   * payloadId exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  private static Type<FeatureDispatchPacket> payloadId(String path) {
    return NetworkPayloadReflection.payloadId(path, "Unable to create payload id for feature_dispatch");
  }

}
