package uk.co.duelmonster.minersadvantage.common.setup;

import uk.co.duelmonster.minersadvantage.common.MinersAdvantageCore;

/**
 * Legacy compatibility entry point for client-side setup wiring.
 */
public final class ClientSetup {
    private final MinersAdvantageCore core;

    public ClientSetup(MinersAdvantageCore core) {
        this.core = core;
    }

    public void initializeClient() {
        // Client registration points are handled by platform entrypoints.
        core.defaultConfig();
    }
}
