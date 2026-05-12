package uk.co.duelmonster.minersadvantage.common.services.mining;

import java.util.ArrayList;
import java.util.List;

/**
 * ShaftanationCoreService keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class ShaftanationCoreService {
    /**
     * ShaftStep keeps this part of Miners Advantage running without turning server ticks into confetti.
     * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
     */
    public record ShaftStep(int depth, boolean placeTorch) {}
    /**
     * ShaftBatch keeps this part of Miners Advantage running without turning server ticks into confetti.
     * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
     */
    public record ShaftBatch(int newDepth, int torchPlacements, List<ShaftStep> steps) {}

    /**
     * isStone exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public boolean isStone(String blockId) {
        return blockId.contains("stone") || blockId.contains("deepslate");
    }

    /**
     * estimatedTurnsToShaft exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public int estimatedTurnsToShaft(int maxDepth) {
        return maxDepth * 2;
    }

    /**
     * buildBatch exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
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
