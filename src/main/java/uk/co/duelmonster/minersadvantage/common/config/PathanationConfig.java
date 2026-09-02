package uk.co.duelmonster.minersadvantage.common.config;

/**
 * PathanationConfig keeps this part of MinersAdvantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public record PathanationConfig(
    boolean enabled,
    int targetBlockRange,
    int pathWidth,
    int maxActiveAgents,
    boolean dedupeAgent
) {
    /**
     * PathanationConfig exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public PathanationConfig(boolean enabled, int targetBlockRange) {
        this(enabled, targetBlockRange, 3, 4, true);
    }

    public PathanationConfig(boolean enabled, int targetBlockRange, int pathWidth) {
        this(enabled, targetBlockRange, pathWidth, 4, true);
    }

    public PathanationConfig {
        maxActiveAgents = Math.max(1, maxActiveAgents);
    }

    /**
     * pathLength exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public int pathLength() {
        return targetBlockRange;
    }
}
