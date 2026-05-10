package uk.co.duelmonster.minersadvantage.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;

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
