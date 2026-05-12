package uk.co.duelmonster.minersadvantage.common.config;

/**
 * IlluminationConfig keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public record IlluminationConfig(
    boolean enabled,
    int radiusHorizontal,
    int radiusVertical,
    int lowestLightLevel,
    boolean useBlockLight
) {
    /**
     * IlluminationConfig exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public IlluminationConfig(boolean enabled, int radiusHorizontal, int radiusVertical) {
        this(enabled, radiusHorizontal, radiusVertical, 7, true);
    }
}



