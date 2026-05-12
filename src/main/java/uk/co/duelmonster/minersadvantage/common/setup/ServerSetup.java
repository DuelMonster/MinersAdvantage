package uk.co.duelmonster.minersadvantage.common.setup;

import uk.co.duelmonster.minersadvantage.common.MinersAdvantageCore;

/**
 * Legacy compatibility entry point for server-side setup wiring.
 */
public final class ServerSetup {
    private final MinersAdvantageCore core;

    /**
     * ServerSetup exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public ServerSetup(MinersAdvantageCore core) {
        this.core = core;
    }

    /**
     * initializeServer exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public void initializeServer() {
        core.tickOrchestrator();
    }
}


