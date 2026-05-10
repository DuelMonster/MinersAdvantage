package uk.co.duelmonster.minersadvantage.common.config;

public record ExcavationConfig(
    boolean enabled,
    int radiusHorizontal,
    int radiusVertical,
    int processesPerTick
) {}
