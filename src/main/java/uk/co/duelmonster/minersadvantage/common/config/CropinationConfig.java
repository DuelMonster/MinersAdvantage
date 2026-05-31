package uk.co.duelmonster.minersadvantage.common.config;

/**
 * CropinationConfig keeps this part of MinersAdvantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public record CropinationConfig(
    boolean enabled,
    boolean harvestSeeds
) {}
