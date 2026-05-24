package uk.co.duelmonster.minersadvantage.common.config;

import java.util.List;

/**
 * VeinationConfig keeps this part of Miners Advantage running without turning server ticks into confetti.
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
    List<String> pickaxeBlacklist
) {
    /**
     * VeinationConfig exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public VeinationConfig(boolean enabled, int maxVeinDistance) {
        this(enabled, maxVeinDistance, List.of(), true, true, true, 0.2D, List.of());
    }

    public VeinationConfig(boolean enabled, int maxVeinDistance, List<String> ores) {
        this(enabled, maxVeinDistance, ores, true, true, true, 0.2D, List.of());
    }

    public VeinationConfig {
        ores = ores == null ? List.of() : List.copyOf(ores);
        pickaxeBlacklist = pickaxeBlacklist == null ? List.of() : List.copyOf(pickaxeBlacklist);
        increasedHarvestingTimePerOreModifier = Math.max(0.01D, Math.min(10.0D, increasedHarvestingTimePerOreModifier));
    }
}
