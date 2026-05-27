package uk.co.duelmonster.minersadvantage.agent;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import uk.co.duelmonster.minersadvantage.common.config.CommonConfig;
import uk.co.duelmonster.minersadvantage.common.config.VeinationConfig;
import uk.co.duelmonster.minersadvantage.common.log.LogUtils;
import uk.co.duelmonster.minersadvantage.common.services.utility.VeinationRuntimeService;

import java.lang.reflect.Method;

/**
 * Base class for all feature agents. Modernized for production wiring.
 */
public abstract class Agent {
    protected final ServerPlayer player;
    protected final Level world;
    protected boolean complete = false;

    public Agent(ServerPlayer player) {
        this.player = player;
        this.world = player.level();
        LogUtils.logDebug("Created {} for player={} dimension={}", getClass().getSimpleName(), player.getScoreboardName(), player.level().dimension());
    }

    /**
     * Called every server tick. Returns true if the agent is finished and should be removed.
     */
    public abstract boolean tick();

    public boolean isComplete() {
        return complete;
    }

    protected boolean finish(String reason) {
        complete = true;
        LogUtils.logDebug("Completed {} for player={} reason={}", getClass().getSimpleName(), player.getScoreboardName(), reason);
        return true;
    }

    /**
     * Places a torch at the given position using the correct block state for the facing direction,
     * consuming one torch from the player's inventory. Returns false if the player has no torches.
     * Pass {@code null} or {@code Direction.UP} for a floor torch; any horizontal direction for a wall torch.
     */
    protected boolean placeTorchWithInventory(BlockPos pos, Direction facing) {
        int slot = findTorchSlot();
        if (slot < 0) {
            return false;
        }
        if (!canPlaceTorchAt(pos, facing)) {
            return false;
        }
        Direction placementDirection = normalizeTorchFacing(facing);
        BlockPos supportPos = placementDirection == Direction.UP ? pos.below() : pos.relative(placementDirection.getOpposite());
        return placeItemFromInventoryByUse(slot, supportPos, placementDirection);
    }

    protected boolean canPlaceTorchAt(BlockPos pos, Direction facing) {
        Direction placementDirection = normalizeTorchFacing(facing);
        if (!world.getBlockState(pos).canBeReplaced()) {
            return false;
        }

        BlockPos supportPos = placementDirection == Direction.UP ? pos.below() : pos.relative(placementDirection.getOpposite());
        BlockState supportState = world.getBlockState(supportPos);
        return supportState.isFaceSturdy(world, supportPos, placementDirection)
            && torchStateForFacing(placementDirection).canSurvive(world, pos);
    }

    protected boolean isAirOrReplaceableAbove(BlockPos pos) {
        BlockState above = world.getBlockState(pos.above());
        return above.isAir() || above.canBeReplaced();
    }

