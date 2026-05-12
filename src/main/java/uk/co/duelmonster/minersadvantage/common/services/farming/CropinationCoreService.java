package uk.co.duelmonster.minersadvantage.common.services.farming;

/**
 * CropinationCoreService keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class CropinationCoreService {
    /**
     * CropAction keeps this part of Miners Advantage running without turning server ticks into confetti.
     * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
     */
    public record CropAction(boolean shouldHarvest, boolean shouldReplant, int seedsConsumed, int durabilityCost) {}

    /**
     * isFullyGrown exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public boolean isFullyGrown(int age, int maxAge) {
        return age >= maxAge;
    }

    /**
     * adjustedDurabilityCost exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public int adjustedDurabilityCost(int harvestedBlocks, int cadence) {
        return (harvestedBlocks % Math.max(1, cadence)) == 0 ? 1 : 0;
    }

    public CropAction evaluateCrop(
        int age,
        int maxAge,
        int availableSeeds,
        boolean harvestSeeds,
        int harvestedBlocks,
        int durabilityCadence
    ) {
        if (!isFullyGrown(age, maxAge)) {
            return new CropAction(false, false, 0, 0);
        }

        boolean shouldReplant = harvestSeeds && availableSeeds > 0;
        int seedsConsumed = shouldReplant ? 1 : 0;
        int durabilityCost = adjustedDurabilityCost(harvestedBlocks, durabilityCadence);
        return new CropAction(true, shouldReplant, seedsConsumed, durabilityCost);
    }
}



