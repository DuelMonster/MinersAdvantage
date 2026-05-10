package uk.co.duelmonster.minersadvantage.common.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import uk.co.duelmonster.minersadvantage.common.services.substitution.SubstitutionCoreService;
import uk.co.duelmonster.minersadvantage.common.services.substitution.SubstitutionCoreService.ToolCandidate;

class SubstitutionCoreServiceTest {
    @Test
    void selectsHighestScoringToolWithPreferences() {
        SubstitutionCoreService service = new SubstitutionCoreService();
        ToolCandidate best = service.selectBest(
            List.of(
                new ToolCandidate("a", 8.0, 0, 3, false),
                new ToolCandidate("b", 8.0, 1, 1, false),
                new ToolCandidate("c", 9.0, 0, 0, true)
            ),
            true,
            false
        );

        assertEquals("b", best.id());
    }
}
