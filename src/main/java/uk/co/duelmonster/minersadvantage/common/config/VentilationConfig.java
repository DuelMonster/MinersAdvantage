package uk.co.duelmonster.minersadvantage.common.config;

public record VentilationConfig(
    boolean enabled,
    int radiusHorizontal,
    int radiusVertical,
    int processesPerTick,
    boolean placeLadders
) {
    public VentilationConfig(boolean enabled, int radiusHorizontal, int radiusVertical, int processesPerTick) {
        this(enabled, radiusHorizontal, radiusVertical, processesPerTick, true);
    }
}
