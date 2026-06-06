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
import uk.co.duelmonster.minersadvantage.common.shape.builtin.shaft.ShaftFloorGeometry;

/**
 * Parity suite for built-in shape registration, index selection behavior, and geometry helpers.
 * If anything drifts here, clients and servers will politely disagree in very impolite ways.
 */
class MAShapeProcessorParityTest {
    /**
     * Bootstrap built-ins once so all tests observe a populated registry.
     */
    @BeforeAll
    /**
     * Run bootstrap once before assertions.
     */
    static void bootstrapShapes() {
        MAShapeBootstrap.ensureInitialized();
    }

    /**
     * Sanity check expected built-in shape counts per feature.
     */
    @Test
    /**
     * Validate expected builtin shape totals.
     */
    void registersExpectedBuiltinShapeCountsPerFeature() {
        assertEquals(7, MAShapeRegistry.forFeature(FeatureId.EXCAVATION).size());
        assertEquals(3, MAShapeRegistry.forFeature(FeatureId.SHAFTANATION).size());
    }

    /**
     * New default excavation shape should be shapeless at index zero.
     */
    @Test
    /**
     * Validate default excavation shape id ordering.
     */
    void excavationDefaultShapeIsShapeless() {
        var defaultShape = MAShapeRegistry.byIndex(FeatureId.EXCAVATION, 0);
        assertTrue(defaultShape.isPresent());
        assertEquals(MAShapeIds.EXCAVATION_SHAPELESS, defaultShape.get().id());
    }

    /**
     * Shape selection should wrap by index instead of exploding when index is larger than list size.
     */
    @Test
    /**
     * Validate wrapping index selection behavior.
     */
    void excavationSelectionWrapsByIndex() {
        var selected = MAShapeRegistry.byIndex(FeatureId.EXCAVATION, 99);
        assertTrue(selected.isPresent());

        String resolvedId = selected.get().id();
        int resolvedIndex = MAShapeRegistry.indexOf(FeatureId.EXCAVATION, resolvedId);
        assertEquals(99 % MAShapeRegistry.forFeature(FeatureId.EXCAVATION).size(), resolvedIndex);
    }

    /**
     * Ensure known shaft builtin ids resolve to real registry entries.
     */
    @Test
    /**
     * Validate known shaft ids resolve.
     */
    void shaftSelectionResolvesKnownBuiltinIds() {
        int shaftIndex = MAShapeRegistry.indexOf(FeatureId.SHAFTANATION, MAShapeIds.SHAFTANATION_SHAFT);
        int upIndex = MAShapeRegistry.indexOf(FeatureId.SHAFTANATION, MAShapeIds.SHAFTANATION_STAIRCASE_UP);
        int downIndex = MAShapeRegistry.indexOf(FeatureId.SHAFTANATION, MAShapeIds.SHAFTANATION_STAIRCASE_DOWN);

        assertTrue(shaftIndex >= 0);
        assertTrue(upIndex >= 0);
        assertTrue(downIndex >= 0);
    }

    /**
     * Bootstrapping multiple times should not duplicate entries.
     */
    @Test
    /**
     * Validate bootstrap idempotency.
     */
    void bootstrapIsIdempotentForRegistryCounts() {
        int excavationBefore = MAShapeRegistry.forFeature(FeatureId.EXCAVATION).size();
        int shaftBefore = MAShapeRegistry.forFeature(FeatureId.SHAFTANATION).size();

        MAShapeBootstrap.ensureInitialized();
        MAShapeBootstrap.ensureInitialized();

        assertEquals(excavationBefore, MAShapeRegistry.forFeature(FeatureId.EXCAVATION).size());
        assertEquals(shaftBefore, MAShapeRegistry.forFeature(FeatureId.SHAFTANATION).size());
    }

    /**
     * Unknown ids should return -1 rather than pretending everything is fine.
     */
    @Test
    /**
     * Validate unknown id sentinel behavior.
     */
    void unknownShapeIdReturnsMissingIndex() {
        assertEquals(-1, MAShapeRegistry.indexOf(FeatureId.EXCAVATION, "minersadvantage:not_real"));
    }

    /**
     * North/South hit axis should generate XY plane offsets with no Z depth spread.
     */
    @Test
    /**
     * Validate Z-face 3x3 offsets.
     */
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

