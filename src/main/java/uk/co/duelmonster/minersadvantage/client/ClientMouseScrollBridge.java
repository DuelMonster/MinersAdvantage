package uk.co.duelmonster.minersadvantage.client;

/**
 * Bridges loader-specific mouse scroll shape cycling.
 */
public final class ClientMouseScrollBridge {
    private ClientMouseScrollBridge() {
    }

    public static boolean onScroll(double scrollY) {
        //? if fabric {
        return ClientInputHandler.onMouseScroll(scrollY);
        //?} else {
        /*
        return NeoForgeClientEvents.onMouseScroll(scrollY);
        */ //?}
    }
}
