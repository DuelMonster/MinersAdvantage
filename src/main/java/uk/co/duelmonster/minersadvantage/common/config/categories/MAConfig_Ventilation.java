package uk.co.duelmonster.minersadvantage.common.config.categories;

import uk.co.duelmonster.minersadvantage.common.config.VentilationConfig;

public final class MAConfig_Ventilation extends MAConfig_BaseCategory {
    private int ventDiameter;
    private int ventDepth;
    private boolean placeLadders;

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
