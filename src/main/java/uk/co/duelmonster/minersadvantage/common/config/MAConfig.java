package uk.co.duelmonster.minersadvantage.common.config;

/**
 * Legacy compatibility entry point for old MAConfig.CLIENT usage.
 */
public final class MAConfig {
    public static final ClientFacade CLIENT = new ClientFacade();

    private MAConfig() {}

    /**
     * ClientFacade is the teammate that keeps this part of the mod understandable and stable.
     * It exists so behavior stays explicit instead of becoming mystery spaghetti at 2 AM.
     */
    public static final class ClientFacade {
        /**
         * disableParticleEffects exists to keep this step focused, predictable, and debuggable.
         * In short: one clear job here beats ten confusing side-effects elsewhere.
         */
        public boolean disableParticleEffects() {
            return MAConfig_Base.getGlobalConfig().client().disableParticleEffects();
        }

        /**
         * allEnabled exists to keep this step focused, predictable, and debuggable.
         * In short: one clear job here beats ten confusing side-effects elsewhere.
         */
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


