package uk.co.duelmonster.minersadvantage.common.config;

/**
 * Legacy compatibility entry point for old MAConfig.CLIENT usage.
 */
public final class MAConfig {
    public static final ClientFacade CLIENT = new ClientFacade();

    private MAConfig() {}

    public static final class ClientFacade {
        public boolean disableParticleEffects() {
            return MAConfig_Base.getGlobalConfig().client().disableParticleEffects();
        }

        public boolean allEnabled() {
            SyncedClientConfig config = MAConfig_Base.getGlobalConfig();
            return config.captivation().enabled()
                && config.cropination().enabled()
                && config.cultivation().enabled()
                && config.excavation().enabled()
                && config.illumination().enabled()
                && config.lumbination().enabled()
                && config.pathanation().enabled()
                && config.shaftanation().enabled()
                && config.substitution().enabled()
                && config.veination().enabled()
                && config.ventilation().enabled();
        }
    }
}
