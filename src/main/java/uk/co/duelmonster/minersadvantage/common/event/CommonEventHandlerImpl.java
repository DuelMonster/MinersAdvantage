package uk.co.duelmonster.minersadvantage.common.event;

import uk.co.duelmonster.minersadvantage.common.MinersAdvantageCore;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;
import uk.co.duelmonster.minersadvantage.common.registry.RegistryPredicates;

/**
 * CommonEventHandlerImpl keeps this part of MinersAdvantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class CommonEventHandlerImpl implements ToolEventHandler {
  private final MinersAdvantageCore core;

  /**
   * CommonEventHandlerImpl exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public CommonEventHandlerImpl(MinersAdvantageCore core) {
    this.core = core;
  }

  @Override
  /**
   * onPickaxeUse exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public void onPickaxeUse(long playerId, int blockX, int blockY, int blockZ, String blockId) {
    if (RegistryPredicates.isStoneLikeBlockId(blockId)) {
      dispatchIfEnabled(playerId, FeatureId.SHAFTANATION, blockX, blockY, blockZ, blockId, "pickaxe");
      return;
    }
    if (RegistryPredicates.isOreLikeBlockId(blockId)) {
      dispatchIfEnabled(playerId, FeatureId.EXCAVATION, blockX, blockY, blockZ, blockId, "pickaxe");
    }
  }

  @Override
  /**
   * onShovelUse exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public void onShovelUse(long playerId, int blockX, int blockY, int blockZ, String blockId) {
    if (RegistryPredicates.isDirtLikeBlockId(blockId)) {
      dispatchIfEnabled(playerId, FeatureId.EXCAVATION, blockX, blockY, blockZ, blockId, "shovel");
    }
  }

  @Override
  /**
   * onHoeUse exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public void onHoeUse(long playerId, int blockX, int blockY, int blockZ, String blockId) {
    if (RegistryPredicates.isCropBlockId(blockId)) {
      dispatchIfEnabled(playerId, FeatureId.CROPINATION, blockX, blockY, blockZ, blockId, "hoe");
    } else if (RegistryPredicates.isDirtLikeBlockId(blockId)) {
      dispatchIfEnabled(playerId, FeatureId.CULTIVATION, blockX, blockY, blockZ, blockId, "hoe");
    }
  }

  @Override
  /**
   * onAxeUse exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public void onAxeUse(long playerId, int blockX, int blockY, int blockZ, String blockId) {
    if (RegistryPredicates.isLogLikeBlockId(blockId)) {
      dispatchIfEnabled(playerId, FeatureId.LUMBINATION, blockX, blockY, blockZ, blockId, "axe");
    }
  }

  @Override
  /**
   * onItemPickup exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public void onItemPickup(long playerId, String itemId, boolean isDirectPickup) {
    if (!isFeatureEnabled(playerId, FeatureId.CAPTIVATION)) {
      return;
    }
    // Captivation handles item pickup events (future-you will thank present-you).
    FeatureEventHandler.onToolUse(FeatureId.CAPTIVATION, 0, 0, 0, "item:" + itemId, "hand", playerId);
  }

  private boolean isFeatureEnabled(long playerId, FeatureId featureId) {
    return core.isFeatureEnabledForPlayer(playerId, featureId);
  }

  private void dispatchIfEnabled(
      long playerId,
      FeatureId featureId,
      int blockX,
      int blockY,
      int blockZ,
      String blockId,
      String toolType) {
    if (!isFeatureEnabled(playerId, featureId)) {
      return;
    }
    FeatureEventHandler.onToolUse(featureId, blockX, blockY, blockZ, blockId, toolType, playerId);
  }

  @Override
  /**
   * onServerTick exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public void onServerTick() {
    core.serverTick();
  }
}
