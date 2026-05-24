package uk.co.duelmonster.minersadvantage.common.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import uk.co.duelmonster.minersadvantage.common.config.storage.MATomlConfigStore;
import uk.co.duelmonster.minersadvantage.common.log.LogUtils;

/**
 * Legacy compatibility facade for per-player config lookups.
 */
public class MAConfig_Base {
    private static final Path CONFIG_DIR = Path.of(System.getProperty("user.dir"), "config", "minersadvantage");
    private static final Path LEGACY_JSON_CONFIG_FILE = CONFIG_DIR.resolve("client-config.json");
    private static final Map<UUID, SyncedClientConfig> PLAYER_CONFIGS = new ConcurrentHashMap<>();
    private static volatile MAClientRootConfig clientRootConfig = MAClientRootConfig.defaults();
    private static volatile MAServerRootConfig serverRootConfig = MAServerRootConfig.defaults();

    static {
        setGlobalConfigInternal(loadGlobalConfig());
    }

    protected MAConfig_Base() {}

    /**
     * getPlayerConfig exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static SyncedClientConfig getPlayerConfig(UUID playerId) {
        if (playerId == null) {
            return getGlobalConfig();
        }
        return PLAYER_CONFIGS.getOrDefault(playerId, getGlobalConfig());
    }

    /**
     * getGlobalConfig exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static SyncedClientConfig getGlobalConfig() {
        return serverRootConfig.toSyncedConfig(clientRootConfig.client());
    }

    public static MAClientRootConfig getClientRootConfig() {
        return clientRootConfig;
    }

    public static MAServerRootConfig getServerRootConfig() {
        return serverRootConfig;
    }

    /**
     * setGlobalConfig exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static void setGlobalConfig(SyncedClientConfig config) {
        setGlobalConfigInternal(config == null ? SyncedClientConfig.defaults() : config);
        saveGlobalConfig(getGlobalConfig());
    }

    public static void setClientRootConfig(MAClientRootConfig clientConfig) {
        clientRootConfig = clientConfig == null ? MAClientRootConfig.defaults() : clientConfig;
        saveGlobalConfig(getGlobalConfig());
    }

    public static void setServerRootConfig(MAServerRootConfig serverConfig) {
        serverRootConfig = serverConfig == null ? MAServerRootConfig.defaults() : serverConfig;
        saveGlobalConfig(getGlobalConfig());
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

    private static SyncedClientConfig loadGlobalConfig() {
        try {
            Files.createDirectories(CONFIG_DIR);
        } catch (Exception exception) {
            LogUtils.logWarn("Unable to create config directory {}: {}", CONFIG_DIR, exception.getMessage());
        }

        SyncedClientConfig loaded = MATomlConfigStore.load(CONFIG_DIR, SyncedClientConfig.defaults());
        setGlobalConfigInternal(loaded);
        saveGlobalConfig(getGlobalConfig());
        removeLegacyJsonConfig();
        return loaded;
    }

    private static void saveGlobalConfig(SyncedClientConfig config) {
        MATomlConfigStore.save(CONFIG_DIR, config == null ? SyncedClientConfig.defaults() : config);
    }

    private static void setGlobalConfigInternal(SyncedClientConfig config) {
        SyncedClientConfig value = config == null ? SyncedClientConfig.defaults() : config;
        clientRootConfig = MAClientRootConfig.fromSyncedConfig(value);
        serverRootConfig = MAServerRootConfig.fromSyncedConfig(value);
    }

    private static void removeLegacyJsonConfig() {
        if (!Files.isRegularFile(LEGACY_JSON_CONFIG_FILE)) {
            return;
        }
        try {
            Files.deleteIfExists(LEGACY_JSON_CONFIG_FILE);
        } catch (Exception exception) {
            LogUtils.logWarn("Unable to remove legacy JSON config {}: {}", LEGACY_JSON_CONFIG_FILE, exception.getMessage());
        }
    }
}
