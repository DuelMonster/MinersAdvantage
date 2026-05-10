package uk.co.duelmonster.minersadvantage.common.services.harvest;

public final class HarvestCoreService {
    public boolean canHarvestCrop(int age, int maxAge) {
        return age >= maxAge;
    }

    public int adjustedDurabilityCost(int processedBlocks, int cadence) {
        return processedBlocks % Math.max(1, cadence) == 0 ? 1 : 0;
    }
}
