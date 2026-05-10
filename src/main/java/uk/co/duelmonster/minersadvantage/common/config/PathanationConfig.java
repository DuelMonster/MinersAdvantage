package uk.co.duelmonster.minersadvantage.common.config;

public record PathanationConfig(
    boolean enabled,
    int targetBlockRange,
    int pathWidth
) {
    public PathanationConfig(boolean enabled, int targetBlockRange) {
        this(enabled, targetBlockRange, 3);
    }

    public int pathLength() {
        return targetBlockRange;
    }
}
