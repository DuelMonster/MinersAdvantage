package uk.co.duelmonster.minersadvantage.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import java.lang.reflect.Method;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import uk.co.duelmonster.minersadvantage.common.network.AbortWorkersPacket;
import uk.co.duelmonster.minersadvantage.common.network.ComponentTogglePacket;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;
import uk.co.duelmonster.minersadvantage.common.services.input.ClientInputService;

/**
 * NeoForge client-side event handlers.
 *
 * Registers key mappings and polls them on the client tick.
 */
@EventBusSubscriber(modid = "minersadvantage", value = Dist.CLIENT)
/**
 * NeoForgeClientEvents keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class NeoForgeClientEvents {
    private static final Map<KeyBindings.ClientAction, KeyMapping> KEY_MAPPINGS = new EnumMap<>(KeyBindings.ClientAction.class);
    private static final KeyMapping.Category KEY_CATEGORY = ClientActionInputSupport.createKeyCategory();
    private static ClientInputService.ClientInputState inputState = ClientInputService.ClientInputState.defaults();
    private static ClientInputService.ClientInputState lastSyncedState = ClientInputService.ClientInputState.defaults();

    /**
     * NeoForgeClientEvents exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private NeoForgeClientEvents() {
    }

    @SubscribeEvent
    /**
     * onRegisterKeyMappings exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        for (KeyBindings.KeyBindingSpec spec : KeyBindings.all()) {
            String translationKey = "key.minersadvantage." + spec.action().name().toLowerCase();
            InputConstants.Key key = parseKeyToken(spec.defaultKey());
            KeyMapping keyMapping = ClientActionInputSupport.createKeyMapping(translationKey, key, KEY_CATEGORY);
            event.register(keyMapping);
            KEY_MAPPINGS.put(spec.action(), keyMapping);
        }
    }

    @SubscribeEvent
    /**
     * onClientTick exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public static void onClientTick(ClientTickEvent.Post event) {
        Set<KeyBindings.ClientAction> pressedSet = ClientActionInputSupport.collectPressedActions(KEY_MAPPINGS);

        ClientInputService.ClientInputResult result = new ClientInputService().process(
            inputState,
            pressedSet,
            false
        );
        inputState = result.state();

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        for (ComponentTogglePacket packet : result.togglePackets()) {
            ClientPacketDistributor.sendToServer(packet);
        }

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (result.abortRequested()) {
            long playerId = ClientActionInputSupport.resolveLocalPlayerId();
            ClientPacketDistributor.sendToServer(new AbortWorkersPacket(playerId, "client:keybind"));
        }

        boolean activationStateChanged = ClientActionInputSupport.hasActivationStateChanged(lastSyncedState, result.state());

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (activationStateChanged && (result.shouldSyncConfig() || result.shouldSyncVariables())) {
            syncStateToServer(result.state());
        }
    }

    @SubscribeEvent
    public static void onExtractBlockOutlineRenderState(ExtractBlockOutlineRenderStateEvent event) {
        double[] cameraPosition = extractCameraCoordinates(event.getCamera());
        event.addCustomRenderer((blockOutlineRenderState, bufferSource, poseStack, translucentPass, levelRenderState) -> {
            ShapePreviewRenderer.renderHeldPreview(
                inputState,
                ensurePoseStack(poseStack),
                cameraPosition[0],
                cameraPosition[1],
                cameraPosition[2]
            );
            return false;
        });
    }

    public static ClientInputService.ClientInputState getInputState() {
        return inputState;
    }

    /**
     * Human-friendly guardrail: o nm ou se sc ro ll exists so this path stays predictable and easier to debug when things get weird.
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
        inputState = scrollResult.state();
        syncStateToServer(inputState);
        return true;
    }

    /**
     * Human-friendly guardrail: s yn cs ta te to se rv er exists so this path stays predictable and easier to debug when things get weird.
     */
    private static void syncStateToServer(ClientInputService.ClientInputState state) {
        ClientPacketDistributor.sendToServer(ClientActionInputSupport.createPlayerStateSyncPacket(state));
        lastSyncedState = state;
    }

    /**
     * parseKeyToken exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private static InputConstants.Key parseKeyToken(String token) {
        return ClientActionInputSupport.parseKeyToken(token);
    }

    private static PoseStack ensurePoseStack(PoseStack poseStack) {
        return poseStack == null ? new PoseStack() : poseStack;
    }

    private static double[] extractCameraCoordinates(Object camera) {
        if (camera == null) {
            return new double[]{0.0d, 0.0d, 0.0d};
        }

        try {
            Method getX = camera.getClass().getMethod("getX");
            Method getY = camera.getClass().getMethod("getY");
            Method getZ = camera.getClass().getMethod("getZ");
            return new double[]{
                ((Number) getX.invoke(camera)).doubleValue(),
                ((Number) getY.invoke(camera)).doubleValue(),
                ((Number) getZ.invoke(camera)).doubleValue()
            };
        } catch (ReflectiveOperationException ignored) {
            // Fall through to position-object based extraction.
        }

        try {
            for (String positionMethodName : new String[]{"getPosition", "getPos", "position"}) {
                try {
                    Method positionMethod = camera.getClass().getMethod(positionMethodName);
                    Object position = positionMethod.invoke(camera);
                    if (position != null) {
                        return extractVectorCoordinates(position);
                    }
                } catch (NoSuchMethodException ignored) {
                    // Try next method name.
                }
            }
        } catch (ReflectiveOperationException ignored) {
            // Use safe fallback below.
        }

        return new double[]{0.0d, 0.0d, 0.0d};
    }

    private static double[] extractVectorCoordinates(Object vector) {
        try {
            Method xMethod = vector.getClass().getMethod("x");
            Method yMethod = vector.getClass().getMethod("y");
            Method zMethod = vector.getClass().getMethod("z");
            return new double[]{
                ((Number) xMethod.invoke(vector)).doubleValue(),
                ((Number) yMethod.invoke(vector)).doubleValue(),
                ((Number) zMethod.invoke(vector)).doubleValue()
            };
        } catch (ReflectiveOperationException ignored) {
            // Fall through to fields.
        }

        try {
            return new double[]{
                ((Number) vector.getClass().getField("x").get(vector)).doubleValue(),
                ((Number) vector.getClass().getField("y").get(vector)).doubleValue(),
                ((Number) vector.getClass().getField("z").get(vector)).doubleValue()
            };
        } catch (ReflectiveOperationException ignored) {
            return new double[]{0.0d, 0.0d, 0.0d};
        }
    }
}
