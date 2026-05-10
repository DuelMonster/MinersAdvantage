package uk.co.duelmonster.minersadvantage.common.services.farming;

import java.util.ArrayList;
import java.util.List;

public final class FarmingCoreService {
    public record CultivationStep(int x, int y, int z, boolean hydrated) {}

    public boolean canHydrate(int distanceToWater, int maxHydrationDistance) {
        return distanceToWater >= 0 && distanceToWater <= maxHydrationDistance;
    }

    public List<CultivationStep> buildCultivationPlan(
        int originX,
        int originY,
        int originZ,
        int hydrationDistance,
        int maxTiles
    ) {
        List<CultivationStep> plan = new ArrayList<>();
        if (maxTiles <= 0) {
            return plan;
        }

        for (int step = 0; step < maxTiles; step++) {
            int x = originX + step;
            boolean hydrated = canHydrate(step, hydrationDistance);
            plan.add(new CultivationStep(x, originY, originZ, hydrated));
        }
        return plan;
    }
}
