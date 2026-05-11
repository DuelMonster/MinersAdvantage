//? if fabric {
package uk.co.duelmonster.minersadvantage.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

/**
 * Fabric client-side initialization entry point.
 * 
 * This class is invoked by Fabric on the client side only (as declared in fabric.mod.json).
 * It provides a hook for client-only systems:
 * - Keybinding registration (M3)
 * - Client tick listener setup (M3)
 * - Config screen factory registration (M6)
 * 
 * Server-side initialization happens in ModEntry (the main Fabric ModInitializer).
 */
public final class FabricClientEntrypoint implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // M3: Register keybindings and wire client tick input loop
        ClientInputHandler.registerKeybindings();
        ClientTickEvents.END_CLIENT_TICK.register(minecraft -> ClientInputHandler.tick());
    }
}
//?} else {
/*
// This class is Fabric-only (client-side initialization).
*/ //?}
