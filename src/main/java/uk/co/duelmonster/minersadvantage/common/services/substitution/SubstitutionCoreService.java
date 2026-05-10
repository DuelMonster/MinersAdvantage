package uk.co.duelmonster.minersadvantage.common.services.substitution;

import java.util.Comparator;
import java.util.List;

public final class SubstitutionCoreService {
    public record ToolCandidate(
        String id,
        double speedScore,
        int silkTouchLevel,
        int fortuneLevel,
        int attackScore,
        boolean blacklisted,
        boolean mendingProtected
    ) {}

    public record SubstitutionDecision(String selectedToolId, boolean switched, boolean switchBackToPrimary, String mode) {}

    private Comparator<ToolCandidate> comparatorFor(boolean favourSilkTouch, boolean favourFortune, boolean combatContext) {
        Comparator<ToolCandidate> comparator;
        if (combatContext) {
            comparator = Comparator.comparingInt(ToolCandidate::attackScore)
                .thenComparingDouble(ToolCandidate::speedScore);
        } else {
            comparator = Comparator.comparingDouble(ToolCandidate::speedScore);
        }

        return comparator
            .thenComparingInt(c -> favourSilkTouch ? c.silkTouchLevel() : 0)
            .thenComparingInt(c -> favourFortune ? c.fortuneLevel() : 0);
    }

    public ToolCandidate selectBest(
        List<ToolCandidate> candidates,
        boolean favourSilkTouch,
        boolean favourFortune
    ) {
        return candidates.stream()
            .filter(c -> !c.blacklisted())
            .max(comparatorFor(favourSilkTouch, favourFortune, false))
            .orElse(null);
    }

    public SubstitutionDecision decideTool(
        String currentToolId,
        List<ToolCandidate> candidates,
        boolean allowMending,
        boolean favourSilkTouch,
        boolean favourFortune,
        boolean combatContext,
        boolean switchBackToPrimary
    ) {
        if (switchBackToPrimary) {
            return new SubstitutionDecision(currentToolId, false, true, "restore");
        }

        ToolCandidate best = candidates.stream()
            .filter(c -> !c.blacklisted())
            .filter(c -> allowMending || !c.mendingProtected())
            .max(comparatorFor(favourSilkTouch, favourFortune, combatContext))
            .orElse(null);

        if (best == null) {
            return new SubstitutionDecision(currentToolId, false, false, "unavailable");
        }

        String mode = combatContext ? "combat" : favourFortune ? "mining_fortune" : favourSilkTouch ? "mining_silk" : "general";
        return new SubstitutionDecision(best.id(), !best.id().equals(currentToolId), false, mode);
    }
}
