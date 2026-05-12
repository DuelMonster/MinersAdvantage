package uk.co.duelmonster.minersadvantage.common.services.tree;

/**
 * TreeCoreService keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class TreeCoreService {
    /**
     * isLikelyTree exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public boolean isLikelyTree(int connectedLogCount, int nearbyLeafCount, int trunkRange, int leafRange) {
        return connectedLogCount >= Math.max(2, trunkRange / 2)
            && nearbyLeafCount >= Math.max(1, leafRange / 2);
    }
}
