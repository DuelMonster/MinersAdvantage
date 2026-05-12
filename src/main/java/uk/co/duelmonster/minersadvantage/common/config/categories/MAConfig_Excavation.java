package uk.co.duelmonster.minersadvantage.common.config.categories;

import java.util.List;
import uk.co.duelmonster.minersadvantage.common.config.ExcavationConfig;

public final class MAConfig_Excavation extends MAConfig_BaseCategory {
    private boolean toggleMode;
    private boolean ignoreBlockVariants;
    private boolean isBlockWhitelist;
    private List<String> blockBlacklist;

    public MAConfig_Excavation(ExcavationConfig config) {
        this.enabled = config.enabled();
        this.toggleMode = config.toggleMode();
        this.ignoreBlockVariants = config.ignoreBlockVariants();
        this.isBlockWhitelist = config.isBlockWhitelist();
        this.blockBlacklist = config.blockBlacklist();
    }

    public boolean toggleMode() { return toggleMode; }
    public boolean ignoreBlockVariants() { return ignoreBlockVariants; }
    public boolean isBlockWhitelist() { return isBlockWhitelist; }
    public List<String> blockBlacklist() { return blockBlacklist; }
}
