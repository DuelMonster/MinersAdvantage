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

public final class PolicyCoreService {
    public int clampRange(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public boolean featureEnabled(boolean clientEnabled, boolean serverOverrideEnabled) {
        return clientEnabled && serverOverrideEnabled;
    }

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

    private CropinationConfig mergeCropination(CropinationConfig client, CropinationConfig server, ServerOverridesConfig overrides) {
        CropinationConfig selected = overrides.enforceCropinationSettings() ? server : client;
        return new CropinationConfig(featureEnabled(client.enabled(), server.enabled(), overrides.overrideFeatureEnablement()), selected.harvestSeeds());
    }

    private CultivationConfig mergeCultivation(CultivationConfig client, CultivationConfig server, ServerOverridesConfig overrides) {
        CultivationConfig selected = overrides.enforceCultivationSettings() ? server : client;
        return new CultivationConfig(
            featureEnabled(client.enabled(), server.enabled(), overrides.overrideFeatureEnablement()),
            clampRange(selected.hydrationDistance(), 0, 16)
        );
    }

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

    private PathanationConfig mergePathanation(PathanationConfig client, PathanationConfig server, ServerOverridesConfig overrides) {
        PathanationConfig selected = overrides.enforcePathanationSettings() ? server : client;
        return new PathanationConfig(
            featureEnabled(client.enabled(), server.enabled(), overrides.overrideFeatureEnablement()),
            clampRange(selected.targetBlockRange(), 1, 64),
            clampRange(selected.pathWidth(), 1, 9)
        );
    }

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
            selected.blacklist()
        );
    }

    private VeinationConfig mergeVeination(VeinationConfig client, VeinationConfig server, ServerOverridesConfig overrides) {
        VeinationConfig selected = overrides.enforceVeinationSettings() ? server : client;
        return new VeinationConfig(
            featureEnabled(client.enabled(), server.enabled(), overrides.overrideFeatureEnablement()),
            clampRange(selected.maxVeinDistance(), 1, 64),
            selected.ores()
        );
    }

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
