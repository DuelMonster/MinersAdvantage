package uk.co.duelmonster.minersadvantage.common.event;

import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;
import uk.co.duelmonster.minersadvantage.common.orchestration.FeatureDispatchBus;
import uk.co.duelmonster.minersadvantage.common.orchestration.FeatureDispatchContext;

public final class FeatureEventHandler {
    @FunctionalInterface
    interface DispatchObserver {
        void onDispatch(FeatureDispatchContext context);
    }

    private static DispatchObserver dispatchObserver = context -> { };

    public static void onToolUse(FeatureId feature, int blockX, int blockY, int blockZ, String blockId, String toolId) {
        FeatureDispatchBus.setContext(new FeatureDispatchContext(feature, blockX, blockY, blockZ, blockId, toolId, 0L));
        try {
            handleFeatureDispatch(feature);
        } finally {
            FeatureDispatchBus.clearContext();
        }
    }

    private static void handleFeatureDispatch(FeatureId feature) {
        FeatureDispatchContext context = FeatureDispatchBus.getContext();
        if (context != null && context.feature() == feature) {
            dispatchObserver.onDispatch(context);
        }
    }

    static void setDispatchObserverForTesting(DispatchObserver observer) {
        dispatchObserver = observer == null ? context -> { } : observer;
    }

    static void resetDispatchObserverForTesting() {
        dispatchObserver = context -> { };
    }
}
