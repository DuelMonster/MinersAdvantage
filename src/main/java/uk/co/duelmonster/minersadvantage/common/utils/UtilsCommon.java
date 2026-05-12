package uk.co.duelmonster.minersadvantage.common.utils;

public final class UtilsCommon {
    private UtilsCommon() {}

    public static <T> void checkNotNull(T object, String name) {
        if (object == null) {
            throw new NullPointerException(name + " must not be null.");
        }
    }
}
