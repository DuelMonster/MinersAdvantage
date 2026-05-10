package uk.co.duelmonster.minersadvantage.common.orchestration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;

class FeatureDispatchCrossComponentTest {
    @Test
    void miningFeaturesRouteToSharedDependentFeatures() {
        assertTrue(FeatureOrchestration.shouldDispatchToFeature(FeatureId.EXCAVATION, FeatureId.ILLUMINATION));
        assertTrue(FeatureOrchestration.shouldDispatchToFeature(FeatureId.EXCAVATION, FeatureId.VEINATION));
        assertTrue(FeatureOrchestration.shouldDispatchToFeature(FeatureId.SHAFTANATION, FeatureId.ILLUMINATION));
        assertTrue(FeatureOrchestration.shouldDispatchToFeature(FeatureId.SHAFTANATION, FeatureId.VEINATION));
        assertTrue(FeatureOrchestration.shouldDispatchToFeature(FeatureId.VENTILATION, FeatureId.ILLUMINATION));
        assertTrue(FeatureOrchestration.shouldDispatchToFeature(FeatureId.VENTILATION, FeatureId.VEINATION));
    }

    @Test
    void unrelatedFeaturesDoNotRouteAcrossTheMatrix() {
        assertFalse(FeatureOrchestration.shouldDispatchToFeature(FeatureId.CAPTIVATION, FeatureId.VEINATION));
        assertFalse(FeatureOrchestration.shouldDispatchToFeature(FeatureId.CROPINATION, FeatureId.ILLUMINATION));
        assertFalse(FeatureOrchestration.shouldDispatchToFeature(FeatureId.SUBSTITUTION, FeatureId.EXCAVATION));
    }
}