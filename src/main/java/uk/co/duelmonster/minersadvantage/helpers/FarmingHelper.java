package uk.co.duelmonster.minersadvantage.helpers;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;

public class FarmingHelper {

  public static AABB getFarmableLand(Level world, BlockPos originPos) {

    BlockPos waterSource = getWaterSource(world, originPos);
    if (waterSource != null)
      return new AABB(
          waterSource.getX() - 4, waterSource.getY(), waterSource.getZ() - 4,
          waterSource.getX() + 4, waterSource.getY(), waterSource.getZ() + 4);
    else
      return new AABB(originPos, originPos);
  }

  public static BlockPos getWaterSource(Level world, BlockPos originPos) {
    // Get closest water source within range of originPos (9x9 square)
    for (int offset = 1; offset <= 4; offset++) {
      AABB box = new AABB(originPos, originPos);
      box = box.inflate(offset, 0, offset);

      Iterable<BlockPos> positions = BlockPos.betweenClosed(new BlockPos((int) box.minX, originPos.getY(), (int) box.minZ), new BlockPos((int) box.maxX, originPos.getY(), (int) box.maxZ));

      for (BlockPos pos : positions) {
        BlockState state = world.getBlockState(pos);
        if (state.getFluidState().is(Fluids.WATER) || (state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED))) {
          return pos;
        }
      }
    }

    return null;
  }

  public static AABB getCropPatch(Level world, BlockPos originPos) {

    AABB cropPatch = getFarmableLand(world, originPos.below());

    return new AABB(
        cropPatch.minX, originPos.getY(), cropPatch.minZ,
        cropPatch.maxX, originPos.getY(), cropPatch.maxZ);
  }

}
