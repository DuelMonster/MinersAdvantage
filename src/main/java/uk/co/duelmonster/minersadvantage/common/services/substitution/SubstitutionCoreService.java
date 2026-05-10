package uk.co.duelmonster.minersadvantage.common.services.substitution;

import java.util.Comparator;
import java.util.List;

public final class SubstitutionCoreService {
    public record ToolCandidate(String id, double speedScore, int silkTouchLevel, int fortuneLevel, boolean blacklisted) {}

    public ToolCandidate selectBest(
        List<ToolCandidate> candidates,
        boolean favourSilkTouch,
        boolean favourFortune
    ) {
        return candidates.stream()
            .filter(c -> !c.blacklisted())
            .max(Comparator
                .comparingDouble(ToolCandidate::speedScore)
                .thenComparingInt(c -> favourSilkTouch ? c.silkTouchLevel() : 0)
                .thenComparingInt(c -> favourFortune ? c.fortuneLevel() : 0))
            .orElse(null);
    }
}
