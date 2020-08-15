package uk.co.duelmonster.minersadvantage.workers;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropsBlock;
import net.minecraft.block.IGrowable;
import net.minecraft.block.NetherWartBlock;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockNamedItem;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.IPlantable;
import uk.co.duelmonster.minersadvantage.common.Functions;
import uk.co.duelmonster.minersadvantage.helpers.FarmingHelper;
import uk.co.duelmonster.minersadvantage.network.packets.PacketCropinate;

public class CropinationAgent extends Agent {
	
	private int iHarvestedCount = 0;
	
	public CropinationAgent(ServerPlayerEntity player, final PacketCropinate pkt) {
		super(player, pkt);
		
		this.originPos = pkt.pos;
		
		// Removed to simplify harvesting crops. This may be an issue for a MASSIVE farm and should be replaced.
		this.interimArea = FarmingHelper.getCropPatch(world, originPos);
		
		addConnectedToQueue(originPos);
	}
	
	// Returns true when Cropination is complete or cancelled
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
			
			BlockState	state	= world.getBlockState(oPos);
			Block		block	= state.getBlock();
			
			if (isFullyGrown(state)) {
				
				BlockState newState = block.getDefaultState();
				
				// Get Block drops
				List<ItemStack> drops = Block.getDrops(state, (ServerWorld) world, oPos, (TileEntity) null, player, player.getHeldItemMainhand());
				
				// Identify the plantable drops and spawn the non-plantable drops
				List<ItemStack> plantables = Lists.newArrayList();
				for (ItemStack item : drops) {
					if (item.getItem() instanceof IPlantable)
						plantables.add(item);
					else
						Block.spawnAsEntity(world, oPos, item);
				}
				
				// Decrease the plantable drops by one to simulate re-planting
				boolean			bReplanted	= false;
				BlockNamedItem	oSeeds		= null;
				for (ItemStack plantable : plantables) {
					if (!bReplanted) {
						if (plantable.getItem() instanceof BlockNamedItem)
							oSeeds = (BlockNamedItem) plantable.getItem();
						
						plantable.setCount(plantable.getCount() - 1);
						bReplanted = true;
					}
					// Spawn drop if it's not a seed or harvestSeeds is enabled
					if (plantable.getCount() > 0 && (clientConfig.cropination.harvestSeeds || !(plantable.getItem() instanceof BlockNamedItem)))
						Block.spawnAsEntity(world, oPos, plantable);
				}
				
				// If seed harvesting is disabled and current crop requires seeds, check players
				// inventory for
				// matching seed stacks and decrease by one to simulate re-planting
				if (!clientConfig.cropination.harvestSeeds && oSeeds != null)
					decreaseSeedsInInventory(oSeeds);
				
				iHarvestedCount++;
				
				world.captureBlockSnapshots = true;
				world.capturedBlockSnapshots.clear();
				
				world.setBlockState(oPos, newState, 1 | 2 | 8);
				
				// Apply Item damage every 4 crops harvested. This makes item damage 1/4 per
				// crop
				if (iHarvestedCount > 0 && iHarvestedCount % 4 == 0
						&& heldItemStack != null && heldItemStack.isDamageable()) {
					heldItemStack.damageItem(1, player, null);
					
					if (heldItemStack.getMaxDamage() <= 0) {
						player.inventory.removeStackFromSlot(player.inventory.currentItem);
						player.openContainer.detectAndSendChanges();
					}
				}
				
				processBlockSnapshots();
				
				Functions.playSound(world, oPos, SoundEvents.ITEM_HOE_TILL, SoundCategory.PLAYERS, 1.0F, world.rand.nextFloat() + 0.5F);
			} else {
				// Minus one from the queue count so that the current position is not included
				// in the block per tick
				// limit
				iQueueCount--;
			}
			
			addConnectedToQueue(oPos);
			processed.add(oPos);
		}
		
		return (bIsComplete || queued.isEmpty());
	}
	
	private boolean isFullyGrown(BlockState state) {
		return (state.hasProperty(BlockStateProperties.AGE_0_1) && state.get(BlockStateProperties.AGE_0_1).equals(1)) ||
				(state.hasProperty(BlockStateProperties.AGE_0_2) && state.get(BlockStateProperties.AGE_0_2).equals(2)) ||
				(state.hasProperty(BlockStateProperties.AGE_0_3) && state.get(BlockStateProperties.AGE_0_3).equals(3)) ||
				(state.hasProperty(BlockStateProperties.AGE_0_5) && state.get(BlockStateProperties.AGE_0_5).equals(5)) ||
				(state.hasProperty(BlockStateProperties.AGE_0_7) && state.get(BlockStateProperties.AGE_0_7).equals(7)) ||
				(state.hasProperty(BlockStateProperties.AGE_0_15) && state.get(BlockStateProperties.AGE_0_15).equals(15)) ||
				(state.hasProperty(BlockStateProperties.AGE_0_25) && state.get(BlockStateProperties.AGE_0_25).equals(25));
	}
	
	@Override
	public void addToQueue(BlockPos oPos) {
		BlockState	state	= world.getBlockState(oPos);
		Block		block	= state.getBlock();
		
		if ((block instanceof IGrowable && block instanceof IPlantable) || block instanceof CropsBlock || block instanceof NetherWartBlock)
			super.addToQueue(oPos);
	}
	
	private void decreaseSeedsInInventory(BlockNamedItem oSeeds) {
		ItemStack stack = null;
		
		// Locate the players seeds in the main inventory
		for (int i = 0; i < player.inventory.mainInventory.size(); i++) {
			stack = player.inventory.mainInventory.get(i);
			if (stack != null && stack.getItem().equals(oSeeds))
				break;
		}
		
		if (stack == null) {
			// Seeds not found in the main inventory check the off hand inventory
			for (int i = 0; i < player.inventory.offHandInventory.size(); i++) {
				stack = player.inventory.offHandInventory.get(i);
				if (stack != null && stack.getItem().equals(oSeeds))
					break;
			}
		}
		
		// If seeds were found then decrease the size of the stack by one
		if (stack != null && stack.getCount() > 0)
			player.inventory.decrStackSize(Functions.getSlotFromInventory(player, stack), 1);
		
	}
}
