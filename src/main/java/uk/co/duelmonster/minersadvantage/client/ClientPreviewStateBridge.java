package uk.co.duelmonster.minersadvantage.client;

import uk.co.duelmonster.minersadvantage.common.services.input.ClientInputService;

/**
 * Bridges loader-specific client input state reads for shared render hooks.
 */
public final class ClientPreviewStateBridge {
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
