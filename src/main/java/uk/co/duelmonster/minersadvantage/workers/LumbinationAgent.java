package uk.co.duelmonster.minersadvantage.workers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import uk.co.duelmonster.minersadvantage.common.Constants;
import uk.co.duelmonster.minersadvantage.common.Functions;
import uk.co.duelmonster.minersadvantage.helpers.BreakBlockController;
import uk.co.duelmonster.minersadvantage.helpers.LumbinationHelper;
import uk.co.duelmonster.minersadvantage.network.packets.PacketLumbinate;

public class LumbinationAgent extends Agent {
	
	private LumbinationHelper lumbinationHelper = new LumbinationHelper();
	
	private BlockPos		originLeafPos	= null;
	private BlockState		originLeafState	= null;
	private Block			originLeafBlock	= null;
	private AxisAlignedBB	trunkArea		= null;
	private List<BlockPos>	trunkPositions	= new ArrayList<BlockPos>();
	
	public LumbinationAgent(ServerPlayerEntity player, PacketLumbinate pkt) {
		super(player, pkt);
		
		this.originPos = pkt.pos;
		this.faceHit = pkt.faceHit;
		this.originState = Block.getStateById(pkt.stateID);
		
		this.originBlock = this.originState.getBlock();
		
		lumbinationHelper.setPlayer(player);
		
		this.harvestArea = lumbinationHelper.identifyTree(originPos, originState);
		this.trunkArea = lumbinationHelper.trunkArea;
		this.trunkPositions = lumbinationHelper.trunkPositions;
		this.originLeafPos = lumbinationHelper.getLeafPos(originPos);
		
		if (this.originLeafPos != null) {
			this.originLeafState = world.getBlockState(originLeafPos);
			this.originLeafBlock = originLeafState.getBlock();
			
			// Add the origin block the queue now that we have all the information. - this.queued.clear();
			addConnectedToQueue(originPos);
		}
	}
	
	// Returns true when Lumbination is complete or cancelled
	@Override
	public boolean tick() {
		if (originPos == null || player == null || !player.isAlive() || processed.size() >= clientConfig.common.blockLimit)
			return true;
		
		boolean bCancelHarvest = false;
		
		for (int iQueueCount = 0; queued.size() > 0; iQueueCount++) {
			if ((clientConfig.common.breakAtToolSpeeds && iQueueCount > 0)
					|| iQueueCount >= clientConfig.common.blocksPerTick
					|| processed.size() >= clientConfig.common.blockLimit
					|| (!clientConfig.common.breakAtToolSpeeds && clientConfig.common.tpsGuard && timer.elapsed(TimeUnit.MILLISECONDS) > 40))
				break;
			
			if (Functions.IsPlayerStarving(player)) {
				bCancelHarvest = true;
				break;
			}
			
			BlockPos oPos = queued.remove(0);
			if (oPos == null || !Functions.isWithinArea(oPos, harvestArea))
				continue;
			
			BlockState state = world.getBlockState(oPos);
			Block block = state.getBlock();
			SoundType soundtype = state.getBlock().getSoundType(state, world, oPos, null);
			
			world.captureBlockSnapshots = true;
			world.capturedBlockSnapshots.clear();
			
			boolean isLeaves = state.getMaterial() == Material.LEAVES;
			boolean isWood = state.getMaterial() == Material.WOOD;
			
			// Process the current block if it is valid.
			if (!fakePlayer().canHarvestBlock(state) || (isLeaves && !clientConfig.lumbination.destroyLeaves)) {
				// Avoid the non-harvestable blocks.
				processed.add(oPos);
				continue;
			} else if (isLeaves && clientConfig.lumbination.useShearsOnLeaves && lumbinationHelper.playerHasShears()) {
				// Harvest the leaves using the players shears
				
				// Set the Fake Players held item to the players shears
				this.fakePlayer().setHeldItem(Hand.MAIN_HAND, lumbinationHelper.getPlayersShears());
				
				HarvestBlock(oPos);
				reportProgessToClient(oPos, soundtype.getBreakSound());
				processBlockSnapshots();
				addConnectedToQueue(oPos);
				
				processed.add(oPos);
				
				// Reset the Fake Players held item back to the players initial held item
				this.fakePlayer().setHeldItem(Hand.MAIN_HAND, this.heldItemStack);
				
			} else if (isLeaves && !clientConfig.lumbination.leavesAffectDurability) {
				// Remove the Leaves without damaging the tool.
				if (state.removedByPlayer(world, oPos, fakePlayer(), true, null)) {
					world.playSound(player, oPos, SoundEvents.BLOCK_GRASS_BREAK, SoundCategory.BLOCKS, 1.0F, 1.0F);
					
					block.onPlayerDestroy(world, oPos, state);
					try {
						block.harvestBlock(world,
								fakePlayer(),
								oPos,
								state,
								world.getTileEntity(oPos),
								(heldItemStack != null ? heldItemStack.copy() : null));
					}
					catch (Exception ex) {
						Constants.LOGGER.error(ex.getClass().getName() + " Exception: " + Functions.getStackTrace());
					}
					
					reportProgessToClient(oPos, soundtype.getBreakSound());
				}
				
				processBlockSnapshots();
				addConnectedToQueue(oPos);
				
				processed.add(oPos);
				
			} else if (isWood) {
				
				boolean bBlockHarvested = false;
				
				if (clientConfig.common.breakAtToolSpeeds) {
					this.breakController = new BreakBlockController(fakePlayer());
					
					breakController.onPlayerDamageBlock(oPos, faceHit);
					if (breakController.bBlockDestroyed)
						bBlockHarvested = HarvestBlock(oPos);
					
				} else
					bBlockHarvested = HarvestBlock(oPos);
				
				if (bBlockHarvested) {
					reportProgessToClient(oPos, soundtype.getBreakSound());
					processBlockSnapshots();
					addConnectedToQueue(oPos);
					
					processed.add(oPos);
				}
			}
		}
		
		// If the queue is empty then the tree has been harvested, so lets re-plant the sapling(s) if enabled
		if (queued.isEmpty() && originLeafPos != null && clientConfig.lumbination.replantSaplings)
			lumbinationHelper.replantSaplings(dropsHistory, originLeafState, originLeafPos);
		
		return (bCancelHarvest || queued.isEmpty());
	}
	
	@Override
	public void addToQueue(BlockPos oPos) {
		BlockState state = world.getBlockState(oPos);
		Block checkBlock = state.getBlock();
		
		if (checkBlock.getClass().isInstance(originBlock)
				&& (trunkArea == null || Functions.isWithinArea(oPos, trunkArea))
				&& (trunkPositions == null || Functions.isPosConnected(trunkPositions, oPos)))
			super.addToQueue(oPos);
		
		if (checkBlock.getClass().isInstance(originLeafBlock)
				&& clientConfig.lumbination.destroyLeaves
				&& (harvestArea == null || Functions.isWithinArea(oPos, harvestArea)))
			super.addToQueue(oPos);
	}
	
}
