package uk.co.duelmonster.minersadvantage.common.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import uk.co.duelmonster.minersadvantage.common.MinersAdvantageCore;
import uk.co.duelmonster.minersadvantage.common.config.ServerOverridesConfig;
import uk.co.duelmonster.minersadvantage.common.config.SyncedClientConfig;

class PlayerStateSyncPacketFlowTest {
    @Test
    void playerStateSyncPacketAppliesServerOverrides() {
        MinersAdvantageCore core = new MinersAdvantageCore();
        SyncedClientConfig client = core.defaultConfig();
        SyncedClientConfig server = SyncedClientConfig.defaults();
        ServerOverridesConfig overrides = new ServerOverridesConfig(true, false, false, false, false, false, false, false, false, false, false, false, false);

        var state = core.handlePlayerStateSyncPacket(new PlayerStateSyncPacket(7L, client, server, overrides));

        assertEquals(1L, state.revision());
        assertFalse(state.serverOverrides().enforceSubstitutionSettings());
        assertEquals("PlayerStateSyncPacket", PacketRegistry.getPacketName(PacketRegistry.PLAYER_STATE_SYNC));
    }
}
