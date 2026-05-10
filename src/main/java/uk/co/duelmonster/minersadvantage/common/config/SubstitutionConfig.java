package uk.co.duelmonster.minersadvantage.common.config;

import java.util.List;

public record SubstitutionConfig(
    boolean enabled,
    boolean allowMending,
    boolean prioritizeSilkTouch,
    boolean switchBack,
    boolean favourFortune,
    boolean ignoreIfValidTool,
    boolean ignorePassiveMobs,
    List<String> blacklist
) {
    public SubstitutionConfig(boolean enabled, boolean allowMending, boolean prioritizeSilkTouch) {
        this(enabled, allowMending, prioritizeSilkTouch, true, true, true, true, List.of());
    }

    public SubstitutionConfig {
        blacklist = blacklist == null ? List.of() : List.copyOf(blacklist);
    }

    public boolean favourSilkTouch() {
        return prioritizeSilkTouch;
    }
}
