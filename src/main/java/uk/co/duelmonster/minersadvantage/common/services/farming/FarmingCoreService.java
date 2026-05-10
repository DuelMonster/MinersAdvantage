package uk.co.duelmonster.minersadvantage.common.services.farming;

public final class FarmingCoreService {
    public boolean canHydrate(int distanceToWater, int maxHydrationDistance) {
        return distanceToWater >= 0 && distanceToWater <= maxHydrationDistance;
    }
}
