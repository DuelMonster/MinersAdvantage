package uk.co.duelmonster.minersadvantage.common.network;

import uk.co.duelmonster.minersadvantage.common.config.ServerOverridesConfig;
import uk.co.duelmonster.minersadvantage.common.config.SyncedClientConfig;

public record PlayerStateSyncPacket(
    long playerId,
    SyncedClientConfig clientConfig,
    SyncedClientConfig serverConfig,
    ServerOverridesConfig serverOverrides
) {}
