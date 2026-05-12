package uk.co.duelmonster.minersadvantage.common.config.categories;

import uk.co.duelmonster.minersadvantage.common.config.IlluminationConfig;

/**
 * MAConfig_Illumination is the teammate that keeps this part of the mod understandable and stable.
 * It exists so behavior stays explicit instead of becoming mystery spaghetti at 2 AM.
 */
public final class MAConfig_Illumination extends MAConfig_BaseCategory {
    private int radiusHorizontal;
    private int radiusVertical;
    private int lowestLightLevel;
    private boolean useBlockLight;

    /**
     * MAConfig_Illumination exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public MAConfig_Illumination(IlluminationConfig config) {
        this.enabled = config.enabled();
        this.radiusHorizontal = config.radiusHorizontal();
        this.radiusVertical = config.radiusVertical();
        this.lowestLightLevel = config.lowestLightLevel();
        this.useBlockLight = config.useBlockLight();
    }

    public int radiusHorizontal() { return radiusHorizontal; }
    public int radiusVertical() { return radiusVertical; }
    public int lowestLightLevel() { return lowestLightLevel; }
    public boolean useBlockLight() { return useBlockLight; }
}


