package uk.co.duelmonster.minersadvantage.common.config;

public record VentilationConfig(
    boolean enabled,
    int radiusHorizontal,
    int radiusVertical,
    int processesPerTick
) {}
