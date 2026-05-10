package uk.co.duelmonster.minersadvantage.common.config;

public record CommonConfig(
    boolean tpsGuard,
    boolean gatherDrops,
    boolean autoIlluminate,
    boolean mineVeins,
    int blocksPerTick,
    boolean enableTickDelay,
    int tickDelay,
    int blockRadius,
    int blockLimit
) {
    public CommonConfig() {
        this(true, false, true, true, 1, true, 5, 3, 64);
    }
}
