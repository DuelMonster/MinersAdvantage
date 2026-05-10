package uk.co.duelmonster.minersadvantage.common.config;

public record IlluminationConfig(
    boolean enabled,
    int radiusHorizontal,
    int radiusVertical,
    int lowestLightLevel,
    boolean useBlockLight
) {
    public IlluminationConfig(boolean enabled, int radiusHorizontal, int radiusVertical) {
        this(enabled, radiusHorizontal, radiusVertical, 7, true);
    }
}