    /**
     * Top hit axis should generate XZ plane offsets with no Y spread.
     */
    @Test
    /**
     * Validate Y-face 3x3 offsets.
     */
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

    /**
     * East/West hit axis should generate ZY plane offsets with no X spread.
     */
    @Test
    /**
     * Validate X-face 3x3 offsets.
     */
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

    /**
     * North/South excavation offsets should flip depth direction on Z while preserving XY plane semantics.
     */
    @Test
    /**
     * Validate north/south excavation mapping.
     */
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

    /**
     * Up/Down excavation offsets should keep Y for height and project depth from player-facing heading.
     */
    @Test
    /**
     * Validate up/down excavation mapping.
     */
    void excavationUpDownHitsUsePlayerFacingForDepth() {
        assertOffset(
            ExcavationFaceGeometry.offsetFor(
                ExcavationFaceGeometry.FaceDirection.UP,
                ExcavationFaceGeometry.FaceDirection.NORTH,
                2,
                1,
                -1
            ),
            1,
            -1,
            -2
        );
        assertOffset(
            ExcavationFaceGeometry.offsetFor(
                ExcavationFaceGeometry.FaceDirection.DOWN,
                ExcavationFaceGeometry.FaceDirection.EAST,
                2,
                1,
                -1
            ),
            2,
            -1,
            1
        );
    }

    /**
     * East/West excavation offsets should flip depth direction on X while preserving ZY plane semantics.
     */
    @Test
    /**
     * Validate east/west excavation mapping.
     */
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

    /**
     * Deep Cuboid offsets should match face-forward formulas exactly.
     */
    @Test
    /**
     * Validate Deep Cuboid mapping formulas.
     */
    void deepCuboidOffsetsMatchFaceForwardFormulas() {
        assertOffset(
            ExcavationFaceGeometry.deepCuboidOffset(ExcavationFaceGeometry.FaceDirection.NORTH, 2, 1, -1),
            1,
            -1,
            2
        );
        assertOffset(
            ExcavationFaceGeometry.deepCuboidOffset(ExcavationFaceGeometry.FaceDirection.SOUTH, 2, 1, -1),
            1,
            -1,
            -2
        );
        assertOffset(
            ExcavationFaceGeometry.deepCuboidOffset(ExcavationFaceGeometry.FaceDirection.EAST, 2, 1, -1),
            -2,
            -1,
            1
        );
        assertOffset(
            ExcavationFaceGeometry.deepCuboidOffset(ExcavationFaceGeometry.FaceDirection.WEST, 2, 1, -1),
            2,
            -1,
            1
        );
        assertOffset(
            ExcavationFaceGeometry.deepCuboidOffset(ExcavationFaceGeometry.FaceDirection.UP, 2, 1, -1),
            1,
            -2,
            -1
        );
        assertOffset(
            ExcavationFaceGeometry.deepCuboidOffset(ExcavationFaceGeometry.FaceDirection.DOWN, 2, 1, -1),
            1,
            2,
            -1
        );
    }

