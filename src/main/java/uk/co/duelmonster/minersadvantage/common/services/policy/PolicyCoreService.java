package uk.co.duelmonster.minersadvantage.common.services.policy;

import uk.co.duelmonster.minersadvantage.common.config.CaptivationConfig;
import uk.co.duelmonster.minersadvantage.common.config.CommonConfig;
import uk.co.duelmonster.minersadvantage.common.config.CropinationConfig;
import uk.co.duelmonster.minersadvantage.common.config.CultivationConfig;
import uk.co.duelmonster.minersadvantage.common.config.ExcavationConfig;
import uk.co.duelmonster.minersadvantage.common.config.IlluminationConfig;
import uk.co.duelmonster.minersadvantage.common.config.LumbinationConfig;
import uk.co.duelmonster.minersadvantage.common.config.PathanationConfig;
import uk.co.duelmonster.minersadvantage.common.config.ServerOverridesConfig;
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

    /**
     * featureEnabled exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public boolean featureEnabled(boolean clientEnabled, boolean serverOverrideEnabled) {
        return clientEnabled && serverOverrideEnabled;
    }

    /**
     * featureEnabled exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public boolean featureEnabled(boolean clientEnabled, boolean serverEnabled, boolean overrideFeatureEnablement) {
        return overrideFeatureEnablement ? serverEnabled : clientEnabled;
    }

    public SyncedClientConfig applyServerOverrides(
        SyncedClientConfig client,
        SyncedClientConfig server,
        ServerOverridesConfig overrides
    ) {
        return new SyncedClientConfig(
            client.client(),
            overrides.enforceCommonSettings() ? clampCommon(server.common()) : clampCommon(client.common()),
            mergeCaptivation(client.captivation(), server.captivation(), overrides),
            mergeCropination(client.cropination(), server.cropination(), overrides),
            mergeCultivation(client.cultivation(), server.cultivation(), overrides),
            mergeExcavation(client.excavation(), server.excavation(), overrides),
            mergePathanation(client.pathanation(), server.pathanation(), overrides),
            mergeIllumination(client.illumination(), server.illumination(), overrides),
            mergeLumbination(client.lumbination(), server.lumbination(), overrides),
            mergeShaftanation(client.shaftanation(), server.shaftanation(), overrides),
            mergeSubstitution(client.substitution(), server.substitution(), overrides),
            mergeVeination(client.veination(), server.veination(), overrides),
            mergeVentilation(client.ventilation(), server.ventilation(), overrides)
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
    private CaptivationConfig mergeCaptivation(CaptivationConfig client, CaptivationConfig server, ServerOverridesConfig overrides) {
        CaptivationConfig selected = overrides.enforceCaptivationSettings() ? server : client;
        return new CaptivationConfig(
            featureEnabled(client.enabled(), server.enabled(), overrides.overrideFeatureEnablement()),
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
    private CropinationConfig mergeCropination(CropinationConfig client, CropinationConfig server, ServerOverridesConfig overrides) {
        CropinationConfig selected = overrides.enforceCropinationSettings() ? server : client;
        return new CropinationConfig(featureEnabled(client.enabled(), server.enabled(), overrides.overrideFeatureEnablement()), selected.harvestSeeds());
    }

    /**
     * mergeCultivation exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private CultivationConfig mergeCultivation(CultivationConfig client, CultivationConfig server, ServerOverridesConfig overrides) {
        CultivationConfig selected = overrides.enforceCultivationSettings() ? server : client;
        return new CultivationConfig(
            featureEnabled(client.enabled(), server.enabled(), overrides.overrideFeatureEnablement()),
            clampRange(selected.hydrationDistance(), 0, 16)
        );
    }

    /**
     * mergeExcavation exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private ExcavationConfig mergeExcavation(ExcavationConfig client, ExcavationConfig server, ServerOverridesConfig overrides) {
        ExcavationConfig selected = overrides.enforceExcavationSettings() ? server : client;
        return new ExcavationConfig(
            featureEnabled(client.enabled(), server.enabled(), overrides.overrideFeatureEnablement()),
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
    private PathanationConfig mergePathanation(PathanationConfig client, PathanationConfig server, ServerOverridesConfig overrides) {
        PathanationConfig selected = overrides.enforcePathanationSettings() ? server : client;
        return new PathanationConfig(
            featureEnabled(client.enabled(), server.enabled(), overrides.overrideFeatureEnablement()),
            clampRange(selected.targetBlockRange(), 1, 64),
            clampRange(selected.pathWidth(), 1, 9)
        );
    }

    /**
     * mergeIllumination exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private IlluminationConfig mergeIllumination(IlluminationConfig client, IlluminationConfig server, ServerOverridesConfig overrides) {
        IlluminationConfig selected = overrides.enforceIlluminationSettings() ? server : client;
        return new IlluminationConfig(
            featureEnabled(client.enabled(), server.enabled(), overrides.overrideFeatureEnablement()),
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
    private LumbinationConfig mergeLumbination(LumbinationConfig client, LumbinationConfig server, ServerOverridesConfig overrides) {
        LumbinationConfig selected = overrides.enforceLumbinationSettings() ? server : client;
        return new LumbinationConfig(
            featureEnabled(client.enabled(), server.enabled(), overrides.overrideFeatureEnablement()),
            clampRange(selected.maxTrunkRange(), 1, 128),
            clampRange(selected.maxLeafRange(), 0, 32),
            clampRange(selected.processesPerTick(), 1, 64),
            selected.chopTreeBelow(),
            selected.destroyLeaves(),
            selected.leavesAffectDurability(),
            selected.replantSaplings(),
            selected.useShearsOnLeaves(),
            selected.logs(),
            selected.leaves(),
            selected.axes()
        );
    }

    /**
     * mergeShaftanation exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private ShaftanationConfig mergeShaftanation(ShaftanationConfig client, ShaftanationConfig server, ServerOverridesConfig overrides) {
        ShaftanationConfig selected = overrides.enforceShaftanationSettings() ? server : client;
        return new ShaftanationConfig(
            featureEnabled(client.enabled(), server.enabled(), overrides.overrideFeatureEnablement()),
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
    private SubstitutionConfig mergeSubstitution(SubstitutionConfig client, SubstitutionConfig server, ServerOverridesConfig overrides) {
        SubstitutionConfig selected = overrides.enforceSubstitutionSettings() ? server : client;
        return new SubstitutionConfig(
            featureEnabled(client.enabled(), server.enabled(), overrides.overrideFeatureEnablement()),
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
    private VeinationConfig mergeVeination(VeinationConfig client, VeinationConfig server, ServerOverridesConfig overrides) {
        VeinationConfig selected = overrides.enforceVeinationSettings() ? server : client;
        return new VeinationConfig(
            featureEnabled(client.enabled(), server.enabled(), overrides.overrideFeatureEnablement()),
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
    private VentilationConfig mergeVentilation(VentilationConfig client, VentilationConfig server, ServerOverridesConfig overrides) {
        VentilationConfig selected = overrides.enforceVentilationSettings() ? server : client;
        return new VentilationConfig(
            featureEnabled(client.enabled(), server.enabled(), overrides.overrideFeatureEnablement()),
            clampRange(selected.radiusHorizontal(), 1, 8),
            clampRange(selected.radiusVertical(), 1, 64),
            clampRange(selected.processesPerTick(), 1, 64),
            selected.placeLadders()
        );
    }
}
