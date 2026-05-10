package uk.co.duelmonster.minersadvantage.common.event;

import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;

public final class FeatureEventHandler {
    public static void onToolUse(FeatureId feature, int blockX, int blockY, int blockZ, String blockId, String toolId) {
        // Dispatch to orchestration bus
        uk.co.duelmonster.minersadvantage.common.orchestration.FeatureDispatchBus.setContext(
            new uk.co.duelmonster.minersadvantage.common.orchestration.FeatureDispatchContext(
                feature, blockX, blockY, blockZ, blockId, toolId, 0L
            )
        );
        try {
            handleFeatureDispatch(feature);
        } finally {
            uk.co.duelmonster.minersadvantage.common.orchestration.FeatureDispatchBus.clearContext();
        }
    }

    private static void handleFeatureDispatch(FeatureId feature) {
        // Feature dispatch will be called from loader event handlers
        // This is a hook point for feature-specific logic
    }
}
