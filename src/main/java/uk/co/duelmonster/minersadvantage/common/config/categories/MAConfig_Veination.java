package uk.co.duelmonster.minersadvantage.common.config.categories;

import java.util.List;
import uk.co.duelmonster.minersadvantage.common.config.VeinationConfig;

/**
 * MAConfig_Veination is the teammate that keeps this part of the mod understandable and stable.
 * It exists so behavior stays explicit instead of becoming mystery spaghetti at 2 AM.
 */
public final class MAConfig_Veination extends MAConfig_BaseCategory {
    private int maxVeinDistance;
    private List<String> ores;
    private boolean oreHarvestWithoutSneak;
    private boolean dropOresAtFirstBrokenBlock;
    private boolean increaseHarvestingTimePerOre;
    private double increasedHarvestingTimePerOreModifier;
    private List<String> pickaxeBlacklist;

    /**
     * MAConfig_Veination exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public MAConfig_Veination(VeinationConfig config) {
        this.enabled = config.enabled();
        this.maxVeinDistance = config.maxVeinDistance();
        this.ores = config.ores();
        this.oreHarvestWithoutSneak = config.oreHarvestWithoutSneak();
        this.dropOresAtFirstBrokenBlock = config.dropOresAtFirstBrokenBlock();
        this.increaseHarvestingTimePerOre = config.increaseHarvestingTimePerOre();
        this.increasedHarvestingTimePerOreModifier = config.increasedHarvestingTimePerOreModifier();
        this.pickaxeBlacklist = config.pickaxeBlacklist();
    }

    public int maxVeinDistance() { return maxVeinDistance; }
    public List<String> ores() { return ores; }
    public boolean oreHarvestWithoutSneak() { return oreHarvestWithoutSneak; }
    public boolean dropOresAtFirstBrokenBlock() { return dropOresAtFirstBrokenBlock; }
    public boolean increaseHarvestingTimePerOre() { return increaseHarvestingTimePerOre; }
    public double increasedHarvestingTimePerOreModifier() { return increasedHarvestingTimePerOreModifier; }
    public List<String> pickaxeBlacklist() { return pickaxeBlacklist; }
}
