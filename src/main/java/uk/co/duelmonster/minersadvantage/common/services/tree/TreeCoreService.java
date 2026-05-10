package uk.co.duelmonster.minersadvantage.common.services.tree;

public final class TreeCoreService {
    public boolean isLikelyTree(int connectedLogCount, int nearbyLeafCount, int trunkRange, int leafRange) {
        return connectedLogCount >= Math.max(2, trunkRange / 2)
            && nearbyLeafCount >= Math.max(1, leafRange / 2);
    }
}
