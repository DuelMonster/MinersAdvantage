package uk.co.duelmonster.minersadvantage.common.services.harvest;

import java.util.ArrayList;
import java.util.List;

/**
 * LumbinationCoreService keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class LumbinationCoreService {
    /**
     * LumbinationStep keeps this part of Miners Advantage running without turning server ticks into confetti.
     * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
     */
    public record LumbinationStep(String phase, int index) {}
    /**
     * LumbinationPlan keeps this part of Miners Advantage running without turning server ticks into confetti.
     * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
     */
    public record LumbinationPlan(int logsToHarvest, int leavesToClear, boolean replantSapling, List<LumbinationStep> steps) {}

    /**
     * isLog exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public boolean isLog(String blockId) {
        return blockId.contains("log") || blockId.contains("stem");
    }

    /**
     * isLeaf exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public boolean isLeaf(String blockId) {
        return blockId.contains("leaf") || blockId.contains("leaves");
    }

    /**
     * isSapling exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public boolean isSapling(String blockId) {
        return blockId.contains("sapling");
    }

    /**
     * estimatedTurnsToFell exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
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

