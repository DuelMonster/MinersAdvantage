package uk.co.duelmonster.minersadvantage.common.network;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * NetworkPayloadReflection is the teammate that keeps this part of the mod understandable and stable.
 * It exists so behavior stays explicit instead of becoming mystery spaghetti at 2 AM.
 */
final class NetworkPayloadReflection {
    private static final String NAMESPACE = "minersadvantage";

    /**
     * NetworkPayloadReflection exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    private NetworkPayloadReflection() {
    }

    @SuppressWarnings("unchecked")
    /**
     * payloadId exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
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

    /**
     * createIdentifier exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
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


