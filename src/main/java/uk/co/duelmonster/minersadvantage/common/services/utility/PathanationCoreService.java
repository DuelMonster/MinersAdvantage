package uk.co.duelmonster.minersadvantage.common.services.utility;

import java.util.ArrayList;
import java.util.List;

public final class PathanationCoreService {
    public record PathStep(int x, int y, int z, String operation) {}

    public boolean isTargetBlock(String blockId) {
        return blockId != null && !blockId.contains("air") && !blockId.contains("bedrock");
    }

    public int estimatedTurnsToPath(int distance) {
        return Math.max(1, distance);
    }

    public List<PathStep> buildPath(int startX, int startY, int startZ, int distance, int maxSteps) {
        int steps = Math.max(0, Math.min(distance, maxSteps));
        List<PathStep> plan = new ArrayList<>();
        for (int i = 1; i <= steps; i++) {
            plan.add(new PathStep(startX + i, startY, startZ, "flatten"));
        }
        return plan;
    }
}
