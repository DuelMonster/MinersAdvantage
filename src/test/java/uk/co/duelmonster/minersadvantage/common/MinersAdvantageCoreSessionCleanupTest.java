package uk.co.duelmonster.minersadvantage.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import uk.co.duelmonster.minersadvantage.common.config.ClientConfig;
import uk.co.duelmonster.minersadvantage.common.config.MAClientRootConfig;
import uk.co.duelmonster.minersadvantage.common.config.MAConfig_Base;
import uk.co.duelmonster.minersadvantage.common.config.MAServerRootConfig;
import uk.co.duelmonster.minersadvantage.common.config.SyncedClientConfig;
import uk.co.duelmonster.minersadvantage.common.network.PlayerStateSyncPacket;
import uk.co.duelmonster.minersadvantage.testutil.TestRuntimeAssumptions;

class MinersAdvantageCoreSessionCleanupTest {
  @Test
  void clearingPlayerSessionRemovesPlayerConfigAndSyncSnapshot() {
    Assumptions.assumeTrue(TestRuntimeAssumptions.canInitializeSoundEvents());
    MinersAdvantageCore core = new MinersAdvantageCore();
    long playerId = 88421L;
    UUID playerUuid = UUID.randomUUID();
    core.handlePlayerStateSyncPacket(playerId, new PlayerStateSyncPacket(true, true, 3, 4));
    SyncedClientConfig globalConfig = MAConfig_Base.getGlobalConfig();
    SyncedClientConfig privateConfig = new MAClientRootConfig(
        new ClientConfig(true, true, 0xFF010203, 0xFF040506, true))
        .toSyncedConfig(MAServerRootConfig.fromSyncedConfig(globalConfig));
    MAConfig_Base.setPlayerConfig(playerUuid, privateConfig);
    assertEquals(privateConfig, MAConfig_Base.getPlayerConfig(playerUuid));

    core.clearPlayerSessionState(playerId, playerUuid);

    assertEquals(1L, core.handlePlayerStateSyncPacket(playerId,
        new PlayerStateSyncPacket(false, false, 0, 0)).revision());
    assertEquals(MAConfig_Base.getGlobalConfig(), MAConfig_Base.getPlayerConfig(playerUuid));
  }
}
