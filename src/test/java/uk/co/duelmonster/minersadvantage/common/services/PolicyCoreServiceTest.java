package uk.co.duelmonster.minersadvantage.common.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import uk.co.duelmonster.minersadvantage.common.config.ServerOverridesConfig;
import uk.co.duelmonster.minersadvantage.common.config.SubstitutionConfig;
import uk.co.duelmonster.minersadvantage.common.config.SyncedClientConfig;
import uk.co.duelmonster.minersadvantage.common.services.policy.PolicyCoreService;

class PolicyCoreServiceTest {
    @Test
    void validatesRangesAndOverrides() {
        PolicyCoreService service = new PolicyCoreService();
        assertEquals(5, service.clampRange(9, 1, 5));
        assertTrue(service.featureEnabled(true, true));
        assertFalse(service.featureEnabled(true, false));
    }

    @Test
    void mergesServerOverridesIntoEffectiveConfig() {
        PolicyCoreService service = new PolicyCoreService();
        SyncedClientConfig client = SyncedClientConfig.defaults();
        SyncedClientConfig server = new SyncedClientConfig(
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
            new SubstitutionConfig(false, true, false, false, false, false, false, client.substitution().blacklist()),
            client.veination(),
            client.ventilation()
        );
        ServerOverridesConfig overrides = new ServerOverridesConfig(true, false, false, false, false, false, false, false, false, false, true, false, false);

        SyncedClientConfig effective = service.applyServerOverrides(client, server, overrides);

        assertFalse(effective.substitution().enabled());
        assertTrue(effective.substitution().allowMending());
        assertFalse(effective.substitution().ignoreIfValidTool());
    }
}
