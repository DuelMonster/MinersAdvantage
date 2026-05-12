package uk.co.duelmonster.minersadvantage.common.services.substitution;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * SubstitutionCoreService keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class SubstitutionCoreService {
    // Why this exists: Parity note: core tool-ranking and decision behavior is ported; deeper combat/tool simulation parity is tracked in checklist. (future-you will thank present-you).

    public enum RankingMode {
        GENERAL,
        MINING_SILK,
        MINING_FORTUNE,
        COMBAT,
        RESTORE
    }

    /*
    public void processToolSubtitution(ServerPlayer player, BlockPos pos) {
        // Why this exists: ...see legacy for logic... (future-you will thank present-you).
    }
    */
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
     * resolveMode exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    private RankingMode resolveMode(boolean favourSilkTouch, boolean favourFortune, boolean combatContext, boolean switchBackToPrimary) {
        if (switchBackToPrimary) {
            return RankingMode.RESTORE;
        }
        if (combatContext) {
            return RankingMode.COMBAT;
        }
        if (favourSilkTouch) {
            return RankingMode.MINING_SILK;
        }
        if (favourFortune) {
            return RankingMode.MINING_FORTUNE;
        }
        return RankingMode.GENERAL;
    }

    /**
     * candidateScore exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    private double candidateScore(ToolCandidate candidate, RankingMode mode) {
        return switch (mode) {
            case COMBAT -> (candidate.attackScore() * 1000.0) + (candidate.speedScore() * 10.0);
            case MINING_SILK -> (candidate.silkTouchLevel() * 1000.0) + (candidate.speedScore() * 100.0) + candidate.fortuneLevel();
            case MINING_FORTUNE -> (candidate.fortuneLevel() * 1000.0) + (candidate.speedScore() * 100.0) + candidate.silkTouchLevel();
            case GENERAL -> (candidate.speedScore() * 100.0) + (candidate.attackScore() * 10.0) + candidate.fortuneLevel() + candidate.silkTouchLevel();
            case RESTORE -> 0.0;
        };
    }

    /**
     * comparatorFor exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private Comparator<ToolCandidate> comparatorFor(boolean favourSilkTouch, boolean favourFortune, boolean combatContext) {
        RankingMode mode = resolveMode(favourSilkTouch, favourFortune, combatContext, false);
        return Comparator
            .comparingDouble((ToolCandidate c) -> candidateScore(c, mode))
            .thenComparing(ToolCandidate::id);
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

    public ToolCandidate selectBest(
        List<ToolCandidate> candidates,
        boolean favourSilkTouch,
        boolean favourFortune,
        boolean combatContext,
        Set<String> blacklist,
        boolean allowMending
    ) {
        return candidates.stream()
            .filter(c -> !c.blacklisted())
            .filter(c -> blacklist == null || !blacklist.contains(c.id()))
            .filter(c -> allowMending || !c.mendingProtected())
            .max(comparatorFor(favourSilkTouch, favourFortune, combatContext))
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
        RankingMode mode = resolveMode(favourSilkTouch, favourFortune, combatContext, switchBackToPrimary);
        if (mode == RankingMode.RESTORE) {
            return new SubstitutionDecision(currentToolId, false, true, mode.name().toLowerCase());
        }

        ToolCandidate best = candidates.stream()
            .filter(c -> !c.blacklisted())
            .filter(c -> allowMending || !c.mendingProtected())
            .max(comparatorFor(favourSilkTouch, favourFortune, combatContext))
            .orElse(null);

        if (best == null) {
            return new SubstitutionDecision(currentToolId, false, false, "unavailable");
        }

        return new SubstitutionDecision(best.id(), !best.id().equals(currentToolId), false, mode.name().toLowerCase());
    }

    public SubstitutionDecision decideTool(
        String currentToolId,
        List<ToolCandidate> candidates,
        boolean allowMending,
        boolean favourSilkTouch,
        boolean favourFortune,
        boolean combatContext,
        boolean switchBackToPrimary,
        Set<String> blacklist
    ) {
        RankingMode mode = resolveMode(favourSilkTouch, favourFortune, combatContext, switchBackToPrimary);
        if (mode == RankingMode.RESTORE) {
            return new SubstitutionDecision(currentToolId, false, true, mode.name().toLowerCase());
        }

        ToolCandidate best = selectBest(candidates, favourSilkTouch, favourFortune, combatContext, blacklist, allowMending);
        if (best == null) {
            return new SubstitutionDecision(currentToolId, false, false, "unavailable");
        }
        return new SubstitutionDecision(best.id(), !best.id().equals(currentToolId), false, mode.name().toLowerCase());
    }
}
