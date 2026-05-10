package uk.co.duelmonster.minersadvantage.common.config;

public record SubstitutionConfig(
    boolean enabled,
    boolean allowMending,
    boolean prioritizeSilkTouch
) {}
