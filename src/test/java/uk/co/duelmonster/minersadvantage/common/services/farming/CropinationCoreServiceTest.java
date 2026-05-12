package uk.co.duelmonster.minersadvantage.common.services.farming;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * CropinationCoreServiceTest keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
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

    @Test
    void evaluatesHarvestAndReplantAction() {
        CropinationCoreService service = new CropinationCoreService();
        CropinationCoreService.CropAction action = service.evaluateCrop(7, 7, 4, true, 5, 5);

        assertTrue(action.shouldHarvest());
        assertTrue(action.shouldReplant());
        assertEquals(1, action.seedsConsumed());
        assertEquals(1, action.durabilityCost());
    }
}



