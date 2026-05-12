package uk.co.duelmonster.minersadvantage.common.services.sync;

import java.util.HashMap;
import java.util.Map;
import uk.co.duelmonster.minersadvantage.common.config.ServerOverridesConfig;
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
                SyncedClientConfig.defaults(),
                SyncedClientConfig.defaults(),
                new ServerOverridesConfig(),
                SyncedClientConfig.defaults()
            )
        );
    }
}
