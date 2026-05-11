package uk.co.duelmonster.minersadvantage.common.services.farming;

import java.util.ArrayList;
import java.util.List;

/**
 * FarmingCoreService keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class FarmingCoreService {
    /**
     * CultivationStep keeps this part of Miners Advantage running without turning server ticks into confetti.
     * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
     */
    public record CultivationStep(int x, int y, int z, boolean hydrated) {}

    /**
     * canHydrate exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
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

