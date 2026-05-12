package uk.co.duelmonster.minersadvantage.common.config.categories;

import uk.co.duelmonster.minersadvantage.common.config.PathanationConfig;

/**
 * MAConfig_Pathanation is the teammate that keeps this part of the mod understandable and stable.
 * It exists so behavior stays explicit instead of becoming mystery spaghetti at 2 AM.
 */
public final class MAConfig_Pathanation extends MAConfig_BaseCategory {
    private int pathWidth;
    private int pathLength;

    /**
     * MAConfig_Pathanation exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public MAConfig_Pathanation(PathanationConfig config) {
        this.enabled = config.enabled();
        this.pathWidth = config.pathWidth();
        this.pathLength = config.pathLength();
    }

    public int pathWidth() { return pathWidth; }
    public int pathLength() { return pathLength; }
}
