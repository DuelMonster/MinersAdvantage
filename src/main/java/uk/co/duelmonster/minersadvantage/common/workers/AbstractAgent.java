package uk.co.duelmonster.minersadvantage.common.workers;

import net.minecraft.core.BlockPos;

/**
 * Shared compatibility base for migrated feature agents.
 */
public abstract class AbstractAgent implements Agent {
    protected final Object player;
    protected final Object packet;

    /**
     * AbstractAgent exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    protected AbstractAgent(Object player, Object packet) {
        this.player = player;
        this.packet = packet;
    }

    @Override
    /**
     * shouldProcess exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public boolean shouldProcess(BlockPos pos) {
        return pos != null;
    }

    @Override
    /**
     * tick exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public void tick(Object worldContext) {
        // Why this exists: Compatibility default no-op. (future-you will thank present-you).
    }
}


