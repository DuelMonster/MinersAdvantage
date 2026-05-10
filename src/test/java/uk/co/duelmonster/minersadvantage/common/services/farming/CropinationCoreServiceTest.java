package uk.co.duelmonster.minersadvantage.common.services.farming;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CropinationCoreServiceTest {
    @Test
    void detectsFullyGrownCrops() {
        CropinationCoreService service = new CropinationCoreService();
        assertTrue(service.isFullyGrown(7, 7));
        assertFalse(service.isFullyGrown(6, 7));
    }

    @Test
    void calculatesReducedDurabilityCadence() {
        CropinationCoreService service = new CropinationCoreService();
        assertEquals(1, service.adjustedDurabilityCost(5, 5));
        assertEquals(0, service.adjustedDurabilityCost(4, 5));
    }
}
