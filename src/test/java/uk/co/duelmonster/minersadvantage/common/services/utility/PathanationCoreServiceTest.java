package uk.co.duelmonster.minersadvantage.common.services.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * PathanationCoreServiceTest keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
class PathanationCoreServiceTest {
    @Test
    void buildsLinearPathPlan() {
        PathanationCoreService service = new PathanationCoreService();
        List<PathanationCoreService.PathStep> steps = service.buildPath(10, 64, 10, 5, 3);

        assertEquals(3, steps.size());
        assertEquals(11, steps.get(0).x());
        assertEquals(13, steps.get(2).x());
        assertTrue(steps.stream().allMatch(step -> step.operation().equals("flatten")));
    }
}

