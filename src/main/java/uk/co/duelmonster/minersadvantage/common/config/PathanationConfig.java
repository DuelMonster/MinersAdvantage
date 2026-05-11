package uk.co.duelmonster.minersadvantage.common.config;

/**
 * PathanationConfig keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public record PathanationConfig(
    boolean enabled,
    int targetBlockRange,
    int pathWidth
) {
    /**
     * PathanationConfig exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public PathanationConfig(boolean enabled, int targetBlockRange) {
        this(enabled, targetBlockRange, 3);
    }

    /**
     * pathLength exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public int pathLength() {
        return targetBlockRange;
    }
}

