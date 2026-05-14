package uk.co.duelmonster.minersadvantage.common.config.categories;

import java.util.List;
import uk.co.duelmonster.minersadvantage.common.config.SubstitutionConfig;
import uk.co.duelmonster.minersadvantage.common.config.SubstitutionConfig.SelectionRule;

/**
 * MAConfig_Substitution is the teammate that keeps this part of the mod understandable and stable.
 * It exists so behavior stays explicit instead of becoming mystery spaghetti at 2 AM.
 */
public final class MAConfig_Substitution extends MAConfig_BaseCategory {
    private boolean switchBack;
    private boolean favourSilkTouch;
    private boolean favourFortune;
    private boolean ignoreIfValidTool;
    private boolean ignorePassiveMobs;
    private List<String> blacklist;
    private List<SelectionRule> selectionRules;

    /**
     * MAConfig_Substitution exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public MAConfig_Substitution(SubstitutionConfig config) {
        this.enabled = config.enabled();
        this.switchBack = config.switchBack();
        this.favourSilkTouch = config.favourSilkTouch();
        this.favourFortune = config.favourFortune();
        this.ignoreIfValidTool = config.ignoreIfValidTool();
        this.ignorePassiveMobs = config.ignorePassiveMobs();
        this.blacklist = config.blacklist();
        this.selectionRules = config.selectionRules();
    }

    public boolean switchBack() { return switchBack; }
    public boolean favourSilkTouch() { return favourSilkTouch; }
    public boolean favourFortune() { return favourFortune; }
    public boolean ignoreIfValidTool() { return ignoreIfValidTool; }
    public boolean ignorePassiveMobs() { return ignorePassiveMobs; }
    public List<String> blacklist() { return blacklist; }
    public List<SelectionRule> selectionRules() { return selectionRules; }
}
