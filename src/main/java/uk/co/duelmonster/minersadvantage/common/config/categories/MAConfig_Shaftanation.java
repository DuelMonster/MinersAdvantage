package uk.co.duelmonster.minersadvantage.common.config.categories;

import uk.co.duelmonster.minersadvantage.common.config.ShaftanationConfig;
import uk.co.duelmonster.minersadvantage.common.services.utility.TorchPlacement;

/**
 * MAConfig_Shaftanation is the teammate that keeps this part of the mod understandable and stable.
 * It exists so behavior stays explicit instead of becoming mystery spaghetti at 2 AM.
 */
public final class MAConfig_Shaftanation extends MAConfig_BaseCategory {
    private int shaftLength;
    private int shaftHeight;
    private int shaftWidth;
    private TorchPlacement torchPlacement;

    /**
     * MAConfig_Shaftanation exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
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
