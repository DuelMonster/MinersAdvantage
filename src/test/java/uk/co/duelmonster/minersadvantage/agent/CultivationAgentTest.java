package uk.co.duelmonster.minersadvantage.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import uk.co.duelmonster.minersadvantage.common.config.CultivationConfig;

class CultivationAgentTest {
  @Test
  void resolveHydrationDistance_shouldRespectConfiguredValuesAboveLegacyCap() {
    assertEquals(12, CultivationAgent.resolveHydrationDistance(new CultivationConfig(true, 12), 4));
  }

  @Test
  void resolveHydrationDistance_shouldAllowZeroWhenConfigured() {
    assertEquals(0, CultivationAgent.resolveHydrationDistance(new CultivationConfig(true, 0), 4));
  }

  @Test
  void resolveHydrationDistance_shouldUseFallbackRadiusWhenConfigIsMissing() {
    assertEquals(7, CultivationAgent.resolveHydrationDistance(null, 7));
  }
}
