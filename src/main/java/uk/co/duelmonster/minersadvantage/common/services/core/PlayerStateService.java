package uk.co.duelmonster.minersadvantage.common.services.core;

import java.util.HashMap;
import java.util.Map;

public final class PlayerStateService {
    private final Map<Long, PlayerState> states = new HashMap<>();

    public record PlayerState(
        long playerId,
        boolean hungerGuardActive,
        long lastHarvestTick,
        int recentHarvests
    ) {}

    public void updatePlayerState(long playerId, PlayerState state) {
        states.put(playerId, state);
    }

    public PlayerState getPlayerState(long playerId) {
        return states.getOrDefault(playerId, new PlayerState(playerId, false, 0L, 0));
    }

    public void clearPlayerState(long playerId) {
        states.remove(playerId);
    }

    public void tick() {
        // Periodic state cleanup can happen here
    }
}
