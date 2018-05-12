package co.uk.duelmonster.minersadvantage.workers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import co.uk.duelmonster.minersadvantage.common.BreakBlockController;
import co.uk.duelmonster.minersadvantage.common.Functions;
import co.uk.duelmonster.minersadvantage.handlers.LumbinationHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockNewLeaf;
import net.minecraft.block.BlockNewLog;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockPlanks.EnumType;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class LumbinationAgent extends Agent {
	
	private LumbinationHandler oLumbinationHandler = new LumbinationHandler();
	
	private BlockPos				originLeafPos		= null;
	private Block					originLeafBlock		= null;
	private int						originLeafMeta		= 0;
	private BlockPlanks.EnumType	originLeafVariant	= null;
	private BlockPlanks.EnumType	originWoodVariant	= null;
	private AxisAlignedBB			trunkArea			= null;
	private List<BlockPos>			trunkPositions		= new ArrayList<BlockPos>();
	
	public LumbinationAgent(EntityPlayerMP player, NBTTagCompound tags) {
		super(player, tags, true);
		
		oLumbinationHandler.setPlayer(player);
		
		PropertyEnum<EnumType> variant = (originBlock instanceof BlockNewLog ? BlockNewLog.VARIANT : (originBlock instanceof BlockOldLog ? BlockOldLog.VARIANT : null));
		
		Object propValue = Functions.getPropertyValue(originState, variant);
		if (propValue != null)
			this.originWoodVariant = (BlockPlanks.EnumType) propValue;
		
		this.harvestArea = oLumbinationHandler.identifyTree(originPos, originBlock);
		this.trunkArea = oLumbinationHandler.trunkArea;
		this.trunkPositions = oLumbinationHandler.trunkPositions;
		this.originLeafPos = oLumbinationHandler.getLeafPos(originPos);
		
		if (this.originLeafPos != null) {
			IBlockState state = world.getBlockState(originLeafPos);
			this.originLeafBlock = state.getBlock();
			this.originLeafMeta = originLeafBlock.getMetaFromState(state);
			
			variant = (originLeafBlock instanceof BlockNewLeaf ? BlockNewLeaf.VARIANT : (originLeafBlock instanceof BlockOldLeaf ? BlockOldLeaf.VARIANT : null));
			
			propValue = Functions.getPropertyValue(state, variant);
			if (propValue != null)
				this.originLeafVariant = (BlockPlanks.EnumType) propValue;
			
			// Add the origin block the queue now that we have all the information. - this.queued.clear();
			addConnectedToQueue(originPos);
		}
	}
	
	// Returns true when Lumbination is complete or cancelled
	@Override
	public boolean tick() {
		if (originPos == null || player == null || !player.isEntityAlive() || processed.size() >= settings.iBlockLimit())
			return true;
		
		timer.start();
		
		boolean bIsComplete = false;
		
		for (int iQueueCount = 0; queued.size() > 0; iQueueCount++) {
			if ((settings.bBreakAtToolSpeeds() && iQueueCount > 0)
					|| iQueueCount >= settings.iBlocksPerTick()
					|| processed.size() >= settings.iBlockLimit()
					|| (!settings.bBreakAtToolSpeeds() && settings.tpsGuard() && timer.elapsed(TimeUnit.MILLISECONDS) > 40))
				break;
			
			if (Functions.IsPlayerStarving(player)) {
				bIsComplete = true;
				break;
			}
			
			BlockPos oPos = queued.remove(0);
			if (oPos == null || !Functions.isWithinArea(oPos, harvestArea))
				continue;
			
			IBlockState state = world.getBlockState(oPos);
			Block block = state.getBlock();
			SoundType soundtype = state.getBlock().getSoundType(state, world, oPos, null);
			
			world.captureBlockSnapshots = true;
			world.capturedBlockSnapshots.clear();
			
			// Process the current block if it is valid.
			if (!fakePlayer.canHarvestBlock(state) || (state.getMaterial() == Material.LEAVES && !settings.bDestroyLeaves())) {
				// Avoid the non-harvestable blocks.
				processed.add(oPos);
				continue;
			} else if (state.getMaterial() == Material.LEAVES && !settings.bLeavesAffectDurability()) {
				// Remove the Leaves without damaging the tool.
				if (block.removedByPlayer(state, world, oPos, fakePlayer, true)) {
					world.playSound(player, oPos, SoundEvents.BLOCK_GRASS_BREAK, SoundCategory.BLOCKS, 1.0F, 1.0F);
					
					block.onBlockDestroyedByPlayer(world, oPos, state);
					block.harvestBlock(world, fakePlayer, oPos, state, world.getTileEntity(oPos), heldItemStack.copy());
					
					reportProgessToClient(oPos, block, soundtype.getBreakSound());
				}
				
				processBlockSnapshots();
				addConnectedToQueue(oPos);
			} else {
				boolean bBlockHarvested = false;
				
				if (settings.bBreakAtToolSpeeds()) {
					this.breakController = new BreakBlockController(fakePlayer);
					
					breakController.onPlayerDamageBlock(oPos, sideHit);
					if (breakController.bBlockDestroyed)
						bBlockHarvested = HarvestBlock(oPos);
					
				} else
					bBlockHarvested = HarvestBlock(oPos);
				
				if (bBlockHarvested) {
					reportProgessToClient(oPos, block, soundtype.getBreakSound());
					processBlockSnapshots();
					addConnectedToQueue(oPos);
					
					processed.add(oPos);
				}
			}
		}
		
		timer.reset();
		
		return (bIsComplete || queued.isEmpty());
	}
	
	@Override
	public void addToQueue(BlockPos oPos) {
		IBlockState state = world.getBlockState(oPos);
		Block checkBlock = state.getBlock();
		int checkMeta = checkBlock.getMetaFromState(state);
		
		PropertyEnum<EnumType> variant = (checkBlock instanceof BlockNewLog ? BlockNewLog.VARIANT : (checkBlock instanceof BlockOldLog ? BlockOldLog.VARIANT : (checkBlock instanceof BlockNewLeaf ? BlockNewLeaf.VARIANT : (checkBlock instanceof BlockOldLeaf ? BlockOldLeaf.VARIANT : null))));
		
		Object checkProp = Functions.getPropertyValue(state, variant);
		
		if (checkBlock.getClass().isInstance(originBlock)
				&& (trunkArea == null || Functions.isWithinArea(oPos, trunkArea))
				&& (trunkPositions == null || Functions.isPosConnected(trunkPositions, oPos))
				&& ((originWoodVariant != null && checkProp != null && checkProp.equals(originWoodVariant)) || checkMeta == originMeta))
			super.addToQueue(oPos);
		
		if (checkBlock.getClass().isInstance(originLeafBlock)
				&& settings.bDestroyLeaves()
				&& (harvestArea == null || Functions.isWithinArea(oPos, harvestArea))
				&& ((originLeafVariant != null && checkProp != null && checkProp.equals(originLeafVariant)) || checkMeta == originLeafMeta))
			super.addToQueue(oPos);
	}
	
}
