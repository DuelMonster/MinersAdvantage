package uk.co.duelmonster.minersadvantage.common.services.harvest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import uk.co.duelmonster.minersadvantage.common.config.CommonConfig;
import uk.co.duelmonster.minersadvantage.common.config.SyncedClientConfig;

/**
 * ToolPreservationPolicyTest checks the reserve boundary without needing a whole server full of blocks.
 */
class ToolPreservationPolicyTest {
  @Test
  void protectsToolsAtOrBelowTheRemainingDurabilityReserve() {
    CommonConfig config = SyncedClientConfig.defaults().common();

    assertTrue(ToolPreservationPolicy.isAtOrBelowReserve(20, config));
    assertFalse(ToolPreservationPolicy.isAtOrBelowReserve(21, config));
    assertTrue(ToolPreservationPolicy.isAtOrBelowReserve(19, config));
  }

  @Test
  void appliesTheConfiguredReserveAndIgnoresDisabledProtection() {
    CommonConfig disabled = new CommonConfig(true, false, true, true, 1, 1, true, 5, 3, false, 20);
    CommonConfig custom = new CommonConfig(true, false, true, true, 1, 1, true, 5, 3, true, 37);

    assertFalse(ToolPreservationPolicy.isAtOrBelowReserve(10, disabled));
    assertTrue(ToolPreservationPolicy.isAtOrBelowReserve(37, custom));
    assertFalse(ToolPreservationPolicy.isAtOrBelowReserve(38, custom));
  }
}
