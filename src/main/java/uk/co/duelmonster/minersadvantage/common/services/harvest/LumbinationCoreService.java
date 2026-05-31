package uk.co.duelmonster.minersadvantage.common.services.harvest;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import uk.co.duelmonster.minersadvantage.common.Functions;
import uk.co.duelmonster.minersadvantage.common.config.LumbinationConfig;
import uk.co.duelmonster.minersadvantage.common.registry.RegistryPredicates;

/**
 * LumbinationCoreService keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class LumbinationCoreService {
    // Why this exists: Parity note: key block/tool validation logic from legacy LumbinationHelper is ported. (future-you will thank present-you).

    /**
     * i sw oo d exists so this path stays predictable and easier to debug when things get weird.
     */
    public boolean isWood(BlockState state) {
        return state.is(BlockTags.LOGS)
            || state.is(BlockTags.PLANKS)
            || state.is(BlockTags.WOODEN_BUTTONS)
            || state.is(BlockTags.WOODEN_DOORS)
            || state.is(BlockTags.WOODEN_FENCES)
            || state.is(BlockTags.WOODEN_PRESSURE_PLATES)
            || state.is(BlockTags.WOODEN_SLABS)
            || state.is(BlockTags.WOODEN_STAIRS)
            || state.is(BlockTags.WOODEN_TRAPDOORS);
    }

    /**
     * Optimized isValidLog to handle large configurations efficiently.
     */
    public boolean isValidLog(BlockState state, LumbinationConfig config) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (state.is(BlockTags.LOGS) || state.getBlock() instanceof RotatedPillarBlock) {
            return true;
        }
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (config == null || config.logs() == null || config.logs().isEmpty()) {
            return false;
        }
        return config.logs().contains(Functions.getName(state.getBlock().asItem()));
    }

    /**
     * Optimized isValidLeaves to handle large configurations efficiently.
     */
    public boolean isValidLeaves(BlockState state, LumbinationConfig config) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (state.is(BlockTags.LEAVES) || state.getBlock() instanceof LeavesBlock) {
            return true;
        }
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (config == null || config.leaves() == null || config.leaves().isEmpty()) {
            return false;
        }
        return config.leaves().contains(Functions.getName(state.getBlock().asItem()));
    }

    /**
     * isValidAxe exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public boolean isValidAxe(Item heldItem, LumbinationConfig config) {
        return heldItem instanceof AxeItem
            || (config != null
                && config.axes() != null
                && !config.axes().isEmpty()
                && config.axes().contains(Functions.getName(heldItem)));
    }
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
        return RegistryPredicates.isLogLikeBlockId(blockId);
    }

    /**
     * isLeaf exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public boolean isLeaf(String blockId) {
        return RegistryPredicates.isLeafLikeBlockId(blockId);
    }

    /**
     * isSapling exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public boolean isSapling(String blockId) {
        return RegistryPredicates.isSaplingBlock(blockId);
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
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        for (int i = 0; i < stepsToEmit; i++) {
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (i < logsToHarvest) {
                steps.add(new LumbinationStep("log", i));
            } else if (i - logsToHarvest < leavesToClear) {
                steps.add(new LumbinationStep("leaf", i - logsToHarvest));
            }
        }

        return new LumbinationPlan(logsToHarvest, leavesToClear, saplingAvailable, steps);
    }
}