    /**
     * Wide Cuboid offsets should match face/player-facing formulas exactly.
     */
    @Test
    /**
     * Validate Wide Cuboid mapping formulas.
     */
    void wideCuboidOffsetsMatchConfiguredFormulas() {
        assertOffset(
            ExcavationFaceGeometry.wideCuboidOffset(
                ExcavationFaceGeometry.FaceDirection.NORTH,
                ExcavationFaceGeometry.FaceDirection.NORTH,
                2,
                1,
                -1
            ),
            1,
            -1,
            2
        );
        assertOffset(
            ExcavationFaceGeometry.wideCuboidOffset(
                ExcavationFaceGeometry.FaceDirection.SOUTH,
                ExcavationFaceGeometry.FaceDirection.NORTH,
                2,
                1,
                -1
            ),
            -1,
            -1,
            -2
        );
        assertOffset(
            ExcavationFaceGeometry.wideCuboidOffset(
                ExcavationFaceGeometry.FaceDirection.EAST,
                ExcavationFaceGeometry.FaceDirection.NORTH,
                2,
                1,
                -1
            ),
            -2,
            -1,
            1
        );
        assertOffset(
            ExcavationFaceGeometry.wideCuboidOffset(
                ExcavationFaceGeometry.FaceDirection.WEST,
                ExcavationFaceGeometry.FaceDirection.NORTH,
                2,
                1,
                -1
            ),
            2,
            -1,
            -1
        );

        assertOffset(
            ExcavationFaceGeometry.wideCuboidOffset(
                ExcavationFaceGeometry.FaceDirection.UP,
                ExcavationFaceGeometry.FaceDirection.NORTH,
                2,
                1,
                -1
            ),
            1,
            1,
            2
        );
        assertOffset(
            ExcavationFaceGeometry.wideCuboidOffset(
                ExcavationFaceGeometry.FaceDirection.UP,
                ExcavationFaceGeometry.FaceDirection.SOUTH,
                2,
                1,
                -1
            ),
            -1,
            1,
            2
        );
        assertOffset(
            ExcavationFaceGeometry.wideCuboidOffset(
                ExcavationFaceGeometry.FaceDirection.UP,
                ExcavationFaceGeometry.FaceDirection.EAST,
                2,
                1,
                -1
            ),
            2,
            1,
            1
        );
        assertOffset(
            ExcavationFaceGeometry.wideCuboidOffset(
                ExcavationFaceGeometry.FaceDirection.UP,
                ExcavationFaceGeometry.FaceDirection.WEST,
                2,
                1,
                -1
            ),
            2,
            1,
            -1
        );
        assertOffset(
            ExcavationFaceGeometry.wideCuboidOffset(
                ExcavationFaceGeometry.FaceDirection.DOWN,
                ExcavationFaceGeometry.FaceDirection.NORTH,
                2,
                1,
                -1
            ),
            1,
            -1,
            2
        );
        assertOffset(
            ExcavationFaceGeometry.wideCuboidOffset(
                ExcavationFaceGeometry.FaceDirection.DOWN,
                ExcavationFaceGeometry.FaceDirection.SOUTH,
                2,
                1,
                -1
            ),
            -1,
            -1,
            2
        );
        assertOffset(
            ExcavationFaceGeometry.wideCuboidOffset(
                ExcavationFaceGeometry.FaceDirection.DOWN,
                ExcavationFaceGeometry.FaceDirection.EAST,
                2,
                1,
                -1
            ),
            2,
            -1,
            1
        );
        assertOffset(
            ExcavationFaceGeometry.wideCuboidOffset(
                ExcavationFaceGeometry.FaceDirection.DOWN,
                ExcavationFaceGeometry.FaceDirection.WEST,
                2,
                1,
                -1
            ),
            2,
            -1,
            -1
        );
    }

    /**
     * Floor anchoring should snap to player feet when origin is inside anchoring window.
     */
    @Test
    /**
     * Validate floor snap to feet.
     */
    void shaftFloorAnchorsToPlayerFeetWhenOriginIsWithinFeetPlusHeightMinusOne() {
        assertEquals(64, ShaftFloorGeometry.resolveFloorY(66, 64, 3));
        assertEquals(64, ShaftFloorGeometry.resolveFloorY(64, 64, 3));
    }

    /**
     * Origins above anchoring window keep their own Y level.
     */
    @Test
    /**
     * Validate high origins keep original floor.
     */
    void shaftFloorUsesOriginWhenOriginIsAboveFeetPlusHeightMinusOne() {
        assertEquals(67, ShaftFloorGeometry.resolveFloorY(67, 64, 3));
    }

    /**
     * Origins below feet should remain below feet; no forced snapping upward.
     */
    @Test
    /**
     * Validate below-feet origins stay below.
     */
    void shaftFloorUsesOriginWhenOriginIsBelowFeetLevel() {
        assertEquals(63, ShaftFloorGeometry.resolveFloorY(63, 64, 3));
    }

    /**
     * Convert raw offset arrays into deterministic string keys for set-based comparison assertions.
     */
    private static Set<String> toOffsetKeys(int[][] offsets) {
        Set<String> keys = new HashSet<>();
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        for (int[] offset : offsets) {
            keys.add(key(offset[0], offset[1], offset[2]));
        }
        return keys;
    }

    /**
     * Compare one produced offset with expected coordinates.
     */
    private static void assertOffset(int[] offset, int x, int y, int z) {
        assertEquals(key(x, y, z), key(offset[0], offset[1], offset[2]));
    }

    /**
     * Build compact coordinate key for readable assertion sets.
     */
    private static String key(int x, int y, int z) {
        return x + "," + y + "," + z;
    }
}
