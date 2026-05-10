package uk.co.duelmonster.minersadvantage.common.config;

import java.util.List;

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
    public ExcavationConfig(boolean enabled, int radiusHorizontal, int radiusVertical, int processesPerTick) {
        this(enabled, radiusHorizontal, radiusVertical, processesPerTick, false, false, false, List.of());
    }

    public ExcavationConfig {
        blockBlacklist = blockBlacklist == null ? List.of() : List.copyOf(blockBlacklist);
    }

    public boolean isBlacklisted(String blockId) {
        if (blockId == null || blockId.isBlank()) {
            return isBlockWhitelist;
        }
        return blockBlacklist.contains(blockId) ? !isBlockWhitelist : isBlockWhitelist;
    }
}
