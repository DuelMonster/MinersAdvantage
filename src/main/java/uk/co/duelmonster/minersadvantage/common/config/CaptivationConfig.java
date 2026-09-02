package uk.co.duelmonster.minersadvantage.common.config;

import java.util.List;

/**
 * CaptivationConfig keeps this part of MinersAdvantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public record CaptivationConfig(
    boolean enabled,
    boolean allowInGUI,
    int radiusHorizontal,
    int radiusVertical,
    boolean isWhitelist,
    boolean unconditionalBlacklist,
    List<String> blacklist,
    int maxActiveAgents,
    boolean dedupeAgent
) {
    /**
     * CaptivationConfig exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public CaptivationConfig(boolean enabled, boolean allowInGUI, int radiusHorizontal, int radiusVertical, boolean isWhitelist, boolean unconditionalBlacklist) {
        this(enabled, allowInGUI, radiusHorizontal, radiusVertical, isWhitelist, unconditionalBlacklist, List.of("minecraft:rotten_flesh", "minecraft:egg"), 4, true);
    }

    public CaptivationConfig(boolean enabled, boolean allowInGUI, int radiusHorizontal, int radiusVertical, boolean isWhitelist, boolean unconditionalBlacklist, List<String> blacklist) {
        this(enabled, allowInGUI, radiusHorizontal, radiusVertical, isWhitelist, unconditionalBlacklist, blacklist, 4, true);
    }

    public CaptivationConfig {
        blacklist = blacklist == null ? List.of() : List.copyOf(blacklist);
        maxActiveAgents = Math.max(1, maxActiveAgents);
    }
}
