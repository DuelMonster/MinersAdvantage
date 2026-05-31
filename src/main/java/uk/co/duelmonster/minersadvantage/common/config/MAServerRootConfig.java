package uk.co.duelmonster.minersadvantage.common.config;

/**
 * Explicit server-authoritative gameplay configuration root.
 */
public record MAServerRootConfig(
    CommonConfig common,
    CaptivationConfig captivation,
    CropinationConfig cropination,
    CultivationConfig cultivation,
    ExcavationConfig excavation,
    PathanationConfig pathanation,
    IlluminationConfig illumination,
    LumbinationConfig lumbination,
    ShaftanationConfig shaftanation,
    SubstitutionConfig substitution,
    VeinationConfig veination,
    VentilationConfig ventilation
) {
    public MAServerRootConfig {
        SyncedClientConfig defaults = SyncedClientConfig.defaults();
        common = common == null ? defaults.common() : common;
        captivation = captivation == null ? defaults.captivation() : captivation;
        cropination = cropination == null ? defaults.cropination() : cropination;
        cultivation = cultivation == null ? defaults.cultivation() : cultivation;
        excavation = excavation == null ? defaults.excavation() : excavation;
        pathanation = pathanation == null ? defaults.pathanation() : pathanation;
        illumination = illumination == null ? defaults.illumination() : illumination;
        lumbination = lumbination == null ? defaults.lumbination() : lumbination;
        shaftanation = shaftanation == null ? defaults.shaftanation() : shaftanation;
        substitution = substitution == null ? defaults.substitution() : substitution;
        veination = veination == null ? defaults.veination() : veination;
        ventilation = ventilation == null ? defaults.ventilation() : ventilation;
    }

    /**
     * d ef au lt s exists so this path stays predictable and easier to debug when things get weird.
     */
    public static MAServerRootConfig defaults() {
        return fromSyncedConfig(SyncedClientConfig.defaults());
    }

    /**
     * f ro ms yn ce dc on fi g exists so this path stays predictable and easier to debug when things get weird.
     */
    public static MAServerRootConfig fromSyncedConfig(SyncedClientConfig synced) {
        SyncedClientConfig value = synced == null ? SyncedClientConfig.defaults() : synced;
        return new MAServerRootConfig(
            value.common(),
            value.captivation(),
            value.cropination(),
            value.cultivation(),
            value.excavation(),
            value.pathanation(),
            value.illumination(),
            value.lumbination(),
            value.shaftanation(),
            value.substitution(),
            value.veination(),
            value.ventilation()
        );
    }

    /**
     * t os yn ce dc on fi g exists so this path stays predictable and easier to debug when things get weird.
     */
    public SyncedClientConfig toSyncedConfig(ClientConfig clientConfig) {
        ClientConfig clientValue = clientConfig == null ? SyncedClientConfig.defaults().client() : clientConfig;
        return new SyncedClientConfig(
            clientValue,
            common,
            captivation,
            cropination,
            cultivation,
            excavation,
            pathanation,
            illumination,
            lumbination,
            shaftanation,
            substitution,
            veination,
            ventilation
        );
    }
}
