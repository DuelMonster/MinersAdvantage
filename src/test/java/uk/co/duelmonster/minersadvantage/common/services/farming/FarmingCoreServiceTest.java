package uk.co.duelmonster.minersadvantage.common.services.farming;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class FarmingCoreServiceTest {
    @Test
    void buildsHydrationAwareCultivationPlan() {
        FarmingCoreService service = new FarmingCoreService();
        List<FarmingCoreService.CultivationStep> plan = service.buildCultivationPlan(10, 64, 10, 2, 4);

        assertEquals(4, plan.size());
        assertTrue(plan.get(0).hydrated());
        assertTrue(plan.get(2).hydrated());
        assertFalse(plan.get(3).hydrated());
    }
}
