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
 * PolicyCoreService keeps this part of Miners Advantage running without turning server ticks into confetti.
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
        SyncedClientConfig server
    ) {
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
            sanitizeVentilation(server.ventilation())
        );
    }

    /**
     * clampCommon exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private CommonConfig clampCommon(CommonConfig config) {
        return new CommonConfig(
            config.tpsGuard(),
            config.gatherDrops(),
            config.autoIlluminate(),
            config.mineVeins(),
            clampRange(config.blocksPerTick(), 1, 64),
            config.enableTickDelay(),
            clampRange(config.tickDelay(), 0, 40),
            clampRange(config.blockRadius(), 1, 16),
            clampRange(config.blockLimit(), 1, 2048)
        );
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
            selected.blacklist()
        );
    }

    /**
     * mergeCropination exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private CropinationConfig sanitizeCropination(CropinationConfig selected) {
        return new CropinationConfig(selected.enabled(), selected.harvestSeeds());
    }

    /**
     * mergeCultivation exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private CultivationConfig sanitizeCultivation(CultivationConfig selected) {
        return new CultivationConfig(
            selected.enabled(),
            clampRange(selected.hydrationDistance(), 0, 16)
        );
    }

    /**
     * mergeExcavation exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private ExcavationConfig sanitizeExcavation(ExcavationConfig selected) {
        return new ExcavationConfig(
            selected.enabled(),
            clampRange(selected.radiusHorizontal(), 1, 8),
            clampRange(selected.radiusVertical(), 0, 8),
            clampRange(selected.processesPerTick(), 1, 64),
            selected.toggleMode(),
            selected.ignoreBlockVariants(),
            selected.isBlockWhitelist(),
            selected.blockBlacklist()
        );
    }

    /**
     * mergePathanation exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private PathanationConfig sanitizePathanation(PathanationConfig selected) {
        return new PathanationConfig(
            selected.enabled(),
            clampRange(selected.targetBlockRange(), 1, 64),
            clampRange(selected.pathWidth(), 1, 9)
        );
    }

    /**
     * mergeIllumination exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private IlluminationConfig sanitizeIllumination(IlluminationConfig selected) {
        return new IlluminationConfig(
            selected.enabled(),
            clampRange(selected.radiusHorizontal(), 1, 8),
            clampRange(selected.radiusVertical(), 0, 8),
            clampRange(selected.lowestLightLevel(), 0, 16),
            selected.useBlockLight()
        );
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
            selected.axes()
        );
    }

    /**
     * mergeShaftanation exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private ShaftanationConfig sanitizeShaftanation(ShaftanationConfig selected) {
        return new ShaftanationConfig(
            selected.enabled(),
            clampRange(selected.maxDepth(), 1, 128),
            clampRange(selected.processesPerTick(), 1, 64),
            clampRange(selected.shaftWidth(), 1, 5),
            clampRange(selected.shaftHeight(), 1, 5),
            selected.torchPlacement()
        );
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
            selected.selectionRules()
        );
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
            selected.pickaxeBlacklist()
        );
    }

    /**
     * mergeVentilation exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private VentilationConfig sanitizeVentilation(VentilationConfig selected) {
        return new VentilationConfig(
            selected.enabled(),
            clampRange(selected.radiusHorizontal(), 1, 8),
            clampRange(selected.radiusVertical(), 1, 64),
            clampRange(selected.processesPerTick(), 1, 64),
            selected.placeLadders()
        );
    }
}
