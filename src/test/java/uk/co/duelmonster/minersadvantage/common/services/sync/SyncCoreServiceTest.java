package uk.co.duelmonster.minersadvantage.common.services.sync;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import uk.co.duelmonster.minersadvantage.common.config.MAClientRootConfig;
import uk.co.duelmonster.minersadvantage.common.config.MAServerRootConfig;
import uk.co.duelmonster.minersadvantage.common.config.SubstitutionConfig;
import uk.co.duelmonster.minersadvantage.common.config.SyncedClientConfig;
import uk.co.duelmonster.minersadvantage.common.services.ConfigTestFixtures;
import uk.co.duelmonster.minersadvantage.common.services.policy.PolicyCoreService;

/**
 * SyncCoreServiceTest keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
class SyncCoreServiceTest {
    @Test
    /**
     * s yn ch ro ni ze st yp ed cl ie nt an ds er ve rs na ps ho ts exists so this path stays predictable and easier to debug when things get weird.
     */
    void synchronizesTypedClientAndServerSnapshots() {
        SyncCoreService service = new SyncCoreService();
        PolicyCoreService policy = new PolicyCoreService();

        SyncedClientConfig client = SyncedClientConfig.defaults();
        SyncedClientConfig server = ConfigTestFixtures.withSubstitution(
            client,
            new SubstitutionConfig(true, false, false, false, true, false, false, client.substitution().blacklist())
        );
        MAClientRootConfig clientRoot = MAClientRootConfig.fromSyncedConfig(client);
        MAServerRootConfig serverRoot = MAServerRootConfig.fromSyncedConfig(server);

        SyncCoreService.PlayerSyncState state = service.synchronize(42L, clientRoot, serverRoot, policy);

        assertEquals(1L, state.revision());
        assertFalse(state.effectiveConfig().substitution().ignoreIfValidTool());
        assertFalse(state.effectiveConfig().substitution().ignorePassiveMobs());
    }
}
