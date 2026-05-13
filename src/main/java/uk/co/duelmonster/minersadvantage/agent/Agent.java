package uk.co.duelmonster.minersadvantage.agent;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import uk.co.duelmonster.minersadvantage.common.log.LogUtils;

/**
 * Base class for all feature agents. Modernized for production wiring.
 */
public abstract class Agent {
    protected final ServerPlayer player;
    protected final Level world;
    protected boolean complete = false;

    public Agent(ServerPlayer player) {
        this.player = player;
        this.world = player.level();
        LogUtils.logDebug("Created {} for player={} dimension={}", getClass().getSimpleName(), player.getScoreboardName(), player.level().dimension());
    }

    /**
     * Called every server tick. Returns true if the agent is finished and should be removed.
     */
    public abstract boolean tick();

    public boolean isComplete() {
        return complete;
    }

    protected boolean finish(String reason) {
        complete = true;
        LogUtils.logDebug("Completed {} for player={} reason={}", getClass().getSimpleName(), player.getScoreboardName(), reason);
        return true;
    }
}
