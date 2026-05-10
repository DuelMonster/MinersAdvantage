package uk.co.duelmonster.minersadvantage.common.config;

import java.util.List;

public record LumbinationConfig(
    boolean enabled,
    int maxTrunkRange,
    int maxLeafRange,
    int processesPerTick,
    boolean chopTreeBelow,
    boolean destroyLeaves,
    boolean leavesAffectDurability,
    boolean replantSaplings,
    boolean useShearsOnLeaves,
    List<String> logs,
    List<String> leaves,
    List<String> axes
) {
    public LumbinationConfig(boolean enabled, int maxTrunkRange, int maxLeafRange, int processesPerTick) {
        this(enabled, maxTrunkRange, maxLeafRange, processesPerTick, true, true, false, true, true, List.of(), List.of(), List.of());
    }

    public LumbinationConfig {
        logs = logs == null ? List.of() : List.copyOf(logs);
        leaves = leaves == null ? List.of() : List.copyOf(leaves);
        axes = axes == null ? List.of() : List.copyOf(axes);
    }

    public int trunkRange() {
        return maxTrunkRange;
    }

    public int leafRange() {
        return maxLeafRange;
    }
}
