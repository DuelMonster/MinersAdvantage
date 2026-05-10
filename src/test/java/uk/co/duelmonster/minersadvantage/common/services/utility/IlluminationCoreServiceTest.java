package uk.co.duelmonster.minersadvantage.common.services.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class IlluminationCoreServiceTest {
    @Test
    void determinesLightLevelThreshold() {
        IlluminationCoreService service = new IlluminationCoreService();
        assertTrue(service.shouldPlaceTorch(0));
        assertTrue(service.shouldPlaceTorch(7));
        assertFalse(service.shouldPlaceTorch(8));
        assertFalse(service.shouldPlaceTorch(15));
    }

    @Test
    void selectsBestTorchPlacement() {
        IlluminationCoreService service = new IlluminationCoreService();
        assertEquals(TorchPlacement.BOTH_WALLS, service.selectPlacement(true, true));
        assertEquals(TorchPlacement.LEFT_WALL, service.selectPlacement(true, false));
        assertEquals(TorchPlacement.RIGHT_WALL, service.selectPlacement(false, true));
        assertEquals(TorchPlacement.FLOOR, service.selectPlacement(false, false));
    }

    @Test
    void estimatesPlacementsInRadius() {
        IlluminationCoreService service = new IlluminationCoreService();
        assertEquals(9, service.expectedPlacementsInRadius(1, 1));
        assertEquals(25, service.expectedPlacementsInRadius(2, 2));
    }

    @Test
    void createsDecisionForDarkAreas() {
        IlluminationCoreService service = new IlluminationCoreService();
        IlluminationCoreService.IlluminationDecision decision =
            service.decidePlacement(4, true, false, 2, 1, "torch_manual_left");

        assertTrue(decision.placeNow());
        assertEquals(TorchPlacement.LEFT_WALL, decision.placement());
        assertTrue(decision.plannedTorches() > 0);
        assertTrue(decision.manualMode());
    }

    @Test
    void skipsPlacementWhenBrightEnough() {
        IlluminationCoreService service = new IlluminationCoreService();
        IlluminationCoreService.IlluminationDecision decision =
            service.decidePlacement(12, true, true, 2, 2, "torch");

        assertFalse(decision.placeNow());
        assertEquals(0, decision.plannedTorches());
    }

    @Test
    void flagsInventoryDepletionWhenTorchSupplyRunsOut() {
        IlluminationCoreService service = new IlluminationCoreService();
        IlluminationCoreService.IlluminationDecision decision =
            service.decidePlacement(3, true, true, 2, 2, "torch_manual_both_empty");

        assertFalse(decision.placeNow());
        assertTrue(decision.inventoryDepleted());
        assertEquals(0, decision.plannedTorches());
    }
}
