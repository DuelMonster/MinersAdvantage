package uk.co.duelmonster.minersadvantage.common.services.mining;

import java.util.ArrayList;
import java.util.List;

/**
 * ExcavationCoreService keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class ExcavationCoreService {
    /**
     * ExcavationTarget keeps this part of Miners Advantage running without turning server ticks into confetti.
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
        return blockId.contains("ore");
    }

    /**
     * estimatedTurnsToExcavate exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public int estimatedTurnsToExcavate(int radiusHorizontal, int radiusVertical) {
        int volume = (2 * radiusHorizontal + 1) * (2 * radiusHorizontal + 1) * (2 * radiusVertical + 1);
        return volume;
    }

    public List<ExcavationTarget> buildPlan(
        int originX,
        int originY,
        int originZ,
        String blockId,
        int radiusHorizontal,
        int radiusVertical,
        int maxTargets
    ) {
        if (!isBlock(blockId) || maxTargets <= 0) {
            return List.of();
        }

        if (isOre(blockId)) {
            return buildVeinPlan(originX, originY, originZ, maxTargets);
        }

        return buildAreaPlan(originX, originY, originZ, radiusHorizontal, radiusVertical, maxTargets);
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
        int radiusHorizontal,
        int radiusVertical,
        int maxTargets
    ) {
        List<ExcavationTarget> targets = new ArrayList<>();
        for (int y = -radiusVertical; y <= radiusVertical && targets.size() < maxTargets; y++) {
            for (int x = -radiusHorizontal; x <= radiusHorizontal && targets.size() < maxTargets; x++) {
                for (int z = -radiusHorizontal; z <= radiusHorizontal && targets.size() < maxTargets; z++) {
                    targets.add(new ExcavationTarget(originX + x, originY + y, originZ + z, "excavate"));
                }
            }
        }
        return targets;
    }
}

