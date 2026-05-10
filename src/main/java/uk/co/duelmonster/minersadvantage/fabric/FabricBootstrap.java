package uk.co.duelmonster.minersadvantage.fabric;

import uk.co.duelmonster.minersadvantage.common.MinersAdvantageCore;

public final class FabricBootstrap {
    private static final MinersAdvantageCore CORE = new MinersAdvantageCore();

    private FabricBootstrap() {
    }

    public static void initialize() {
        CORE.bootstrap();
    }
}
