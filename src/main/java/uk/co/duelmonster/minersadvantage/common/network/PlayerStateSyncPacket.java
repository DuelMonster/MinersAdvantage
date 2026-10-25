package uk.co.duelmonster.minersadvantage.common.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import uk.co.duelmonster.minersadvantage.common.config.MAClientRootConfig;
import uk.co.duelmonster.minersadvantage.common.config.MAServerRootConfig;

/**
 * Player id and config roots remain for local compatibility; the wire codec sends only player-owned state.
 */
public record PlayerStateSyncPacket(
    long playerId,
    MAClientRootConfig clientConfig,
    MAServerRootConfig serverConfig,
    boolean excavationToggled,
    boolean shaftVentToggled,
    int selectedExcavationShapeIndex,
    int selectedShaftanationShapeIndex) implements CustomPacketPayload {
  public static final Type<PlayerStateSyncPacket> TYPE = createType();
  public static final StreamCodec<RegistryFriendlyByteBuf, PlayerStateSyncPacket> STREAM_CODEC = createStreamCodec();

  public PlayerStateSyncPacket(
      long playerId,
      MAClientRootConfig clientConfig,
      MAServerRootConfig serverConfig) {
    this(playerId, clientConfig, serverConfig, false, false, 0, 0);
  }

  public PlayerStateSyncPacket(
      long playerId,
      MAClientRootConfig clientConfig,
      MAServerRootConfig serverConfig,
      boolean excavationToggled,
      boolean shaftVentToggled) {
    this(playerId, clientConfig, serverConfig, excavationToggled, shaftVentToggled, 0, 0);
  }

  public PlayerStateSyncPacket(
      boolean excavationToggled,
      boolean shaftVentToggled,
      int selectedExcavationShapeIndex,
      int selectedShaftanationShapeIndex) {
    this(0L, MAClientRootConfig.defaults(), MAServerRootConfig.defaults(), excavationToggled, shaftVentToggled,
        selectedExcavationShapeIndex, selectedShaftanationShapeIndex);
  }

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
  private static Type<PlayerStateSyncPacket> createType() {
    try {
      return payloadId("player_state_sync");
    } catch (Throwable throwable) {
      return null;
    }
  }

  /**
   * createStreamCodec exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  private static StreamCodec<RegistryFriendlyByteBuf, PlayerStateSyncPacket> createStreamCodec() {
    try {
      return StreamCodec.composite(
          ByteBufCodecs.BOOL,
          PlayerStateSyncPacket::excavationToggled,
          ByteBufCodecs.BOOL,
          PlayerStateSyncPacket::shaftVentToggled,
          ByteBufCodecs.VAR_INT,
          PlayerStateSyncPacket::selectedExcavationShapeIndex,
          ByteBufCodecs.VAR_INT,
          PlayerStateSyncPacket::selectedShaftanationShapeIndex,
          PlayerStateSyncPacket::new);
    } catch (Throwable throwable) {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  /**
   * payloadId exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  private static Type<PlayerStateSyncPacket> payloadId(String path) {
    return NetworkPayloadReflection.payloadId(path, "Unable to create payload id for player_state_sync");
  }

}
