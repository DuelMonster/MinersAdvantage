package uk.co.duelmonster.minersadvantage.common.config;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import uk.co.duelmonster.minersadvantage.common.JsonHelper;
import uk.co.duelmonster.minersadvantage.common.log.LogUtils;

/**
 * Legacy compatibility facade for per-player config lookups.
 */
public class MAConfig_Base {
    private static final Path CONFIG_DIR = Path.of(System.getProperty("user.dir"), "config", "minersadvantage");
    private static final Path GLOBAL_CONFIG_FILE = CONFIG_DIR.resolve("client-config.json");
    private static final Map<UUID, SyncedClientConfig> PLAYER_CONFIGS = new ConcurrentHashMap<>();
    private static volatile SyncedClientConfig globalConfig = loadGlobalConfig();

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
        saveGlobalConfig(globalConfig);
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
            if (Files.isRegularFile(GLOBAL_CONFIG_FILE)) {
                String json = Files.readString(GLOBAL_CONFIG_FILE, StandardCharsets.UTF_8);
                SyncedClientConfig parsed = JsonHelper.fromJson(json, SyncedClientConfig.class);
                if (parsed != null) {
                    return parsed;
                }
                LogUtils.logWarn("Config parse failed for {}. Recreating with defaults.", GLOBAL_CONFIG_FILE);
            }
        } catch (Exception exception) {
            LogUtils.logWarn("Unable to load config from {}: {}", GLOBAL_CONFIG_FILE, exception.getMessage());
        }

        SyncedClientConfig defaults = SyncedClientConfig.defaults();
        saveGlobalConfig(defaults);
        return defaults;
    }

    private static void saveGlobalConfig(SyncedClientConfig config) {
        SyncedClientConfig value = Objects.requireNonNullElseGet(config, SyncedClientConfig::defaults);
        try {
            Files.createDirectories(CONFIG_DIR);
            Files.writeString(GLOBAL_CONFIG_FILE, JsonHelper.toJson(value), StandardCharsets.UTF_8);
        } catch (Exception exception) {
            LogUtils.logWarn("Unable to save config to {}: {}", GLOBAL_CONFIG_FILE, exception.getMessage());
        }
    }
}
