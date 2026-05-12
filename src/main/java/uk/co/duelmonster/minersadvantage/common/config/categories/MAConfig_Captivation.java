package uk.co.duelmonster.minersadvantage.common.config.categories;

import java.util.List;
import uk.co.duelmonster.minersadvantage.common.config.CaptivationConfig;

public final class MAConfig_Captivation extends MAConfig_BaseCategory {
    private boolean allowInGUI;
    private double radiusHorizontal;
    private double radiusVertical;
    private boolean isWhitelist;
    private boolean unconditionalBlacklist;
    private List<String> blacklist;

    public MAConfig_Captivation(CaptivationConfig config) {
        this.enabled = config.enabled();
        this.allowInGUI = config.allowInGUI();
        this.radiusHorizontal = config.radiusHorizontal();
        this.radiusVertical = config.radiusVertical();
        this.isWhitelist = config.isWhitelist();
        this.unconditionalBlacklist = config.unconditionalBlacklist();
        this.blacklist = config.blacklist();
    }

    public boolean allowInGUI() { return allowInGUI; }
    public double radiusHorizontal() { return radiusHorizontal; }
    public double radiusVertical() { return radiusVertical; }
    public boolean isWhitelist() { return isWhitelist; }
    public boolean unconditionalBlacklist() { return unconditionalBlacklist; }
    public List<String> blacklist() { return blacklist; }
}
