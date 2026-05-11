package uk.co.duelmonster.minersadvantage.common.config;

/**
 * ServerOverridesConfig keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public record ServerOverridesConfig(
    boolean overrideFeatureEnablement,
    boolean enforceCommonSettings,
    boolean enforceCaptivationSettings,
    boolean enforceCropinationSettings,
    boolean enforceCultivationSettings,
    boolean enforceExcavationSettings,
    boolean enforcePathanationSettings,
    boolean enforceIlluminationSettings,
    boolean enforceLumbinationSettings,
    boolean enforceShaftanationSettings,
    boolean enforceSubstitutionSettings,
    boolean enforceVeinationSettings,
    boolean enforceVentilationSettings
) {
    /**
     * ServerOverridesConfig exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public ServerOverridesConfig() {
        this(false, false, false, false, false, false, false, false, false, false, false, false, false);
    }
}

