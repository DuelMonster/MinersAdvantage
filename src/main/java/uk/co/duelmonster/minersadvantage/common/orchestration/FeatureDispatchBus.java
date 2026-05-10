package uk.co.duelmonster.minersadvantage.common.orchestration;

public final class FeatureDispatchBus {
    private static final ThreadLocal<FeatureDispatchContext> CURRENT_CONTEXT = new ThreadLocal<>();

    public static void setContext(FeatureDispatchContext context) {
        CURRENT_CONTEXT.set(context);
    }

    public static FeatureDispatchContext getContext() {
        return CURRENT_CONTEXT.get();
    }

    public static void clearContext() {
        CURRENT_CONTEXT.remove();
    }

    public static boolean hasContext() {
        return CURRENT_CONTEXT.get() != null;
    }
}
