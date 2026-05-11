package uk.co.duelmonster.minersadvantage.common.config;

import java.util.List;

/**
 * CaptivationConfig keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public record CaptivationConfig(
    boolean enabled,
    boolean allowInGUI,
    int radiusHorizontal,
    int radiusVertical,
    boolean isWhitelist,
    boolean unconditionalBlacklist,
    List<String> blacklist
) {
    /**
     * CaptivationConfig exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public CaptivationConfig(boolean enabled, boolean allowInGUI, int radiusHorizontal, int radiusVertical, boolean isWhitelist, boolean unconditionalBlacklist) {
        this(enabled, allowInGUI, radiusHorizontal, radiusVertical, isWhitelist, unconditionalBlacklist, List.of("minecraft:rotten_flesh", "minecraft:egg"));
    }

    public CaptivationConfig {
        blacklist = blacklist == null ? List.of() : List.copyOf(blacklist);
    }
}

