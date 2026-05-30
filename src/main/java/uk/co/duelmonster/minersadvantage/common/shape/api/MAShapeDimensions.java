package uk.co.duelmonster.minersadvantage.common.shape.api;

/**
 * MAShapeDimensions centralizes shared shape dimension math used by runtime and preview paths.
 */
public final class MAShapeDimensions {
    /**
     * Dimensions carries normalized width, height, and depth values for a shape context.
     */
    public record Dimensions(int width, int height, int depth) {
    }

    private MAShapeDimensions() {
    }

    public static Dimensions excavationFromRadii(int horizontalRadius, int verticalRadius) {
        int radiusHorizontal = Math.max(1, horizontalRadius);
        int radiusVertical = Math.max(1, verticalRadius);
        return new Dimensions((radiusHorizontal * 2) + 1, (radiusVertical * 2) + 1, radiusHorizontal);
    }

    public static Dimensions shaftFromConfig(int shaftWidth, int shaftHeight, int maxDepth) {
        return new Dimensions(Math.max(1, shaftWidth), Math.max(1, shaftHeight), Math.max(1, maxDepth));
    }
}
