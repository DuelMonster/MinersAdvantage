package uk.co.duelmonster.minersadvantage.common.shape.builtin.shaft;

/**
 * Tiny helper that settles the eternal argument between origin Y and player-feet Y when building shafts.
 */
public final class ShaftFloorGeometry {
    /**
     * Utility class only; if this gets instantiated, something has gone wonderfully off-script.
     */
    private ShaftFloorGeometry() {
    }

    /**
     * Anchor floors to player feet when origin is within shaft height window, otherwise honor explicit origin.
     */
    public static int resolveFloorY(int originY, int playerFeetY, int shaftHeight) {
        // Never allow zero-height shafts; that just produces existential geometry.
        int clampedHeight = Math.max(1, shaftHeight);
        int maxAnchoredY = playerFeetY + clampedHeight - 1;
        return originY >= playerFeetY && originY <= maxAnchoredY ? playerFeetY : originY;
    }

    /**
     * Resolve the relative floor offset (floorY - originY) used by shaft geometry.
     */
    public static int resolveFloorOffset(int originY, int playerFeetY, int shaftHeight) {
        return resolveFloorY(originY, playerFeetY, shaftHeight) - originY;
    }
}