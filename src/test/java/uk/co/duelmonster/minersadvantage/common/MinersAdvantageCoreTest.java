package uk.co.duelmonster.minersadvantage.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;

/**
 * MinersAdvantageCoreTest keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
class MinersAdvantageCoreTest {
    @Test
    void bootstrapsAllPlannedFeatures() {
        MinersAdvantageCore core = new MinersAdvantageCore();
        core.bootstrap();

        assertEquals(FeatureId.values().length, core.components().size());
        assertTrue(core.components().values().stream().allMatch(component -> component.isEnabled()));

        core.shutdown();
    }
}
