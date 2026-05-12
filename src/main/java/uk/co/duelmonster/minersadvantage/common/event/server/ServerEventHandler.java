package uk.co.duelmonster.minersadvantage.common.event.server;

import uk.co.duelmonster.minersadvantage.common.MinersAdvantageCore;
import uk.co.duelmonster.minersadvantage.common.Variables;

/**
 * Legacy compatibility facade for server-side event entry points.
 */
public final class ServerEventHandler {
    private final MinersAdvantageCore core;

    /**
     * ServerEventHandler exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public ServerEventHandler(MinersAdvantageCore core) {
        this.core = core;
    }

    /**
     * onPlayerLoggedIn exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public void onPlayerLoggedIn(long playerId) {
        Variables vars = Variables.get(new java.util.UUID(0L, playerId));
        vars.HasPlayerSpawned = true;
        Variables.syncToPlayer(playerId);
    }

    /**
     * onPlayerLoggedOut exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public void onPlayerLoggedOut(long playerId) {
        Variables vars = Variables.get(new java.util.UUID(0L, playerId));
        vars.HasPlayerSpawned = false;
        Variables.syncToPlayer(playerId);
    }

    /**
     * onServerTick exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public void onServerTick() {
        core.serverTick();
    }
}
