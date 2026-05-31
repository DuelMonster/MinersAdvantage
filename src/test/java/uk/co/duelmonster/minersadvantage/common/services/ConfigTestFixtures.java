package uk.co.duelmonster.minersadvantage.common.services;

import uk.co.duelmonster.minersadvantage.common.config.CommonConfig;
import uk.co.duelmonster.minersadvantage.common.config.SubstitutionConfig;
import uk.co.duelmonster.minersadvantage.common.config.SyncedClientConfig;

/**
 * Test fixture helpers for quickly cloning SyncedClientConfig with one targeted section replaced,
 * because hand-writing 13-argument constructors in every test is a special kind of punishment.
 */
public final class ConfigTestFixtures {
    /**
     * Utility class only; tests call static helpers.
     */
    private ConfigTestFixtures() {
    }

    /**
     * Return config copy with substitution section swapped while keeping everything else intact.
     */
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

    /**
     * Return config copy with common section swapped while preserving every other category.
     */
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