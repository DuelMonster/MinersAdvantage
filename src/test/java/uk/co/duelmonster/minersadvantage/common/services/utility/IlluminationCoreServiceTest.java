package uk.co.duelmonster.minersadvantage.common.services.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import uk.co.duelmonster.minersadvantage.testutil.TestRuntimeAssumptions;

/**
 * IlluminationCoreServiceTest keeps this part of MinersAdvantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
class IlluminationCoreServiceTest {
    private static void assumeBlockRegistries() {
        Assumptions.assumeTrue(TestRuntimeAssumptions.canInitializeBlockRegistries());
    }

    @Test
    /**
     * Verify dark-threshold placement gate behavior.
     */
    void determinesLightLevelThreshold() {
        IlluminationCoreService service = new IlluminationCoreService();
        assertTrue(service.shouldPlaceTorch(0));
        assertTrue(service.shouldPlaceTorch(7));
        assertFalse(service.shouldPlaceTorch(8));
        assertFalse(service.shouldPlaceTorch(15));
    }

    @Test
    /**
     * Verify wall/floor placement strategy selection.
     */
    void selectsBestTorchPlacement() {
        IlluminationCoreService service = new IlluminationCoreService();
        assertEquals(TorchPlacement.BOTH_WALLS, service.selectPlacement(true, true));
        assertEquals(TorchPlacement.LEFT_WALL, service.selectPlacement(true, false));
        assertEquals(TorchPlacement.RIGHT_WALL, service.selectPlacement(false, true));
        assertEquals(TorchPlacement.FLOOR, service.selectPlacement(false, false));
    }

    @Test
    /**
     * Verify planned placement count estimation.
     */
    void estimatesPlacementsInRadius() {
        IlluminationCoreService service = new IlluminationCoreService();
        assertEquals(9, service.expectedPlacementsInRadius(1, 1));
        assertEquals(25, service.expectedPlacementsInRadius(2, 2));
    }

    @Test
    /**
     * Verify decision payload for dark areas.
     */
    void createsDecisionForDarkAreas() {
        assumeBlockRegistries();
        IlluminationCoreService service = new IlluminationCoreService();
        IlluminationCoreService.IlluminationDecision decision =
            service.decidePlacement(4, true, false, 2, 1, "torch_manual_left");

        assertTrue(decision.placeNow());
        assertEquals(TorchPlacement.LEFT_WALL, decision.placement());
        assertTrue(decision.plannedTorches() > 0);
        assertTrue(decision.manualMode());
    }

    @Test
    /**
     * Verify no placement decision in bright conditions.
     */
    void skipsPlacementWhenBrightEnough() {
        assumeBlockRegistries();
        IlluminationCoreService service = new IlluminationCoreService();
        IlluminationCoreService.IlluminationDecision decision =
            service.decidePlacement(12, true, true, 2, 2, "torch");

        assertFalse(decision.placeNow());
        assertEquals(0, decision.plannedTorches());
    }

    @Test
    /**
     * Verify depletion flag when manual mode has no torch supply.
     */
    void flagsInventoryDepletionWhenTorchSupplyRunsOut() {
        assumeBlockRegistries();
        IlluminationCoreService service = new IlluminationCoreService();
        IlluminationCoreService.IlluminationDecision decision =
            service.decidePlacement(3, true, true, 2, 2, "torch_manual_both_empty");

        assertFalse(decision.placeNow());
        assertTrue(decision.inventoryDepleted());
        assertEquals(0, decision.plannedTorches());
    }
}
