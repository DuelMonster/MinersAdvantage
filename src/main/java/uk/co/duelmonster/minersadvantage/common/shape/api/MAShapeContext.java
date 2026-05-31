package uk.co.duelmonster.minersadvantage.common.shape.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * MAShapeContext contains the data needed to resolve shape geometry deterministically.
 */
public record MAShapeContext(
    Level level,
    Player player,
    BlockPos origin,
    BlockState originState,
    Direction hitFace,
    Direction playerFacing,
    int width,
    int height,
    int depth,
    int maxBlocks
) {
    public MAShapeContext {
        width = Math.max(1, width);
        height = Math.max(1, height);
        depth = Math.max(1, depth);
        maxBlocks = Math.max(1, maxBlocks);
        hitFace = hitFace == null ? Direction.NORTH : hitFace;
        playerFacing = playerFacing == null ? Direction.NORTH : playerFacing;
        originState = originState == null
            ? (level == null || origin == null ? Blocks.AIR.defaultBlockState() : level.getBlockState(origin))
            : originState;
    }
}
