package uk.co.duelmonster.minersadvantage.neoforge;

import uk.co.duelmonster.minersadvantage.common.MinersAdvantageCore;

public final class NeoForgeBootstrap {
    private static final MinersAdvantageCore CORE = new MinersAdvantageCore();

    private NeoForgeBootstrap() {
    }

    public static void initialize() {
        CORE.bootstrap();
    }
}
