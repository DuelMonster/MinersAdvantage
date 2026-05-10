package uk.co.duelmonster.minersadvantage.common.services.inventory;

import java.util.Map;

public final class InventoryCoreService {
    public boolean hasAtLeast(Map<String, Integer> inventory, String itemId, int amount) {
        return inventory.getOrDefault(itemId, 0) >= amount;
    }

    public boolean consume(Map<String, Integer> inventory, String itemId, int amount) {
        int current = inventory.getOrDefault(itemId, 0);
        if (current < amount) {
            return false;
        }
        inventory.put(itemId, current - amount);
        return true;
    }
}
