package uk.co.duelmonster.minersadvantage.common.services.mining;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import uk.co.duelmonster.minersadvantage.common.Functions;

/**
 * VentilationCoreService keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class VentilationCoreService {
    private int ladderStackCount = 0;
    private int ladderIndex = -1;

    /**
     * playerHasLadders exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public boolean playerHasLadders(ServerPlayer player) {
        getLadderSlot(player);
        return ladderIndex >= 0;
    }

    /**
     * getLadderSlot exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public void getLadderSlot(ServerPlayer player) {
        ladderStackCount = 0;
        ladderIndex = -1;

        Item ladderItem = Blocks.LADDER.asItem();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack != null && stack.getItem().equals(ladderItem)) {
                ladderStackCount++;
                ladderIndex = Functions.getSlotFromInventory(player, stack);
            }
        }
    }

    /**
     * isLadderablePosition exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public boolean isLadderablePosition(Level world, BlockPos pos) {
        return world.isEmptyBlock(pos)
            && !world.isEmptyBlock(pos.south())
            && canPlaceLadderOnFace(world, pos.south());
    }

    /**
     * canPlaceLadderOnFace exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    private boolean canPlaceLadderOnFace(Level world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        boolean validFace = state.isFaceSturdy(world, pos, Direction.NORTH)
            && world.getBlockState(pos.relative(Direction.NORTH)).canBeReplaced();
        boolean validBlockType = block != Blocks.END_GATEWAY && block != Blocks.JACK_O_LANTERN;
        return validFace && validBlockType;
    }
    /**
     * VentilationStep keeps this part of Miners Advantage running without turning server ticks into confetti.
     * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
     */
    public record VentilationStep(int progressIndex, boolean placeLadder) {}
    /**
     * VentilationBatch keeps this part of Miners Advantage running without turning server ticks into confetti.
     * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
     */
    public record VentilationBatch(int newProgress, int ladderPlacements, List<VentilationStep> steps) {}

    /**
     * isCave exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public boolean isCave(int surfaceLevel, int currentLevel) {
        return currentLevel < surfaceLevel - 10;
    }

    /**
     * estimatedTurnsToVentilate exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
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



