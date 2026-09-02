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
            MAConfig_Defaults.Captivation.blacklist,
            MAConfig_Defaults.Captivation.maxActiveAgents,
            MAConfig_Defaults.Captivation.dedupeAgent),
        new CropinationConfig(
            MAConfig_Defaults.Cropination.enabled,
            MAConfig_Defaults.Cropination.harvestSeeds,
            MAConfig_Defaults.Cropination.maxActiveAgents,
            MAConfig_Defaults.Cropination.dedupeAgent),
        new CultivationConfig(
            MAConfig_Defaults.Cultivation.enabled,
            MAConfig_Defaults.Cultivation.hydrationDistance,
            MAConfig_Defaults.Cultivation.maxActiveAgents,
            MAConfig_Defaults.Cultivation.dedupeAgent),
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
            MAConfig_Defaults.Excavation.dedupeAgent),
        new PathanationConfig(
            MAConfig_Defaults.Pathanation.enabled,
            MAConfig_Defaults.Pathanation.targetBlockRange,
            MAConfig_Defaults.Pathanation.pathWidth,
            MAConfig_Defaults.Pathanation.maxActiveAgents,
            MAConfig_Defaults.Pathanation.dedupeAgent),
        new IlluminationConfig(
            MAConfig_Defaults.Illumination.enabled,
            MAConfig_Defaults.Illumination.radiusHorizontal,
            MAConfig_Defaults.Illumination.radiusVertical,
            MAConfig_Defaults.Illumination.lowestLightLevel,
            MAConfig_Defaults.Illumination.useBlockLight,
            MAConfig_Defaults.Illumination.maxActiveAgents,
            MAConfig_Defaults.Illumination.dedupeAgent),
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
            MAConfig_Defaults.Lumbination.dedupeAgent),
        new ShaftanationConfig(
            MAConfig_Defaults.Shaftanation.enabled,
            MAConfig_Defaults.Shaftanation.depth,
            MAConfig_Defaults.Shaftanation.processesPerTick,
            MAConfig_Defaults.Shaftanation.width,
            MAConfig_Defaults.Shaftanation.height,
            MAConfig_Defaults.Shaftanation.torchPlacement,
            MAConfig_Defaults.Shaftanation.maxActiveAgents,
            MAConfig_Defaults.Shaftanation.dedupeAgent),
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
            MAConfig_Defaults.Substitution.selectionRules,
            MAConfig_Defaults.Substitution.maxActiveAgents,
            MAConfig_Defaults.Substitution.dedupeAgent),
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
            MAConfig_Defaults.Veination.dedupeAgent),
        new VentilationConfig(
            MAConfig_Defaults.Ventilation.enabled,
            MAConfig_Defaults.Ventilation.width,
            MAConfig_Defaults.Ventilation.height,
            MAConfig_Defaults.Ventilation.depth,
            MAConfig_Defaults.Ventilation.processesPerTick,
            MAConfig_Defaults.Ventilation.placeLadders,
            MAConfig_Defaults.Ventilation.maxActiveAgents,
            MAConfig_Defaults.Ventilation.dedupeAgent));
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
      case "CaptivationAgent" -> captivation.dedupeAgent();
      case "CropinationAgent" -> cropination.dedupeAgent();
      case "CultivationAgent" -> cultivation.dedupeAgent();
      case "ExcavationAgent" -> excavation.dedupeAgent();
      case "IlluminationAgent" -> illumination.dedupeAgent();
      case "LumbinationAgent" -> lumbination.dedupeAgent();
      case "PathanationAgent" -> pathanation.dedupeAgent();
      case "ShaftanationAgent" -> shaftanation.dedupeAgent();
      case "SubstitutionAgent" -> substitution.dedupeAgent();
      case "VeinationAgent" -> veination.dedupeAgent();
      case "VentilationAgent" -> ventilation.dedupeAgent();
      default -> true;
    };
  }

  /**
   * Resolve the configured concurrent-instance cap for a given runtime agent type.
   */
  public int maxActiveAgentsForType(Class<?> agentType) {
    if (agentType == null) {
      return 1;
    }

    String simpleName = agentType.getSimpleName();
    return switch (simpleName) {
      case "CaptivationAgent" -> captivation.maxActiveAgents();
      case "CropinationAgent" -> cropination.maxActiveAgents();
      case "CultivationAgent" -> cultivation.maxActiveAgents();
      case "ExcavationAgent" -> excavation.maxActiveAgents();
      case "IlluminationAgent" -> illumination.maxActiveAgents();
      case "LumbinationAgent" -> lumbination.maxActiveAgents();
      case "PathanationAgent" -> pathanation.maxActiveAgents();
      case "ShaftanationAgent" -> shaftanation.maxActiveAgents();
      case "SubstitutionAgent" -> substitution.maxActiveAgents();
      case "VeinationAgent" -> veination.maxActiveAgents();
      case "VentilationAgent" -> ventilation.maxActiveAgents();
      default -> 1;
    };
  }
}

