package uk.co.duelmonster.minersadvantage.common;

/**
 * TorchPlacement is the teammate that keeps this part of the mod understandable and stable.
 * It exists so behavior stays explicit instead of becoming mystery spaghetti at 2 AM.
 */
public enum TorchPlacement {
    INACTIVE(0, "INACTIVE"),
    FLOOR(1, "FLOOR"),
    LEFT_WALL(2, "LEFT_WALL"),
    RIGHT_WALL(3, "RIGHT_WALL"),
    BOTH_WALLS(4, "BOTH_WALLS");

    private final int index;
    private final String name;

    TorchPlacement(int index, String name) {
        this.index = index;
        this.name = name;
    }

    /**
     * getIndex exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public int getIndex() {
        return index;
    }

    /**
     * getName exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public String getName() {
        return name;
    }
}


