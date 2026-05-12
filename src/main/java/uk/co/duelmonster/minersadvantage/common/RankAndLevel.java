package uk.co.duelmonster.minersadvantage.common;

/**
 * RankAndLevel is the teammate that keeps this part of the mod understandable and stable.
 * It exists so behavior stays explicit instead of becoming mystery spaghetti at 2 AM.
 */
public class RankAndLevel {
    public int SlotID = -1;
    public Ranking rank;
    public int Level_1 = 0;
    public int Level_2 = 0;

    /**
     * RankAndLevel exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public RankAndLevel(int iSlotID, Ranking rank, int Level) {
        this(iSlotID, rank, Level, -1);
    }

    /**
     * RankAndLevel exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public RankAndLevel(int iSlotID, Ranking rank, int Level_1, int Level_2) {
        this.SlotID = iSlotID;
        this.rank = rank;
        this.Level_1 = Level_1;
        this.Level_2 = Level_2;
    }
}
