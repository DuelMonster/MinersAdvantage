package uk.co.duelmonster.minersadvantage.common.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;

/**
 * Guards the feature enable/disable helper used by the feature toggle keybinds.
 */
class MAServerRootConfigTest {
  @Test
  void togglesEveryFeatureWithoutDisturbingOtherSettings() {
    MAServerRootConfig defaults = MAServerRootConfig.defaults();

    for (FeatureId feature : FeatureId.values()) {
      assertTrue(defaults.isFeatureEnabled(feature), () -> feature + " should start enabled");

      MAServerRootConfig disabled = defaults.withFeatureEnabled(feature, false);
      assertFalse(disabled.isFeatureEnabled(feature), () -> feature + " should be disabled");
      assertEquals(defaults.common(), disabled.common());

      for (FeatureId other : FeatureId.values()) {
        if (other != feature) {
          assertTrue(disabled.isFeatureEnabled(other), () -> other + " should be untouched");
        }
      }

      assertEquals(defaults, disabled.withFeatureEnabled(feature, true));
    }
  }

  @Test
  void returnsSameInstanceWhenNothingChanges() {
    MAServerRootConfig defaults = MAServerRootConfig.defaults();

    assertSame(defaults, defaults.withFeatureEnabled(FeatureId.EXCAVATION, true));
    assertSame(defaults, defaults.withFeatureEnabled(null, false));
  }
}
