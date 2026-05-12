package uk.co.duelmonster.minersadvantage.common.event.client;

import uk.co.duelmonster.minersadvantage.common.Variables;

/**
 * Legacy compatibility facade for key input state updates.
 */
public final class KeyInputEvents {
    /**
     * onExcavationToggleChanged exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public void onExcavationToggleChanged(boolean excavationToggled, boolean singleLayerToggled) {
        Variables vars = Variables.get();
        vars.IsExcavationToggled = excavationToggled;
        vars.IsSingleLayerToggled = singleLayerToggled;
        Variables.syncToServer();
    }

    /**
     * onShaftanationToggleChanged exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public void onShaftanationToggleChanged(boolean shaftanationToggled) {
        Variables vars = Variables.get();
        vars.IsShaftanationToggled = shaftanationToggled;
        Variables.syncToServer();
    }
}


