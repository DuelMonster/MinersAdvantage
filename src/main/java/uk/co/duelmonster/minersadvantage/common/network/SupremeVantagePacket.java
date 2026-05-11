package uk.co.duelmonster.minersadvantage.common.network;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SupremeVantagePacket(
    long playerId,
    String code
) implements CustomPacketPayload {
    public static final Type<SupremeVantagePacket> TYPE = payloadId("supreme_vantage");
    public static final StreamCodec<RegistryFriendlyByteBuf, SupremeVantagePacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_LONG,
        SupremeVantagePacket::playerId,
        ByteBufCodecs.STRING_UTF8,
        SupremeVantagePacket::code,
        SupremeVantagePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @SuppressWarnings("unchecked")
    private static Type<SupremeVantagePacket> payloadId(String path) {
        try {
            Object identifier = createIdentifier(path);
            Constructor<Type> constructor = (Constructor<Type>) Type.class.getConstructor(identifier.getClass());
            return (Type<SupremeVantagePacket>) constructor.newInstance(identifier);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to create payload id for supreme_vantage", exception);
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
