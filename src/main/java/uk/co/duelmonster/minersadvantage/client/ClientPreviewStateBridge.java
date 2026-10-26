package uk.co.duelmonster.minersadvantage.client;

import uk.co.duelmonster.minersadvantage.common.config.MAServerRootConfig;
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
        return uk.co.duelmonster.minersadvantage.platform.neoforge.client.NeoForgeClientEvents.getInputState();
        */ //?}
    }

    public static void applyFeatureConfigChanges(
            MAServerRootConfig previousConfig,
            MAServerRootConfig updatedConfig) {
        //? if fabric {
        ClientInputHandler.applyFeatureConfigChanges(previousConfig, updatedConfig);
        //?} else {
        /*
        uk.co.duelmonster.minersadvantage.platform.neoforge.client.NeoForgeClientEvents
                .applyFeatureConfigChanges(previousConfig, updatedConfig);
        */ //?}
    }
}
