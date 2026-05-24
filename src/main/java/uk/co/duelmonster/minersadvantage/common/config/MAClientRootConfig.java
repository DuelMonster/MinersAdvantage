package uk.co.duelmonster.minersadvantage.common.config;

/**
 * Explicit client-side configuration root.
 */
public record MAClientRootConfig(
    ClientConfig client
) {
    public MAClientRootConfig {
        client = client == null ? SyncedClientConfig.defaults().client() : client;
    }

    public static MAClientRootConfig defaults() {
        return new MAClientRootConfig(SyncedClientConfig.defaults().client());
    }

    public static MAClientRootConfig fromSyncedConfig(SyncedClientConfig synced) {
        SyncedClientConfig value = synced == null ? SyncedClientConfig.defaults() : synced;
        return new MAClientRootConfig(value.client());
    }

    public SyncedClientConfig toSyncedConfig(MAServerRootConfig serverConfig) {
        MAServerRootConfig serverValue = serverConfig == null ? MAServerRootConfig.defaults() : serverConfig;
        return serverValue.toSyncedConfig(client);
    }
}