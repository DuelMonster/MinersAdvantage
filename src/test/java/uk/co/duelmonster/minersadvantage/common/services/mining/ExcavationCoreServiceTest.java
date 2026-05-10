package uk.co.duelmonster.minersadvantage.common.services.mining;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ExcavationCoreServiceTest {
    @Test
    void identifiesValidBlocks() {
        ExcavationCoreService service = new ExcavationCoreService();
        assertTrue(service.isBlock("dirt"));
        assertFalse(service.isBlock("air"));
        assertFalse(service.isBlock(null));
    }

    @Test
    void detectsOres() {
        ExcavationCoreService service = new ExcavationCoreService();
        assertTrue(service.isOre("iron_ore"));
        assertFalse(service.isOre("dirt"));
    }

    @Test
    void estimatesExcavationVolume() {
        ExcavationCoreService service = new ExcavationCoreService();
        assertEquals(27, service.estimatedTurnsToExcavate(1, 1));
    }
}
