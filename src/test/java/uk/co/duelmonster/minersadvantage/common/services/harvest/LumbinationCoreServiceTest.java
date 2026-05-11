package uk.co.duelmonster.minersadvantage.common.services.harvest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * LumbinationCoreServiceTest keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
class LumbinationCoreServiceTest {
    @Test
    void buildsTrunkLeafAndSaplingPlan() {
        LumbinationCoreService service = new LumbinationCoreService();
        LumbinationCoreService.LumbinationPlan plan = service.buildPlan(6, 4, 3, 5, true);

        assertEquals(4, plan.logsToHarvest());
        assertEquals(3, plan.leavesToClear());
        assertTrue(plan.replantSapling());
        assertTrue(!plan.steps().isEmpty());
    }
}

