package uk.co.duelmonster.minersadvantage.common.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import uk.co.duelmonster.minersadvantage.common.services.world.WorldQueryService;

/**
 * WorldQueryServiceTest keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
class WorldQueryServiceTest {
    @Test
    /**
     * c om pu te sa re aa nd di st an ce he lp er s exists so this path stays predictable and easier to debug when things get weird.
     */
    void computesAreaAndDistanceHelpers() {
        WorldQueryService service = new WorldQueryService();
        assertEquals(6, service.manhattanDistance(0, 0, 0, 1, 2, 3));
        assertTrue(service.insideBox(0, 0, 0, 2, 2, 2, 1, 1, 1));
        assertFalse(service.insideBox(0, 0, 0, 2, 2, 2, 3, 1, 1));
        assertEquals(5, service.normalizedOddWidth(4));
    }
}
