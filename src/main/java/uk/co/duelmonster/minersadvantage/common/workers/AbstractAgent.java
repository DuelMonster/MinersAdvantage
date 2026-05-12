package uk.co.duelmonster.minersadvantage.common.workers;

import net.minecraft.core.BlockPos;

/**
 * Shared compatibility base for migrated feature agents.
 */
public abstract class AbstractAgent implements Agent {
    protected final Object player;
    protected final Object packet;

    protected AbstractAgent(Object player, Object packet) {
        this.player = player;
        this.packet = packet;
    }

    @Override
    public boolean shouldProcess(BlockPos pos) {
        return pos != null;
    }

    @Override
    public void tick(Object worldContext) {
        // Compatibility default no-op.
    }
}
