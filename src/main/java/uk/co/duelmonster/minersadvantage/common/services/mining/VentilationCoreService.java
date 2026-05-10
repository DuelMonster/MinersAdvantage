package uk.co.duelmonster.minersadvantage.common.services.mining;

import java.util.ArrayList;
import java.util.List;

public final class VentilationCoreService {
    public record VentilationStep(int progressIndex, boolean placeLadder) {}
    public record VentilationBatch(int newProgress, int ladderPlacements, List<VentilationStep> steps) {}

    public boolean isCave(int surfaceLevel, int currentLevel) {
        return currentLevel < surfaceLevel - 10;
    }

    public int estimatedTurnsToVentilate(int radiusHorizontal, int radiusVertical) {
        int volume = (2 * radiusHorizontal + 1) * (2 * radiusHorizontal + 1) * (2 * radiusVertical + 1);
        return volume / 2;
    }

    public VentilationBatch buildBatch(
        int currentProgress,
        int radiusHorizontal,
        int radiusVertical,
        int processesPerTick
    ) {
        if (processesPerTick <= 0) {
            return new VentilationBatch(currentProgress, 0, List.of());
        }

        int targetTurns = estimatedTurnsToVentilate(radiusHorizontal, radiusVertical);
        int newProgress = Math.min(targetTurns, currentProgress + processesPerTick);
        int ladderPlacements = 0;
        List<VentilationStep> steps = new ArrayList<>();

        for (int index = currentProgress + 1; index <= newProgress; index++) {
            boolean placeLadder = index % 3 == 0;
            if (placeLadder) {
                ladderPlacements++;
            }
            steps.add(new VentilationStep(index, placeLadder));
        }

        return new VentilationBatch(newProgress, ladderPlacements, steps);
    }
}
