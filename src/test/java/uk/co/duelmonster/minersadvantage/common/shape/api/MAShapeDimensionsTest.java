package uk.co.duelmonster.minersadvantage.common.shape.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Tests for shared shape-dimension normalization helpers.
 */
class MAShapeDimensionsTest {
    /**
     * Excavation dimensions should preserve valid configured values exactly.
     */
    @Test
    /**
     * e xc av at io nd im en si on su se co nf ig ur ed wi dt hh ei gh td ep th exists so this path stays predictable and easier to debug when things get weird.
     */
    void excavationDimensionsUseConfiguredWidthHeightDepth() {
        MAShapeDimensions.Dimensions dimensions = MAShapeDimensions.excavationFromConfig(7, 5, 3);

        assertEquals(7, dimensions.width());
        assertEquals(5, dimensions.height());
        assertEquals(3, dimensions.depth());
    }

    /**
     * Excavation dimensions should clamp non-positive values to one.
     */
    @Test
    /**
     * e xc av at io nd im en si on sc la mp mi ni mu ms to on e exists so this path stays predictable and easier to debug when things get weird.
     */
    void excavationDimensionsClampMinimumsToOne() {
        MAShapeDimensions.Dimensions dimensions = MAShapeDimensions.excavationFromConfig(0, -4, 0);

        assertEquals(1, dimensions.width());
        assertEquals(1, dimensions.height());
        assertEquals(1, dimensions.depth());
    }

    /**
     * Shaft dimensions use the same lower-bound protection rules as excavation dimensions.
     */
    @Test
    /**
     * s ha ft di me ns io ns cl am pm in im um st oo ne exists so this path stays predictable and easier to debug when things get weird.
     */
    void shaftDimensionsClampMinimumsToOne() {
        MAShapeDimensions.Dimensions dimensions = MAShapeDimensions.shaftFromConfig(0, -2, 0);

        assertEquals(1, dimensions.width());
        assertEquals(1, dimensions.height());
        assertEquals(1, dimensions.depth());
    }
}
