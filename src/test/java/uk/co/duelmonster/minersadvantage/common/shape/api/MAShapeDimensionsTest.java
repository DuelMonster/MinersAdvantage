package uk.co.duelmonster.minersadvantage.common.shape.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MAShapeDimensionsTest {
    @Test
    void excavationDimensionsAreCenteredAndDepthUsesHorizontalRadius() {
        MAShapeDimensions.Dimensions dimensions = MAShapeDimensions.excavationFromRadii(3, 2);

        assertEquals(7, dimensions.width());
        assertEquals(5, dimensions.height());
        assertEquals(3, dimensions.depth());
    }

    @Test
    void excavationDimensionsClampMinimumsToOne() {
        MAShapeDimensions.Dimensions dimensions = MAShapeDimensions.excavationFromRadii(0, -4);

        assertEquals(3, dimensions.width());
        assertEquals(3, dimensions.height());
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
