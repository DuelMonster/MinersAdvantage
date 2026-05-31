package uk.co.duelmonster.minersadvantage.client;

/**
 * Bridges loader-specific mouse scroll shape cycling.
 */
public final class ClientMouseScrollBridge {
    /**
     * c li en tm ou se sc ro ll br id ge exists so this path stays predictable and easier to debug when things get weird.
     */
    private ClientMouseScrollBridge() {
    }

    /**
     * o ns cr ol l exists so this path stays predictable and easier to debug when things get weird.
     */
    public static boolean onScroll(double scrollY) {
        //? if fabric {
        return ClientInputHandler.onMouseScroll(scrollY);
        //?} else {
        /*
        return NeoForgeClientEvents.onMouseScroll(scrollY);
        */ //?}
    }
}
