package uk.co.duelmonster.minersadvantage.common.services.sync;

import java.util.HashMap;
import java.util.Map;
import uk.co.duelmonster.minersadvantage.common.config.MAClientRootConfig;
import uk.co.duelmonster.minersadvantage.common.config.MAServerRootConfig;
import uk.co.duelmonster.minersadvantage.common.config.SyncedClientConfig;
import uk.co.duelmonster.minersadvantage.common.services.policy.PolicyCoreService;

/**
 * SyncCoreService keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class SyncCoreService {
    /**
     * PlayerSyncState keeps this part of Miners Advantage running without turning server ticks into confetti.
     * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
     */
    public record PlayerSyncState(
        long playerId,
        long revision,
        MAClientRootConfig clientConfig,
        MAServerRootConfig serverConfig,
        SyncedClientConfig effectiveConfig
    ) {}

    private final Map<Long, PlayerSyncState> perPlayerState = new HashMap<>();

    public PlayerSyncState synchronize(
        long playerId,
        MAClientRootConfig clientConfig,
        MAServerRootConfig serverConfig,
        PolicyCoreService policyCoreService
    ) {
        PlayerSyncState previous = perPlayerState.get(playerId);
        long revision = previous == null ? 1L : previous.revision() + 1L;
        MAClientRootConfig clientValue = clientConfig == null ? MAClientRootConfig.defaults() : clientConfig;
        MAServerRootConfig serverValue = serverConfig == null ? MAServerRootConfig.defaults() : serverConfig;
        SyncedClientConfig clientSynced = clientValue.toSyncedConfig(serverValue);
        SyncedClientConfig serverSynced = serverValue.toSyncedConfig(clientValue.client());
        SyncedClientConfig effectiveConfig = policyCoreService.applyServerAuthoritative(clientSynced, serverSynced);
        PlayerSyncState state = new PlayerSyncState(playerId, revision, clientValue, serverValue, effectiveConfig);
        perPlayerState.put(playerId, state);
        return state;
    }

    /**
     * getPlayerState exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public PlayerSyncState getPlayerState(long playerId) {
        return perPlayerState.getOrDefault(
            playerId,
            new PlayerSyncState(
                playerId,
                0L,
                MAClientRootConfig.defaults(),
                MAServerRootConfig.defaults(),
                SyncedClientConfig.defaults()
            )
        );
    }
}
