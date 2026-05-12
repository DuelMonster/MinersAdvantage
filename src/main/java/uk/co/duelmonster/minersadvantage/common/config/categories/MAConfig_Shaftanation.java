package uk.co.duelmonster.minersadvantage.common.config.categories;

import uk.co.duelmonster.minersadvantage.common.config.ShaftanationConfig;
import uk.co.duelmonster.minersadvantage.common.services.utility.TorchPlacement;

public final class MAConfig_Shaftanation extends MAConfig_BaseCategory {
    private int shaftLength;
    private int shaftHeight;
    private int shaftWidth;
    private TorchPlacement torchPlacement;

    public MAConfig_Shaftanation(ShaftanationConfig config) {
        this.enabled = config.enabled();
        this.shaftLength = config.shaftLength();
        this.shaftHeight = config.shaftHeight();
        this.shaftWidth = config.shaftWidth();
        this.torchPlacement = config.torchPlacement();
    }

    public int shaftLength() { return shaftLength; }
    public int shaftHeight() { return shaftHeight; }
    public int shaftWidth() { return shaftWidth; }
    public TorchPlacement torchPlacement() { return torchPlacement; }
}
