package uk.co.duelmonster.minersadvantage.common.services.world;

/**
 * WorldQueryService keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class WorldQueryService {
    /**
     * manhattanDistance exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public int manhattanDistance(int x1, int y1, int z1, int x2, int y2, int z2) {
        return Math.abs(x2 - x1) + Math.abs(y2 - y1) + Math.abs(z2 - z1);
    }

    /**
     * insideBox exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public boolean insideBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, int x, int y, int z) {
        return x >= minX && x <= maxX
            && y >= minY && y <= maxY
            && z >= minZ && z <= maxZ;
    }

    /**
     * normalizedOddWidth exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public int normalizedOddWidth(int configuredWidth) {
        int positive = Math.max(1, configuredWidth);
        return positive % 2 == 0 ? positive + 1 : positive;
    }
}



