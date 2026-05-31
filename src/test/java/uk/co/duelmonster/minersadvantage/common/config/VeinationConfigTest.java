package uk.co.duelmonster.minersadvantage.common.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Regression tests for VeinationConfig constructors and safety clamping.
 */
class VeinationConfigTest {
    /**
     * Legacy constructor should still produce the expected parity defaults used by existing runtime paths.
     */
    @Test
    /**
     * l eg ac yc on st ru ct or ap pl ie sp ar it yd ef au lt s exists so this path stays predictable and easier to debug when things get weird.
     */
    void legacyConstructorAppliesParityDefaults() {
        VeinationConfig config = new VeinationConfig(true, 5, List.of("minecraft:diamond_ore"));

        assertTrue(config.enabled());
        assertEquals(5, config.maxVeinDistance());
        assertEquals(List.of("minecraft:diamond_ore"), config.ores());
        assertEquals(true, config.oreHarvestWithoutSneak());
        assertEquals(true, config.dropOresAtFirstBrokenBlock());
        assertEquals(true, config.increaseHarvestingTimePerOre());
        assertEquals(0.2D, config.increasedHarvestingTimePerOreModifier());
        assertEquals(List.of(), config.pickaxeBlacklist());
    }

    /**
     * Modifier values outside safe range should clamp, because runaway config values are comedy for nobody.
     */
    @Test
    /**
     * m od if ie ri sc la mp ed in to sa fe ra ng e exists so this path stays predictable and easier to debug when things get weird.
     */
    void modifierIsClampedIntoSafeRange() {
        VeinationConfig low = new VeinationConfig(true, 4, List.of(), false, true, true, -1.0D, List.of());
        VeinationConfig high = new VeinationConfig(true, 4, List.of(), false, true, true, 99.0D, List.of());

        assertEquals(0.01D, low.increasedHarvestingTimePerOreModifier());
        assertEquals(10.0D, high.increasedHarvestingTimePerOreModifier());
    }
}
