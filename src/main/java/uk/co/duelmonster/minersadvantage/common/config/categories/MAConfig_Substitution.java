package uk.co.duelmonster.minersadvantage.common.config.categories;

import java.util.List;
import uk.co.duelmonster.minersadvantage.common.config.SubstitutionConfig;

public final class MAConfig_Substitution extends MAConfig_BaseCategory {
    private boolean switchBack;
    private boolean favourSilkTouch;
    private boolean favourFortune;
    private boolean ignoreIfValidTool;
    private boolean ignorePassiveMobs;
    private List<String> blacklist;

    public MAConfig_Substitution(SubstitutionConfig config) {
        this.enabled = config.enabled();
        this.switchBack = config.switchBack();
        this.favourSilkTouch = config.favourSilkTouch();
        this.favourFortune = config.favourFortune();
        this.ignoreIfValidTool = config.ignoreIfValidTool();
        this.ignorePassiveMobs = config.ignorePassiveMobs();
        this.blacklist = config.blacklist();
    }

    public boolean switchBack() { return switchBack; }
    public boolean favourSilkTouch() { return favourSilkTouch; }
    public boolean favourFortune() { return favourFortune; }
    public boolean ignoreIfValidTool() { return ignoreIfValidTool; }
    public boolean ignorePassiveMobs() { return ignorePassiveMobs; }
    public List<String> blacklist() { return blacklist; }
}
