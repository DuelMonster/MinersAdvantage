package uk.co.duelmonster.minersadvantage.common.event.client;

import uk.co.duelmonster.minersadvantage.common.Variables;

/**
 * Legacy compatibility facade for key input state updates.
 */
public final class KeyInputEvents {
    public void onExcavationToggleChanged(boolean excavationToggled, boolean singleLayerToggled) {
        Variables vars = Variables.get();
        vars.IsExcavationToggled = excavationToggled;
        vars.IsSingleLayerToggled = singleLayerToggled;
        Variables.syncToServer();
    }

    public void onShaftanationToggleChanged(boolean shaftanationToggled) {
        Variables vars = Variables.get();
        vars.IsShaftanationToggled = shaftanationToggled;
        Variables.syncToServer();
    }
}
