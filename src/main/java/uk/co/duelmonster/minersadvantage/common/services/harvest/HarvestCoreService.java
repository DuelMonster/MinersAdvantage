package uk.co.duelmonster.minersadvantage.common.services.harvest;

/**
 * HarvestCoreService keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class HarvestCoreService {
    /**
     * canHarvestCrop exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public boolean canHarvestCrop(int age, int maxAge) {
        return age >= maxAge;
    }

    /**
     * Optimized adjustedDurabilityCost to improve clarity and performance.
     */
    public int adjustedDurabilityCost(int processedBlocks, int cadence) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (cadence <= 0) {
            throw new IllegalArgumentException("Cadence must be greater than zero.");
        }
        return (processedBlocks % cadence == 0) ? 1 : 0;
    }
}
