package uk.co.duelmonster.minersadvantage.common.event;

public interface ToolEventHandler {
    void onPickaxeUse(int blockX, int blockY, int blockZ, String blockId);

    void onShovelUse(int blockX, int blockY, int blockZ, String blockId);

    void onHoeUse(int blockX, int blockY, int blockZ, String blockId);

    void onAxeUse(int blockX, int blockY, int blockZ, String blockId);

    void onItemPickup(String itemId, boolean isDirectPickup);

    void onServerTick();
}
