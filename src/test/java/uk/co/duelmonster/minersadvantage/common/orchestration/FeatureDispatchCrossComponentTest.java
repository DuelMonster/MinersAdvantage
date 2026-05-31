package uk.co.duelmonster.minersadvantage.common.orchestration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;

/**
 * FeatureDispatchCrossComponentTest keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
class FeatureDispatchCrossComponentTest {
    @Test
    /**
     * m in in gf ea tu re sr ou te to sh ar ed de pe nd en tf ea tu re s exists so this path stays predictable and easier to debug when things get weird.
     */
    void miningFeaturesRouteToSharedDependentFeatures() {
        assertTrue(FeatureOrchestration.shouldDispatchToFeature(FeatureId.EXCAVATION, FeatureId.ILLUMINATION));
        assertTrue(FeatureOrchestration.shouldDispatchToFeature(FeatureId.EXCAVATION, FeatureId.VEINATION));
        assertTrue(FeatureOrchestration.shouldDispatchToFeature(FeatureId.SHAFTANATION, FeatureId.ILLUMINATION));
        assertTrue(FeatureOrchestration.shouldDispatchToFeature(FeatureId.SHAFTANATION, FeatureId.VEINATION));
        assertTrue(FeatureOrchestration.shouldDispatchToFeature(FeatureId.VENTILATION, FeatureId.ILLUMINATION));
        assertTrue(FeatureOrchestration.shouldDispatchToFeature(FeatureId.VENTILATION, FeatureId.VEINATION));
    }

    @Test
    /**
     * u nr el at ed fe at ur es do no tr ou te ac ro ss th em at ri x exists so this path stays predictable and easier to debug when things get weird.
     */
    void unrelatedFeaturesDoNotRouteAcrossTheMatrix() {
        assertFalse(FeatureOrchestration.shouldDispatchToFeature(FeatureId.CAPTIVATION, FeatureId.VEINATION));
        assertFalse(FeatureOrchestration.shouldDispatchToFeature(FeatureId.CROPINATION, FeatureId.ILLUMINATION));
        assertFalse(FeatureOrchestration.shouldDispatchToFeature(FeatureId.SUBSTITUTION, FeatureId.EXCAVATION));
    }
}
