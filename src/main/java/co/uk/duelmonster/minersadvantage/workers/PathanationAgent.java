package co.uk.duelmonster.minersadvantage.workers;

import java.util.concurrent.TimeUnit;

import co.uk.duelmonster.minersadvantage.common.Functions;
import co.uk.duelmonster.minersadvantage.settings.Settings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class PathanationAgent extends Agent {
	
	public PathanationAgent(EntityPlayerMP player, NBTTagCompound tags) {
		super(player, tags, true);
		
		setupPath();
		
		// Add the origin block the queue now that we have all the information.  -  this.queued.clear();
		addConnectedToQueue(originPos);
	}
	
	// Returns true when Excavation is complete or cancelled
	@Override
	public boolean tick() {
		if (originPos == null || player == null || !player.isEntityAlive() || processed.size() >= settings.iBlockLimit())
			return true;
		
		timer.start();
		
		boolean bIsComplete = false;
		
		for (int iQueueCount = 0; queued.size() > 0; iQueueCount++) {
			if (iQueueCount >= settings.iBlocksPerTick()
					|| processed.size() >= settings.iBlockLimit()
					|| (settings.tpsGuard() && timer.elapsed(TimeUnit.MILLISECONDS) > 40))
				break;
			
			if (heldItem != Functions.getHeldItem(player) || Functions.IsPlayerStarving(player)) {
				bIsComplete = true;
				break;
			}
			
			BlockPos oPos = queued.remove(0);
			if (oPos == null)
				continue;
			
			IBlockState state = world.getBlockState(oPos);
			Block block = state.getBlock();
			
			if (!(block instanceof BlockDirt || block instanceof BlockGrass)) {
				// Add the non-harvestable blocks to the processed list so that they can be avoided.
				processed.add(oPos);
				continue;
			}
			
			world.captureBlockSnapshots = true;
			world.capturedBlockSnapshots.clear();
			
			if (world.isAirBlock(oPos.up())) {
				world.playSound(player, oPos, SoundEvents.ITEM_SHOVEL_FLATTEN, SoundCategory.BLOCKS, 1.0F, 1.0F);
				
				world.setBlockState(oPos, Blocks.GRASS_PATH.getDefaultState());// , 11);
				heldItemStack.damageItem(1, player);
				
				SoundType soundtype = block.getSoundType(state, world, oPos, null);
				reportProgessToClient(oPos, soundtype.getHitSound());
				
				processBlockSnapshots();
				addConnectedToQueue(oPos);
			}
			
			processed.add(oPos);
		}
		
		timer.reset();
		
		return (bIsComplete || queued.isEmpty());
	}
	
	@Override
	public void addToQueue(BlockPos oPos) {
		Block block = world.getBlockState(oPos).getBlock();
		
		if (block instanceof BlockDirt || block instanceof BlockGrass)
			super.addToQueue(oPos);
	}
	
	private void setupPath() {
		// Shaft area info
		Settings settings = Settings.get(player.getUniqueID());
		int xStart = 0;
		int xEnd = 0;
		int yBottom = originPos.getY() - 1;
		int yTop = originPos.getY() + 1;
		int zStart = 0;
		int zEnd = 0;
		
		int iLength = settings.iPathLength() - 1;
		
		// if the ShaftWidth is divisible by 2 we don't want to do anything
		double dDivision = ((settings.iPathWidth() & 1) != 0 ? 0 : 0.5);
		
		EnumFacing direction = player.getAdjustedHorizontalFacing().getOpposite();
		
		switch (direction) {
		case SOUTH: // Positive Z
			xStart = originPos.getX() + ((int) ((settings.iPathWidth() / 2) - dDivision));
			xEnd = originPos.getX() - (settings.iPathWidth() / 2);
			zStart = originPos.getZ();
			zEnd = originPos.getZ() - iLength;
			break;
		case NORTH: // Negative Z
			xStart = originPos.getX() - (settings.iPathWidth() / 2);
			xEnd = originPos.getX() + ((int) ((settings.iPathWidth() / 2) - dDivision));
			zStart = originPos.getZ();
			zEnd = originPos.getZ() + iLength;
			break;
		case EAST: // Positive X
			xStart = originPos.getX();
			xEnd = originPos.getX() - iLength;
			zStart = originPos.getZ() + ((int) ((settings.iPathWidth() / 2) - dDivision));
			zEnd = originPos.getZ() - (settings.iPathWidth() / 2);
			break;
		case WEST: // Negative X
			xStart = originPos.getX();
			xEnd = originPos.getX() + iLength;
			zStart = originPos.getZ() - (settings.iPathWidth() / 2);
			zEnd = originPos.getZ() + ((int) ((settings.iPathWidth() / 2) - dDivision));
			break;
		default:
			break;
		}
		
		harvestArea = new AxisAlignedBB(
				xStart, yBottom, zStart,
				xEnd, yTop, zEnd);
	}
}
