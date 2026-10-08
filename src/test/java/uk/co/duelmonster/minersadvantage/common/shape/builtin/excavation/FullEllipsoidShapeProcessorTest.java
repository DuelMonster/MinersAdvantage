package uk.co.duelmonster.minersadvantage.common.shape.builtin.excavation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class FullEllipsoidShapeProcessorTest {
    @Test
    void oneByOneStartingLayerStillProducesFullDepth() {
        for (int depth = 0; depth < 5; depth++) {
            assertTrue(contains(1, 1, 5, depth, 0, 0));
        }
    }

    @Test
    void oneBlockWideStartingLayerStillExpandsVertically() {
        assertTrue(contains(1, 3, 5, 0, 0, 0));
        assertFalse(contains(1, 3, 5, 0, 0, 1));
        assertTrue(contains(1, 3, 5, 2, 0, 1));
        assertTrue(contains(1, 3, 5, 2, 0, -1));
    }

    @Test
    void oneBlockHighStartingLayerStillExpandsHorizontally() {
        assertTrue(contains(3, 1, 5, 0, 0, 0));
        assertFalse(contains(3, 1, 5, 0, 1, 0));
        assertTrue(contains(3, 1, 5, 2, -1, 0));
        assertTrue(contains(3, 1, 5, 2, 1, 0));
    }

    private static boolean contains(int width, int height, int depth, int depthIndex, int widthOffset,
            int heightOffset) {
        return FullEllipsoidShapeProcessor.isInsideEllipsoid(
            FullEllipsoidShapeProcessor.normalizedOffset(widthOffset, width),
            FullEllipsoidShapeProcessor.normalizedOffset(heightOffset, height),
            FullEllipsoidShapeProcessor.normalizedDepth(depthIndex, depth));
    }
}
