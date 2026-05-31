package uk.co.duelmonster.minersadvantage.common.services.mining;

import java.util.ArrayList;
import java.util.List;
import uk.co.duelmonster.minersadvantage.common.registry.RegistryPredicates;

/**
 * ExcavationCoreService keeps this part of MinersAdvantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class ExcavationCoreService {
    /**
     * ExcavationTarget keeps this part of MinersAdvantage running without turning server ticks into confetti.
     * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
     */
    public record ExcavationTarget(int x, int y, int z, String operation) {}

    /**
     * isBlock exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public boolean isBlock(String blockId) {
        return blockId != null && !blockId.isEmpty() && !blockId.equals("air");
    }

    /**
     * isOre exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public boolean isOre(String blockId) {
        return RegistryPredicates.isOreLikeBlockId(blockId);
    }

    /**
     * estimatedTurnsToExcavate exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public int estimatedTurnsToExcavate(int width, int height, int depth) {
        int volume = Math.max(1, width) * Math.max(1, height) * Math.max(1, depth);
        return volume;
    }

    public List<ExcavationTarget> buildPlan(
        int originX,
        int originY,
        int originZ,
        String blockId,
        int width,
        int height,
        int depth,
        int maxTargets
    ) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (!isBlock(blockId) || maxTargets <= 0) {
            return List.of();
        }

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (isOre(blockId)) {
            return buildVeinPlan(originX, originY, originZ, maxTargets);
        }

        return buildAreaPlan(originX, originY, originZ, width, height, depth, maxTargets);
    }

    /**
     * buildVeinPlan exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private List<ExcavationTarget> buildVeinPlan(int originX, int originY, int originZ, int maxTargets) {
        int[][] offsets = new int[][] {
            {0, 0, 0},
            {1, 0, 0}, {-1, 0, 0},
            {0, 1, 0}, {0, -1, 0},
            {0, 0, 1}, {0, 0, -1}
        };
        List<ExcavationTarget> targets = new ArrayList<>();
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        for (int i = 0; i < offsets.length && targets.size() < maxTargets; i++) {
            int[] offset = offsets[i];
            targets.add(new ExcavationTarget(
                originX + offset[0],
                originY + offset[1],
                originZ + offset[2],
                "vein"
            ));
        }
        return targets;
    }

    private List<ExcavationTarget> buildAreaPlan(
        int originX,
        int originY,
        int originZ,
        int width,
        int height,
        int depth,
        int maxTargets
    ) {
        int halfWidth = Math.max(0, width / 2);
        int halfHeight = Math.max(0, height / 2);
        int halfDepth = Math.max(0, depth / 2);
        List<ExcavationTarget> targets = new ArrayList<>();
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        for (int y = -halfHeight; y <= halfHeight && targets.size() < maxTargets; y++) {
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            for (int x = -halfWidth; x <= halfWidth && targets.size() < maxTargets; x++) {
                // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                for (int z = -halfDepth; z <= halfDepth && targets.size() < maxTargets; z++) {
                    targets.add(new ExcavationTarget(originX + x, originY + y, originZ + z, "excavate"));
                }
            }
        }
        return targets;
    }
}
