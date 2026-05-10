package uk.co.duelmonster.minersadvantage.fabric;

import uk.co.duelmonster.minersadvantage.common.MinersAdvantageCore;
import uk.co.duelmonster.minersadvantage.common.event.CommonEventHandlerImpl;

public final class FabricBootstrap {
    private static final MinersAdvantageCore CORE = new MinersAdvantageCore();
    private static CommonEventHandlerImpl eventHandler;

    private FabricBootstrap() {
    }

    public static void initialize() {
        CORE.bootstrap();
        eventHandler = new CommonEventHandlerImpl(CORE);
        // Event wiring will be registered via Fabric event callbacks
    }

    public static CommonEventHandlerImpl getEventHandler() {
        return eventHandler;
    }
}
