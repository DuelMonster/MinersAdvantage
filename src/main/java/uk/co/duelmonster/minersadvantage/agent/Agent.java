package uk.co.duelmonster.minersadvantage.agent;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import uk.co.duelmonster.minersadvantage.common.log.LogUtils;

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
        BlockState torchState = torchStateForFacing(facing);
        world.setBlockAndUpdate(pos, torchState);
        player.getInventory().removeItem(slot, 1);
        return true;
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
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).is(Items.TORCH)) {
                return i;
            }
        }
        return -1;
    }

    /** Returns true if the player has at least one torch in their inventory. */
    protected boolean playerHasTorches() {
        return findTorchSlot() >= 0;
    }
}
