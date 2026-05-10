package uk.co.duelmonster.minersadvantage.common.services.sync;

import java.util.HashMap;
import java.util.Map;
import uk.co.duelmonster.minersadvantage.common.config.ServerOverridesConfig;
import uk.co.duelmonster.minersadvantage.common.config.SyncedClientConfig;
import uk.co.duelmonster.minersadvantage.common.services.policy.PolicyCoreService;

public final class SyncCoreService {
    public record PlayerSyncState(
        long playerId,
        long revision,
        SyncedClientConfig clientConfig,
        SyncedClientConfig serverConfig,
        ServerOverridesConfig serverOverrides,
        SyncedClientConfig effectiveConfig
    ) {}

    private final Map<Long, PlayerSyncState> perPlayerState = new HashMap<>();

    public PlayerSyncState synchronize(
        long playerId,
        SyncedClientConfig clientConfig,
        SyncedClientConfig serverConfig,
        ServerOverridesConfig serverOverrides,
        PolicyCoreService policyCoreService
    ) {
        PlayerSyncState previous = perPlayerState.get(playerId);
        long revision = previous == null ? 1L : previous.revision() + 1L;
        SyncedClientConfig effectiveConfig = policyCoreService.applyServerOverrides(clientConfig, serverConfig, serverOverrides);
        PlayerSyncState state = new PlayerSyncState(playerId, revision, clientConfig, serverConfig, serverOverrides, effectiveConfig);
        perPlayerState.put(playerId, state);
        return state;
    }

    public PlayerSyncState getPlayerState(long playerId) {
        return perPlayerState.getOrDefault(
            playerId,
            new PlayerSyncState(
                playerId,
                0L,
                SyncedClientConfig.defaults(),
                SyncedClientConfig.defaults(),
                new ServerOverridesConfig(),
                SyncedClientConfig.defaults()
            )
        );
    }
}
