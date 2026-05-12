package uk.co.duelmonster.minersadvantage.client;

import uk.co.duelmonster.minersadvantage.common.config.MAConfig;

/**
 * Legacy compatibility holder for particle manager interception state.
 */
public final class MAParticleManager {
    private static Object maParticleManager;
    private static Object originalParticleManager;

    private MAParticleManager() {}

    /**
     * get exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static Object get() {
        return maParticleManager;
    }

    /**
     * set exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static Object set(Object value) {
        maParticleManager = value;
        return value;
    }

    /**
     * getOriginal exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static Object getOriginal() {
        return originalParticleManager;
    }

    /**
     * setOriginal exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static void setOriginal(Object value) {
        originalParticleManager = value;
    }

    /**
     * shouldAddTerrainParticles exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static boolean shouldAddTerrainParticles() {
        return !MAConfig.CLIENT.disableParticleEffects();
    }
}
