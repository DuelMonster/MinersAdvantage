package uk.co.duelmonster.minersadvantage.common.config;

import uk.co.duelmonster.minersadvantage.common.config.defaults.MAConfig_Defaults;

/**
 * ClientConfig keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public record ClientConfig(
    boolean disableParticleEffects,
    int outlineForegroundColor,
    int outlineSeeThroughColor
) {
    /**
     * ClientConfig exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public ClientConfig() {
        this(
            MAConfig_Defaults.Client.disableParticleEffects,
            MAConfig_Defaults.Client.outlineForegroundColorArgb,
            MAConfig_Defaults.Client.outlineSeeThroughColorArgb
        );
    }
}
