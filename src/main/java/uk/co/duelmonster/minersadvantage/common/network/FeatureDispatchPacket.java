package uk.co.duelmonster.minersadvantage.common.network;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;

public record FeatureDispatchPacket(
    FeatureId feature,
    int blockX,
    int blockY,
    int blockZ,
    String blockId,
    String toolId
) implements CustomPacketPayload {
    @SuppressWarnings("unchecked")
    private static final StreamCodec<RegistryFriendlyByteBuf, FeatureId> FEATURE_CODEC =
        (StreamCodec<RegistryFriendlyByteBuf, FeatureId>) (StreamCodec<?, FeatureId>) ByteBufCodecs.STRING_UTF8.map(FeatureId::valueOf, FeatureId::name);

    public static final Type<FeatureDispatchPacket> TYPE = payloadId("feature_dispatch");
    public static final StreamCodec<RegistryFriendlyByteBuf, FeatureDispatchPacket> STREAM_CODEC = StreamCodec.composite(
        FEATURE_CODEC,
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
        FeatureDispatchPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @SuppressWarnings("unchecked")
    private static Type<FeatureDispatchPacket> payloadId(String path) {
        try {
            Object identifier = createIdentifier(path);
            Constructor<Type> constructor = (Constructor<Type>) Type.class.getConstructor(identifier.getClass());
            return (Type<FeatureDispatchPacket>) constructor.newInstance(identifier);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to create payload id for feature_dispatch", exception);
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
