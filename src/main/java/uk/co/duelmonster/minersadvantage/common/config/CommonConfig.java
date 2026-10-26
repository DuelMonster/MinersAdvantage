package uk.co.duelmonster.minersadvantage.common.config;

/**
 * CommonConfig keeps this part of MinersAdvantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public record CommonConfig(
    boolean tpsGuard,
    boolean gatherDrops,
    boolean autoIlluminate,
    boolean mineVeins,
    int ticksPerBlock,
    int maxBlocksPerTick,
    boolean enableTickDelay,
    int tickDelay,
    int blockRadius,
    boolean toolPreservationEnabled,
    int toolPreservationReserve) {
  /**
   * Keep older call sites source-compatible while giving the new safeguard its safe defaults.
   */
  public CommonConfig(
      boolean tpsGuard,
      boolean gatherDrops,
      boolean autoIlluminate,
      boolean mineVeins,
      int ticksPerBlock,
      int maxBlocksPerTick,
      boolean enableTickDelay,
      int tickDelay,
      int blockRadius) {
    this(tpsGuard, gatherDrops, autoIlluminate, mineVeins, ticksPerBlock, maxBlocksPerTick, enableTickDelay, tickDelay,
        blockRadius, true, 20);
  }

  /**
   * CommonConfig exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public CommonConfig() {
    this(true, false, true, true, 1, 1, true, 5, 3, true, 20);
  }
}
