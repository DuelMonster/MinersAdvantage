package uk.co.duelmonster.minersadvantage.helpers;

import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
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
		// for (int yOffset = originPos.getY() - 1; yOffset <= originPos.getY() + 1;
		// ++yOffset)
		for (int xOffset = originPos.getX() - 4; xOffset <= originPos.getX() + 4; ++xOffset)
			for (int zOffset = originPos.getZ() - 4; zOffset <= originPos.getZ() + 4; ++zOffset) {
				BlockPos oPos = new BlockPos(xOffset, originPos.getY(), zOffset);
				BlockState state = world.getBlockState(oPos);
				if (state.getMaterial() == Material.WATER)
					return oPos;
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
