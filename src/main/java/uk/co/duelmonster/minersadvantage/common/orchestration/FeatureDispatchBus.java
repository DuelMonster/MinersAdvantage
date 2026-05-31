package uk.co.duelmonster.minersadvantage.common.orchestration;

/**
 * FeatureDispatchBus keeps this part of MinersAdvantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class FeatureDispatchBus {
    private static final ThreadLocal<FeatureDispatchContext> CURRENT_CONTEXT = new ThreadLocal<>();

    /**
     * setContext exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public static void setContext(FeatureDispatchContext context) {
        CURRENT_CONTEXT.set(context);
    }

    /**
     * getContext exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public static FeatureDispatchContext getContext() {
        return CURRENT_CONTEXT.get();
    }

    /**
     * clearContext exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public static void clearContext() {
        CURRENT_CONTEXT.remove();
    }

    /**
     * hasContext exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public static boolean hasContext() {
        return CURRENT_CONTEXT.get() != null;
    }
}
