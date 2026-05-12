package uk.co.duelmonster.minersadvantage.common.config.categories;

import uk.co.duelmonster.minersadvantage.common.config.CommonConfig;

public final class MAConfig_Common extends MAConfig_BaseCategory {
    private boolean tpsGuard;
    private boolean gatherDrops;
    private boolean autoIlluminate;
    private boolean mineVeins;
    private int blocksPerTick;

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
