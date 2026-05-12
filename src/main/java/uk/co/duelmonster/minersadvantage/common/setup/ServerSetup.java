package uk.co.duelmonster.minersadvantage.common.setup;

import uk.co.duelmonster.minersadvantage.common.MinersAdvantageCore;

/**
 * Legacy compatibility entry point for server-side setup wiring.
 */
public final class ServerSetup {
    private final MinersAdvantageCore core;

    public ServerSetup(MinersAdvantageCore core) {
        this.core = core;
    }

    public void initializeServer() {
        core.tickOrchestrator();
    }
}
