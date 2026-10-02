package uk.co.duelmonster.minersadvantage.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import uk.co.duelmonster.minersadvantage.common.config.SubstitutionConfig.SubstitutionAction;

class SubstitutionAgentTest {
  @Test
  void restoreIdleTicksForAction_shouldKeepAttackWindowLongerThanBreakWindow() {
    int breakWindow = SubstitutionAgent.restoreIdleTicksForAction(SubstitutionAction.BREAK);
    int attackWindow = SubstitutionAgent.restoreIdleTicksForAction(SubstitutionAction.ATTACK);

    assertEquals(3, breakWindow);
    assertEquals(12, attackWindow);
  }

  @Test
  void shouldSkipAttackSwapWhenMainHandAlreadyHasSwordOrAxe() {
    assertTrue(SubstitutionAgent.shouldSkipAttackSwap(
        SubstitutionAction.ATTACK,
        true,
        false));
    assertTrue(SubstitutionAgent.shouldSkipAttackSwap(
        SubstitutionAction.ATTACK,
        false,
        true));
  }

  @Test
  void shouldOnlySkipAttackSwapForAnAlreadyHeldCombatTool() {
    assertFalse(SubstitutionAgent.shouldSkipAttackSwap(
        SubstitutionAction.ATTACK,
        false,
        false));
    assertFalse(SubstitutionAgent.shouldSkipAttackSwap(
        SubstitutionAction.BREAK,
        true,
        false));
  }
}
