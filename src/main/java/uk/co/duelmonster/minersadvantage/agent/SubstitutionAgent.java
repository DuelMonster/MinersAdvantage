package uk.co.duelmonster.minersadvantage.agent;

import net.minecraft.tags.BlockTags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.block.state.BlockState;

/**
 * SubstitutionAgent: swaps the player's held tool for the best available one in inventory.
 */
public class SubstitutionAgent extends Agent {
    private enum ToolKind {
        PICKAXE,
        AXE,
        SHOVEL,
        HOE
    }

    private final BlockState targetState;

    public SubstitutionAgent(ServerPlayer player) {
        this(player, player.level().getBlockState(player.blockPosition()));
    }

    public SubstitutionAgent(ServerPlayer player, BlockState targetState) {
        super(player);
        this.targetState = targetState;
    }

    @Override
    public boolean tick() {
        if (targetState == null || targetState.isAir()) {
            return finish("no substitution target state");
        }

        ItemStack main = player.getMainHandItem();
        ToolKind requiredKind = inferRequiredToolKind(main, targetState);
        double bestScore = scoreTool(main, requiredKind, targetState);
        int bestSlot = -1;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!matchesToolKind(stack, requiredKind)) {
                continue;
            }

            double score = scoreTool(stack, requiredKind, targetState);
            if (score > bestScore) {
                bestScore = score;
                bestSlot = i;
            }
        }

        if (bestSlot >= 0) {
            ItemStack replacement = player.getInventory().getItem(bestSlot);
            ItemStack previousMain = main.copy();
            player.setItemInHand(InteractionHand.MAIN_HAND, replacement.copy());
            player.getInventory().setItem(bestSlot, previousMain);
        }
        return finish("tool substitution evaluated");
    }

    private ToolKind inferRequiredToolKind(ItemStack main, BlockState state) {
        if (state.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
            return ToolKind.PICKAXE;
        }
        if (state.is(BlockTags.MINEABLE_WITH_AXE)) {
            return ToolKind.AXE;
        }
        if (state.is(BlockTags.MINEABLE_WITH_SHOVEL)) {
            return ToolKind.SHOVEL;
        }
        if (state.is(BlockTags.MINEABLE_WITH_HOE)) {
            return ToolKind.HOE;
        }

        if (isPickaxeTool(main)) {
            return ToolKind.PICKAXE;
        }
        if (main.getItem() instanceof AxeItem) {
            return ToolKind.AXE;
        }
        if (main.getItem() instanceof HoeItem) {
            return ToolKind.HOE;
        }
        return ToolKind.SHOVEL;
    }

    private boolean matchesToolKind(ItemStack stack, ToolKind requiredKind) {
        if (stack.isEmpty()) {
            return false;
        }
        return switch (requiredKind) {
            case PICKAXE -> isPickaxeTool(stack);
            case AXE -> stack.getItem() instanceof AxeItem;
            case SHOVEL -> stack.getItem() instanceof ShovelItem;
            case HOE -> stack.getItem() instanceof HoeItem;
        };
    }

    private double scoreTool(ItemStack stack, ToolKind requiredKind, BlockState state) {
        if (!matchesToolKind(stack, requiredKind)) {
            return -1.0;
        }

        double speed = stack.getDestroySpeed(state);
        if (stack.isCorrectToolForDrops(state)) {
            speed += 2.0;
        }

        if (stack.isDamageableItem() && stack.getMaxDamage() > 0) {
            double durabilityFactor = (double) (stack.getMaxDamage() - stack.getDamageValue()) / (double) stack.getMaxDamage();
            speed += durabilityFactor * 0.25;
        }
        return speed;
    }

    private boolean isPickaxeTool(ItemStack stack) {
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        return path.endsWith("_pickaxe");
    }
}