package uk.co.duelmonster.minersadvantage.common.config;

import java.util.List;

public record CaptivationConfig(
    boolean enabled,
    boolean allowInGUI,
    int radiusHorizontal,
    int radiusVertical,
    boolean isWhitelist,
    boolean unconditionalBlacklist,
    List<String> blacklist
) {
    public CaptivationConfig(boolean enabled, boolean allowInGUI, int radiusHorizontal, int radiusVertical, boolean isWhitelist, boolean unconditionalBlacklist) {
        this(enabled, allowInGUI, radiusHorizontal, radiusVertical, isWhitelist, unconditionalBlacklist, List.of("minecraft:rotten_flesh", "minecraft:egg"));
    }

    public CaptivationConfig {
        blacklist = blacklist == null ? List.of() : List.copyOf(blacklist);
    }
}
