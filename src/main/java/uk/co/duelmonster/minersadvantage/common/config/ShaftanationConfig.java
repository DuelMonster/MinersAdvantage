package uk.co.duelmonster.minersadvantage.common.config;

import uk.co.duelmonster.minersadvantage.common.services.utility.TorchPlacement;

/**
 * ShaftanationConfig keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public record ShaftanationConfig(
    boolean enabled,
    int maxDepth,
    int processesPerTick,
    int shaftWidth,
    int shaftHeight,
    TorchPlacement torchPlacement
) {
    /**
     * ShaftanationConfig exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public ShaftanationConfig(boolean enabled, int maxDepth, int processesPerTick) {
        this(enabled, maxDepth, processesPerTick, 1, 2, TorchPlacement.FLOOR);
    }

    /**
     * shaftLength exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public int shaftLength() {
        return maxDepth;
    }
}



