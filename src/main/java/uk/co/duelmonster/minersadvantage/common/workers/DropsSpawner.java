package uk.co.duelmonster.minersadvantage.common.workers;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Legacy compatibility drop capture buffer used by migrated worker paths.
 */
public final class DropsSpawner {
    private static final List<Entity> DROP_HISTORY = new ArrayList<>();
    private static int capturedXp = 0;

    private DropsSpawner() {}

    /**
     * recordDrop exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static void recordDrop(ItemEntity itemEntity) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (itemEntity != null) {
            DROP_HISTORY.add(itemEntity);
        }
    }

    /**
     * addXP exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static void addXP(int amount) {
        capturedXp += Math.max(0, amount);
    }

    /**
     * getAndResetXP exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static int getAndResetXP() {
        int value = capturedXp;
        capturedXp = 0;
        return value;
    }

    /**
     * getDropHistorySnapshot exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static List<Entity> getDropHistorySnapshot() {
        return List.copyOf(DROP_HISTORY);
    }

    /**
     * reset exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static void reset() {
        DROP_HISTORY.clear();
        capturedXp = 0;
    }

    /**
     * getDropOfBlockTypeFromList exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static Block getDropOfBlockTypeFromList(Class<?> blockType, List<Entity> dropsHistory) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (blockType == null || dropsHistory == null || dropsHistory.isEmpty()) {
            return Blocks.AIR;
        }

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        for (Entity entity : dropsHistory) {
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (entity instanceof ItemEntity itemEntity) {
                ItemStack stack = itemEntity.getItem();
                // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                if (stack != null && !stack.isEmpty() && stack.getItem() instanceof net.minecraft.world.item.BlockItem blockItem) {
                    Block block = blockItem.getBlock();
                    // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                    if (blockType.isInstance(block)) {
                        return block;
                    }
                }
            }
        }

        return Blocks.AIR;
    }
}
