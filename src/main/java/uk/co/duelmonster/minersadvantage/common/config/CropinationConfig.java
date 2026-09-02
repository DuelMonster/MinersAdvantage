package uk.co.duelmonster.minersadvantage.common.config;

/**
 * CropinationConfig keeps this part of MinersAdvantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public record CropinationConfig(
    boolean enabled,
    boolean harvestSeeds,
    int maxActiveAgents,
    boolean enforceAgentLimit) {
  /**
   * CropinationConfig exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public CropinationConfig(boolean enabled, boolean harvestSeeds) {
    this(enabled, harvestSeeds, 4, true);
  }

  public CropinationConfig {
    maxActiveAgents = Math.max(1, maxActiveAgents);
  }
}
