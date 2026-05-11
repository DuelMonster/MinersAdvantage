package uk.co.duelmonster.minersadvantage.common.network;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record AbortWorkersPacket(
    long playerId,
    String source
) implements CustomPacketPayload {
    public static final Type<AbortWorkersPacket> TYPE = payloadId("abort_workers");
    public static final StreamCodec<RegistryFriendlyByteBuf, AbortWorkersPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_LONG,
        AbortWorkersPacket::playerId,
        ByteBufCodecs.STRING_UTF8,
        AbortWorkersPacket::source,
        AbortWorkersPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @SuppressWarnings("unchecked")
    private static Type<AbortWorkersPacket> payloadId(String path) {
        try {
            Object identifier = createIdentifier(path);
            Constructor<Type> constructor = (Constructor<Type>) Type.class.getConstructor(identifier.getClass());
            return (Type<AbortWorkersPacket>) constructor.newInstance(identifier);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to create payload id for abort_workers", exception);
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
