package uk.co.duelmonster.minersadvantage.common.config.categories;

import uk.co.duelmonster.minersadvantage.common.config.PathanationConfig;

public final class MAConfig_Pathanation extends MAConfig_BaseCategory {
    private int pathWidth;
    private int pathLength;

    public MAConfig_Pathanation(PathanationConfig config) {
        this.enabled = config.enabled();
        this.pathWidth = config.pathWidth();
        this.pathLength = config.pathLength();
    }

    public int pathWidth() { return pathWidth; }
    public int pathLength() { return pathLength; }
}
