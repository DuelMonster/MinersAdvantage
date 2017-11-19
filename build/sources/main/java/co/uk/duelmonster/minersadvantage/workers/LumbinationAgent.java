package co.uk.duelmonster.minersadvantage.workers;

import java.util.concurrent.TimeUnit;

import co.uk.duelmonster.minersadvantage.common.Functions;
import co.uk.duelmonster.minersadvantage.handlers.LumbinationHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class LumbinationAgent extends Agent {
	
	private BlockPos				originLeafPos		= null;
	private Block					originLeafBlock		= null;
	private int						originLeafMeta		= 0;
	private BlockPlanks.EnumType	originLeafVariant	= null;
	
	public LumbinationAgent(EntityPlayerMP player, NBTTagCompound tags) {
		super(player, tags);
		
		this.harvestArea = LumbinationHandler.instance.identifyTree(originPos, originBlock);
		this.originLeafPos = LumbinationHandler.instance.getLeafPos(originPos);
		
		IBlockState state = world.getBlockState(originLeafPos);
		this.originLeafBlock = state.getBlock();
		this.originLeafMeta = originLeafBlock.getMetaFromState(state);
		
		Object propValue = Functions.getPropertyValue(state, "variant");
		if (propValue != null)
			this.originLeafVariant = (BlockPlanks.EnumType) propValue;
	}
	
	// Returns true when Excavation is complete or cancelled
	@Override
	public boolean tick() {
		if (originPos == null || player == null || !player.isEntityAlive() || processed.size() >= settings.iBlockLimit)
			return true;
		
		timer.start();
		
		boolean bIsComplete = false;
		
		for (int iQueueCount = 0; queued.size() > 0; iQueueCount++) {
			if (iQueueCount >= settings.iBreakSpeed
					|| processed.size() >= settings.iBlockLimit
					|| (settings.tpsGuard && timer.elapsed(TimeUnit.MILLISECONDS) > 40))
				break;
			
			if (heldItem != Functions.getHeldItem(player) || Functions.IsPlayerStarving(player)) {
				bIsComplete = true;
				break;
			}
			
			BlockPos oPos = queued.remove(0);
			if (oPos == null || !Functions.isWithinArea(oPos, harvestArea))
				continue;
			
			IBlockState state = world.getBlockState(oPos);
			
			world.captureBlockSnapshots = true;
			world.capturedBlockSnapshots.clear();
			
			// Process the current block if it is valid.
			if (!player.canHarvestBlock(state)) {
				// Add the non-harvestable blocks to the processed list so that they can be avoided.
				processed.add(oPos);
				continue;
			} else if (player.interactionManager.tryHarvestBlock(oPos)) {
				spawnProgressParticle(oPos);
				processBlockSnapshots();
				addConnectedToQueue(oPos);
			}
			
			processed.add(oPos);
		}
		
		timer.reset();
		
		return (bIsComplete || queued.isEmpty());
	}
	
	@Override
	public void addConnectedToQueue(BlockPos oPos) {
		for (int xOffset = -1; xOffset <= 1; xOffset++)
			for (int yOffset = -1; yOffset <= 1; yOffset++)
				for (int zOffset = -1; zOffset <= 1; zOffset++) {
					BlockPos toQueuePos = oPos.add(xOffset, yOffset, zOffset);
					IBlockState state = world.getBlockState(toQueuePos);
					Block checkBlock = state.getBlock();
					int checkMeta = checkBlock.getMetaFromState(state);
					Object checkProp = Functions.getPropertyValue(state, "variant");
					
					if (checkBlock.equals(originBlock) // && checkMeta == originMeta)
							|| (checkBlock.equals(originLeafBlock)
									&& (checkMeta == originLeafMeta
											|| (checkProp != null && checkProp.equals(originLeafVariant)))))
						addToQueue(toQueuePos);
				}
	}
	
}
