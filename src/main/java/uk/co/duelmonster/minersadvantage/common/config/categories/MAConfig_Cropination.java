package uk.co.duelmonster.minersadvantage.common.config.categories;

import uk.co.duelmonster.minersadvantage.common.config.CropinationConfig;

/**
 * MAConfig_Cropination is the teammate that keeps this part of the mod understandable and stable.
 * It exists so behavior stays explicit instead of becoming mystery spaghetti at 2 AM.
 */
public final class MAConfig_Cropination extends MAConfig_BaseCategory {
    private boolean harvestSeeds;

    /**
     * MAConfig_Cropination exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public MAConfig_Cropination(CropinationConfig config) {
        this.enabled = config.enabled();
        this.harvestSeeds = config.harvestSeeds();
    }

    public boolean harvestSeeds() { return harvestSeeds; }
}


