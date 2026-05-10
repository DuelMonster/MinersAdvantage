package uk.co.duelmonster.minersadvantage.common.services.mining;

import java.util.ArrayList;
import java.util.List;

public final class ShaftanationCoreService {
    public record ShaftStep(int depth, boolean placeTorch) {}
    public record ShaftBatch(int newDepth, int torchPlacements, List<ShaftStep> steps) {}

    public boolean isStone(String blockId) {
        return blockId.contains("stone") || blockId.contains("deepslate");
    }

    public int estimatedTurnsToShaft(int maxDepth) {
        return maxDepth * 2;
    }

    public ShaftBatch buildBatch(int currentDepth, int maxDepth, int processesPerTick) {
        if (processesPerTick <= 0 || currentDepth >= maxDepth) {
            return new ShaftBatch(currentDepth, 0, List.of());
        }

        int newDepth = Math.min(maxDepth, currentDepth + processesPerTick);
        int torchPlacements = 0;
        List<ShaftStep> steps = new ArrayList<>();

        for (int depth = currentDepth + 1; depth <= newDepth; depth++) {
            boolean placeTorch = depth % 5 == 0;
            if (placeTorch) {
                torchPlacements++;
            }
            steps.add(new ShaftStep(depth, placeTorch));
        }

        return new ShaftBatch(newDepth, torchPlacements, steps);
    }
}
