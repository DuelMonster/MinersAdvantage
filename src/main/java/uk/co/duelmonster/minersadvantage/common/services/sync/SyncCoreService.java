package uk.co.duelmonster.minersadvantage.common.services.sync;

import java.util.HashMap;
import java.util.Map;

public final class SyncCoreService {
    private final Map<String, Map<String, Object>> perPlayerState = new HashMap<>();

    public void updatePlayerState(String playerId, Map<String, Object> state) {
        perPlayerState.put(playerId, new HashMap<>(state));
    }

    public Map<String, Object> getPlayerState(String playerId) {
        return perPlayerState.getOrDefault(playerId, Map.of());
    }
}
