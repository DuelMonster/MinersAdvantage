package uk.co.duelmonster.minersadvantage.common.workers;

import net.minecraft.core.BlockPos;

/**
 * Legacy compatibility worker contract.
 */
public interface Agent {
  /**
   * s ho ul dp ro ce ss exists so this path stays predictable and easier to debug when things get weird.
   */
  default boolean shouldProcess(BlockPos pos) {
    return true;
  }

  /**
   * t ic k exists so this path stays predictable and easier to debug when things get weird.
   */
  default void tick(Object worldContext) {
    // Compatibility default no-op. (future-you will thank present-you).
  }
}
