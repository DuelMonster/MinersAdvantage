package uk.co.duelmonster.minersadvantage.common.config;

public record ServerOverridesConfig(
    boolean overrideFeatureEnablement,
    boolean enforceCommonSettings,
    boolean enforceCaptivationSettings,
    boolean enforceCropinationSettings,
    boolean enforceCultivationSettings,
    boolean enforceExcavationSettings,
    boolean enforcePathanationSettings,
    boolean enforceIlluminationSettings,
    boolean enforceLumbinationSettings,
    boolean enforceShaftanationSettings,
    boolean enforceSubstitutionSettings,
    boolean enforceVeinationSettings,
    boolean enforceVentilationSettings
) {
    public ServerOverridesConfig() {
        this(false, false, false, false, false, false, false, false, false, false, false, false, false);
    }
}
