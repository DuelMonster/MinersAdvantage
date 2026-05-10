package uk.co.duelmonster.minersadvantage.common.services.utility;

public final class SubstitutionCoreService {
    public ToolCandidate selectBest(ToolCandidate primary, ToolCandidate secondary, boolean prioritizeSilkTouch) {
        if (primary.blacklisted()) return secondary;
        if (secondary.blacklisted()) return primary;

        if (prioritizeSilkTouch) {
            if (primary.silkTouchLevel() > 0 && secondary.silkTouchLevel() == 0) return primary;
            if (secondary.silkTouchLevel() > 0 && primary.silkTouchLevel() == 0) return secondary;
        }

        return primary.speedScore() >= secondary.speedScore() ? primary : secondary;
    }
}
