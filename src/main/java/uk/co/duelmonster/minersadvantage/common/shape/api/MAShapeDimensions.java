package uk.co.duelmonster.minersadvantage.common.shape.api;

/**
 * Shared dimension sanitizer so runtime and preview code stop duplicating the same "at least 1" math.
 */
public final class MAShapeDimensions {
    /**
     * Immutable dimension tuple used by shape context builders.
     */
    public record Dimensions(int width, int height, int depth) {
    }

    /**
     * Utility class only; static helpers keep call sites tiny and boring.
     */
    private MAShapeDimensions() {
    }

    /**
     * Normalize excavation dimensions so zero or negative config values cannot break shape computation.
     */
    public static Dimensions excavationFromConfig(int width, int height, int depth) {
        return new Dimensions(Math.max(1, width), Math.max(1, height), Math.max(1, depth));
    }

    /**
     * Normalize shaft dimensions using the same lower-bound rules as excavation.
     */
    public static Dimensions shaftFromConfig(int width, int height, int depth) {
        return new Dimensions(Math.max(1, width), Math.max(1, height), Math.max(1, depth));
    }
}
