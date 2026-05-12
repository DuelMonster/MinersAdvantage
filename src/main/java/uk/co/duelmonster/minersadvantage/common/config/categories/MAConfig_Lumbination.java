package uk.co.duelmonster.minersadvantage.common.config.categories;

import java.util.List;
import uk.co.duelmonster.minersadvantage.common.config.LumbinationConfig;

/**
 * MAConfig_Lumbination is the teammate that keeps this part of the mod understandable and stable.
 * It exists so behavior stays explicit instead of becoming mystery spaghetti at 2 AM.
 */
public final class MAConfig_Lumbination extends MAConfig_BaseCategory {
    private int trunkRange;
    private int leafRange;
    private boolean chopTreeBelow;
    private boolean destroyLeaves;
    private boolean leavesAffectDurability;
    private boolean replantSaplings;
    private boolean useShearsOnLeaves;
    private List<String> logs;
    private List<String> leaves;
    private List<String> axes;

    /**
     * MAConfig_Lumbination exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public MAConfig_Lumbination(LumbinationConfig config) {
        this.enabled = config.enabled();
        this.trunkRange = config.trunkRange();
        this.leafRange = config.leafRange();
        this.chopTreeBelow = config.chopTreeBelow();
        this.destroyLeaves = config.destroyLeaves();
        this.leavesAffectDurability = config.leavesAffectDurability();
        this.replantSaplings = config.replantSaplings();
        this.useShearsOnLeaves = config.useShearsOnLeaves();
        this.logs = config.logs();
        this.leaves = config.leaves();
        this.axes = config.axes();
    }

    public int trunkRange() { return trunkRange; }
    public int leafRange() { return leafRange; }
    public boolean chopTreeBelow() { return chopTreeBelow; }
    public boolean destroyLeaves() { return destroyLeaves; }
    public boolean leavesAffectDurability() { return leavesAffectDurability; }
    public boolean replantSaplings() { return replantSaplings; }
    public boolean useShearsOnLeaves() { return useShearsOnLeaves; }
    public List<String> logs() { return logs; }
    public List<String> leaves() { return leaves; }
    public List<String> axes() { return axes; }
}
