package uk.co.duelmonster.minersadvantage.common.services.utility;

public final class PathanationCoreService {
    public boolean isTargetBlock(String blockId) {
        return !blockId.contains("air") && !blockId.contains("bedrock");
    }

    public int estimatedTurnsToPath(int distance) {
        return Math.max(1, distance);
    }
}
