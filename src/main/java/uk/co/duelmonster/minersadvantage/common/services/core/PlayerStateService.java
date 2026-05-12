package uk.co.duelmonster.minersadvantage.common.services.core;

import java.util.HashMap;
import java.util.Map;

/**
 * PlayerStateService keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class PlayerStateService {
    private final Map<Long, PlayerState> states = new HashMap<>();

    /**
     * PlayerState keeps this part of Miners Advantage running without turning server ticks into confetti.
     * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
     */
    public record PlayerState(
        long playerId,
        boolean hungerGuardActive,
        long lastHarvestTick,
        int recentHarvests
    ) {}

    /**
     * updatePlayerState exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public void updatePlayerState(long playerId, PlayerState state) {
        states.put(playerId, state);
    }

    /**
     * getPlayerState exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public PlayerState getPlayerState(long playerId) {
        return states.getOrDefault(playerId, new PlayerState(playerId, false, 0L, 0));
    }

    /**
     * clearPlayerState exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public void clearPlayerState(long playerId) {
        states.remove(playerId);
    }

    /**
     * tick exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public void tick() {
        // Why this exists: Periodic state cleanup can happen here (future-you will thank present-you).
    }
}




