package uk.co.duelmonster.minersadvantage.common.config;

/**
 * IlluminationConfig keeps this part of MinersAdvantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public record IlluminationConfig(
    boolean enabled,
    int radiusHorizontal,
    int radiusVertical,
    int lowestLightLevel,
    boolean useBlockLight,
    int maxActiveAgents,
    boolean enforceAgentLimit) {
  /**
   * IlluminationConfig exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public IlluminationConfig(boolean enabled, int radiusHorizontal, int radiusVertical) {
    this(enabled, radiusHorizontal, radiusVertical, 1, true, 4, true);
  }

  public IlluminationConfig(boolean enabled, int radiusHorizontal, int radiusVertical, int lowestLightLevel,
      boolean useBlockLight) {
    this(enabled, radiusHorizontal, radiusVertical, lowestLightLevel, useBlockLight, 4, true);
  }

  public IlluminationConfig {
    maxActiveAgents = Math.max(1, maxActiveAgents);
  }
}
