package uk.co.duelmonster.minersadvantage.common.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import uk.co.duelmonster.minersadvantage.common.config.SubstitutionConfig;
import uk.co.duelmonster.minersadvantage.common.config.SyncedClientConfig;
import uk.co.duelmonster.minersadvantage.common.services.policy.PolicyCoreService;

/**
 * PolicyCoreServiceTest keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
class PolicyCoreServiceTest {
    @Test
    void validatesRanges() {
        PolicyCoreService service = new PolicyCoreService();
        assertEquals(5, service.clampRange(9, 1, 5));
    }

    @Test
    void appliesServerAuthoritativeConfigToEffectiveConfig() {
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

        SyncedClientConfig effective = service.applyServerAuthoritative(client, server);

        assertFalse(effective.substitution().enabled());
        assertTrue(effective.substitution().allowMending());
        assertFalse(effective.substitution().ignoreIfValidTool());
    }

    @Test
    void enforcesServerCommonPolicyValues() {
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
            client.substitution(),
            client.veination(),
            client.ventilation()
        );
        SyncedClientConfig serverWithCrossFeaturePolicy = new SyncedClientConfig(
            server.client(),
            new uk.co.duelmonster.minersadvantage.common.config.CommonConfig(false, true, false, false, 6, false, 0, 7, 128),
            server.captivation(),
            server.cropination(),
            server.cultivation(),
            server.excavation(),
            server.pathanation(),
            server.illumination(),
            server.lumbination(),
            server.shaftanation(),
            server.substitution(),
            server.veination(),
            server.ventilation()
        );

        SyncedClientConfig effective = service.applyServerAuthoritative(client, serverWithCrossFeaturePolicy);

        assertFalse(effective.common().mineVeins());
        assertFalse(effective.common().autoIlluminate());
        assertEquals(6, effective.common().blocksPerTick());
        assertEquals(7, effective.common().blockRadius());
    }

    @Test
    void usesServerGameplayConfigEvenWithoutEnforcementFlags() {
        PolicyCoreService service = new PolicyCoreService();
        SyncedClientConfig client = SyncedClientConfig.defaults();
        SyncedClientConfig server = new SyncedClientConfig(
            client.client(),
            new uk.co.duelmonster.minersadvantage.common.config.CommonConfig(true, false, true, true, 2, true, 5, 3, 64),
            client.captivation(),
            client.cropination(),
            client.cultivation(),
            client.excavation(),
            client.pathanation(),
            client.illumination(),
            client.lumbination(),
            client.shaftanation(),
            new SubstitutionConfig(false, true, false, true, true, true, true, client.substitution().blacklist()),
            client.veination(),
            client.ventilation()
        );

        SyncedClientConfig effective = service.applyServerAuthoritative(client, server);

        assertFalse(effective.substitution().enabled());
        assertTrue(effective.substitution().allowMending());
    }
}
