package uk.co.duelmonster.minersadvantage.common.config;

import uk.co.duelmonster.minersadvantage.common.services.utility.TorchPlacement;

/**
 * ShaftanationConfig keeps this part of MinersAdvantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public record ShaftanationConfig(
    boolean enabled,
    int depth,
    int processesPerTick,
    int width,
    int height,
    TorchPlacement torchPlacement,
    int maxActiveAgents,
    boolean dedupeAgent
) {
    /**
     * ShaftanationConfig exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public ShaftanationConfig(boolean enabled, int depth, int processesPerTick) {
        this(enabled, depth, processesPerTick, 1, 2, TorchPlacement.FLOOR, 4, true);
    }

    public ShaftanationConfig(boolean enabled, int depth, int processesPerTick, int width, int height, TorchPlacement torchPlacement) {
        this(enabled, depth, processesPerTick, width, height, torchPlacement, 4, true);
    }

    public ShaftanationConfig {
        maxActiveAgents = Math.max(1, maxActiveAgents);
    }

    /**
     * shaftLength exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public int shaftLength() {
        return depth;
    }
}
