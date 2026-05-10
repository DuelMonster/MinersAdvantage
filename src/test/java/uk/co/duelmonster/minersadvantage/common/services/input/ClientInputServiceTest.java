package uk.co.duelmonster.minersadvantage.common.services.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;
import uk.co.duelmonster.minersadvantage.client.KeyBindings.ClientAction;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;

class ClientInputServiceTest {
    @Test
    void togglesFeaturesAndRequestsSync() {
        ClientInputService service = new ClientInputService();
        ClientInputService.ClientInputResult result = service.process(
            ClientInputService.ClientInputState.defaults(),
            Set.of(ClientAction.CAPTIVATION_TOGGLE, ClientAction.ABORT_WORKERS),
            false
        );

        assertFalse(result.state().featureEnabled().get(FeatureId.CAPTIVATION));
        assertEquals(1, result.togglePackets().size());
        assertTrue(result.abortRequested());
        assertTrue(result.shouldSyncVariables());
        assertTrue(result.shouldSyncConfig());
    }

    @Test
    void mirrorsHoldModeExcavationTogglesWhenToggleModeDisabled() {
        ClientInputService service = new ClientInputService();
        ClientInputService.ClientInputResult result = service.process(
            ClientInputService.ClientInputState.defaults(),
            Set.of(ClientAction.EXCAVATION_MODE_TOGGLE, ClientAction.EXCAVATION_SINGLE_LAYER_TOGGLE),
            false
        );

        assertTrue(result.state().excavationToggled());
        assertTrue(result.state().singleLayerToggled());
    }

    @Test
    void togglesExclusiveExcavationModesWhenToggleModeEnabled() {
        ClientInputService service = new ClientInputService();
        ClientInputService.ClientInputState initial = ClientInputService.ClientInputState.defaults();

        ClientInputService.ClientInputResult excavationResult = service.process(
            initial,
            Set.of(ClientAction.EXCAVATION_MODE_TOGGLE),
            true
        );
        assertTrue(excavationResult.state().excavationToggled());
        assertFalse(excavationResult.state().singleLayerToggled());

        ClientInputService.ClientInputResult layerResult = service.process(
            excavationResult.state(),
            Set.of(ClientAction.EXCAVATION_SINGLE_LAYER_TOGGLE),
            true
        );
        assertFalse(layerResult.state().excavationToggled());
        assertTrue(layerResult.state().singleLayerToggled());
    }

    @Test
    void gatesIlluminationActionsOnFeatureState() {
        ClientInputService service = new ClientInputService();
        ClientInputService.ClientInputState disabled = new ClientInputService.ClientInputState(
            java.util.Map.of(FeatureId.ILLUMINATION, false),
            false,
            false,
            false
        );

        ClientInputService.ClientInputResult disabledResult = service.process(
            disabled,
            Set.of(ClientAction.ILLUMINATION_PLACE, ClientAction.ILLUMINATION_AREA),
            false
        );
        assertFalse(disabledResult.illuminatePlace());
        assertFalse(disabledResult.illuminateArea());

        ClientInputService.ClientInputResult enabledResult = service.process(
            ClientInputService.ClientInputState.defaults(),
            Set.of(ClientAction.ILLUMINATION_PLACE, ClientAction.ILLUMINATION_AREA),
            false
        );
        assertTrue(enabledResult.illuminatePlace());
        assertTrue(enabledResult.illuminateArea());
    }

    @Test
    void mirrorsShaftVentHoldToggle() {
        ClientInputService service = new ClientInputService();
        ClientInputService.ClientInputResult result = service.process(
            ClientInputService.ClientInputState.defaults(),
            Set.of(ClientAction.SHAFT_VENT_TOGGLE),
            false
        );

        assertTrue(result.state().shaftVentToggled());
    }
}
