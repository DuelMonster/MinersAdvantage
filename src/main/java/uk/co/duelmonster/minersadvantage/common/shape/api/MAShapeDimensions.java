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

    public static Dimensions excavationFromConfig(int width, int height, int depth) {
        return new Dimensions(Math.max(1, width), Math.max(1, height), Math.max(1, depth));
    }

    public static Dimensions shaftFromConfig(int width, int height, int depth) {
        return new Dimensions(Math.max(1, width), Math.max(1, height), Math.max(1, depth));
    }
}
