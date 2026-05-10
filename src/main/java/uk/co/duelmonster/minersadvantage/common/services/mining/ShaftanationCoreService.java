package uk.co.duelmonster.minersadvantage.common.services.mining;

public final class ShaftanationCoreService {
    public boolean isStone(String blockId) {
        return blockId.contains("stone") || blockId.contains("deepslate");
    }

    public int estimatedTurnsToShaft(int maxDepth) {
        return maxDepth * 2;
    }
}
