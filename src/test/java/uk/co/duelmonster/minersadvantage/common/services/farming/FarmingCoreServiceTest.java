package uk.co.duelmonster.minersadvantage.common.services.farming;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * FarmingCoreServiceTest keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
class FarmingCoreServiceTest {
    @Test
    /**
     * b ui ld sh yd ra ti on aw ar ec ul ti va ti on pl an exists so this path stays predictable and easier to debug when things get weird.
     */
    void buildsHydrationAwareCultivationPlan() {
        FarmingCoreService service = new FarmingCoreService();
        List<FarmingCoreService.CultivationStep> plan = service.buildCultivationPlan(10, 64, 10, 2, 4);

        assertEquals(4, plan.size());
        assertTrue(plan.get(0).hydrated());
        assertTrue(plan.get(2).hydrated());
        assertFalse(plan.get(3).hydrated());
    }
}
