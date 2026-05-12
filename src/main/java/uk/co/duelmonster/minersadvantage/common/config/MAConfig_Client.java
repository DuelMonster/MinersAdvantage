package uk.co.duelmonster.minersadvantage.common.config;

import java.util.UUID;

/**
 * Legacy compatibility facade for client-side config access.
 */
public final class MAConfig_Client extends MAConfig_Base {
    private MAConfig_Client() {}

    public static SyncedClientConfig getPlayerConfig(UUID playerId) {
        return MAConfig_Base.getPlayerConfig(playerId);
    }
}
