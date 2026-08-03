package uk.co.duelmonster.minersadvantage.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
