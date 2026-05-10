package uk.co.duelmonster.minersadvantage.neoforge;

import uk.co.duelmonster.minersadvantage.common.MinersAdvantageCore;
import uk.co.duelmonster.minersadvantage.common.event.CommonEventHandlerImpl;

public final class NeoForgeBootstrap {
    private static final MinersAdvantageCore CORE = new MinersAdvantageCore();
    private static CommonEventHandlerImpl eventHandler;

    private NeoForgeBootstrap() {
    }

    public static void initialize() {
        CORE.bootstrap();
        eventHandler = new CommonEventHandlerImpl(CORE);
        // Event wiring will be registered via NeoForge event handlers
    }

    public static CommonEventHandlerImpl getEventHandler() {
        return eventHandler;
    }
}
