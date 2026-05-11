package uk.co.duelmonster.minersadvantage.common.config;

/**
 * ClientConfig keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public record ClientConfig(
    boolean disableParticleEffects
) {
    /**
     * ClientConfig exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public ClientConfig() {
        this(false);
    }
}

