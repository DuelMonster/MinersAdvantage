package uk.co.duelmonster.minersadvantage.common.services.world;

public final class WorldQueryService {
    public int manhattanDistance(int x1, int y1, int z1, int x2, int y2, int z2) {
        return Math.abs(x2 - x1) + Math.abs(y2 - y1) + Math.abs(z2 - z1);
    }

    public boolean insideBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, int x, int y, int z) {
        return x >= minX && x <= maxX
            && y >= minY && y <= maxY
            && z >= minZ && z <= maxZ;
    }

    public int normalizedOddWidth(int configuredWidth) {
        int positive = Math.max(1, configuredWidth);
        return positive % 2 == 0 ? positive + 1 : positive;
    }
}
