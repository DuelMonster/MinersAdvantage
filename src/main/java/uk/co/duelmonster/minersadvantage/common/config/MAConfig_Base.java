package uk.co.duelmonster.minersadvantage.common.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import uk.co.duelmonster.minersadvantage.common.config.storage.MATomlConfigStore;
import uk.co.duelmonster.minersadvantage.common.log.LogUtils;

/**
 * Central facade for per-player and global config snapshots.
 */
public class MAConfig_Base {
    private static final Path CONFIG_DIR = Path.of(System.getProperty("user.dir"), "config");
    private static final Map<UUID, SyncedClientConfig> PLAYER_CONFIGS = new ConcurrentHashMap<>();
    private static volatile MAClientRootConfig clientRootConfig = MAClientRootConfig.defaults();
    private static volatile MAServerRootConfig serverRootConfig = MAServerRootConfig.defaults();

    static {
        setGlobalConfigInternal(loadGlobalConfig());
    }

    /**
     * Utility holder; do not instantiate.
     */
    protected MAConfig_Base() {}

    /**
     * getPlayerConfig exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static SyncedClientConfig getPlayerConfig(UUID playerId) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
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

    /**
     * Return current persisted client-root configuration snapshot.
     */
    public static MAClientRootConfig getClientRootConfig() {
        return clientRootConfig;
    }

    /**
     * Return current persisted server-root configuration snapshot.
     */
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

    /**
     * Set client-root config and persist merged global snapshot.
     */
    public static void setClientRootConfig(MAClientRootConfig clientConfig) {
        clientRootConfig = clientConfig == null ? MAClientRootConfig.defaults() : clientConfig;
        LogUtils.setConfigDebugLoggingEnabled(clientRootConfig.client().debugLogging());
        saveGlobalConfig(getGlobalConfig());
    }

    /**
     * Set server-root config and persist merged global snapshot.
     */
    public static void setServerRootConfig(MAServerRootConfig serverConfig) {
        serverRootConfig = serverConfig == null ? MAServerRootConfig.defaults() : serverConfig;
        saveGlobalConfig(getGlobalConfig());
    }

    /**
     * setPlayerConfig exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static void setPlayerConfig(UUID playerId, SyncedClientConfig config) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (playerId == null) {
            return;
        }
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
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
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
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

    /**
     * Load global config from disk, normalize, and resave canonical form.
     */
    private static SyncedClientConfig loadGlobalConfig() {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        try {
            Files.createDirectories(CONFIG_DIR);
        } catch (Exception exception) {
            LogUtils.logWarn("Unable to create config directory {}: {}", CONFIG_DIR, exception.getMessage());
        }

        SyncedClientConfig loaded = MATomlConfigStore.load(CONFIG_DIR, SyncedClientConfig.defaults());
        setGlobalConfigInternal(loaded);
        saveGlobalConfig(getGlobalConfig());
        return loaded;
    }

    /**
     * Persist global config to split TOML files.
     */
    private static void saveGlobalConfig(SyncedClientConfig config) {
        MATomlConfigStore.save(CONFIG_DIR, config == null ? SyncedClientConfig.defaults() : config);
    }

    /**
     * Update root config snapshots from one synced config payload.
     */
    private static void setGlobalConfigInternal(SyncedClientConfig config) {
        SyncedClientConfig value = config == null ? SyncedClientConfig.defaults() : config;
        clientRootConfig = MAClientRootConfig.fromSyncedConfig(value);
        serverRootConfig = MAServerRootConfig.fromSyncedConfig(value);
        LogUtils.setConfigDebugLoggingEnabled(clientRootConfig.client().debugLogging());
    }
}
