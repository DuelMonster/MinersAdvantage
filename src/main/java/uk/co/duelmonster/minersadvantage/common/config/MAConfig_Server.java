package uk.co.duelmonster.minersadvantage.common.config;

import java.util.UUID;
import uk.co.duelmonster.minersadvantage.common.JsonHelper;

/**
 * Legacy compatibility facade for server-side config access.
 */
public final class MAConfig_Server extends MAConfig_Base {
    private MAConfig_Server() {}

    /**
     * getServerConfig exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static SyncedClientConfig getServerConfig() {
        return MAConfig_Base.getGlobalConfig();
    }

    /**
     * getPlayerConfigJson exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static String getPlayerConfigJson(UUID playerId) {
        return JsonHelper.toJson(MAConfig_Base.getPlayerConfig(playerId));
    }

    /**
     * setPlayerConfig exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static void setPlayerConfig(UUID playerId, String json) {
        SyncedClientConfig config = JsonHelper.fromJson(json, SyncedClientConfig.class);
        if (config == null) {
            return;
        }
        MAConfig_Base.setPlayerConfig(playerId, config);
    }
}


