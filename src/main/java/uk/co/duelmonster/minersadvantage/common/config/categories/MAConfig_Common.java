package uk.co.duelmonster.minersadvantage.common.config.categories;

import uk.co.duelmonster.minersadvantage.common.config.CommonConfig;

/**
 * MAConfig_Common is the teammate that keeps this part of the mod understandable and stable.
 * It exists so behavior stays explicit instead of becoming mystery spaghetti at 2 AM.
 */
public final class MAConfig_Common extends MAConfig_BaseCategory {
    private boolean tpsGuard;
    private boolean gatherDrops;
    private boolean autoIlluminate;
    private boolean mineVeins;
    private int blocksPerTick;

    /**
     * MAConfig_Common exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public MAConfig_Common(CommonConfig config) {
        this.enabled = true;
        this.tpsGuard = config.tpsGuard();
        this.gatherDrops = config.gatherDrops();
        this.autoIlluminate = config.autoIlluminate();
        this.mineVeins = config.mineVeins();
        this.blocksPerTick = config.blocksPerTick();
    }

    public boolean tpsGuard() { return tpsGuard; }
    public boolean gatherDrops() { return gatherDrops; }
    public boolean autoIlluminate() { return autoIlluminate; }
    public boolean mineVeins() { return mineVeins; }
    public int blocksPerTick() { return blocksPerTick; }
}


