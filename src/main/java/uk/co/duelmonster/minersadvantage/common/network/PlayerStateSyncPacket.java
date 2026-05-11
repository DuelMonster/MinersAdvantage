package uk.co.duelmonster.minersadvantage.common.network;

import com.google.gson.Gson;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import uk.co.duelmonster.minersadvantage.common.config.ServerOverridesConfig;
import uk.co.duelmonster.minersadvantage.common.config.SyncedClientConfig;

public record PlayerStateSyncPacket(
    long playerId,
    SyncedClientConfig clientConfig,
    SyncedClientConfig serverConfig,
    ServerOverridesConfig serverOverrides
) implements CustomPacketPayload {
    private static final Gson GSON = new Gson();

    public static final Type<PlayerStateSyncPacket> TYPE = createType();
    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerStateSyncPacket> STREAM_CODEC = createStreamCodec();

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private static Type<PlayerStateSyncPacket> createType() {
        try {
            return payloadId("player_state_sync");
        } catch (Throwable ignored) {
            return null;
        }
    }

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
                (playerId, clientJson, serverJson, overridesJson) -> new PlayerStateSyncPacket(
                    playerId,
                    GSON.fromJson(clientJson, SyncedClientConfig.class),
                    GSON.fromJson(serverJson, SyncedClientConfig.class),
                    GSON.fromJson(overridesJson, ServerOverridesConfig.class)
                )
            );
        } catch (Throwable ignored) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static Type<PlayerStateSyncPacket> payloadId(String path) {
        try {
            Object identifier = createIdentifier(path);
            Constructor<Type> constructor = (Constructor<Type>) Type.class.getConstructor(identifier.getClass());
            return (Type<PlayerStateSyncPacket>) constructor.newInstance(identifier);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to create payload id for player_state_sync", exception);
        }
    }

    private static Object createIdentifier(String path) throws ReflectiveOperationException {
        try {
            Class<?> identifierClass = Class.forName("net.minecraft.resources.Identifier");
            Method factory = identifierClass.getMethod("fromNamespaceAndPath", String.class, String.class);
            return factory.invoke(null, "minersadvantage", path);
        } catch (ClassNotFoundException missingFabricIdentifier) {
            Class<?> resourceLocationClass = Class.forName("net.minecraft.resources.ResourceLocation");
            Method factory = resourceLocationClass.getMethod("fromNamespaceAndPath", String.class, String.class);
            return factory.invoke(null, "minersadvantage", path);
        }
    }
}
