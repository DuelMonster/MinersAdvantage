package uk.co.duelmonster.minersadvantage.common.component;

import uk.co.duelmonster.minersadvantage.common.orchestration.FeatureDispatchBus;
import uk.co.duelmonster.minersadvantage.common.orchestration.FeatureDispatchContext;

/**
 * Helper for component tick execution with dispatch bus integration.
 * Reduces duplication across all 11 feature components.
 */
public final class ComponentTickHelper {
    private ComponentTickHelper() {
    }

    /**
     * Check if component should execute during tick.
     * Components are enabled only when feature is enabled AND dispatch context is active.
     *
     * @param isEnabled whether the component is currently enabled
     * @return true if tick should proceed
     */
    public static boolean shouldExecute(boolean isEnabled) {
        return isEnabled && FeatureDispatchBus.hasContext();
    }

    /**
     * Get the active dispatch context for the current tick.
     *
     * @return the current feature dispatch context
     */
    public static FeatureDispatchContext getContext() {
        return FeatureDispatchBus.getContext();
    }
}
