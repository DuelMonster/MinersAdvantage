package uk.co.duelmonster.minersadvantage.common.config;

import java.util.UUID;

/**
 * Legacy compatibility facade for client-side config access.
 */
public final class MAConfig_Client extends MAConfig_Base {
    private MAConfig_Client() {}

    /**
     * getPlayerConfig exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static SyncedClientConfig getPlayerConfig(UUID playerId) {
        return MAConfig_Base.getPlayerConfig(playerId);
    }
}
