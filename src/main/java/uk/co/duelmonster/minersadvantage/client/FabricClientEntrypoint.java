//? if fabric {
package uk.co.duelmonster.minersadvantage.client;

import net.fabricmc.api.ClientModInitializer;

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
        // Client-side initialization will be populated in later milestones (M3, M6):
        // - M3: Keybinding registration and client tick input loop wiring
        // - M6: Config screen factory binding (delegated to ModMenuEntrypoint for ModMenu integration)
    }
}
//? }
