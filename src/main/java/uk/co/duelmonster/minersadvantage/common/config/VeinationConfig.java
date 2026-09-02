package uk.co.duelmonster.minersadvantage.common.config;

import java.util.List;

/**
 * VeinationConfig keeps this part of MinersAdvantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public record VeinationConfig(
    boolean enabled,
    int maxVeinDistance,
    List<String> ores,
    boolean oreHarvestWithoutSneak,
    boolean dropOresAtFirstBrokenBlock,
    boolean increaseHarvestingTimePerOre,
    double increasedHarvestingTimePerOreModifier,
    List<String> pickaxeBlacklist,
    int maxActiveAgents,
    boolean dedupeAgent
) {
    /**
     * VeinationConfig exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public VeinationConfig(boolean enabled, int maxVeinDistance) {
        this(enabled, maxVeinDistance, List.of(), true, true, true, 0.2D, List.of(), 4, true);
    }

    /**
     * v ei na ti on co nf ig exists so this path stays predictable and easier to debug when things get weird.
     */
    public VeinationConfig(boolean enabled, int maxVeinDistance, List<String> ores) {
        this(enabled, maxVeinDistance, ores, true, true, true, 0.2D, List.of(), 4, true);
    }

    public VeinationConfig(boolean enabled, int maxVeinDistance, List<String> ores, boolean oreHarvestWithoutSneak,
            boolean dropOresAtFirstBrokenBlock, boolean increaseHarvestingTimePerOre,
            double increasedHarvestingTimePerOreModifier, List<String> pickaxeBlacklist) {
        this(enabled, maxVeinDistance, ores, oreHarvestWithoutSneak, dropOresAtFirstBrokenBlock,
            increaseHarvestingTimePerOre, increasedHarvestingTimePerOreModifier, pickaxeBlacklist, 4, true);
    }

    public VeinationConfig {
        maxVeinDistance = Math.max(1, Math.min(12, maxVeinDistance));
        ores = ores == null ? List.of() : List.copyOf(ores);
        pickaxeBlacklist = pickaxeBlacklist == null ? List.of() : List.copyOf(pickaxeBlacklist);
        increasedHarvestingTimePerOreModifier = Math.max(0.01D, Math.min(10.0D, increasedHarvestingTimePerOreModifier));
        maxActiveAgents = Math.max(1, maxActiveAgents);
    }
}
