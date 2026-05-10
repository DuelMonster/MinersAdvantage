package uk.co.duelmonster.minersadvantage.common.config;

public record CaptivationConfig(
    boolean enabled,
    boolean allowInGUI,
    int radiusHorizontal,
    int radiusVertical,
    boolean isWhitelist,
    boolean unconditionalBlacklist
) {}
