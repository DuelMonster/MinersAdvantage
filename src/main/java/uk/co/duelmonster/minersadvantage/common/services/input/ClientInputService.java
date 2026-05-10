package uk.co.duelmonster.minersadvantage.common.services.input;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import uk.co.duelmonster.minersadvantage.client.KeyBindings.ClientAction;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;
import uk.co.duelmonster.minersadvantage.common.network.ComponentTogglePacket;

public final class ClientInputService {
    public record ClientInputState(
        Map<FeatureId, Boolean> featureEnabled,
        boolean excavationToggled,
        boolean singleLayerToggled,
        boolean shaftVentToggled
    ) {
        public ClientInputState {
            featureEnabled = Map.copyOf(featureEnabled);
        }

        public static ClientInputState defaults() {
            EnumMap<FeatureId, Boolean> features = new EnumMap<>(FeatureId.class);
            for (FeatureId feature : FeatureId.values()) {
                features.put(feature, true);
            }
            return new ClientInputState(features, false, false, false);
        }
    }

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
        boolean singleLayerToggled = state.singleLayerToggled();
        if (excavationEnabled) {
            if (!excavationToggleMode) {
                excavationToggled = pressedActions.contains(ClientAction.EXCAVATION_MODE_TOGGLE);
                singleLayerToggled = pressedActions.contains(ClientAction.EXCAVATION_SINGLE_LAYER_TOGGLE);
            } else if (pressedActions.contains(ClientAction.EXCAVATION_MODE_TOGGLE)) {
                if (singleLayerToggled) {
                    singleLayerToggled = false;
                }
                excavationToggled = !excavationToggled;
            } else if (pressedActions.contains(ClientAction.EXCAVATION_SINGLE_LAYER_TOGGLE)) {
                if (excavationToggled) {
                    excavationToggled = false;
                }
                singleLayerToggled = !singleLayerToggled;
            }
        } else {
            excavationToggled = false;
            singleLayerToggled = false;
        }

        boolean shaftEnabled = features.getOrDefault(FeatureId.SHAFTANATION, false);
        boolean shaftVentToggled = shaftEnabled && pressedActions.contains(ClientAction.SHAFT_VENT_TOGGLE);
        boolean illuminationEnabled = features.getOrDefault(FeatureId.ILLUMINATION, false);

        ClientInputState nextState = new ClientInputState(features, excavationToggled, singleLayerToggled, shaftVentToggled);
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
        if (!pressedActions.contains(action)) {
            return;
        }
        boolean next = !features.getOrDefault(feature, false);
        features.put(feature, next);
        packets.add(new ComponentTogglePacket(feature, next));
    }
}
