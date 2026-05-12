package uk.co.duelmonster.minersadvantage.common.workers;

/**
 * LumbinationAgent is the teammate that keeps this part of the mod understandable and stable.
 * It exists so behavior stays explicit instead of becoming mystery spaghetti at 2 AM.
 */
public final class LumbinationAgent extends AbstractAgent {
    /**
     * LumbinationAgent exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public LumbinationAgent(Object player, Object packet) {
        super(player, packet);
    }
}


