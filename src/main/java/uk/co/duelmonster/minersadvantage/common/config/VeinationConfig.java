package uk.co.duelmonster.minersadvantage.common.config;

import java.util.List;

public record VeinationConfig(
    boolean enabled,
    int maxVeinDistance,
    List<String> ores
) {
    public VeinationConfig(boolean enabled, int maxVeinDistance) {
        this(enabled, maxVeinDistance, List.of());
    }

    public VeinationConfig {
        ores = ores == null ? List.of() : List.copyOf(ores);
    }
}
