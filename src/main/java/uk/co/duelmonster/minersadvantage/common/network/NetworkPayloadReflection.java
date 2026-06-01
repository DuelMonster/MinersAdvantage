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
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        try {
            Constructor<CustomPacketPayload.Type> constructor = singleArgumentTypeConstructor();
            Class<?> identifierClass = constructor.getParameterTypes()[0];
            Object identifier = createIdentifier(identifierClass, path);
            return (CustomPacketPayload.Type<T>) constructor.newInstance(identifier);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(errorMessage, exception);
        }
    }

    /**
     * createIdentifier exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    static Object createIdentifier(Class<?> identifierClass, String path) throws ReflectiveOperationException {
        ReflectiveOperationException lastException = null;

        // Prefer public constructors because signatures survive remapping and avoid accessibility issues.
        try {
            Constructor<?> constructor = identifierClass.getConstructor(String.class, String.class);
            return constructor.newInstance(NAMESPACE, path);
        } catch (ReflectiveOperationException exception) {
            lastException = exception;
        }

        try {
            Constructor<?> constructor = identifierClass.getConstructor(String.class);
            return constructor.newInstance(NAMESPACE + ":" + path);
        } catch (ReflectiveOperationException exception) {
            lastException = exception;
        }

        try {
            Constructor<?> constructor = identifierClass.getDeclaredConstructor(String.class, String.class);
            if (constructor.trySetAccessible()) {
                return constructor.newInstance(NAMESPACE, path);
            }
        } catch (ReflectiveOperationException exception) {
            lastException = exception;
        }

        try {
            Constructor<?> constructor = identifierClass.getDeclaredConstructor(String.class);
            if (constructor.trySetAccessible()) {
                return constructor.newInstance(NAMESPACE + ":" + path);
            }
        } catch (ReflectiveOperationException exception) {
            lastException = exception;
        }

        // Fallback to common static factories for environments that do keep named methods.
        for (String methodName : new String[]{"fromNamespaceAndPath", "create", "of", "parse", "tryParse"}) {
            try {
                Method factory = identifierClass.getMethod(methodName, String.class, String.class);
                Object value = factory.invoke(null, NAMESPACE, path);
                if (value != null) {
                    return value;
                }
            } catch (NoSuchMethodException ignored) {
                try {
                    Method singleArgFactory = identifierClass.getMethod(methodName, String.class);
                    Object value = singleArgFactory.invoke(null, NAMESPACE + ":" + path);
                    if (value != null) {
                        return value;
                    }
                } catch (ReflectiveOperationException innerException) {
                    lastException = innerException;
                }
            } catch (ReflectiveOperationException exception) {
                lastException = exception;
            }
        }

        throw new NoSuchMethodException("Unable to construct identifier for class " + identifierClass.getName() + " (last error: " + (lastException == null ? "none" : lastException.getMessage()) + ")");
    }

    @SuppressWarnings("unchecked")
    private static Constructor<CustomPacketPayload.Type> singleArgumentTypeConstructor() throws NoSuchMethodException {
        for (Constructor<?> constructor : CustomPacketPayload.Type.class.getConstructors()) {
            if (constructor.getParameterCount() == 1) {
                return (Constructor<CustomPacketPayload.Type>) constructor;
            }
        }

        for (Constructor<?> constructor : CustomPacketPayload.Type.class.getDeclaredConstructors()) {
            if (constructor.getParameterCount() == 1 && constructor.trySetAccessible()) {
                return (Constructor<CustomPacketPayload.Type>) constructor;
            }
        }

        throw new NoSuchMethodException("CustomPacketPayload.Type single-argument constructor not found");
    }
}
