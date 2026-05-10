package uk.co.duelmonster.minersadvantage.common.config;

public record LumbinationConfig(
    boolean enabled,
    int maxTrunkRange,
    int maxLeafRange,
    int processesPerTick
) {}
