package uk.co.duelmonster.minersadvantage.common.config.categories;

import uk.co.duelmonster.minersadvantage.common.config.VentilationConfig;

/**
 * MAConfig_Ventilation is the teammate that keeps this part of the mod understandable and stable.
 * It exists so behavior stays explicit instead of becoming mystery spaghetti at 2 AM.
 */
public final class MAConfig_Ventilation extends MAConfig_BaseCategory {
    private int ventDiameter;
    private int ventDepth;
    private boolean placeLadders;

    /**
     * MAConfig_Ventilation exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public MAConfig_Ventilation(VentilationConfig config) {
        this.enabled = config.enabled();
        this.ventDiameter = config.radiusHorizontal();
        this.ventDepth = config.radiusVertical();
        this.placeLadders = config.placeLadders();
    }

    public int ventDiameter() { return ventDiameter; }
    public int ventDepth() { return ventDepth; }
    public boolean placeLadders() { return placeLadders; }
}
