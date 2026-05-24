package uk.co.duelmonster.minersadvantage.common.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class VeinationConfigTest {
    @Test
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

    @Test
    void modifierIsClampedIntoSafeRange() {
        VeinationConfig low = new VeinationConfig(true, 4, List.of(), false, true, true, -1.0D, List.of());
        VeinationConfig high = new VeinationConfig(true, 4, List.of(), false, true, true, 99.0D, List.of());

        assertEquals(0.01D, low.increasedHarvestingTimePerOreModifier());
        assertEquals(10.0D, high.increasedHarvestingTimePerOreModifier());
    }
}
