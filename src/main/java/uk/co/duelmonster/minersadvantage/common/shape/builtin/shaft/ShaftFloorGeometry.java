package uk.co.duelmonster.minersadvantage.common.shape.builtin.shaft;

public final class ShaftFloorGeometry {
    private ShaftFloorGeometry() {
    }

    public static int resolveFloorY(int originY, int playerFeetY, int shaftHeight) {
        int clampedHeight = Math.max(1, shaftHeight);
        int maxAnchoredY = playerFeetY + clampedHeight - 1;
        return originY >= playerFeetY && originY <= maxAnchoredY ? playerFeetY : originY;
    }
}