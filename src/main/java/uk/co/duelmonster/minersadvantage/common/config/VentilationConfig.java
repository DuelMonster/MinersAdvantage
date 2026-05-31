package uk.co.duelmonster.minersadvantage.common.config;

/**
 * VentilationConfig keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public record VentilationConfig(
    boolean enabled,
    int width,
    int height,
    int depth,
    int processesPerTick,
    boolean placeLadders
) {
    /**
     * VentilationConfig exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public VentilationConfig(boolean enabled, int width, int height, int depth, int processesPerTick) {
        this(enabled, width, height, depth, processesPerTick, true);
    }
}
