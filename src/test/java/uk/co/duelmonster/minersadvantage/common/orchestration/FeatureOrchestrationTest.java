package uk.co.duelmonster.minersadvantage.common.orchestration;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;

/**
 * FeatureOrchestrationTest keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
class FeatureOrchestrationTest {
    @Test
    void routesMiningFeaturesToVeinationAndIllumination() {
        assertTrue(FeatureOrchestration.shouldDispatchToFeature(FeatureId.EXCAVATION, FeatureId.VEINATION));
        assertTrue(FeatureOrchestration.shouldDispatchToFeature(FeatureId.EXCAVATION, FeatureId.ILLUMINATION));
        assertTrue(FeatureOrchestration.shouldDispatchToFeature(FeatureId.SHAFTANATION, FeatureId.VEINATION));
        assertTrue(FeatureOrchestration.shouldDispatchToFeature(FeatureId.VENTILATION, FeatureId.ILLUMINATION));
    }

    @Test
    void maintainsBusContext() {
        FeatureDispatchContext ctx = new FeatureDispatchContext(FeatureId.EXCAVATION, 0, 0, 0, "stone", "pickaxe", 1L);
        FeatureDispatchBus.setContext(ctx);
        assertTrue(FeatureDispatchBus.hasContext());
        FeatureDispatchBus.clearContext();
    }
}



