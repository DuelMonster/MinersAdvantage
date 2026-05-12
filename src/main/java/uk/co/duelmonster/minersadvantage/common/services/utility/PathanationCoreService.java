package uk.co.duelmonster.minersadvantage.common.services.utility;

import java.util.ArrayList;
import java.util.List;

/**
 * PathanationCoreService keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class PathanationCoreService {
    /**
     * PathStep keeps this part of Miners Advantage running without turning server ticks into confetti.
     * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
     */
    public record PathStep(int x, int y, int z, String operation) {}

    /**
     * isTargetBlock exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public boolean isTargetBlock(String blockId) {
        return blockId != null && !blockId.contains("air") && !blockId.contains("bedrock");
    }

    /**
     * estimatedTurnsToPath exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public int estimatedTurnsToPath(int distance) {
        return Math.max(1, distance);
    }

    /**
     * buildPath exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public List<PathStep> buildPath(int startX, int startY, int startZ, int distance, int maxSteps) {
        int steps = Math.max(0, Math.min(distance, maxSteps));
        List<PathStep> plan = new ArrayList<>();
        for (int i = 1; i <= steps; i++) {
            plan.add(new PathStep(startX + i, startY, startZ, "flatten"));
        }
        return plan;
    }
}
