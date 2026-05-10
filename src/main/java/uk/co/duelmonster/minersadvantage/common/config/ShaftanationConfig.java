package uk.co.duelmonster.minersadvantage.common.config;

public record ShaftanationConfig(
    boolean enabled,
    int maxDepth,
    int processesPerTick
) {}
