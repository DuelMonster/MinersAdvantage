package uk.co.duelmonster.minersadvantage.common.shape.builtin.excavation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * Focused tests for Shapeless helper behavior used by connected-neighbor traversal.
 */
class ShapelessShapeProcessorTest {
    /**
     * Ensure precomputed offset table remains an 18-neighbor shell (faces + edges, no corners).
     */
    @Test
    /**
     * Validate shapeless neighbor offset table integrity.
     */
    void createNeighborOffsetsProducesAllEighteenNeighborsWithoutOriginOrCorners() {
        int[][] offsets = ShapelessShapeProcessor.createNeighborOffsets();

        assertEquals(18, offsets.length);

        Set<String> keys = new HashSet<>();
        for (int[] offset : offsets) {
            assertFalse(offset[0] == 0 && offset[1] == 0 && offset[2] == 0);
            assertFalse(offset[0] != 0 && offset[1] != 0 && offset[2] != 0);
            keys.add(key(offset[0], offset[1], offset[2]));
        }

        assertEquals(18, keys.size());
    }

    /**
     * Neighbor ordering should be non-decreasing by Manhattan distance to absolute origin.
     */
    @Test
    /**
     * Validate LiteMiner-style neighbor sort ordering.
     */
    void orderedNeighborsSortsByDistanceToAbsoluteOrigin() {
        int[][] neighbors = ShapelessShapeProcessor.orderedNeighborCoordinates(8, 6, 4, 0, 0, 0);
        assertEquals(18, neighbors.length);

        int lastDistance = -1;
        for (int[] neighbor : neighbors) {
            int distance = ShapelessShapeProcessor.manhattanDistance(neighbor[0], neighbor[1], neighbor[2], 0, 0, 0);
            assertTrue(distance >= lastDistance);
            lastDistance = distance;
        }

        Set<String> unique = new HashSet<>();
        for (int[] neighbor : neighbors) {
            unique.add(key(neighbor[0], neighbor[1], neighbor[2]));
        }
        assertEquals(18, unique.size());
    }

    /**
     * Manhattan distance utility should match coordinate-delta sum semantics.
     */
    @Test
    /**
     * Validate Manhattan distance helper.
     */
    void manhattanDistanceSumsAbsoluteAxisDeltas() {
        assertEquals(12, ShapelessShapeProcessor.manhattanDistance(3, -2, 9, -1, 1, 4));
        assertEquals(12, ShapelessShapeProcessor.manhattanDistance(-1, 1, 4, 3, -2, 9));
    }

    /**
     * Origin air state from the initiating break should still seed shapeless traversal.
     */
    @Test
    /**
     * Validate broken-origin seed behavior.
     */
    void brokenOriginIsAllowedAsTraversalSeed() {
        int originX = 10;
        int originY = 64;
        int originZ = -3;

        assertTrue(
            ShapelessShapeProcessor.shouldTreatBrokenOriginAsMatch(
                originX,
                originY,
                originZ,
                originX,
                originY,
                originZ,
                true
            )
        );
        assertFalse(
            ShapelessShapeProcessor.shouldTreatBrokenOriginAsMatch(
                originX + 1,
                originY,
                originZ,
                originX,
                originY,
                originZ,
                true
            )
        );
        assertFalse(
            ShapelessShapeProcessor.shouldTreatBrokenOriginAsMatch(
                originX,
                originY,
                originZ,
                originX,
                originY,
                originZ,
                false
            )
        );
    }

    /**
     * Build compact coordinate key for uniqueness checks.
     */
    private static String key(int x, int y, int z) {
        return x + "," + y + "," + z;
    }
}
