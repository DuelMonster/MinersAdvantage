//? if fabric {
package uk.co.duelmonster.minersadvantage.client;

import java.lang.reflect.Method;
import java.util.Set;
import uk.co.duelmonster.minersadvantage.common.network.AbortWorkersPacket;
import uk.co.duelmonster.minersadvantage.common.network.ComponentTogglePacket;
import uk.co.duelmonster.minersadvantage.common.network.IlluminationActionPacket;
import uk.co.duelmonster.minersadvantage.common.services.input.ClientInputService;

//? if fabric {
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeBootstrap;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeRegistry;
//?} else {
/*
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
// import net.minecraft.client.KeyMapping;
*/ //?}

/**
 * Client input tick handler for Fabric.
     *
     * Called each client tick to:
     * 1. Register keybindings (Fabric-only)
     * 2. Poll keybindings for pressed actions
     * 3. Update client input state via ClientInputService.process()
     * 4. Send component toggle packets to server
     * 5. Send illumination and abort actions
     */
public final class ClientInputHandler {
    private static ClientInputService.ClientInputState inputState = ClientInputService.ClientInputState.defaults();
    private static ClientInputService.ClientInputState lastSyncedState = ClientInputService.ClientInputState.defaults();
    private static final KeyMapping.Category KEY_CATEGORY = createKeyCategory();
    //? if fabric {
    private static java.util.Map<KeyBindings.ClientAction, KeyMapping> keyMappings = new java.util.EnumMap<>(KeyBindings.ClientAction.class);
    //?} else {
    /*
    // NeoForge uses RegisterKeyMappingsEvent for registration
    */ //?}

    /**
     * ClientInputHandler exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private ClientInputHandler() {
    }

    /**
     * Convert token key names (KP_1, DELETE, F12, etc.) to Minecraft format.
     * Returns InputConstants.Key that can be used to construct KeyMapping.
     */
    private static InputConstants.Key parseKeyToken(String token) {
        // Why this exists: Map token names to Minecraft key format (future-you will thank present-you).
        return ClientActionInputSupport.parseKeyToken(token);
    }

