package uk.co.duelmonster.minersadvantage.helpers;

import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FarmingHelper {
	
	public static AxisAlignedBB getFarmableLand(World world, BlockPos originPos) {
		
		BlockPos waterSource = getWaterSource(world, originPos);
		if (waterSource != null)
			return new AxisAlignedBB(
					waterSource.getX() - 4, waterSource.getY(), waterSource.getZ() - 4,
					waterSource.getX() + 4, waterSource.getY(), waterSource.getZ() + 4);
		else
			return new AxisAlignedBB(originPos, originPos);
	}
	
	public static BlockPos getWaterSource(World world, BlockPos originPos) {
		// Get closest water source within range of originPos (9x9 square)
		for (int offset = 1; offset <= 4; offset++) {
			AxisAlignedBB box = new AxisAlignedBB(originPos, originPos);
			box = box.grow(offset, 0, offset);
			
			Iterable<BlockPos> positions = BlockPos.getAllInBoxMutable(new BlockPos(box.minX, originPos.getY(), box.minZ), new BlockPos(box.maxX, originPos.getY(), box.maxZ));
			
			for (BlockPos pos : positions) {
				BlockState state = world.getBlockState(pos);
				// if (state.getMaterial() == Material.WATER || (state.getProperties().contains(BlockStateProperties.WATERLOGGED) && state.get(BlockStateProperties.WATERLOGGED))) {
				if (state.getMaterial() == Material.WATER || (state.func_235901_b_(BlockStateProperties.WATERLOGGED) && state.get(BlockStateProperties.WATERLOGGED))) {
					return pos;
				}
			}
		}
		
		return null;
	}
	
	public static AxisAlignedBB getCropPatch(World world, BlockPos originPos) {
		
		AxisAlignedBB cropPatch = getFarmableLand(world, originPos.down());
		
		return new AxisAlignedBB(
				cropPatch.minX, originPos.getY(), cropPatch.minZ,
				cropPatch.maxX, originPos.getY(), cropPatch.maxZ);
	}
	
}
