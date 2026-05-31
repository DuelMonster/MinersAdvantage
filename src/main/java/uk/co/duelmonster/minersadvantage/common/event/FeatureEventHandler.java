package uk.co.duelmonster.minersadvantage.common.event;

import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;
import uk.co.duelmonster.minersadvantage.common.orchestration.FeatureDispatchBus;
import uk.co.duelmonster.minersadvantage.common.orchestration.FeatureDispatchContext;

/**
 * FeatureEventHandler keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class FeatureEventHandler {
    @FunctionalInterface
    /**
     * DispatchObserver keeps this part of Miners Advantage running without turning server ticks into confetti.
     * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
     */
    interface DispatchObserver {
        void onDispatch(FeatureDispatchContext context);
    }

    private static DispatchObserver dispatchObserver = context -> { };

    /**
     * onToolUse exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public static void onToolUse(FeatureId feature, int blockX, int blockY, int blockZ, String blockId, String toolId) {
        FeatureDispatchBus.setContext(new FeatureDispatchContext(feature, blockX, blockY, blockZ, blockId, toolId, 0L));
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        try {
            handleFeatureDispatch(feature);
        } finally {
            FeatureDispatchBus.clearContext();
        }
    }

    /**
     * handleFeatureDispatch exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private static void handleFeatureDispatch(FeatureId feature) {
        FeatureDispatchContext context = FeatureDispatchBus.getContext();
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (context != null && context.feature() == feature) {
            dispatchObserver.onDispatch(context);
        }
    }

    /**
     * setDispatchObserverForTesting exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    static void setDispatchObserverForTesting(DispatchObserver observer) {
        dispatchObserver = observer == null ? context -> { } : observer;
    }

    /**
     * resetDispatchObserverForTesting exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    static void resetDispatchObserverForTesting() {
        dispatchObserver = context -> { };
    }
}
