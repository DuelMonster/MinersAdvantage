package uk.co.duelmonster.minersadvantage.common.services.harvest;

import java.util.ArrayList;
import java.util.List;

public final class LumbinationCoreService {
    public record LumbinationStep(String phase, int index) {}
    public record LumbinationPlan(int logsToHarvest, int leavesToClear, boolean replantSapling, List<LumbinationStep> steps) {}

    public boolean isLog(String blockId) {
        return blockId.contains("log") || blockId.contains("stem");
    }

    public boolean isLeaf(String blockId) {
        return blockId.contains("leaf") || blockId.contains("leaves");
    }

    public boolean isSapling(String blockId) {
        return blockId.contains("sapling");
    }

    public int estimatedTurnsToFell(int connectedLogCount) {
        return Math.max(1, connectedLogCount / 2);
    }

    public LumbinationPlan buildPlan(
        int estimatedConnectedLogs,
        int maxTrunkRange,
        int maxLeafRange,
        int processesPerTick,
        boolean saplingAvailable
    ) {
        int logsToHarvest = Math.min(Math.max(0, estimatedConnectedLogs), Math.max(1, maxTrunkRange));
        int leavesToClear = Math.min(Math.max(0, estimatedConnectedLogs / 2), Math.max(1, maxLeafRange));
        int stepsToEmit = Math.max(1, processesPerTick);

        List<LumbinationStep> steps = new ArrayList<>();
        for (int i = 0; i < stepsToEmit; i++) {
            if (i < logsToHarvest) {
                steps.add(new LumbinationStep("log", i));
            } else if (i - logsToHarvest < leavesToClear) {
                steps.add(new LumbinationStep("leaf", i - logsToHarvest));
            }
        }

        return new LumbinationPlan(logsToHarvest, leavesToClear, saplingAvailable, steps);
    }
}
