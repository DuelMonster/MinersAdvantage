package uk.co.duelmonster.minersadvantage.common.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import uk.co.duelmonster.minersadvantage.common.services.substitution.SubstitutionCoreService;
import uk.co.duelmonster.minersadvantage.common.services.substitution.SubstitutionCoreService.ToolCandidate;

/**
 * SubstitutionCoreServiceTest keeps this part of MinersAdvantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
class SubstitutionCoreServiceTest {
  @Test
  /**
   * s el ec ts hi gh es ts co ri ng to ol wi th pr ef er en ce s exists so this path stays predictable and easier to debug when things get weird.
   */
  void selectsHighestScoringToolWithPreferences() {
    SubstitutionCoreService service = new SubstitutionCoreService();
    ToolCandidate best = service.selectBest(
        List.of(
            new ToolCandidate("a", 8.0, 0, 3, 1, false, false),
            new ToolCandidate("b", 8.0, 1, 1, 1, false, false),
            new ToolCandidate("c", 9.0, 0, 0, 1, true, false)),
        true,
        false);

    assertEquals("b", best.id());
  }

  @Test
  /**
   * p re fe rs co mb at to ol an dc an sw it ch ba ck exists so this path stays predictable and easier to debug when things get weird.
   */
  void prefersCombatToolAndCanSwitchBack() {
    SubstitutionCoreService service = new SubstitutionCoreService();

    SubstitutionCoreService.SubstitutionDecision combatDecision = service.decideTool(
        "pickaxe",
        List.of(
            new ToolCandidate("pickaxe", 8.0, 0, 0, 2, false, false),
            new ToolCandidate("battle_blade", 5.0, 0, 0, 9, false, false)),
        true,
        false,
        false,
        true,
        false);

    assertEquals("battle_blade", combatDecision.selectedToolId());
    assertTrue(combatDecision.switched());

    SubstitutionCoreService.SubstitutionDecision restoreDecision = service.decideTool(
        "pickaxe",
        List.of(),
        true,
        false,
        false,
        false,
        true);

    assertEquals("pickaxe", restoreDecision.selectedToolId());
    assertTrue(restoreDecision.switchBackToPrimary());
    assertFalse(restoreDecision.switched());
  }

  @Test
  /**
   * f il te rs bl ac kl is t an dm en di ng pr ot ec te dc an di da te s exists so this path stays predictable and easier to debug when things get weird.
   */
  void filtersBlacklistedAndMendingProtectedCandidates() {
    SubstitutionCoreService service = new SubstitutionCoreService();

    ToolCandidate best = service.selectBest(
        List.of(
            new ToolCandidate("safe", 7.0, 0, 0, 1, false, false),
            new ToolCandidate("blocked", 9.0, 1, 2, 3, false, false),
            new ToolCandidate("mending", 10.0, 0, 3, 2, false, true)),
        false,
        true,
        false,
        Set.of("blocked"),
        false);

    assertEquals("safe", best.id());
  }

  @Test
  /**
   * re tu rn sn ul lw he nn oe li gi bl ec an di da te ex is ts exists so this path stays predictable and easier to debug when things get weird.
   */
  void returnsNullWhenNoEligibleCandidateExists() {
    SubstitutionCoreService service = new SubstitutionCoreService();

    ToolCandidate best = service.selectBest(
        List.of(
            new ToolCandidate("blocked", 9.0, 1, 0, 1, false, false),
            new ToolCandidate("mending", 8.0, 0, 3, 1, false, true)),
        true,
        false,
        false,
        Set.of("blocked"),
        false);

    assertNull(best);
  }
}
