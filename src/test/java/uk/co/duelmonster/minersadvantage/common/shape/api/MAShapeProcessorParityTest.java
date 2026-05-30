package uk.co.duelmonster.minersadvantage.common.shape.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;

class MAShapeProcessorParityTest {
    @BeforeAll
    static void bootstrapShapes() {
        MAShapeBootstrap.ensureInitialized();
    }

    @Test
    void registersExpectedBuiltinShapeCountsPerFeature() {
        assertEquals(6, MAShapeRegistry.forFeature(FeatureId.EXCAVATION).size());
        assertEquals(3, MAShapeRegistry.forFeature(FeatureId.SHAFTANATION).size());
    }

    @Test
    void excavationSelectionWrapsByIndex() {
        var selected = MAShapeRegistry.byIndex(FeatureId.EXCAVATION, 99);
        assertTrue(selected.isPresent());

        String resolvedId = selected.get().id();
        int resolvedIndex = MAShapeRegistry.indexOf(FeatureId.EXCAVATION, resolvedId);
        assertEquals(99 % MAShapeRegistry.forFeature(FeatureId.EXCAVATION).size(), resolvedIndex);
    }

    @Test
    void shaftSelectionResolvesKnownBuiltinIds() {
        int shaftIndex = MAShapeRegistry.indexOf(FeatureId.SHAFTANATION, MAShapeIds.SHAFTANATION_SHAFT);
        int upIndex = MAShapeRegistry.indexOf(FeatureId.SHAFTANATION, MAShapeIds.SHAFTANATION_STAIRCASE_UP);
        int downIndex = MAShapeRegistry.indexOf(FeatureId.SHAFTANATION, MAShapeIds.SHAFTANATION_STAIRCASE_DOWN);

        assertTrue(shaftIndex >= 0);
        assertTrue(upIndex >= 0);
        assertTrue(downIndex >= 0);
    }

    @Test
    void bootstrapIsIdempotentForRegistryCounts() {
        int excavationBefore = MAShapeRegistry.forFeature(FeatureId.EXCAVATION).size();
        int shaftBefore = MAShapeRegistry.forFeature(FeatureId.SHAFTANATION).size();

        MAShapeBootstrap.ensureInitialized();
        MAShapeBootstrap.ensureInitialized();

        assertEquals(excavationBefore, MAShapeRegistry.forFeature(FeatureId.EXCAVATION).size());
        assertEquals(shaftBefore, MAShapeRegistry.forFeature(FeatureId.SHAFTANATION).size());
    }

    @Test
    void unknownShapeIdReturnsMissingIndex() {
        assertEquals(-1, MAShapeRegistry.indexOf(FeatureId.EXCAVATION, "minersadvantage:not_real"));
    }
}
