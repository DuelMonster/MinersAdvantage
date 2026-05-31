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

    /**
     * d ef au lt s exists so this path stays predictable and easier to debug when things get weird.
     */
    public static MAClientRootConfig defaults() {
        return new MAClientRootConfig(SyncedClientConfig.defaults().client());
    }

    /**
     * f ro ms yn ce dc on fi g exists so this path stays predictable and easier to debug when things get weird.
     */
    public static MAClientRootConfig fromSyncedConfig(SyncedClientConfig synced) {
        SyncedClientConfig value = synced == null ? SyncedClientConfig.defaults() : synced;
        return new MAClientRootConfig(value.client());
    }

    /**
     * t os yn ce dc on fi g exists so this path stays predictable and easier to debug when things get weird.
     */
    public SyncedClientConfig toSyncedConfig(MAServerRootConfig serverConfig) {
        MAServerRootConfig serverValue = serverConfig == null ? MAServerRootConfig.defaults() : serverConfig;
        return serverValue.toSyncedConfig(client);
    }
}
