package uk.co.duelmonster.minersadvantage.common.network;

import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;

public record FeatureDispatchPacket(
    FeatureId feature,
    int blockX,
    int blockY,
    int blockZ,
    String blockId,
    String toolId
) {}
