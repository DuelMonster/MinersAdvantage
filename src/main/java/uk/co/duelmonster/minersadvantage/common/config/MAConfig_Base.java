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

    public static SyncedClientConfig getPlayerConfig(UUID playerId) {
        if (playerId == null) {
            return globalConfig;
        }
        return PLAYER_CONFIGS.getOrDefault(playerId, globalConfig);
    }

    public static SyncedClientConfig getGlobalConfig() {
        return globalConfig;
    }

    public static void setGlobalConfig(SyncedClientConfig config) {
        globalConfig = config == null ? SyncedClientConfig.defaults() : config;
    }

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

    public static void clearPlayerConfig(UUID playerId) {
        if (playerId == null) {
            return;
        }
        PLAYER_CONFIGS.remove(playerId);
    }

    public static void clearAllPlayerConfigs() {
        PLAYER_CONFIGS.clear();
    }
}
