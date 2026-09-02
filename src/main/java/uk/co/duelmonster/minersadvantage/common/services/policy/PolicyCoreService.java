package uk.co.duelmonster.minersadvantage.common.services.policy;

import uk.co.duelmonster.minersadvantage.common.config.CaptivationConfig;
import uk.co.duelmonster.minersadvantage.common.config.CommonConfig;
import uk.co.duelmonster.minersadvantage.common.config.CropinationConfig;
import uk.co.duelmonster.minersadvantage.common.config.CultivationConfig;
import uk.co.duelmonster.minersadvantage.common.config.ExcavationConfig;
import uk.co.duelmonster.minersadvantage.common.config.IlluminationConfig;
import uk.co.duelmonster.minersadvantage.common.config.LumbinationConfig;
import uk.co.duelmonster.minersadvantage.common.config.PathanationConfig;
import uk.co.duelmonster.minersadvantage.common.config.ShaftanationConfig;
import uk.co.duelmonster.minersadvantage.common.config.SubstitutionConfig;
import uk.co.duelmonster.minersadvantage.common.config.SyncedClientConfig;
import uk.co.duelmonster.minersadvantage.common.config.VeinationConfig;
import uk.co.duelmonster.minersadvantage.common.config.VentilationConfig;

/**
 * PolicyCoreService keeps this part of MinersAdvantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class PolicyCoreService {
  /**
   * clampRange exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public int clampRange(int value, int min, int max) {
    return Math.max(min, Math.min(max, value));
  }

  public SyncedClientConfig applyServerAuthoritative(
      SyncedClientConfig client,
      SyncedClientConfig server) {
    return new SyncedClientConfig(
        client.client(),
        clampCommon(server.common()),
        sanitizeCaptivation(server.captivation()),
        sanitizeCropination(server.cropination()),
        sanitizeCultivation(server.cultivation()),
        sanitizeExcavation(server.excavation()),
        sanitizePathanation(server.pathanation()),
        sanitizeIllumination(server.illumination()),
        sanitizeLumbination(server.lumbination()),
        sanitizeShaftanation(server.shaftanation()),
        sanitizeSubstitution(server.substitution()),
        sanitizeVeination(server.veination()),
        sanitizeVentilation(server.ventilation()));
  }

  /**
   * clampCommon exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  private CommonConfig clampCommon(CommonConfig config) {
    int ticksPerBlock = clampRange(config.ticksPerBlock(), 1, 40);
    int maxBlocksPerTick = clampRange(config.maxBlocksPerTick(), 1, 64);
    int burstGuardrail = Math.max(1, 256 / ticksPerBlock);
    maxBlocksPerTick = Math.min(maxBlocksPerTick, burstGuardrail);

    return new CommonConfig(
        config.tpsGuard(),
        config.gatherDrops(),
        config.autoIlluminate(),
        config.mineVeins(),
        ticksPerBlock,
        maxBlocksPerTick,
        config.enableTickDelay(),
        clampRange(config.tickDelay(), 0, 40),
        clampRange(config.blockRadius(), 1, 16));
  }

  /**
   * mergeCaptivation exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  private CaptivationConfig sanitizeCaptivation(CaptivationConfig selected) {
    return new CaptivationConfig(
        selected.enabled(),
        selected.allowInGUI(),
        clampRange(selected.radiusHorizontal(), 1, 64),
        clampRange(selected.radiusVertical(), 1, 64),
        selected.isWhitelist(),
        selected.unconditionalBlacklist(),
        selected.blacklist(),
        clampRange(selected.maxActiveAgents(), 1, 256),
        selected.dedupeAgent());
  }

  /**
   * mergeCropination exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  private CropinationConfig sanitizeCropination(CropinationConfig selected) {
    return new CropinationConfig(selected.enabled(), selected.harvestSeeds(),
        clampRange(selected.maxActiveAgents(), 1, 256), selected.dedupeAgent());
  }

  /**
   * mergeCultivation exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  private CultivationConfig sanitizeCultivation(CultivationConfig selected) {
    return new CultivationConfig(
        selected.enabled(),
        clampRange(selected.hydrationDistance(), 0, 16),
        clampRange(selected.maxActiveAgents(), 1, 256),
        selected.dedupeAgent());
  }

  /**
   * mergeExcavation exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  private ExcavationConfig sanitizeExcavation(ExcavationConfig selected) {
    return new ExcavationConfig(
        selected.enabled(),
        clampRange(selected.width(), 1, 64),
        clampRange(selected.height(), 1, 64),
        clampRange(selected.depth(), 1, 64),
        clampRange(selected.processesPerTick(), 1, 64),
        selected.toggleMode(),
        selected.ignoreBlockVariants(),
        selected.isBlockWhitelist(),
        selected.blockBlacklist(),
        clampRange(selected.maxActiveAgents(), 1, 256),
        selected.dedupeAgent());
  }

  /**
   * mergePathanation exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  private PathanationConfig sanitizePathanation(PathanationConfig selected) {
    return new PathanationConfig(
        selected.enabled(),
        clampRange(selected.targetBlockRange(), 1, 64),
        clampRange(selected.pathWidth(), 1, 64),
        clampRange(selected.maxActiveAgents(), 1, 256),
        selected.dedupeAgent());
  }

  /**
   * mergeIllumination exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  private IlluminationConfig sanitizeIllumination(IlluminationConfig selected) {
    return new IlluminationConfig(
        selected.enabled(),
        clampRange(selected.radiusHorizontal(), 1, 64),
        clampRange(selected.radiusVertical(), 1, 64),
        clampRange(selected.lowestLightLevel(), 0, 16),
        selected.useBlockLight(),
        clampRange(selected.maxActiveAgents(), 1, 256),
        selected.dedupeAgent());
  }

  /**
   * mergeLumbination exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  private LumbinationConfig sanitizeLumbination(LumbinationConfig selected) {
    return new LumbinationConfig(
        selected.enabled(),
        clampRange(selected.maxTrunkRange(), 1, 128),
        clampRange(selected.maxLeafRange(), 0, 32),
        clampRange(selected.processesPerTick(), 1, 64),
        selected.chopTreeBelow(),
        selected.destroyLeaves(),
        selected.leavesAffectDurability(),
        selected.replantSaplings(),
        selected.useCanopyTool(),
        selected.ignorePlayerPlacedLeaves(),
        selected.logs(),
        selected.leaves(),
        selected.axes(),
        clampRange(selected.maxActiveAgents(), 1, 256),
        selected.dedupeAgent());
  }

  /**
   * mergeShaftanation exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  private ShaftanationConfig sanitizeShaftanation(ShaftanationConfig selected) {
    return new ShaftanationConfig(
        selected.enabled(),
        clampRange(selected.depth(), 1, 64),
        clampRange(selected.processesPerTick(), 1, 64),
        clampRange(selected.width(), 1, 64),
        clampRange(selected.height(), 1, 64),
        selected.torchPlacement(),
        clampRange(selected.maxActiveAgents(), 1, 256),
        selected.dedupeAgent());
  }

  /**
   * mergeSubstitution exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  private SubstitutionConfig sanitizeSubstitution(SubstitutionConfig selected) {
    return new SubstitutionConfig(
        selected.enabled(),
        selected.allowMending(),
        selected.prioritizeSilkTouch(),
        selected.switchBack(),
        selected.favourFortune(),
        selected.ignoreIfValidTool(),
        selected.ignorePassiveMobs(),
        selected.blacklist(),
        selected.blockBlacklist(),
        selected.selectionRules(),
        clampRange(selected.maxActiveAgents(), 1, 256),
        selected.dedupeAgent());
  }

  /**
   * mergeVeination exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  private VeinationConfig sanitizeVeination(VeinationConfig selected) {
    return new VeinationConfig(
        selected.enabled(),
        clampRange(selected.maxVeinDistance(), 1, 64),
        selected.ores(),
        selected.oreHarvestWithoutSneak(),
        selected.dropOresAtFirstBrokenBlock(),
        selected.increaseHarvestingTimePerOre(),
        selected.increasedHarvestingTimePerOreModifier(),
        selected.pickaxeBlacklist(),
        clampRange(selected.maxActiveAgents(), 1, 256),
        selected.dedupeAgent());
  }

  /**
   * mergeVentilation exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  private VentilationConfig sanitizeVentilation(VentilationConfig selected) {
    return new VentilationConfig(
        selected.enabled(),
        clampRange(selected.width(), 1, 64),
        clampRange(selected.height(), 1, 64),
        clampRange(selected.depth(), 1, 64),
        clampRange(selected.processesPerTick(), 1, 64),
        selected.placeLadders(),
        clampRange(selected.maxActiveAgents(), 1, 256),
        selected.dedupeAgent());
  }
}
