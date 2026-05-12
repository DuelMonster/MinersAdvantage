package uk.co.duelmonster.minersadvantage.common.config.categories;

import java.util.List;
import uk.co.duelmonster.minersadvantage.common.config.ExcavationConfig;

/**
 * MAConfig_Excavation is the teammate that keeps this part of the mod understandable and stable.
 * It exists so behavior stays explicit instead of becoming mystery spaghetti at 2 AM.
 */
public final class MAConfig_Excavation extends MAConfig_BaseCategory {
    private boolean toggleMode;
    private boolean ignoreBlockVariants;
    private boolean isBlockWhitelist;
    private List<String> blockBlacklist;

    /**
     * MAConfig_Excavation exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
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
