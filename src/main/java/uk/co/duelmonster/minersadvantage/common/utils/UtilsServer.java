package uk.co.duelmonster.minersadvantage.common.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Compatibility utility cache for server-directed item operations.
 * Actual inventory mutation remains platform-integrated elsewhere.
 */
public final class UtilsServer {
    private static final Map<Long, String> LAST_GIVEN_ITEM = new ConcurrentHashMap<>();
    private static final Map<Long, Integer> LAST_HELD_SLOT = new ConcurrentHashMap<>();

    private UtilsServer() {}

    public static void giveToInventory(long playerId, String itemId) {
        if (itemId == null || itemId.isBlank()) {
            return;
        }
        LAST_GIVEN_ITEM.put(playerId, itemId);
    }

    public static void setHotbarSlot(long playerId, String itemId, int hotbarSlot) {
        if (hotbarSlot < 0 || hotbarSlot > 8) {
            return;
        }
        if (itemId != null && !itemId.isBlank()) {
            LAST_GIVEN_ITEM.put(playerId, itemId);
        }
        LAST_HELD_SLOT.put(playerId, hotbarSlot);
    }

    public static void setHeldItemSlot(long playerId, int hotbarSlot) {
        if (hotbarSlot < 0 || hotbarSlot > 8) {
            return;
        }
        LAST_HELD_SLOT.put(playerId, hotbarSlot);
    }

    public static String lastGivenItem(long playerId) {
        return LAST_GIVEN_ITEM.getOrDefault(playerId, "");
    }

    public static int lastHeldSlot(long playerId) {
        return LAST_HELD_SLOT.getOrDefault(playerId, -1);
    }
}
