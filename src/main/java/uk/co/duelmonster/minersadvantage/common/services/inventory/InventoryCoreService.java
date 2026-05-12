package uk.co.duelmonster.minersadvantage.common.services.inventory;

import java.util.Map;

/**
 * InventoryCoreService keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class InventoryCoreService {
    /**
     * hasAtLeast exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public boolean hasAtLeast(Map<String, Integer> inventory, String itemId, int amount) {
        return inventory.getOrDefault(itemId, 0) >= amount;
    }

    /**
     * consume exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public boolean consume(Map<String, Integer> inventory, String itemId, int amount) {
        int current = inventory.getOrDefault(itemId, 0);
        if (current < amount) {
            return false;
        }
        inventory.put(itemId, current - amount);
        return true;
    }
}
