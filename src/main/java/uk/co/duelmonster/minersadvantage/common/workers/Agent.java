package uk.co.duelmonster.minersadvantage.common.workers;

import net.minecraft.core.BlockPos;

/**
 * Legacy compatibility worker contract.
 */
public interface Agent {
    default boolean shouldProcess(BlockPos pos) {
        return true;
    }

    default void tick(Object worldContext) {
        // Why this exists: Compatibility default no-op. (future-you will thank present-you).
    }
}


