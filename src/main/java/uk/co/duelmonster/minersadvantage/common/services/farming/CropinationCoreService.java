package uk.co.duelmonster.minersadvantage.common.services.farming;

public final class CropinationCoreService {
    public boolean isFullyGrown(int age, int maxAge) {
        return age >= maxAge;
    }

    public int adjustedDurabilityCost(int harvestedBlocks, int cadence) {
        return (harvestedBlocks % Math.max(1, cadence)) == 0 ? 1 : 0;
    }
}
