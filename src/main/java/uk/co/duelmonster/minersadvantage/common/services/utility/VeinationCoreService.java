package uk.co.duelmonster.minersadvantage.common.services.utility;

public final class VeinationCoreService {
    public boolean sameVein(String blockId1, String blockId2) {
        return blockId1.equals(blockId2);
    }

    public int estimatedBlocksInVein(int maxVeinDistance, int foundCount) {
        return foundCount + (maxVeinDistance * 3);
    }
}
