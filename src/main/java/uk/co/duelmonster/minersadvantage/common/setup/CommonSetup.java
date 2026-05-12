package uk.co.duelmonster.minersadvantage.common.setup;

import uk.co.duelmonster.minersadvantage.common.MinersAdvantageCore;

/**
 * Legacy compatibility entry point for common setup wiring.
 */
public final class CommonSetup {
    private final MinersAdvantageCore core;

    /**
     * CommonSetup exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public CommonSetup(MinersAdvantageCore core) {
        this.core = core;
    }

    /**
     * initialize exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public void initialize() {
        core.bootstrap();
    }
}


