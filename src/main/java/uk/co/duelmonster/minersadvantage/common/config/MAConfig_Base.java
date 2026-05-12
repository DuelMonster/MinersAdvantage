package uk.co.duelmonster.minersadvantage.common.config;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Legacy compatibility facade for per-player config lookups.
 */
public class MAConfig_Base {
    private static final Map<UUID, SyncedClientConfig> PLAYER_CONFIGS = new ConcurrentHashMap<>();
    private static volatile SyncedClientConfig globalConfig = SyncedClientConfig.defaults();

    protected MAConfig_Base() {}

    /**
     * getPlayerConfig exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static SyncedClientConfig getPlayerConfig(UUID playerId) {
        if (playerId == null) {
            return globalConfig;
        }
        return PLAYER_CONFIGS.getOrDefault(playerId, globalConfig);
    }

    /**
     * getGlobalConfig exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static SyncedClientConfig getGlobalConfig() {
        return globalConfig;
    }

    /**
     * setGlobalConfig exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static void setGlobalConfig(SyncedClientConfig config) {
        globalConfig = config == null ? SyncedClientConfig.defaults() : config;
    }

    /**
     * setPlayerConfig exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static void setPlayerConfig(UUID playerId, SyncedClientConfig config) {
        if (playerId == null) {
            return;
        }
        if (config == null) {
            PLAYER_CONFIGS.remove(playerId);
            return;
        }
        PLAYER_CONFIGS.put(playerId, config);
    }

    /**
     * clearPlayerConfig exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static void clearPlayerConfig(UUID playerId) {
        if (playerId == null) {
            return;
        }
        PLAYER_CONFIGS.remove(playerId);
    }

    /**
     * clearAllPlayerConfigs exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static void clearAllPlayerConfigs() {
        PLAYER_CONFIGS.clear();
    }
}


