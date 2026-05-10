package uk.co.duelmonster.minersadvantage.common.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import uk.co.duelmonster.minersadvantage.common.services.illumination.IlluminationCoreService;

class IlluminationCoreServiceTest {
    @Test
    void computesPlacementDecisions() {
        IlluminationCoreService service = new IlluminationCoreService();
        assertTrue(service.shouldPlaceTorch(3, 7, true));
        assertEquals(2, service.expectedPlacements(IlluminationCoreService.TorchPlacement.BOTH_WALLS));
    }
}
