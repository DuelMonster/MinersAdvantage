package uk.co.duelmonster.minersadvantage.common.shape.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;
import uk.co.duelmonster.minersadvantage.common.shape.builtin.excavation.ExcavationFaceGeometry;
import uk.co.duelmonster.minersadvantage.common.shape.builtin.excavation.ThreeByThreeGeometry;

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

    @Test
    void threeByThreeNorthHitUsesEastWestAndUpDownWithSingleDepth() {
        Set<String> offsets = toOffsetKeys(ThreeByThreeGeometry.offsetsForFaceAxis(ThreeByThreeGeometry.FaceAxis.Z));

        assertEquals(9, offsets.size());
        assertTrue(offsets.contains(key(-1, 0, 0)));
        assertTrue(offsets.contains(key(1, 0, 0)));
        assertTrue(offsets.contains(key(0, -1, 0)));
        assertTrue(offsets.contains(key(0, 1, 0)));
        assertFalse(offsets.contains(key(0, 0, -1)));
        assertFalse(offsets.contains(key(0, 0, 1)));
    }

    @Test
    void threeByThreeTopHitUsesEastWestAndNorthSouthWithSingleDepth() {
        Set<String> offsets = toOffsetKeys(ThreeByThreeGeometry.offsetsForFaceAxis(ThreeByThreeGeometry.FaceAxis.Y));

        assertEquals(9, offsets.size());
        assertTrue(offsets.contains(key(-1, 0, 0)));
        assertTrue(offsets.contains(key(1, 0, 0)));
        assertTrue(offsets.contains(key(0, 0, -1)));
        assertTrue(offsets.contains(key(0, 0, 1)));
        assertFalse(offsets.contains(key(0, 1, 0)));
        assertFalse(offsets.contains(key(0, -1, 0)));
    }

    @Test
    void threeByThreeEastHitUsesNorthSouthAndUpDownWithSingleDepth() {
        Set<String> offsets = toOffsetKeys(ThreeByThreeGeometry.offsetsForFaceAxis(ThreeByThreeGeometry.FaceAxis.X));

        assertEquals(9, offsets.size());
        assertTrue(offsets.contains(key(0, 0, -1)));
        assertTrue(offsets.contains(key(0, 0, 1)));
        assertTrue(offsets.contains(key(0, 1, 0)));
        assertTrue(offsets.contains(key(0, -1, 0)));
        assertFalse(offsets.contains(key(1, 0, 0)));
        assertFalse(offsets.contains(key(-1, 0, 0)));
    }

    @Test
    void excavationNorthSouthHitsUseXYPlaneAndFlipDepthOnZ() {
        assertOffset(
            ExcavationFaceGeometry.offsetFor(ExcavationFaceGeometry.FaceDirection.NORTH, 2, 1, -1),
            1,
            -1,
            2
        );
        assertOffset(
            ExcavationFaceGeometry.offsetFor(ExcavationFaceGeometry.FaceDirection.SOUTH, 2, 1, -1),
            1,
            -1,
            -2
        );
    }

    @Test
    void excavationUpDownHitsUseXZPlaneAndFlipDepthOnY() {
        assertOffset(
            ExcavationFaceGeometry.offsetFor(ExcavationFaceGeometry.FaceDirection.UP, 2, 1, -1),
            1,
            -2,
            -1
        );
        assertOffset(
            ExcavationFaceGeometry.offsetFor(ExcavationFaceGeometry.FaceDirection.DOWN, 2, 1, -1),
            1,
            2,
            -1
        );
    }

    @Test
    void excavationEastWestHitsUseZYPlaneAndFlipDepthOnX() {
        assertOffset(
            ExcavationFaceGeometry.offsetFor(ExcavationFaceGeometry.FaceDirection.EAST, 2, 1, -1),
            -2,
            -1,
            1
        );
        assertOffset(
            ExcavationFaceGeometry.offsetFor(ExcavationFaceGeometry.FaceDirection.WEST, 2, 1, -1),
            2,
            -1,
            1
        );
    }

    private static Set<String> toOffsetKeys(int[][] offsets) {
        Set<String> keys = new HashSet<>();
        for (int[] offset : offsets) {
            keys.add(key(offset[0], offset[1], offset[2]));
        }
        return keys;
    }

    private static void assertOffset(int[] offset, int x, int y, int z) {
        assertEquals(key(x, y, z), key(offset[0], offset[1], offset[2]));
    }

    private static String key(int x, int y, int z) {
        return x + "," + y + "," + z;
    }
}
