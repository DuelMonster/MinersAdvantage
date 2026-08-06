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
            MAConfig_Defaults.Captivation.blacklist),
        new CropinationConfig(
            MAConfig_Defaults.Cropination.enabled,
            MAConfig_Defaults.Cropination.harvestSeeds),
        new CultivationConfig(
            MAConfig_Defaults.Cultivation.enabled,
            MAConfig_Defaults.Cultivation.hydrationDistance),
        new ExcavationConfig(
            MAConfig_Defaults.Excavation.enabled,
            MAConfig_Defaults.Excavation.width,
            MAConfig_Defaults.Excavation.height,
            MAConfig_Defaults.Excavation.depth,
            MAConfig_Defaults.Excavation.processesPerTick,
            MAConfig_Defaults.Excavation.toggleMode,
            MAConfig_Defaults.Excavation.ignoreBlockVariants,
            MAConfig_Defaults.Excavation.isBlockWhitelist,
            MAConfig_Defaults.Excavation.blockBlacklist),
        new PathanationConfig(
            MAConfig_Defaults.Pathanation.enabled,
            MAConfig_Defaults.Pathanation.targetBlockRange,
            MAConfig_Defaults.Pathanation.pathWidth),
        new IlluminationConfig(
            MAConfig_Defaults.Illumination.enabled,
            MAConfig_Defaults.Illumination.radiusHorizontal,
            MAConfig_Defaults.Illumination.radiusVertical,
            MAConfig_Defaults.Illumination.lowestLightLevel,
            MAConfig_Defaults.Illumination.useBlockLight),
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
            MAConfig_Defaults.Lumbination.axes),
        new ShaftanationConfig(
            MAConfig_Defaults.Shaftanation.enabled,
            MAConfig_Defaults.Shaftanation.depth,
            MAConfig_Defaults.Shaftanation.processesPerTick,
            MAConfig_Defaults.Shaftanation.width,
            MAConfig_Defaults.Shaftanation.height,
            MAConfig_Defaults.Shaftanation.torchPlacement),
        new SubstitutionConfig(
            MAConfig_Defaults.Substitution.enabled,
            MAConfig_Defaults.Substitution.allowMending,
            MAConfig_Defaults.Substitution.prioritizeSilkTouch,
            MAConfig_Defaults.Substitution.switchBack,
            MAConfig_Defaults.Substitution.favourFortune,
            MAConfig_Defaults.Substitution.ignoreIfValidTool,
            MAConfig_Defaults.Substitution.ignorePassiveMobs,
            MAConfig_Defaults.Substitution.blacklist,
            MAConfig_Defaults.Substitution.selectionRules),
        new VeinationConfig(
            MAConfig_Defaults.Veination.enabled,
            MAConfig_Defaults.Veination.maxVeinDistance,
            MAConfig_Defaults.Veination.ores,
            MAConfig_Defaults.Veination.oreHarvestWithoutSneak,
            MAConfig_Defaults.Veination.dropOresAtFirstBrokenBlock,
            MAConfig_Defaults.Veination.increaseHarvestingTimePerOre,
            MAConfig_Defaults.Veination.increasedHarvestingTimePerOreModifier,
            MAConfig_Defaults.Veination.pickaxeBlacklist),
        new VentilationConfig(
            MAConfig_Defaults.Ventilation.enabled,
            MAConfig_Defaults.Ventilation.width,
            MAConfig_Defaults.Ventilation.height,
            MAConfig_Defaults.Ventilation.depth,
            MAConfig_Defaults.Ventilation.processesPerTick,
            MAConfig_Defaults.Ventilation.placeLadders));
  }
}
