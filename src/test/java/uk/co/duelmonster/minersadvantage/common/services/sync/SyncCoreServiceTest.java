package uk.co.duelmonster.minersadvantage.common.services.sync;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import uk.co.duelmonster.minersadvantage.common.config.ServerOverridesConfig;
import uk.co.duelmonster.minersadvantage.common.config.SubstitutionConfig;
import uk.co.duelmonster.minersadvantage.common.config.SyncedClientConfig;
import uk.co.duelmonster.minersadvantage.common.services.policy.PolicyCoreService;

class SyncCoreServiceTest {
    @Test
    void synchronizesTypedClientAndServerSnapshots() {
        SyncCoreService service = new SyncCoreService();
        PolicyCoreService policy = new PolicyCoreService();

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
            new SubstitutionConfig(true, false, false, false, true, false, false, client.substitution().blacklist()),
            client.veination(),
            client.ventilation()
        );
        ServerOverridesConfig overrides = new ServerOverridesConfig(false, false, false, false, false, false, false, false, false, false, true, false, false);

        SyncCoreService.PlayerSyncState state = service.synchronize(42L, client, server, overrides, policy);

        assertEquals(1L, state.revision());
        assertFalse(state.effectiveConfig().substitution().ignoreIfValidTool());
        assertFalse(state.effectiveConfig().substitution().ignorePassiveMobs());
    }
}