    /**
     * registerKeyMapping exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private static void registerKeyMapping(KeyMapping keyMapping) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        try {
            Class<?> helperClass;
            Method registerMethod;
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            try {
                helperClass = Class.forName("net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper");
                registerMethod = helperClass.getMethod("registerKeyBinding", KeyMapping.class);
            } catch (ClassNotFoundException oldApiMissing) {
                helperClass = Class.forName("net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper");
                registerMethod = helperClass.getMethod("registerKeyMapping", KeyMapping.class);
            }
            registerMethod.invoke(null, keyMapping);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to register key mapping", exception);
        }
    }

    private static KeyMapping.Category createKeyCategory() {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        try {
            Method registerCategoryMethod = findCategoryFactoryMethod();
            if (registerCategoryMethod == null) {
                KeyMapping.Category fallback = resolveFallbackCategory();
                if (fallback != null) {
                    return fallback;
                }
                throw new NoSuchMethodException("KeyMapping.Category factory method not found");
            }

            Class<?> parameterType = registerCategoryMethod.getParameterTypes()[0];
            Object categoryId = createCategoryIdentifier(parameterType);
            return (KeyMapping.Category) registerCategoryMethod.invoke(null, categoryId);
        } catch (ReflectiveOperationException exception) {
            KeyMapping.Category fallback = resolveFallbackCategory();
            if (fallback != null) {
                return fallback;
            }
            throw new IllegalStateException("Unable to create key mapping category", exception);
        }
    }

    /**
     * Resolve a version-safe identifier object so key category registration works across mapping variants.
     */
    private static Object createCategoryIdentifier(Class<?> identifierType) throws ReflectiveOperationException {
        String namespace = uk.co.duelmonster.minersadvantage.ModCommon.MOD_ID;
        String path = "keybinds";

        if (identifierType == String.class) {
            // Some runtimes treat the String overload as a path under minecraft namespace.
            return namespace + "/" + path;
        }

        ReflectiveOperationException lastException = null;

        try {
            var constructor = identifierType.getDeclaredConstructor(String.class, String.class);
            constructor.setAccessible(true);
            return constructor.newInstance(namespace, path);
        } catch (ReflectiveOperationException exception) {
            lastException = exception;
        }

        try {
            var constructor = identifierType.getDeclaredConstructor(String.class);
            constructor.setAccessible(true);
            return constructor.newInstance(namespace + ":" + path);
        } catch (ReflectiveOperationException exception) {
            lastException = exception;
        }

        for (String methodName : new String[]{"fromNamespaceAndPath", "create", "of", "parse", "tryParse"}) {
            try {
                Method twoArgFactory = identifierType.getMethod(methodName, String.class, String.class);
                Object value = twoArgFactory.invoke(null, namespace, path);
                if (value != null) {
                    return value;
                }
            } catch (NoSuchMethodException ignored) {
                try {
                    Method oneArgFactory = identifierType.getMethod(methodName, String.class);
                    Object value = oneArgFactory.invoke(null, namespace + ":" + path);
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

        throw new IllegalStateException(
            "Unable to create key category identifier for type " + identifierType.getName(),
            lastException
        );
    }

    private static Method findCategoryFactoryMethod() {
        for (Method method : KeyMapping.Category.class.getDeclaredMethods()) {
            if (java.lang.reflect.Modifier.isStatic(method.getModifiers())
                && method.getParameterCount() == 1
                && method.getReturnType() == KeyMapping.Category.class) {
                method.setAccessible(true);
                return method;
            }
        }
        return null;
    }

    private static KeyMapping.Category resolveFallbackCategory() {
        try {
            for (java.lang.reflect.Field field : KeyMapping.class.getDeclaredFields()) {
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers())
                    && field.getType() == KeyMapping.Category.class) {
                    field.setAccessible(true);
                    Object value = field.get(null);
                    if (value instanceof KeyMapping.Category category) {
                        return category;
                    }
                }
            }

            for (java.lang.reflect.Field field : KeyMapping.Category.class.getDeclaredFields()) {
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers())
                    && field.getType() == KeyMapping.Category.class) {
                    field.setAccessible(true);
                    Object value = field.get(null);
                    if (value instanceof KeyMapping.Category category) {
                        return category;
                    }
                }
            }
        } catch (ReflectiveOperationException ignored) {
            // Best-effort fallback only.
        }
        return null;
    }

    //? if fabric {
    /**
     * Register keybindings on Fabric client startup.
     * Called from FabricClientEntrypoint.onInitializeClient().
     */
    public static void registerKeybindings() {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        for (KeyBindings.KeyBindingSpec spec : KeyBindings.all()) {
            String translationKey = "key." + uk.co.duelmonster.minersadvantage.ModCommon.MOD_ID + "." + spec.action().name().toLowerCase();

            InputConstants.Key key = parseKeyToken(spec.defaultKey());
            KeyMapping keyMapping = ClientActionInputSupport.createKeyMapping(translationKey, key, KEY_CATEGORY);
            registerKeyMapping(keyMapping);
            keyMappings.put(spec.action(), keyMapping);
        }
    }

    /**
     * Collect currently pressed keybindings as a set of actions (Fabric).
     */
    private static Set<KeyBindings.ClientAction> getPressedActions() {
        return ClientActionInputSupport.collectPressedActions(keyMappings);
    }
    //?} else {
    /*
    // NeoForge keybinding registration and polling are implemented in NeoForgeClientEvents.
    */ //?}

    //? if fabric {
    /**
     * Called each client tick. Consumes key presses and sends packets.
     */
    public static void tick() {
        // Why this exists: Collect currently pressed keybindings (future-you will thank present-you).
        Set<KeyBindings.ClientAction> pressedSet = getPressedActions();

        // Why this exists: Process input state machine (future-you will thank present-you).
        // Why this exists: Hold-mode remains the default here until a dedicated local toggle setting is introduced. (future-you will thank present-you).
        boolean excavationToggleMode = false;
        ClientInputService.ClientInputState previousState = inputState;
        ClientInputService.ClientInputResult result = new ClientInputService().process(inputState, pressedSet, excavationToggleMode);

        // Why this exists: Update state (future-you will thank present-you).
        inputState = result.state();
        showShapeHudIfChangedOrActivated(previousState, inputState);

        // Why this exists: Send component toggle packets to the server. (future-you will thank present-you).
        for (ComponentTogglePacket packet : result.togglePackets()) {
            ClientPlayNetworking.send(packet);
        }

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (result.illuminatePlace()) {
            sendIlluminationAction(false);
        }
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (result.illuminateArea()) {
            sendIlluminationAction(true);
        }

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (result.abortRequested()) {
            long playerId = ClientActionInputSupport.resolveLocalPlayerId();
            ClientPlayNetworking.send(new AbortWorkersPacket(playerId, "client:keybind"));
        }

        boolean activationStateChanged = ClientActionInputSupport.hasActivationStateChanged(lastSyncedState, result.state());

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (activationStateChanged && (result.shouldSyncConfig() || result.shouldSyncVariables())) {
            syncStateToServer(result.state());
        }
    }

    public static ClientInputService.ClientInputState getInputState() {
        return inputState;
    }

    /**
     * o nm ou se sc ro ll exists so this path stays predictable and easier to debug when things get weird.
     */
    public static boolean onMouseScroll(double scrollY) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (scrollY == 0.0d) {
            return false;
        }

        Set<KeyBindings.ClientAction> actions = ClientActionInputSupport.collectScrollActions(inputState, scrollY);
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (actions.isEmpty()) {
            return false;
        }

        ClientInputService.ClientInputResult scrollResult = new ClientInputService().process(inputState, actions, false);
        ClientInputService.ClientInputState previousState = inputState;
        inputState = scrollResult.state();
        showShapeHudIfChangedOrActivated(previousState, inputState);
        syncStateToServer(inputState);
        return true;
    }

    /**
     * s ho ws ha pe hu di fc ha ng ed or ac ti va te d exists so this path stays predictable and easier to debug when things get weird.
     */
    private static void showShapeHudIfChangedOrActivated(ClientInputService.ClientInputState previous, ClientInputService.ClientInputState current) {
        boolean excavationChanged = previous.selectedExcavationShapeIndex() != current.selectedExcavationShapeIndex();
        boolean shaftChanged = previous.selectedShaftanationShapeIndex() != current.selectedShaftanationShapeIndex();
        boolean excavationActivated = !previous.excavationToggled() && current.excavationToggled();
        boolean shaftActivated = !previous.shaftVentToggled() && current.shaftVentToggled();
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (!excavationChanged && !shaftChanged && !excavationActivated && !shaftActivated) {
            return;
        }

        MAShapeBootstrap.ensureInitialized();
        StringBuilder message = new StringBuilder();
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (excavationChanged || excavationActivated) {
            String excavationName = MAShapeRegistry.byIndex(FeatureId.EXCAVATION, current.selectedExcavationShapeIndex())
                .map(shape -> shape.displayName())
                .orElse("#" + current.selectedExcavationShapeIndex());
            message.append("Excavation Shape: ").append(excavationName);
        }
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (shaftChanged || shaftActivated) {
            String shaftName = MAShapeRegistry.byIndex(FeatureId.SHAFTANATION, current.selectedShaftanationShapeIndex())
                .map(shape -> shape.displayName())
                .orElse("#" + current.selectedShaftanationShapeIndex());
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (!message.isEmpty()) {
                message.append(" | ");
            }
            message.append("Shaft Shape: ").append(shaftName);
        }

        Minecraft minecraft = Minecraft.getInstance();
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (minecraft.gui != null) {
            minecraft.gui.setOverlayMessage(Component.literal(message.toString()), false);
        }
    }

    /**
     * s yn cs ta te to se rv er exists so this path stays predictable and easier to debug when things get weird.
     */
    private static void syncStateToServer(ClientInputService.ClientInputState state) {
        ClientPlayNetworking.send(ClientActionInputSupport.createPlayerStateSyncPacket(state));
        lastSyncedState = state;
    }

    /**
     * s en di ll um in at io na ct io n exists so this path stays predictable and easier to debug when things get weird.
     */
    private static void sendIlluminationAction(boolean area) {
        Minecraft client = Minecraft.getInstance();
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (!(client.hitResult instanceof BlockHitResult blockHit)) {
            return;
        }

        var hitFace = blockHit.getDirection();
        var pos = area ? blockHit.getBlockPos() : blockHit.getBlockPos().relative(hitFace);
        ClientPlayNetworking.send(new IlluminationActionPacket(pos.getX(), pos.getY(), pos.getZ(), area, hitFace));
    }
    //?} else {
    /*
    // NeoForge tick handler is in NeoForgeClientEvents
    */ //?}
}
//?} else {
/*
// This handler is Fabric-only. NeoForge uses ForgeClientInputHandler.
*/ //?}