    private BlockState torchStateForFacing(Direction facing) {
        Direction placementDirection = normalizeTorchFacing(facing);
        return placementDirection == Direction.UP
            ? Blocks.TORCH.defaultBlockState()
            : Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, placementDirection);
    }

    private Direction normalizeTorchFacing(Direction facing) {
        return facing == null || facing == Direction.UP || facing == Direction.DOWN ? Direction.UP : facing;
    }

    /**
     * Returns the first inventory slot containing torches, or -1 if none.
     */
    protected int findTorchSlot() {
        return findFirstInventorySlot(Items.TORCH);
    }

    protected int findFirstInventorySlot(Item item) {
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (inventory.getItem(i).is(item)) {
                return i;
            }
        }
        return -1;
    }

    protected boolean placeItemFromInventoryByUse(int slot, BlockPos supportPos, Direction clickedFace) {
        if (player.gameMode == null) {
            return false;
        }

        Inventory inventory = player.getInventory();
        if (slot < 0 || slot >= inventory.getContainerSize()) {
            return false;
        }

        ItemStack sourceStack = inventory.getItem(slot).copy();
        if (sourceStack.isEmpty()) {
            return false;
        }

        BlockPos placementPos = supportPos.relative(clickedFace);
        BlockState beforeState = world.getBlockState(placementPos);
        ItemStack previousOffhand = player.getOffhandItem().copy();

        inventory.setItem(slot, ItemStack.EMPTY);
        player.setItemInHand(InteractionHand.OFF_HAND, sourceStack);

        try {
            Vec3 hitVec = Vec3.atCenterOf(supportPos).add(
                clickedFace.getStepX() * 0.5D,
                clickedFace.getStepY() * 0.5D,
                clickedFace.getStepZ() * 0.5D
            );
            BlockHitResult hitResult = new BlockHitResult(hitVec, clickedFace, supportPos, false);
            InteractionResult result = useItemOnAsPlayer(InteractionHand.OFF_HAND, hitResult);
            BlockState afterState = world.getBlockState(placementPos);
            boolean placed = !afterState.equals(beforeState);
            if (placed) {
                SoundType soundType = afterState.getSoundType();
                world.playSound(
                    null,
                    placementPos,
                    soundType.getPlaceSound(),
                    SoundSource.BLOCKS,
                    (soundType.getVolume() + 1.0F) / 2.0F,
                    soundType.getPitch() * 0.8F
                );
            }
            return result.consumesAction() || placed;
        } finally {
            ItemStack updatedOffhand = player.getOffhandItem().copy();
            inventory.setItem(slot, updatedOffhand);
            player.setItemInHand(InteractionHand.OFF_HAND, previousOffhand);
        }
    }

    private InteractionResult useItemOnAsPlayer(InteractionHand hand, BlockHitResult hitResult) {
        try {
            Method method = player.gameMode.getClass().getMethod(
                "useItemOn",
                ServerPlayer.class,
                Level.class,
                InteractionHand.class,
                BlockHitResult.class
            );
            Object result = method.invoke(player.gameMode, player, world, hand, hitResult);
            if (result instanceof InteractionResult interactionResult) {
                return interactionResult;
            }
        } catch (ReflectiveOperationException ignored) {
            // Why this exists: mixed mappings expose different useItemOn overloads.
        }

        try {
            Method method = player.gameMode.getClass().getMethod(
                "useItemOn",
                ServerPlayer.class,
                Level.class,
                ItemStack.class,
                InteractionHand.class,
                BlockHitResult.class
            );
            Object result = method.invoke(player.gameMode, player, world, player.getItemInHand(hand), hand, hitResult);
            if (result instanceof InteractionResult interactionResult) {
                return interactionResult;
            }
        } catch (ReflectiveOperationException ignored) {
            // Why this exists: mixed mappings expose different useItemOn overloads.
        }

        return InteractionResult.PASS;
    }

    /** Returns true if the player has at least one torch in their inventory. */
    protected boolean playerHasTorches() {
        return findTorchSlot() >= 0;
    }

    /**
     * maybeFanOutVeination keeps veination fan-out checks consistent across agents.
     */
    protected boolean maybeFanOutVeination(
        BlockPos pos,
        BlockState candidateState,
        boolean mineVeins,
        CommonConfig commonConfig,
        VeinationRuntimeService veinationRuntime,
        VeinationConfig veinationConfig,
        ItemStack veinationTriggerTool
    ) {
        if (!mineVeins || veinationRuntime == null || veinationConfig == null || !veinationConfig.enabled()) {
            return false;
        }

        AgentManager agentManager = AgentManager.get();
        if (agentManager.hasAgentType(player, VeinationAgent.class)) {
            return true;
        }

        ItemStack toolStack = veinationTriggerTool == null || veinationTriggerTool.isEmpty()
            ? player.getMainHandItem()
            : veinationTriggerTool;

        boolean toolAllowedByAllowlist = veinationRuntime.isPickaxeAllowed(world, veinationConfig, toolStack);
        boolean toolMinesCandidate = candidateState != null
            && (!candidateState.requiresCorrectToolForDrops() || toolStack.isCorrectToolForDrops(candidateState));

        if (!toolAllowedByAllowlist && !toolMinesCandidate) {
            return false;
        }

        if (!veinationRuntime.isOreAllowed(veinationConfig, candidateState)) {
            return false;
        }

        veinationRuntime.registerDropAnchor(player, pos, veinationConfig);
        agentManager.addAgent(player, new VeinationAgent(player, pos, candidateState, commonConfig, veinationRuntime, veinationConfig));
        return true;
    }
}
