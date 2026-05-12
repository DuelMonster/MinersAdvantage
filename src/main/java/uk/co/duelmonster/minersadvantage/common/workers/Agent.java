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
        // Compatibility default no-op.
    }
}
