package uk.co.duelmonster.minersadvantage.common.config;

import java.util.List;

/**
 * SubstitutionConfig keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public record SubstitutionConfig(
    boolean enabled,
    boolean allowMending,
    boolean prioritizeSilkTouch,
    boolean switchBack,
    boolean favourFortune,
    boolean ignoreIfValidTool,
    boolean ignorePassiveMobs,
    List<String> blacklist
) {
    /**
     * SubstitutionConfig exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public SubstitutionConfig(boolean enabled, boolean allowMending, boolean prioritizeSilkTouch) {
        this(enabled, allowMending, prioritizeSilkTouch, true, true, true, true, List.of());
    }

    public SubstitutionConfig {
        blacklist = blacklist == null ? List.of() : List.copyOf(blacklist);
    }

    /**
     * favourSilkTouch exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public boolean favourSilkTouch() {
        return prioritizeSilkTouch;
    }
}

