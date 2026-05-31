package uk.co.duelmonster.minersadvantage.client;

import uk.co.duelmonster.minersadvantage.common.services.input.ClientInputService;

/**
 * Bridges loader-specific client input state reads for shared render hooks.
 */
public final class ClientPreviewStateBridge {
    /**
     * c li en tp re vi ew st at eb ri dg e exists so this path stays predictable and easier to debug when things get weird.
     */
    private ClientPreviewStateBridge() {
    }

    public static ClientInputService.ClientInputState currentState() {
        //? if fabric {
        return ClientInputHandler.getInputState();
        //?} else {
        /*
        return NeoForgeClientEvents.getInputState();
        */ //?}
    }
}
