package uk.co.duelmonster.minersadvantage.common.services.input;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import uk.co.duelmonster.minersadvantage.client.KeyBindings.ClientAction;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;
import uk.co.duelmonster.minersadvantage.common.network.ComponentTogglePacket;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeRegistry;

/**
 * ClientInputService keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class ClientInputService {
    /**
     * ClientInputState keeps this part of Miners Advantage running without turning server ticks into confetti.
     * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
     */
    public record ClientInputState(
        Map<FeatureId, Boolean> featureEnabled,
        boolean excavationToggled,
        boolean shaftVentToggled,
        int selectedExcavationShapeIndex,
        int selectedShaftanationShapeIndex
    ) {
        public ClientInputState {
            featureEnabled = Map.copyOf(featureEnabled);
            selectedExcavationShapeIndex = Math.max(0, selectedExcavationShapeIndex);
            selectedShaftanationShapeIndex = Math.max(0, selectedShaftanationShapeIndex);
        }

        /**
         * c li en ti np ut st at e exists so this path stays predictable and easier to debug when things get weird.
         */
        public ClientInputState(Map<FeatureId, Boolean> featureEnabled, boolean excavationToggled, boolean shaftVentToggled) {
            this(featureEnabled, excavationToggled, shaftVentToggled, 0, 0);
        }

        /**
         * defaults exists so this code path does one job clearly instead of spreading chaos across callers.
         * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
         */
        public static ClientInputState defaults() {
            EnumMap<FeatureId, Boolean> features = new EnumMap<>(FeatureId.class);
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            for (FeatureId feature : FeatureId.values()) {
                features.put(feature, true);
            }
            return new ClientInputState(features, false, false, 0, 0);
        }
    }

    /**
     * ClientInputResult keeps this part of Miners Advantage running without turning server ticks into confetti.
     * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
     */
    public record ClientInputResult(
        ClientInputState state,
        List<ComponentTogglePacket> togglePackets,
        boolean illuminatePlace,
        boolean illuminateArea,
        boolean abortRequested,
        boolean shouldSyncVariables,
        boolean shouldSyncConfig
    ) {}

    public ClientInputResult process(
        ClientInputState state,
        Set<ClientAction> pressedActions,
        boolean excavationToggleMode
    ) {
        EnumMap<FeatureId, Boolean> features = new EnumMap<>(state.featureEnabled());
        List<ComponentTogglePacket> packets = new ArrayList<>();

        toggleFeature(features, packets, FeatureId.CAPTIVATION, ClientAction.CAPTIVATION_TOGGLE, pressedActions);
        toggleFeature(features, packets, FeatureId.CROPINATION, ClientAction.CROPINATION_TOGGLE, pressedActions);
        toggleFeature(features, packets, FeatureId.EXCAVATION, ClientAction.EXCAVATION_TOGGLE, pressedActions);
        toggleFeature(features, packets, FeatureId.ILLUMINATION, ClientAction.ILLUMINATION_TOGGLE, pressedActions);
        toggleFeature(features, packets, FeatureId.LUMBINATION, ClientAction.LUMBINATION_TOGGLE, pressedActions);
        toggleFeature(features, packets, FeatureId.SHAFTANATION, ClientAction.SHAFTANATION_TOGGLE, pressedActions);
        toggleFeature(features, packets, FeatureId.SUBSTITUTION, ClientAction.SUBSTITUTION_TOGGLE, pressedActions);
        toggleFeature(features, packets, FeatureId.VEINATION, ClientAction.VEINATION_TOGGLE, pressedActions);

        boolean excavationEnabled = features.getOrDefault(FeatureId.EXCAVATION, false);
        boolean excavationToggled = state.excavationToggled();
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (excavationEnabled) {
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (!excavationToggleMode) {
                excavationToggled = pressedActions.contains(ClientAction.EXCAVATION_MODE_TOGGLE);
            } else if (pressedActions.contains(ClientAction.EXCAVATION_MODE_TOGGLE)) {
                excavationToggled = !excavationToggled;
            }
        } else {
            excavationToggled = false;
        }

        boolean shaftEnabled = features.getOrDefault(FeatureId.SHAFTANATION, false);
        boolean shaftVentToggled = shaftEnabled && pressedActions.contains(ClientAction.SHAFT_VENT_TOGGLE);
        boolean illuminationEnabled = features.getOrDefault(FeatureId.ILLUMINATION, false);

        int selectedExcavationShapeIndex = state.selectedExcavationShapeIndex();
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (excavationEnabled) {
            selectedExcavationShapeIndex = cycleIndex(
                selectedExcavationShapeIndex,
                MAShapeRegistry.forFeature(FeatureId.EXCAVATION).size(),
                pressedActions.contains(ClientAction.EXCAVATION_SHAPE_NEXT),
                pressedActions.contains(ClientAction.EXCAVATION_SHAPE_PREV)
            );
        }

        int selectedShaftanationShapeIndex = state.selectedShaftanationShapeIndex();
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (shaftEnabled) {
            selectedShaftanationShapeIndex = cycleIndex(
                selectedShaftanationShapeIndex,
                MAShapeRegistry.forFeature(FeatureId.SHAFTANATION).size(),
                pressedActions.contains(ClientAction.SHAFTANATION_SHAPE_NEXT),
                pressedActions.contains(ClientAction.SHAFTANATION_SHAPE_PREV)
            );
        }

        ClientInputState nextState = new ClientInputState(
            features,
            excavationToggled,
            shaftVentToggled,
            selectedExcavationShapeIndex,
            selectedShaftanationShapeIndex
        );
        return new ClientInputResult(
            nextState,
            List.copyOf(packets),
            illuminationEnabled && pressedActions.contains(ClientAction.ILLUMINATION_PLACE),
            illuminationEnabled && pressedActions.contains(ClientAction.ILLUMINATION_AREA),
            pressedActions.contains(ClientAction.ABORT_WORKERS),
            true,
            true
        );
    }

    private void toggleFeature(
        EnumMap<FeatureId, Boolean> features,
        List<ComponentTogglePacket> packets,
        FeatureId feature,
        ClientAction action,
        Set<ClientAction> pressedActions
    ) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (!pressedActions.contains(action)) {
            return;
        }
        boolean next = !features.getOrDefault(feature, false);
        features.put(feature, next);
        packets.add(new ComponentTogglePacket(feature, next));
    }

    /**
     * c yc le in de x exists so this path stays predictable and easier to debug when things get weird.
     */
    private int cycleIndex(int currentIndex, int size, boolean nextPressed, boolean prevPressed) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (size <= 0) {
            return 0;
        }
        int result = currentIndex;
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (nextPressed) {
            result = Math.floorMod(result + 1, size);
        }
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (prevPressed) {
            result = Math.floorMod(result - 1, size);
        }
        return result;
    }
}
