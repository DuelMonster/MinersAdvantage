package co.uk.duelmonster.minersadvantage.workers;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.ArrayUtils;

import co.uk.duelmonster.minersadvantage.common.BreakBlockController;
import co.uk.duelmonster.minersadvantage.common.Functions;
import co.uk.duelmonster.minersadvantage.common.PacketID;
import co.uk.duelmonster.minersadvantage.common.Variables;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class ExcavationAgent extends Agent {
	
	private boolean bIsSingleLayerToggled = false;
	
	public ExcavationAgent(EntityPlayerMP player, NBTTagCompound tags) {
		super(player, tags, true);
		
		this.bIsSingleLayerToggled = Variables.get(player.getUniqueID()).IsSingleLayerToggled;
		
		// Add the origin block the queue now that we have all the information.
		addConnectedToQueue(originPos);
	}
	
	// Returns true when Excavation is complete or cancelled
	@Override
	public boolean tick() {
		if (originPos == null || player == null || !player.isEntityAlive() || processed.size() >= settings.common.iBlockLimit())
			return true;
		
		timer.start();
		
		boolean bIsComplete = false;
		
		for (int iQueueCount = 0; queued.size() > 0; iQueueCount++) {
			if ((settings.common.bBreakAtToolSpeeds() && iQueueCount > 0)
					|| iQueueCount >= settings.common.iBlocksPerTick()
					|| processed.size() >= settings.common.iBlockLimit()
					|| (!settings.common.bBreakAtToolSpeeds() && settings.common.tpsGuard() && timer.elapsed(TimeUnit.MILLISECONDS) > 40))
				break;
			
			if (Functions.IsPlayerStarving(player)) {
				bIsComplete = true;
				break;
			}
			
			BlockPos oPos = queued.remove(0);
			if (oPos == null)
				continue;
			
			IBlockState state = world.getBlockState(oPos);
			Block block = state.getBlock();
			
			if (!fakePlayer().canHarvestBlock(state)) {
				// Avoid the non-harvestable blocks.
				processed.add(oPos);
				continue;
			}
			
			int meta = block.getMetaFromState(state);
			
			// Process the current block if it is valid.
			if (packetID != PacketID.Veinate && settings.common.bMineVeins() && ArrayUtils.contains(settings.veination.ores(), block.getRegistryName().toString().trim())) {
				processed.add(oPos);
				excavateOreVein(state, oPos);
			} else if ((block == originBlock && (settings.excavation.bIgnoreBlockVariants() || originMeta == meta))
					|| (isRedStone && (block == Blocks.REDSTONE_ORE || block == Blocks.LIT_REDSTONE_ORE))) {
				
				world.captureBlockSnapshots = true;
				world.capturedBlockSnapshots.clear();
				
				boolean bBlockHarvested = false;
				
				if (settings.common.bBreakAtToolSpeeds()) {
					this.breakController = new BreakBlockController(fakePlayer());
					
					breakController.onPlayerDamageBlock(oPos, sideHit);
					if (breakController.bBlockDestroyed)
						bBlockHarvested = HarvestBlock(oPos);
					
				} else
					bBlockHarvested = HarvestBlock(oPos);
				
				if (bBlockHarvested) {
					processBlockSnapshots();
					
					SoundType soundtype = block.getSoundType(state, world, oPos, null);
					reportProgessToClient(oPos, soundtype.getBreakSound());
					
					autoIlluminate(oPos);
					addConnectedToQueue(oPos);
					
					processed.add(oPos);
				}
			}
		}
		
		timer.reset();
		
		return (bIsComplete || queued.isEmpty());
	}
	
	@Override
	public void addConnectedToQueue(BlockPos oPos) {
		int xStart = -1, yStart = -1, zStart = -1;
		int xEnd = 1, yEnd = 1, zEnd = 1;
		
		if (packetID == PacketID.Excavate && (oPos.getX() == originPos.getX() || oPos.getY() == originPos.getY() || oPos.getZ() == originPos.getZ()))
			switch (sideHit.getOpposite()) {
			case SOUTH: // Positive Z
				zStart = 0;
				break;
			case NORTH: // Negative Z
				zEnd = 0;
				break;
			case EAST: // Positive X
				xStart = 0;
				break;
			case WEST: // Negative X
				xEnd = 0;
				break;
			case UP: // Positive Y
				yStart = 0;
				break;
			case DOWN: // Negative Y
				yEnd = 0;
				break;
			default:
				break;
			}
		
		if (bIsSingleLayerToggled) {
			yStart = 0;
			yEnd = 0;
		}
		
		for (int xOffset = xStart; xOffset <= xEnd; xOffset++)
			for (int yOffset = yStart; yOffset <= yEnd; yOffset++)
				for (int zOffset = zStart; zOffset <= zEnd; zOffset++)
					addToQueue(oPos.add(xOffset, yOffset, zOffset));
	}
	
	@Override
	public void addToQueue(BlockPos oPos) {
		IBlockState state = world.getBlockState(oPos);
		
		if (fakePlayer().canHarvestBlock(state)) {
			Block block = state.getBlock();
			int meta = block.getMetaFromState(state);
			
			if ((settings.common.bMineVeins() && ArrayUtils.contains(settings.veination.ores(), block.getRegistryName().toString().trim()))
					|| (block == originBlock && (settings.excavation.bIgnoreBlockVariants() || originMeta == meta))
					|| (isRedStone && (block == Blocks.REDSTONE_ORE || block == Blocks.LIT_REDSTONE_ORE))) {
				super.addToQueue(oPos);
			}
		}
	}
}
