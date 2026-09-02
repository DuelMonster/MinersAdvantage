package uk.co.duelmonster.minersadvantage.common.config;

import uk.co.duelmonster.minersadvantage.common.config.defaults.MAConfig_Defaults;

/**
 * SyncedClientConfig keeps this part of MinersAdvantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public record SyncedClientConfig(
    ClientConfig client,
    CommonConfig common,
    CaptivationConfig captivation,
    CropinationConfig cropination,
    CultivationConfig cultivation,
    ExcavationConfig excavation,
    PathanationConfig pathanation,
    IlluminationConfig illumination,
    LumbinationConfig lumbination,
    ShaftanationConfig shaftanation,
    SubstitutionConfig substitution,
    VeinationConfig veination,
    VentilationConfig ventilation) {
  // Fixed cap used for features without a configurable agent limit (Captivation, Substitution).
  private static final int DEFAULT_MAX_ACTIVE_AGENTS = 4;

  /**
   * defaults exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public static SyncedClientConfig defaults() {
    return new SyncedClientConfig(
        new ClientConfig(
            MAConfig_Defaults.Client.disableParticleEffects,
            MAConfig_Defaults.Client.debugLogging,
            MAConfig_Defaults.Client.outlineForegroundColorArgb,
            MAConfig_Defaults.Client.outlineSeeThroughColorArgb),
        new CommonConfig(
            MAConfig_Defaults.Common.tpsGuard,
            MAConfig_Defaults.Common.gatherDrops,
            MAConfig_Defaults.Common.autoIlluminate,
            MAConfig_Defaults.Common.mineVeins,
            MAConfig_Defaults.Common.ticksPerBlock,
            MAConfig_Defaults.Common.maxBlocksPerTick,
            MAConfig_Defaults.Common.enableTickDelay,
            MAConfig_Defaults.Common.tickDelay,
            MAConfig_Defaults.Common.blockRadius),
        new CaptivationConfig(
            MAConfig_Defaults.Captivation.enabled,
            MAConfig_Defaults.Captivation.allowInGUI,
            MAConfig_Defaults.Captivation.radiusHorizontal,
            MAConfig_Defaults.Captivation.radiusVertical,
            MAConfig_Defaults.Captivation.isWhitelist,
            MAConfig_Defaults.Captivation.unconditionalBlacklist,
            MAConfig_Defaults.Captivation.blacklist),
        new CropinationConfig(
            MAConfig_Defaults.Cropination.enabled,
            MAConfig_Defaults.Cropination.harvestSeeds,
            MAConfig_Defaults.Cropination.maxActiveAgents,
            MAConfig_Defaults.Cropination.enforceAgentLimit),
        new CultivationConfig(
            MAConfig_Defaults.Cultivation.enabled,
            MAConfig_Defaults.Cultivation.hydrationDistance,
            MAConfig_Defaults.Cultivation.maxActiveAgents,
            MAConfig_Defaults.Cultivation.enforceAgentLimit),
        new ExcavationConfig(
            MAConfig_Defaults.Excavation.enabled,
            MAConfig_Defaults.Excavation.width,
            MAConfig_Defaults.Excavation.height,
            MAConfig_Defaults.Excavation.depth,
            MAConfig_Defaults.Excavation.processesPerTick,
            MAConfig_Defaults.Excavation.toggleMode,
            MAConfig_Defaults.Excavation.ignoreBlockVariants,
            MAConfig_Defaults.Excavation.isBlockWhitelist,
            MAConfig_Defaults.Excavation.blockBlacklist,
            MAConfig_Defaults.Excavation.maxActiveAgents,
            MAConfig_Defaults.Excavation.enforceAgentLimit),
        new PathanationConfig(
            MAConfig_Defaults.Pathanation.enabled,
            MAConfig_Defaults.Pathanation.targetBlockRange,
            MAConfig_Defaults.Pathanation.pathWidth,
            MAConfig_Defaults.Pathanation.maxActiveAgents,
            MAConfig_Defaults.Pathanation.enforceAgentLimit),
        new IlluminationConfig(
            MAConfig_Defaults.Illumination.enabled,
            MAConfig_Defaults.Illumination.radiusHorizontal,
            MAConfig_Defaults.Illumination.radiusVertical,
            MAConfig_Defaults.Illumination.lowestLightLevel,
            MAConfig_Defaults.Illumination.useBlockLight,
            MAConfig_Defaults.Illumination.maxActiveAgents,
            MAConfig_Defaults.Illumination.enforceAgentLimit),
        new LumbinationConfig(
            MAConfig_Defaults.Lumbination.enabled,
            MAConfig_Defaults.Lumbination.maxTrunkRange,
            MAConfig_Defaults.Lumbination.maxLeafRange,
            MAConfig_Defaults.Lumbination.processesPerTick,
            MAConfig_Defaults.Lumbination.chopTreeBelow,
            MAConfig_Defaults.Lumbination.destroyLeaves,
            MAConfig_Defaults.Lumbination.leavesAffectDurability,
            MAConfig_Defaults.Lumbination.replantSaplings,
            MAConfig_Defaults.Lumbination.useCanopyTool,
            MAConfig_Defaults.Lumbination.ignorePlayerPlacedLeaves,
            MAConfig_Defaults.Lumbination.logs,
            MAConfig_Defaults.Lumbination.leaves,
            MAConfig_Defaults.Lumbination.axes,
            MAConfig_Defaults.Lumbination.maxActiveAgents,
            MAConfig_Defaults.Lumbination.enforceAgentLimit),
        new ShaftanationConfig(
            MAConfig_Defaults.Shaftanation.enabled,
            MAConfig_Defaults.Shaftanation.depth,
            MAConfig_Defaults.Shaftanation.processesPerTick,
            MAConfig_Defaults.Shaftanation.width,
            MAConfig_Defaults.Shaftanation.height,
            MAConfig_Defaults.Shaftanation.torchPlacement,
            MAConfig_Defaults.Shaftanation.maxActiveAgents,
            MAConfig_Defaults.Shaftanation.enforceAgentLimit),
        new SubstitutionConfig(
            MAConfig_Defaults.Substitution.enabled,
            MAConfig_Defaults.Substitution.allowMending,
            MAConfig_Defaults.Substitution.prioritizeSilkTouch,
            MAConfig_Defaults.Substitution.switchBack,
            MAConfig_Defaults.Substitution.favourFortune,
            MAConfig_Defaults.Substitution.ignoreIfValidTool,
            MAConfig_Defaults.Substitution.ignorePassiveMobs,
            MAConfig_Defaults.Substitution.blacklist,
            MAConfig_Defaults.Substitution.blockBlacklist,
            MAConfig_Defaults.Substitution.selectionRules),
        new VeinationConfig(
            MAConfig_Defaults.Veination.enabled,
            MAConfig_Defaults.Veination.maxVeinDistance,
            MAConfig_Defaults.Veination.ores,
            MAConfig_Defaults.Veination.oreHarvestWithoutSneak,
            MAConfig_Defaults.Veination.dropOresAtFirstBrokenBlock,
            MAConfig_Defaults.Veination.increaseHarvestingTimePerOre,
            MAConfig_Defaults.Veination.increasedHarvestingTimePerOreModifier,
            MAConfig_Defaults.Veination.pickaxeBlacklist,
            MAConfig_Defaults.Veination.maxActiveAgents,
            MAConfig_Defaults.Veination.enforceAgentLimit),
        new VentilationConfig(
            MAConfig_Defaults.Ventilation.enabled,
            MAConfig_Defaults.Ventilation.width,
            MAConfig_Defaults.Ventilation.height,
            MAConfig_Defaults.Ventilation.depth,
            MAConfig_Defaults.Ventilation.processesPerTick,
            MAConfig_Defaults.Ventilation.placeLadders,
            MAConfig_Defaults.Ventilation.maxActiveAgents,
            MAConfig_Defaults.Ventilation.enforceAgentLimit));
  }

  /**
   * Resolve whether the configured max-active-agents cap should be enforced for a given runtime agent type.
   * When disabled, that agent type may queue an unlimited number of concurrent instances.
   */
  public boolean isAgentLimitEnforced(Class<?> agentType) {
    if (agentType == null) {
      return true;
    }

    String simpleName = agentType.getSimpleName();
    return switch (simpleName) {
      case "CropinationAgent" -> cropination.enforceAgentLimit();
      case "CultivationAgent" -> cultivation.enforceAgentLimit();
      case "ExcavationAgent" -> excavation.enforceAgentLimit();
      case "IlluminationAgent" -> illumination.enforceAgentLimit();
      case "LumbinationAgent" -> lumbination.enforceAgentLimit();
      case "PathanationAgent" -> pathanation.enforceAgentLimit();
      case "ShaftanationAgent" -> shaftanation.enforceAgentLimit();
      case "VeinationAgent" -> veination.enforceAgentLimit();
      case "VentilationAgent" -> ventilation.enforceAgentLimit();
      default -> true;
    };
  }

  /**
   * Resolve the configured concurrent-instance cap for a given runtime agent type.
   * Features without a configurable cap (e.g. Captivation, Substitution) fall back to a fixed default.
   */
  public int maxActiveAgentsForType(Class<?> agentType) {
    if (agentType == null) {
      return DEFAULT_MAX_ACTIVE_AGENTS;
    }

    String simpleName = agentType.getSimpleName();
    return switch (simpleName) {
      case "CropinationAgent" -> cropination.maxActiveAgents();
      case "CultivationAgent" -> cultivation.maxActiveAgents();
      case "ExcavationAgent" -> excavation.maxActiveAgents();
      case "IlluminationAgent" -> illumination.maxActiveAgents();
      case "LumbinationAgent" -> lumbination.maxActiveAgents();
      case "PathanationAgent" -> pathanation.maxActiveAgents();
      case "ShaftanationAgent" -> shaftanation.maxActiveAgents();
      case "VeinationAgent" -> veination.maxActiveAgents();
      case "VentilationAgent" -> ventilation.maxActiveAgents();
      default -> DEFAULT_MAX_ACTIVE_AGENTS;
    };
  }
}
