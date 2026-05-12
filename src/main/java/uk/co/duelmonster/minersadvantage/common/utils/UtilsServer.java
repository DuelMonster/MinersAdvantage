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

    /**
     * giveToInventory exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static void giveToInventory(long playerId, String itemId) {
        if (itemId == null || itemId.isBlank()) {
            return;
        }
        LAST_GIVEN_ITEM.put(playerId, itemId);
    }

    /**
     * setHotbarSlot exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static void setHotbarSlot(long playerId, String itemId, int hotbarSlot) {
        if (hotbarSlot < 0 || hotbarSlot > 8) {
            return;
        }
        if (itemId != null && !itemId.isBlank()) {
            LAST_GIVEN_ITEM.put(playerId, itemId);
        }
        LAST_HELD_SLOT.put(playerId, hotbarSlot);
    }

    /**
     * setHeldItemSlot exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static void setHeldItemSlot(long playerId, int hotbarSlot) {
        if (hotbarSlot < 0 || hotbarSlot > 8) {
            return;
        }
        LAST_HELD_SLOT.put(playerId, hotbarSlot);
    }

    /**
     * lastGivenItem exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static String lastGivenItem(long playerId) {
        return LAST_GIVEN_ITEM.getOrDefault(playerId, "");
    }

    /**
     * lastHeldSlot exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static int lastHeldSlot(long playerId) {
        return LAST_HELD_SLOT.getOrDefault(playerId, -1);
    }
}


