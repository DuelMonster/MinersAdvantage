package uk.co.duelmonster.minersadvantage.common.event.server;

import uk.co.duelmonster.minersadvantage.common.MinersAdvantageCore;
import uk.co.duelmonster.minersadvantage.common.Variables;

/**
 * Legacy compatibility facade for server-side event entry points.
 */
public final class ServerEventHandler {
    private final MinersAdvantageCore core;

    public ServerEventHandler(MinersAdvantageCore core) {
        this.core = core;
    }

    public void onPlayerLoggedIn(long playerId) {
        Variables vars = Variables.get(new java.util.UUID(0L, playerId));
        vars.HasPlayerSpawned = true;
        Variables.syncToPlayer(playerId);
    }

    public void onPlayerLoggedOut(long playerId) {
        Variables vars = Variables.get(new java.util.UUID(0L, playerId));
        vars.HasPlayerSpawned = false;
        Variables.syncToPlayer(playerId);
    }

    public void onServerTick() {
        core.serverTick();
    }
}
