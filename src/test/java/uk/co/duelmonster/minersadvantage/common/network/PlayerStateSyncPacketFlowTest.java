package uk.co.duelmonster.minersadvantage.common.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;
import uk.co.duelmonster.minersadvantage.common.MinersAdvantageCore;
import uk.co.duelmonster.minersadvantage.common.config.MAClientRootConfig;
import uk.co.duelmonster.minersadvantage.common.config.MAServerRootConfig;
import uk.co.duelmonster.minersadvantage.common.config.SyncedClientConfig;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;
import uk.co.duelmonster.minersadvantage.common.services.utility.SupremeVantageService;

/**
 * PlayerStateSyncPacketFlowTest keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
class PlayerStateSyncPacketFlowTest {
    @Test
    void playerStateSyncPacketAppliesServerAuthoritativeConfig() {
        Assumptions.assumeTrue(isSlf4jAvailable());
        MinersAdvantageCore core = new MinersAdvantageCore();
        SyncedClientConfig client = core.defaultConfig();
        SyncedClientConfig server = SyncedClientConfig.defaults();
        MAClientRootConfig clientRoot = MAClientRootConfig.fromSyncedConfig(client);
        MAServerRootConfig serverRoot = MAServerRootConfig.fromSyncedConfig(server);

        var state = core.handlePlayerStateSyncPacket(new PlayerStateSyncPacket(7L, clientRoot, serverRoot));

        assertEquals(1L, state.revision());
        assertEquals("PlayerStateSyncPacket", PacketRegistry.getPacketName(PacketRegistry.PLAYER_STATE_SYNC));
    }

    @Test
    void componentTogglePacketEnablesAndDisablesRegisteredFeatures() {
        Assumptions.assumeTrue(isSlf4jAvailable());
        MinersAdvantageCore core = new MinersAdvantageCore();
        core.bootstrap();

        assertTrue(core.handleComponentTogglePacket(new ComponentTogglePacket(FeatureId.CAPTIVATION, false)) == false);
        assertTrue(core.handleComponentTogglePacket(new ComponentTogglePacket(FeatureId.CAPTIVATION, true)));
        assertEquals("ComponentTogglePacket", PacketRegistry.getPacketName(PacketRegistry.COMPONENT_TOGGLE));
    }

    @Test
    void supremeVantagePacketReturnsRewardGrant() {
        Assumptions.assumeTrue(isSlf4jAvailable());
        MinersAdvantageCore core = new MinersAdvantageCore();

        SupremeVantageService.RewardGrant reward = core.handleSupremeVantagePacket(
            new SupremeVantagePacket(12L, SupremeVantageService.CODE_D)
        );

        assertEquals("Soulblade", reward.displayName());
        assertEquals("SupremeVantagePacket", PacketRegistry.getPacketName(PacketRegistry.SUPREME_VANTAGE));
    }

    private static boolean isSlf4jAvailable() {
        try {
            Class.forName("org.slf4j.LoggerFactory");
            return true;
        } catch (ClassNotFoundException exception) {
            return false;
        }
    }
}
