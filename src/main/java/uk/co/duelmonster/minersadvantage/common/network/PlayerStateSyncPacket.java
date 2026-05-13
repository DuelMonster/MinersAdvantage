package uk.co.duelmonster.minersadvantage.common.network;

import com.google.gson.Gson;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import uk.co.duelmonster.minersadvantage.common.config.ServerOverridesConfig;
import uk.co.duelmonster.minersadvantage.common.config.SyncedClientConfig;

/**
 * PlayerStateSyncPacket keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public record PlayerStateSyncPacket(
    long playerId,
    SyncedClientConfig clientConfig,
    SyncedClientConfig serverConfig,
    ServerOverridesConfig serverOverrides,
    boolean excavationToggled,
    boolean singleLayerToggled,
    boolean shaftVentToggled
) implements CustomPacketPayload {
    private static final Gson GSON = new Gson();

    public static final Type<PlayerStateSyncPacket> TYPE = createType();
    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerStateSyncPacket> STREAM_CODEC = createStreamCodec();

    public PlayerStateSyncPacket(
        long playerId,
        SyncedClientConfig clientConfig,
        SyncedClientConfig serverConfig,
        ServerOverridesConfig serverOverrides
    ) {
        this(playerId, clientConfig, serverConfig, serverOverrides, false, false, false);
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
                ByteBufCodecs.VAR_LONG,
                PlayerStateSyncPacket::playerId,
                ByteBufCodecs.STRING_UTF8,
                packet -> GSON.toJson(packet.clientConfig()),
                ByteBufCodecs.STRING_UTF8,
                packet -> GSON.toJson(packet.serverConfig()),
                ByteBufCodecs.STRING_UTF8,
                packet -> GSON.toJson(packet.serverOverrides()),
                ByteBufCodecs.BOOL,
                PlayerStateSyncPacket::excavationToggled,
                ByteBufCodecs.BOOL,
                PlayerStateSyncPacket::singleLayerToggled,
                ByteBufCodecs.BOOL,
                PlayerStateSyncPacket::shaftVentToggled,
                (playerId, clientJson, serverJson, overridesJson, excavationToggled, singleLayerToggled, shaftVentToggled) -> new PlayerStateSyncPacket(
                    playerId,
                    GSON.fromJson(clientJson, SyncedClientConfig.class),
                    GSON.fromJson(serverJson, SyncedClientConfig.class),
                    GSON.fromJson(overridesJson, ServerOverridesConfig.class),
                    excavationToggled,
                    singleLayerToggled,
                    shaftVentToggled
                )
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
    private static Type<PlayerStateSyncPacket> payloadId(String path) {
        return NetworkPayloadReflection.payloadId(path, "Unable to create payload id for player_state_sync");
    }

}
