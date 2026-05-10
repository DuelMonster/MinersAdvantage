package uk.co.duelmonster.minersadvantage.common.services.farming;

public final class CropinationCoreService {
    public record CropAction(boolean shouldHarvest, boolean shouldReplant, int seedsConsumed, int durabilityCost) {}

    public boolean isFullyGrown(int age, int maxAge) {
        return age >= maxAge;
    }

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
