package uk.co.duelmonster.minersadvantage.common.orchestration;

import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;

public record FeatureDispatchContext(
    FeatureId feature,
    int blockX,
    int blockY,
    int blockZ,
    String blockId,
    String toolId,
    long playerId
) {}
