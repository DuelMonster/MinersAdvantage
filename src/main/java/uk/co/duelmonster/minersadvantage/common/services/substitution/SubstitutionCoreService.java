package uk.co.duelmonster.minersadvantage.common.services.substitution;

import java.util.Comparator;
import java.util.List;

/**
 * SubstitutionCoreService keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class SubstitutionCoreService {
    /**
     * ToolCandidate keeps this part of Miners Advantage running without turning server ticks into confetti.
     * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
     */
    public record ToolCandidate(
        String id,
        double speedScore,
        int silkTouchLevel,
        int fortuneLevel,
        int attackScore,
        boolean blacklisted,
        boolean mendingProtected
    ) {}

    /**
     * SubstitutionDecision keeps this part of Miners Advantage running without turning server ticks into confetti.
     * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
     */
    public record SubstitutionDecision(String selectedToolId, boolean switched, boolean switchBackToPrimary, String mode) {}

    /**
     * comparatorFor exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
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

