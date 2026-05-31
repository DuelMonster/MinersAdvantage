package uk.co.duelmonster.minersadvantage.common.config;

import java.util.List;

/**
 * LumbinationConfig keeps this part of MinersAdvantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public record LumbinationConfig(
    boolean enabled,
    int maxTrunkRange,
    int maxLeafRange,
    int processesPerTick,
    boolean chopTreeBelow,
    boolean destroyLeaves,
    boolean leavesAffectDurability,
    boolean replantSaplings,
    boolean useCanopyTool,
    boolean ignorePlayerPlacedLeaves,
    List<String> logs,
    List<String> leaves,
    List<String> axes
) {
    /**
     * LumbinationConfig exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public LumbinationConfig(boolean enabled, int maxTrunkRange, int maxLeafRange, int processesPerTick) {
        this(enabled, maxTrunkRange, maxLeafRange, processesPerTick, true, true, false, true, true, true, List.of(), List.of(), List.of());
    }

    public LumbinationConfig {
        logs = logs == null ? List.of() : List.copyOf(logs);
        leaves = leaves == null ? List.of() : List.copyOf(leaves);
        axes = axes == null ? List.of() : List.copyOf(axes);
    }

    /**
     * trunkRange exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public int trunkRange() {
        return maxTrunkRange;
    }

    /**
     * leafRange exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public int leafRange() {
        return maxLeafRange;
    }
}
