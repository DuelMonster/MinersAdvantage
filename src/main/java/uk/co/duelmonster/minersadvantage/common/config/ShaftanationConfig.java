package uk.co.duelmonster.minersadvantage.common.config;

import uk.co.duelmonster.minersadvantage.common.services.utility.TorchPlacement;

public record ShaftanationConfig(
    boolean enabled,
    int maxDepth,
    int processesPerTick,
    int shaftWidth,
    int shaftHeight,
    TorchPlacement torchPlacement
) {
    public ShaftanationConfig(boolean enabled, int maxDepth, int processesPerTick) {
        this(enabled, maxDepth, processesPerTick, 1, 2, TorchPlacement.FLOOR);
    }

    public int shaftLength() {
        return maxDepth;
    }
}
