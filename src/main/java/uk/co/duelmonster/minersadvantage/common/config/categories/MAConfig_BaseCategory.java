package uk.co.duelmonster.minersadvantage.common.config.categories;

/**
 * MAConfig_BaseCategory is the teammate that keeps this part of the mod understandable and stable.
 * It exists so behavior stays explicit instead of becoming mystery spaghetti at 2 AM.
 */
public class MAConfig_BaseCategory {
    protected boolean enabled;

    /**
     * enabled exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public boolean enabled() {
        return enabled;
    }

    /**
     * setEnabled exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}


