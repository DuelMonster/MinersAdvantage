package uk.co.duelmonster.minersadvantage.common.orchestration;

import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;

/**
 * FeatureDispatchContext keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public record FeatureDispatchContext(
    FeatureId feature,
    int blockX,
    int blockY,
    int blockZ,
    String blockId,
    String toolId,
    long playerId
) {}



