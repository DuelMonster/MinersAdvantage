package uk.co.duelmonster.minersadvantage.common.event.client;

import uk.co.duelmonster.minersadvantage.common.Variables;
import uk.co.duelmonster.minersadvantage.common.services.utility.SupremeVantageService;

/**
 * Legacy compatibility facade for client tick and pickup hooks.
 */
public final class ClientEventHandler {
    private final SupremeVantageService supremeVantageService;

    public ClientEventHandler(SupremeVantageService supremeVantageService) {
        this.supremeVantageService = supremeVantageService;
    }

    public void onClientTick(SupremeVantageService.ClientState state, boolean excavationToggled, boolean allFeaturesEnabled) {
        Variables vars = Variables.get();
        vars.IsExcavationToggled = excavationToggled;
        supremeVantageService.processClientTick(state, java.util.Set.of(), excavationToggled, allFeaturesEnabled);
        Variables.syncToServer();
    }

    public boolean shouldCancelItemPickup(String itemId, boolean captivationEnabled, boolean unconditionalBlacklist, java.util.List<String> blacklist) {
        if (!captivationEnabled || !unconditionalBlacklist || blacklist == null || blacklist.isEmpty()) {
            return false;
        }
        return blacklist.contains(itemId);
    }
}
