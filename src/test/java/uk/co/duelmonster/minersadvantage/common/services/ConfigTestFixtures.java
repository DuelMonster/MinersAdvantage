package uk.co.duelmonster.minersadvantage.common.services;

import uk.co.duelmonster.minersadvantage.common.config.CommonConfig;
import uk.co.duelmonster.minersadvantage.common.config.SubstitutionConfig;
import uk.co.duelmonster.minersadvantage.common.config.SyncedClientConfig;

public final class ConfigTestFixtures {
    private ConfigTestFixtures() {
    }

    public static SyncedClientConfig withSubstitution(SyncedClientConfig client, SubstitutionConfig substitutionConfig) {
        return new SyncedClientConfig(
            client.client(),
            client.common(),
            client.captivation(),
            client.cropination(),
            client.cultivation(),
            client.excavation(),
            client.pathanation(),
            client.illumination(),
            client.lumbination(),
            client.shaftanation(),
            substitutionConfig,
            client.veination(),
            client.ventilation()
        );
    }

    public static SyncedClientConfig withCommon(SyncedClientConfig base, CommonConfig commonConfig) {
        return new SyncedClientConfig(
            base.client(),
            commonConfig,
            base.captivation(),
            base.cropination(),
            base.cultivation(),
            base.excavation(),
            base.pathanation(),
            base.illumination(),
            base.lumbination(),
            base.shaftanation(),
            base.substitution(),
            base.veination(),
            base.ventilation()
        );
    }
}