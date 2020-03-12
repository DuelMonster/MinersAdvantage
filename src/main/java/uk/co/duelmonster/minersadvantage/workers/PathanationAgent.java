package uk.co.duelmonster.minersadvantage.workers;

import java.util.concurrent.TimeUnit;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import uk.co.duelmonster.minersadvantage.common.Constants;
import uk.co.duelmonster.minersadvantage.common.Functions;
import uk.co.duelmonster.minersadvantage.network.packets.PacketPathanate;

public class PathanationAgent extends Agent {
	
	public PathanationAgent(ServerPlayerEntity player, PacketPathanate pkt) {
		super(player, pkt);
		
		this.originPos = pkt.pos;
		
		setupPath();
		
		addConnectedToQueue(originPos);
	}
	
	// Returns true when Pathanation is complete or cancelled
	@Override
	public boolean tick() {
		if (originPos == null || player == null || !player.isAlive() || processed.size() >= clientConfig.common.blockLimit)
			return true;
		
		boolean bIsComplete = false;
		
		for (int iQueueCount = 0; queued.size() > 0; iQueueCount++) {
			if (iQueueCount >= clientConfig.common.blocksPerTick
					|| processed.size() >= clientConfig.common.blockLimit
					|| (clientConfig.common.tpsGuard && timer.elapsed(TimeUnit.MILLISECONDS) > 40))
				break;
			
			if (Functions.IsPlayerStarving(player)) {
				bIsComplete = true;
				break;
			}
			
			BlockPos oPos = queued.remove(0);
			if (oPos == null)
				continue;
			
			BlockState state = world.getBlockState(oPos);
			
			if (!Constants.DIRT_BLOCKS.contains(state.getBlock())) {
				// Add the non-harvestable blocks to the processed list so that they can be avoided.
				processed.add(oPos);
				continue;
			}
			
			world.captureBlockSnapshots = true;
			world.capturedBlockSnapshots.clear();
			
			if (world.isAirBlock(oPos.up()) || world.getBlockState(oPos.up()).getMaterial().isReplaceable()) {
				world.playSound(player, oPos, SoundEvents.ITEM_SHOVEL_FLATTEN, SoundCategory.BLOCKS, 1.0F, 1.0F);
				
				if (!world.isAirBlock(oPos.up())) {
					world.setBlockState(oPos.up(), Blocks.AIR.getDefaultState());
				}
				
				world.setBlockState(oPos, Blocks.GRASS_PATH.getDefaultState(), 11);
				
				if (heldItemStack != null && heldItemStack.isDamageable()) {
					heldItemStack.damageItem(1, player, null);
					
					if (heldItemStack.getMaxDamage() <= 0) {
						player.inventory.removeStackFromSlot(player.inventory.currentItem);
						player.openContainer.detectAndSendChanges();
					}
				}
				
				SoundType soundtype = state.getSoundType(world, oPos, null);
				reportProgessToClient(oPos, soundtype.getHitSound());
				
				processBlockSnapshots();
				addConnectedToQueue(oPos);
			}
			
			processed.add(oPos);
		}
		
		return (bIsComplete || queued.isEmpty());
	}
	
	@Override
	public void addToQueue(BlockPos oPos) {
		if (Constants.DIRT_BLOCKS.contains(world.getBlockState(oPos).getBlock()))
			super.addToQueue(oPos);
	}
	
	private void setupPath() {
		// Shaft area info
		int xStart = 0;
		int xEnd = 0;
		int yBottom = originPos.getY() - 1;
		int yTop = originPos.getY() + 1;
		int zStart = 0;
		int zEnd = 0;
		
		int iLength = clientConfig.pathanation.pathLength - 1;
		
		// if the ShaftWidth is divisible by 2 we don't want to do anything
		double dDivision = ((clientConfig.pathanation.pathWidth & 1) != 0 ? 0 : 1);// 0.5);
		
		Direction direction = player.getAdjustedHorizontalFacing().getOpposite();
		
		switch (direction) {
		case SOUTH: // Positive Z
			xStart = originPos.getX() + ((int) ((clientConfig.pathanation.pathWidth / 2) - dDivision));
			xEnd = originPos.getX() - (clientConfig.pathanation.pathWidth / 2);
			zStart = originPos.getZ();
			zEnd = originPos.getZ() - iLength;
			break;
		case NORTH: // Negative Z
			xStart = originPos.getX() - (clientConfig.pathanation.pathWidth / 2);
			xEnd = originPos.getX() + ((int) ((clientConfig.pathanation.pathWidth / 2) - dDivision));
			zStart = originPos.getZ();
			zEnd = originPos.getZ() + iLength;
			break;
		case EAST: // Positive X
			xStart = originPos.getX();
			xEnd = originPos.getX() - iLength;
			zStart = originPos.getZ() + ((int) ((clientConfig.pathanation.pathWidth / 2) - dDivision));
			zEnd = originPos.getZ() - (clientConfig.pathanation.pathWidth / 2);
			break;
		case WEST: // Negative X
			xStart = originPos.getX();
			xEnd = originPos.getX() + iLength;
			zStart = originPos.getZ() - (clientConfig.pathanation.pathWidth / 2);
			zEnd = originPos.getZ() + ((int) ((clientConfig.pathanation.pathWidth / 2) - dDivision));
			break;
		default:
			break;
		}
		
		interimArea = new AxisAlignedBB(
				xStart, yBottom, zStart,
				xEnd, yTop, zEnd);
	}
}
