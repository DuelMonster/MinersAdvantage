package uk.co.duelmonster.minersadvantage.common.event;

/**
 * ToolEventHandler keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public interface ToolEventHandler {
    void onPickaxeUse(int blockX, int blockY, int blockZ, String blockId);

    void onShovelUse(int blockX, int blockY, int blockZ, String blockId);

    void onHoeUse(int blockX, int blockY, int blockZ, String blockId);

    void onAxeUse(int blockX, int blockY, int blockZ, String blockId);

    void onItemPickup(String itemId, boolean isDirectPickup);

    void onServerTick();
}



