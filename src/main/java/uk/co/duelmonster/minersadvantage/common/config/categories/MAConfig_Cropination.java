package uk.co.duelmonster.minersadvantage.common.config.categories;

import uk.co.duelmonster.minersadvantage.common.config.CropinationConfig;

public final class MAConfig_Cropination extends MAConfig_BaseCategory {
    private boolean harvestSeeds;

    public MAConfig_Cropination(CropinationConfig config) {
        this.enabled = config.enabled();
        this.harvestSeeds = config.harvestSeeds();
    }

    public boolean harvestSeeds() { return harvestSeeds; }
}
