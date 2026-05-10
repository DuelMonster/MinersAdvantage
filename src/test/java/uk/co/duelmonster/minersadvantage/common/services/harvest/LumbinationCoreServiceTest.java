package uk.co.duelmonster.minersadvantage.common.services.harvest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

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
