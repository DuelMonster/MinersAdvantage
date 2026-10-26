package uk.co.duelmonster.minersadvantage.common.services.harvest;

import uk.co.duelmonster.minersadvantage.common.config.CommonConfig;

/**
 * ToolPreservationPolicy applies the one simple rule: keep the configured number of uses in reserve.
 */
public final class ToolPreservationPolicy {
  private ToolPreservationPolicy() {
  }

  /**
   * Check a remaining-use count against the enabled common reserve.
   */
  public static boolean isAtOrBelowReserve(int remainingDurability, CommonConfig config) {
    return config != null && config.toolPreservationEnabled()
        && remainingDurability <= Math.max(1, config.toolPreservationReserve());
  }
}
