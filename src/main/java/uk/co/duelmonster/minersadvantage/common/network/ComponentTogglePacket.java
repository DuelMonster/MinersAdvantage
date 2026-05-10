package uk.co.duelmonster.minersadvantage.common.network;

import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;

public record ComponentTogglePacket(
    FeatureId feature,
    boolean enabled
) {}
