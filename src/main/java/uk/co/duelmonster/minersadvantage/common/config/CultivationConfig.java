package uk.co.duelmonster.minersadvantage.common.config;

/**
 * CultivationConfig keeps this part of MinersAdvantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public record CultivationConfig(
    boolean enabled,
    int hydrationDistance,
    int maxActiveAgents,
    boolean dedupeAgent
) {
    /**
     * CultivationConfig exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public CultivationConfig(boolean enabled, int hydrationDistance) {
        this(enabled, hydrationDistance, 4, true);
    }

    public CultivationConfig {
        maxActiveAgents = Math.max(1, maxActiveAgents);
    }
}
