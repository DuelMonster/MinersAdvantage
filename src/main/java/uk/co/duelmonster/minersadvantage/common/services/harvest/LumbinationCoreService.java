package uk.co.duelmonster.minersadvantage.common.services.harvest;

public final class LumbinationCoreService {
    public boolean isLog(String blockId) {
        return blockId.contains("log") || blockId.contains("stem");
    }

    public boolean isLeaf(String blockId) {
        return blockId.contains("leaf") || blockId.contains("leaves");
    }

    public boolean isSapling(String blockId) {
        return blockId.contains("sapling");
    }

    public int estimatedTurnsToFell(int connectedLogCount) {
        return Math.max(1, connectedLogCount / 2);
    }
}
