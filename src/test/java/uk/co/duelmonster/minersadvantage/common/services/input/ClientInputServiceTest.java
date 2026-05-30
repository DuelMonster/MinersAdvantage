package uk.co.duelmonster.minersadvantage.common.services.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.co.duelmonster.minersadvantage.client.KeyBindings.ClientAction;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeBootstrap;

/**
 * ClientInputServiceTest keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
class ClientInputServiceTest {
    @BeforeAll
    static void bootstrapShapes() {
        MAShapeBootstrap.ensureInitialized();
    }

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
            Set.of(ClientAction.EXCAVATION_MODE_TOGGLE),
            false
        );

        assertTrue(result.state().excavationToggled());
    }

    @Test
    void togglesExcavationModeWhenToggleModeEnabled() {
        ClientInputService service = new ClientInputService();
        ClientInputService.ClientInputState initial = ClientInputService.ClientInputState.defaults();

        ClientInputService.ClientInputResult firstToggleResult = service.process(
            initial,
            Set.of(ClientAction.EXCAVATION_MODE_TOGGLE),
            true
        );
        assertTrue(firstToggleResult.state().excavationToggled());

        ClientInputService.ClientInputResult secondToggleResult = service.process(
            firstToggleResult.state(),
            Set.of(ClientAction.EXCAVATION_MODE_TOGGLE),
            true
        );
        assertFalse(secondToggleResult.state().excavationToggled());
    }

    @Test
    void gatesIlluminationActionsOnFeatureState() {
        ClientInputService service = new ClientInputService();
        ClientInputService.ClientInputState disabled = new ClientInputService.ClientInputState(
            java.util.Map.of(FeatureId.ILLUMINATION, false),
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

    @Test
    void cyclesExcavationShapeIndexWithWrapAround() {
        ClientInputService service = new ClientInputService();
        ClientInputService.ClientInputState initial = ClientInputService.ClientInputState.defaults();

        ClientInputService.ClientInputResult nextResult = service.process(
            initial,
            Set.of(ClientAction.EXCAVATION_SHAPE_NEXT),
            false
        );
        assertEquals(1, nextResult.state().selectedExcavationShapeIndex());

        ClientInputService.ClientInputState wrapStart = new ClientInputService.ClientInputState(
            nextResult.state().featureEnabled(),
            false,
            false,
            0,
            0
        );
        ClientInputService.ClientInputResult prevResult = service.process(
            wrapStart,
            Set.of(ClientAction.EXCAVATION_SHAPE_PREV),
            false
        );
        assertEquals(5, prevResult.state().selectedExcavationShapeIndex());
    }

    @Test
    void cyclesShaftShapeIndexWithWrapAround() {
        ClientInputService service = new ClientInputService();
        ClientInputService.ClientInputState initial = ClientInputService.ClientInputState.defaults();

        ClientInputService.ClientInputResult nextResult = service.process(
            initial,
            Set.of(ClientAction.SHAFTANATION_SHAPE_NEXT),
            false
        );
        assertEquals(1, nextResult.state().selectedShaftanationShapeIndex());

        ClientInputService.ClientInputState wrapStart = new ClientInputService.ClientInputState(
            nextResult.state().featureEnabled(),
            false,
            false,
            0,
            0
        );
        ClientInputService.ClientInputResult prevResult = service.process(
            wrapStart,
            Set.of(ClientAction.SHAFTANATION_SHAPE_PREV),
            false
        );
        assertEquals(2, prevResult.state().selectedShaftanationShapeIndex());
    }
}
