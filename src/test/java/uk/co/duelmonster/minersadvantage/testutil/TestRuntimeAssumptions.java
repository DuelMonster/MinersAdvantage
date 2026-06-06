package uk.co.duelmonster.minersadvantage.testutil;

/**
 * Runtime assumptions shared by tests that touch Minecraft bootstrap classes.
 */
public final class TestRuntimeAssumptions {
    private TestRuntimeAssumptions() {
    }

    public static boolean canInitializeSoundEvents() {
        try {
            Class.forName("net.minecraft.sounds.SoundEvents");
            return true;
        } catch (ClassNotFoundException | ExceptionInInitializerError | LinkageError exception) {
            return false;
        }
    }
}
