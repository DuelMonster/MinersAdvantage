package uk.co.duelmonster.minersadvantage.common;

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

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }
}
