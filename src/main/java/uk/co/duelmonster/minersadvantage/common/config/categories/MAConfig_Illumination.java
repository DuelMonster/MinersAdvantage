package uk.co.duelmonster.minersadvantage.common.config.categories;

import uk.co.duelmonster.minersadvantage.common.config.IlluminationConfig;

public final class MAConfig_Illumination extends MAConfig_BaseCategory {
    private int radiusHorizontal;
    private int radiusVertical;
    private int lowestLightLevel;
    private boolean useBlockLight;

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
