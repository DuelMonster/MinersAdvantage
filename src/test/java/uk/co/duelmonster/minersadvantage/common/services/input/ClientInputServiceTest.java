package uk.co.duelmonster.minersadvantage.common.services.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.co.duelmonster.minersadvantage.client.KeyBindings.ClientAction;
import uk.co.duelmonster.minersadvantage.common.config.MAServerRootConfig;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeBootstrap;

/**
 * ClientInputServiceTest keeps this part of MinersAdvantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
class ClientInputServiceTest {
    @BeforeAll
    /**
     * Initialize shape registry used by input tests.
     */
    static void bootstrapShapes() {
        MAShapeBootstrap.ensureInitialized();
    }

    @Test
    /**
     * Verify toggles and sync flags are produced.
     */
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
    /**
     * Verify hold-mode excavation mirrors key press.
     */
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
    /**
     * Verify toggle-mode excavation flips on repeated presses.
     */
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
    /**
     * Verify illumination actions are gated by feature state.
     */
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
    /**
     * Verify shaft/vent hold toggle mirrors key state.
     */
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
    void appliesGuiFeatureEnablementToCurrentPlayerState() {
        ClientInputService service = new ClientInputService();
        MAServerRootConfig disabled = MAServerRootConfig.defaults()
            .withFeatureEnabled(FeatureId.SHAFTANATION, false);
        ClientInputService.ClientInputState state = new ClientInputService.ClientInputState(
            java.util.Map.of(FeatureId.SHAFTANATION, false),
            false,
            false);

        ClientInputService.FeatureConfigResult result = service.synchronizeFeatureSettings(
            state,
            disabled,
            MAServerRootConfig.defaults());

        assertTrue(result.state().featureEnabled().get(FeatureId.SHAFTANATION));
        assertEquals(1, result.togglePackets().size());
        assertEquals(FeatureId.SHAFTANATION, result.togglePackets().getFirst().feature());
        assertTrue(result.togglePackets().getFirst().enabled());
    }

    @Test
    void disablingShaftFromConfigClearsItsActiveHoldWithoutChangingOtherFeaturePreferences() {
        ClientInputService service = new ClientInputService();
        MAServerRootConfig enabled = MAServerRootConfig.defaults();
        MAServerRootConfig disabled = enabled.withFeatureEnabled(FeatureId.SHAFTANATION, false);
        ClientInputService.ClientInputState state = new ClientInputService.ClientInputState(
            java.util.Map.of(FeatureId.SHAFTANATION, true, FeatureId.EXCAVATION, false),
            false,
            true);

        ClientInputService.FeatureConfigResult result = service.synchronizeFeatureSettings(
            state,
            enabled,
            disabled);

        assertFalse(result.state().featureEnabled().get(FeatureId.SHAFTANATION));
        assertFalse(result.state().shaftVentToggled());
        assertFalse(result.state().featureEnabled().get(FeatureId.EXCAVATION));
        assertEquals(1, result.togglePackets().size());
        assertFalse(result.togglePackets().getFirst().enabled());
    }

    @Test
    /**
     * Verify excavation shape selection wraps around valid range.
     */
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
        assertEquals(6, prevResult.state().selectedExcavationShapeIndex());
    }

    @Test
    /**
     * Verify shaft shape selection wraps around valid range.
     */
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
