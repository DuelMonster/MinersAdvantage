package uk.co.duelmonster.minersadvantage.common.services.core;

import java.util.HashMap;
import java.util.Map;

/**
 * PlayerStateService keeps this part of MinersAdvantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class PlayerStateService {
  private final Map<Long, PlayerState> states = new HashMap<>();

  /**
   * PlayerState keeps this part of MinersAdvantage running without turning server ticks into confetti.
   * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
   */
  public record PlayerState(
      long playerId,
      boolean hungerGuardActive,
      long lastHarvestTick,
      int recentHarvests,
      boolean excavationToggled,
      boolean shaftVentToggled,
      int selectedExcavationShapeIndex,
      int selectedShaftanationShapeIndex) {
    /**
     * i se xc av at io na ct iv e exists so this path stays predictable and easier to debug when things get weird.
     */
    public boolean isExcavationActive() {
      return excavationToggled;
    }

    /**
     * p la ye rs ta te exists so this path stays predictable and easier to debug when things get weird.
     */
    public PlayerState(long playerId, boolean hungerGuardActive, long lastHarvestTick, int recentHarvests,
        boolean excavationToggled, boolean shaftVentToggled) {
      this(playerId, hungerGuardActive, lastHarvestTick, recentHarvests, excavationToggled, shaftVentToggled, 0, 0);
    }
  }

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
    return states.getOrDefault(playerId, new PlayerState(playerId, false, 0L, 0, false, false, 0, 0));
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
    // Periodic state cleanup can happen here (future-you will thank present-you).
  }
}
