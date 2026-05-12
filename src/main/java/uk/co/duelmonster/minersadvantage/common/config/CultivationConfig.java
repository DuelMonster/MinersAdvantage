package uk.co.duelmonster.minersadvantage.common.config;

/**
 * CultivationConfig keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public record CultivationConfig(
    boolean enabled,
    int hydrationDistance
) {}
