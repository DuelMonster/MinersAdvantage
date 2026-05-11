package uk.co.duelmonster.minersadvantage.common.network;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;

public record ComponentTogglePacket(
    FeatureId feature,
    boolean enabled
) implements CustomPacketPayload {
    @SuppressWarnings("unchecked")
    private static final StreamCodec<RegistryFriendlyByteBuf, FeatureId> FEATURE_CODEC =
        (StreamCodec<RegistryFriendlyByteBuf, FeatureId>) (StreamCodec<?, FeatureId>) ByteBufCodecs.STRING_UTF8.map(FeatureId::valueOf, FeatureId::name);

    public static final Type<ComponentTogglePacket> TYPE = payloadId("component_toggle");
    public static final StreamCodec<RegistryFriendlyByteBuf, ComponentTogglePacket> STREAM_CODEC = StreamCodec.composite(
        FEATURE_CODEC,
        ComponentTogglePacket::feature,
        ByteBufCodecs.BOOL,
        ComponentTogglePacket::enabled,
        ComponentTogglePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @SuppressWarnings("unchecked")
    private static Type<ComponentTogglePacket> payloadId(String path) {
        try {
            Object identifier = createIdentifier(path);
            Constructor<Type> constructor = (Constructor<Type>) Type.class.getConstructor(identifier.getClass());
            return (Type<ComponentTogglePacket>) constructor.newInstance(identifier);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to create payload id for component_toggle", exception);
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
