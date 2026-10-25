package uk.co.duelmonster.minersadvantage.common.event;

/**
 * ToolEventHandler keeps this part of MinersAdvantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public interface ToolEventHandler {
    void onPickaxeUse(long playerId, int blockX, int blockY, int blockZ, String blockId);
    void onShovelUse(long playerId, int blockX, int blockY, int blockZ, String blockId);
    void onHoeUse(long playerId, int blockX, int blockY, int blockZ, String blockId);
    void onAxeUse(long playerId, int blockX, int blockY, int blockZ, String blockId);
    void onItemPickup(long playerId, String itemId, boolean isDirectPickup);

    default void onPickaxeUse(int blockX, int blockY, int blockZ, String blockId) {
        onPickaxeUse(0L, blockX, blockY, blockZ, blockId);
    }

    default void onShovelUse(int blockX, int blockY, int blockZ, String blockId) {
        onShovelUse(0L, blockX, blockY, blockZ, blockId);
    }

    default void onHoeUse(int blockX, int blockY, int blockZ, String blockId) {
        onHoeUse(0L, blockX, blockY, blockZ, blockId);
    }

    default void onAxeUse(int blockX, int blockY, int blockZ, String blockId) {
        onAxeUse(0L, blockX, blockY, blockZ, blockId);
    }

    default void onItemPickup(String itemId, boolean isDirectPickup) {
        onItemPickup(0L, itemId, isDirectPickup);
    }

    void onServerTick();
}
