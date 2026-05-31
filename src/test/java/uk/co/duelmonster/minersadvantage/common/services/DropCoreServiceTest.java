package uk.co.duelmonster.minersadvantage.common.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import uk.co.duelmonster.minersadvantage.common.services.drop.DropCoreService;

/**
 * DropCoreServiceTest keeps this part of MinersAdvantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
class DropCoreServiceTest {
    @Test
    /**
     * c ap tu re sa nd fl us he sd ro ps exists so this path stays predictable and easier to debug when things get weird.
     */
    void capturesAndFlushesDrops() {
        DropCoreService service = new DropCoreService();
        service.capture("item", 2);
        service.capture("xp", 8);

        assertEquals(2, service.count());
        assertEquals(2, service.flush().size());
        assertEquals(0, service.count());
    }
}
