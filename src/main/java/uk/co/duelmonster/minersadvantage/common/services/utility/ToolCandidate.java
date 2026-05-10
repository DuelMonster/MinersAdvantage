package uk.co.duelmonster.minersadvantage.common.services.utility;

public record ToolCandidate(
    int speedScore,
    int silkTouchLevel,
    int fortuneLevel,
    boolean blacklisted
) {}
