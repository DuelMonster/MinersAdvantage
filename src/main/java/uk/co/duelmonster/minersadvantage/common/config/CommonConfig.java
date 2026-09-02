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
    int maxActiveCaptivationAgent,
    int maxActiveCropinationAgent,
    int maxActiveCultivationAgent,
    int maxActiveExcavationAgent,
    int maxActiveIlluminationAgent,
    int maxActiveLumbinationAgent,
    int maxActivePathanationAgent,
    int maxActiveShaftanationAgent,
    int maxActiveSubstitutionAgent,
    int maxActiveVeinationAgent,
    int maxActiveVentilationAgent,
    boolean dedupeCaptivationAgent,
    boolean dedupeCropinationAgent,
    boolean dedupeCultivationAgent,
    boolean dedupeExcavationAgent,
    boolean dedupeIlluminationAgent,
    boolean dedupeLumbinationAgent,
    boolean dedupePathanationAgent,
    boolean dedupeShaftanationAgent,
    boolean dedupeSubstitutionAgent,
    boolean dedupeVeinationAgent,
    boolean dedupeVentilationAgent) {
  /**
   * CommonConfig exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public CommonConfig() {
    this(true, false, true, true, 1, 1, true, 5, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
        true, true, true, true, true, true, true, true, true, true, true);
  }

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
    this(tpsGuard, gatherDrops, autoIlluminate, mineVeins, ticksPerBlock, maxBlocksPerTick, enableTickDelay,
        tickDelay, blockRadius, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
        true, true, true, true, true, true, true, true, true, true, true);
  }

  public CommonConfig {
    maxActiveCaptivationAgent = Math.max(1, maxActiveCaptivationAgent);
    maxActiveCropinationAgent = Math.max(1, maxActiveCropinationAgent);
    maxActiveCultivationAgent = Math.max(1, maxActiveCultivationAgent);
    maxActiveExcavationAgent = Math.max(1, maxActiveExcavationAgent);
    maxActiveIlluminationAgent = Math.max(1, maxActiveIlluminationAgent);
    maxActiveLumbinationAgent = Math.max(1, maxActiveLumbinationAgent);
    maxActivePathanationAgent = Math.max(1, maxActivePathanationAgent);
    maxActiveShaftanationAgent = Math.max(1, maxActiveShaftanationAgent);
    maxActiveSubstitutionAgent = Math.max(1, maxActiveSubstitutionAgent);
    maxActiveVeinationAgent = Math.max(1, maxActiveVeinationAgent);
    maxActiveVentilationAgent = Math.max(1, maxActiveVentilationAgent);
  }

  /**
   * Resolve whether a given runtime agent type is allowed to dedupe to a single instance.
   */
  public boolean isAgentTypeDeduplicationEnabled(Class<?> agentType) {
    if (agentType == null) {
      return true;
    }

    String simpleName = agentType.getSimpleName();
    return switch (simpleName) {
      case "CaptivationAgent" -> dedupeCaptivationAgent;
      case "CropinationAgent" -> dedupeCropinationAgent;
      case "CultivationAgent" -> dedupeCultivationAgent;
      case "ExcavationAgent" -> dedupeExcavationAgent;
      case "IlluminationAgent" -> dedupeIlluminationAgent;
      case "LumbinationAgent" -> dedupeLumbinationAgent;
      case "PathanationAgent" -> dedupePathanationAgent;
      case "ShaftanationAgent" -> dedupeShaftanationAgent;
      case "SubstitutionAgent" -> dedupeSubstitutionAgent;
      case "VeinationAgent" -> dedupeVeinationAgent;
      case "VentilationAgent" -> dedupeVentilationAgent;
      default -> true;
    };
  }

  public int maxActiveAgentsForType(Class<?> agentType) {
    if (agentType == null) {
      return 1;
    }

    String simpleName = agentType.getSimpleName();
    return switch (simpleName) {
      case "CaptivationAgent" -> maxActiveCaptivationAgent;
      case "CropinationAgent" -> maxActiveCropinationAgent;
      case "CultivationAgent" -> maxActiveCultivationAgent;
      case "ExcavationAgent" -> maxActiveExcavationAgent;
      case "IlluminationAgent" -> maxActiveIlluminationAgent;
      case "LumbinationAgent" -> maxActiveLumbinationAgent;
      case "PathanationAgent" -> maxActivePathanationAgent;
      case "ShaftanationAgent" -> maxActiveShaftanationAgent;
      case "SubstitutionAgent" -> maxActiveSubstitutionAgent;
      case "VeinationAgent" -> maxActiveVeinationAgent;
      case "VentilationAgent" -> maxActiveVentilationAgent;
      default -> 1;
    };
  }

}
