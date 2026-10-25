package uk.co.duelmonster.minersadvantage.common.services.core;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;

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
      int selectedShaftanationShapeIndex,
      Map<FeatureId, Boolean> featureEnabled) {
    public PlayerState {
      featureEnabled = featureEnabled == null ? Map.of() : Map.copyOf(featureEnabled);
    }

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
      this(playerId, hungerGuardActive, lastHarvestTick, recentHarvests, excavationToggled, shaftVentToggled, 0, 0,
          Map.of());
    }

    public PlayerState(long playerId, boolean hungerGuardActive, long lastHarvestTick, int recentHarvests,
        boolean excavationToggled, boolean shaftVentToggled, int selectedExcavationShapeIndex,
        int selectedShaftanationShapeIndex) {
      this(playerId, hungerGuardActive, lastHarvestTick, recentHarvests, excavationToggled, shaftVentToggled,
          selectedExcavationShapeIndex, selectedShaftanationShapeIndex, Map.of());
    }

    public boolean isFeatureEnabled(FeatureId featureId) {
      return featureId != null && featureEnabled.getOrDefault(featureId, true);
    }

    private PlayerState withFeatureEnabled(FeatureId featureId, boolean enabled) {
      EnumMap<FeatureId, Boolean> nextFeatureEnabled = new EnumMap<>(FeatureId.class);
      nextFeatureEnabled.putAll(featureEnabled);
      nextFeatureEnabled.put(featureId, enabled);
      return new PlayerState(
          playerId,
          hungerGuardActive,
          lastHarvestTick,
          recentHarvests,
          excavationToggled,
          shaftVentToggled,
          selectedExcavationShapeIndex,
          selectedShaftanationShapeIndex,
          nextFeatureEnabled);
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
   * Record a keybind preference without changing the server-wide feature configuration.
   */
  public void updateFeatureEnabled(long playerId, FeatureId featureId, boolean enabled) {
    if (featureId == null) {
      return;
    }
    PlayerState current = getPlayerState(playerId);
    states.put(playerId, current.withFeatureEnabled(featureId, enabled));
  }

  /**
   * tick exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public void tick() {
    // Periodic state cleanup can happen here (future-you will thank present-you).
  }
}
