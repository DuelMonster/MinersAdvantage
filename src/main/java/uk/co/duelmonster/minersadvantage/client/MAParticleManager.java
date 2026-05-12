package uk.co.duelmonster.minersadvantage.client;

import uk.co.duelmonster.minersadvantage.common.config.MAConfig;

/**
 * Legacy compatibility holder for particle manager interception state.
 */
public final class MAParticleManager {
    private static Object maParticleManager;
    private static Object originalParticleManager;

    private MAParticleManager() {}

    public static Object get() {
        return maParticleManager;
    }

    public static Object set(Object value) {
        maParticleManager = value;
        return value;
    }

    public static Object getOriginal() {
        return originalParticleManager;
    }

    public static void setOriginal(Object value) {
        originalParticleManager = value;
    }

    public static boolean shouldAddTerrainParticles() {
        return !MAConfig.CLIENT.disableParticleEffects();
    }
}
