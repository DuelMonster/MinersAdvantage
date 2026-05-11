package uk.co.duelmonster.minersadvantage.common.network;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

final class NetworkPayloadReflection {
    private static final String NAMESPACE = "minersadvantage";

    private NetworkPayloadReflection() {
    }

    @SuppressWarnings("unchecked")
    static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> payloadId(String path, String errorMessage) {
        try {
            Object identifier = createIdentifier(path);
            Constructor<CustomPacketPayload.Type> constructor =
                (Constructor<CustomPacketPayload.Type>) CustomPacketPayload.Type.class.getConstructor(identifier.getClass());
            return (CustomPacketPayload.Type<T>) constructor.newInstance(identifier);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(errorMessage, exception);
        }
    }

    static Object createIdentifier(String path) throws ReflectiveOperationException {
        try {
            Class<?> identifierClass = Class.forName("net.minecraft.resources.Identifier");
            Method factory = identifierClass.getMethod("fromNamespaceAndPath", String.class, String.class);
            return factory.invoke(null, NAMESPACE, path);
        } catch (ClassNotFoundException missingFabricIdentifier) {
            Class<?> resourceLocationClass = Class.forName("net.minecraft.resources.ResourceLocation");
            Method factory = resourceLocationClass.getMethod("fromNamespaceAndPath", String.class, String.class);
            return factory.invoke(null, NAMESPACE, path);
        }
    }
}