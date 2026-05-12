package uk.co.duelmonster.minersadvantage.common.event.client;

import uk.co.duelmonster.minersadvantage.common.Variables;
import uk.co.duelmonster.minersadvantage.common.services.utility.SupremeVantageService;

/**
 * Legacy compatibility facade for client tick and pickup hooks.
 */
public final class ClientEventHandler {
    private final SupremeVantageService supremeVantageService;

    /**
     * ClientEventHandler exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public ClientEventHandler(SupremeVantageService supremeVantageService) {
        this.supremeVantageService = supremeVantageService;
    }

    /**
     * onClientTick exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public void onClientTick(SupremeVantageService.ClientState state, boolean excavationToggled, boolean allFeaturesEnabled) {
        Variables vars = Variables.get();
        vars.IsExcavationToggled = excavationToggled;
        supremeVantageService.processClientTick(state, java.util.Set.of(), excavationToggled, allFeaturesEnabled);
        Variables.syncToServer();
    }

    /**
     * shouldCancelItemPickup exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public boolean shouldCancelItemPickup(String itemId, boolean captivationEnabled, boolean unconditionalBlacklist, java.util.List<String> blacklist) {
        if (!captivationEnabled || !unconditionalBlacklist || blacklist == null || blacklist.isEmpty()) {
            return false;
        }
        return blacklist.contains(itemId);
    }
}


