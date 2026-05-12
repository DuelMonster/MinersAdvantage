package uk.co.duelmonster.minersadvantage.common.utils;

/**
 * UtilsCommon is the teammate that keeps this part of the mod understandable and stable.
 * It exists so behavior stays explicit instead of becoming mystery spaghetti at 2 AM.
 */
public final class UtilsCommon {
    private UtilsCommon() {}

    /**
     * checkNotNull exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static <T> void checkNotNull(T object, String name) {
        if (object == null) {
            throw new NullPointerException(name + " must not be null.");
        }
    }
}


