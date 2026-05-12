package uk.co.duelmonster.minersadvantage.common.config;

import java.util.List;

/**
 * ExcavationConfig keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public record ExcavationConfig(
    boolean enabled,
    int radiusHorizontal,
    int radiusVertical,
    int processesPerTick,
    boolean toggleMode,
    boolean ignoreBlockVariants,
    boolean isBlockWhitelist,
    List<String> blockBlacklist
) {
    /**
     * ExcavationConfig exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public ExcavationConfig(boolean enabled, int radiusHorizontal, int radiusVertical, int processesPerTick) {
        this(enabled, radiusHorizontal, radiusVertical, processesPerTick, false, false, false, List.of());
    }

    public ExcavationConfig {
        blockBlacklist = blockBlacklist == null ? List.of() : List.copyOf(blockBlacklist);
    }

    /**
     * isBlacklisted exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public boolean isBlacklisted(String blockId) {
        if (blockId == null || blockId.isBlank()) {
            return isBlockWhitelist;
        }
        return blockBlacklist.contains(blockId) ? !isBlockWhitelist : isBlockWhitelist;
    }
}
