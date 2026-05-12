package uk.co.duelmonster.minersadvantage.common.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import uk.co.duelmonster.minersadvantage.common.services.substitution.SubstitutionCoreService;
import uk.co.duelmonster.minersadvantage.common.services.substitution.SubstitutionCoreService.ToolCandidate;

/**
 * SubstitutionCoreServiceTest keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
class SubstitutionCoreServiceTest {
    @Test
    void selectsHighestScoringToolWithPreferences() {
        SubstitutionCoreService service = new SubstitutionCoreService();
        ToolCandidate best = service.selectBest(
            List.of(
                new ToolCandidate("a", 8.0, 0, 3, 1, false, false),
                new ToolCandidate("b", 8.0, 1, 1, 1, false, false),
                new ToolCandidate("c", 9.0, 0, 0, 1, true, false)
            ),
            true,
            false
        );

        assertEquals("b", best.id());
    }

    @Test
    void prefersCombatToolAndCanSwitchBack() {
        SubstitutionCoreService service = new SubstitutionCoreService();

        SubstitutionCoreService.SubstitutionDecision combatDecision = service.decideTool(
            "pickaxe",
            List.of(
                new ToolCandidate("pickaxe", 8.0, 0, 0, 2, false, false),
                new ToolCandidate("battle_blade", 5.0, 0, 0, 9, false, false)
            ),
            true,
            false,
            false,
            true,
            false
        );

        assertEquals("battle_blade", combatDecision.selectedToolId());
        assertTrue(combatDecision.switched());

        SubstitutionCoreService.SubstitutionDecision restoreDecision = service.decideTool(
            "pickaxe",
            List.of(),
            true,
            false,
            false,
            false,
            true
        );

        assertEquals("pickaxe", restoreDecision.selectedToolId());
        assertTrue(restoreDecision.switchBackToPrimary());
        assertFalse(restoreDecision.switched());
    }
}
