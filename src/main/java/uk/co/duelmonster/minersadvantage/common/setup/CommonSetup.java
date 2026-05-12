package uk.co.duelmonster.minersadvantage.common.setup;

import uk.co.duelmonster.minersadvantage.common.MinersAdvantageCore;

/**
 * Legacy compatibility entry point for common setup wiring.
 */
public final class CommonSetup {
    private final MinersAdvantageCore core;

    public CommonSetup(MinersAdvantageCore core) {
        this.core = core;
    }

    public void initialize() {
        core.bootstrap();
    }
}
