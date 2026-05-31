package uk.co.duelmonster.minersadvantage.common.shape.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MAShapeDimensionsTest {
    @Test
    void excavationDimensionsUseConfiguredWidthHeightDepth() {
        MAShapeDimensions.Dimensions dimensions = MAShapeDimensions.excavationFromConfig(7, 5, 3);

        assertEquals(7, dimensions.width());
        assertEquals(5, dimensions.height());
        assertEquals(3, dimensions.depth());
    }

    @Test
    void excavationDimensionsClampMinimumsToOne() {
        MAShapeDimensions.Dimensions dimensions = MAShapeDimensions.excavationFromConfig(0, -4, 0);

        assertEquals(1, dimensions.width());
        assertEquals(1, dimensions.height());
        assertEquals(1, dimensions.depth());
    }

    @Test
    void shaftDimensionsClampMinimumsToOne() {
        MAShapeDimensions.Dimensions dimensions = MAShapeDimensions.shaftFromConfig(0, -2, 0);

        assertEquals(1, dimensions.width());
        assertEquals(1, dimensions.height());
        assertEquals(1, dimensions.depth());
    }
}
