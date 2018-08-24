package co.uk.duelmonster.minersadvantage.workers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import co.uk.duelmonster.minersadvantage.common.Functions;
import co.uk.duelmonster.minersadvantage.common.PacketID;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.BlockNetherWart;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemSeeds;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.IPlantable;

public class CropinationAgent extends Agent {
	
	private int iHarvestedCount = 0;
	
	public CropinationAgent(EntityPlayerMP player, NBTTagCompound tags) {
		super(player, tags, false);
		
		if (this.packetID == PacketID.TileFarmland) {
			detectFarmableLandArea();
			
			// Reset the Queue now that we have the harvest area.
			this.queued.clear();
			addConnectedToQueue(originPos);
		}
	}
	
	// Returns true when Cropination is complete or cancelled
	@Override
	public boolean tick() {
		if (originPos == null || player == null || !player.isEntityAlive() || processed.size() >= settings.common.iBlockLimit())
			return true;
		
		timer.start();
		
		boolean bIsComplete = false;
		
		for (int iQueueCount = 0; queued.size() > 0; iQueueCount++) {
			if (iQueueCount >= settings.common.iBlocksPerTick()
					|| processed.size() >= settings.common.iBlockLimit()
					|| (settings.common.tpsGuard() && timer.elapsed(TimeUnit.MILLISECONDS) > 40))
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
			
			if (this.packetID == PacketID.TileFarmland) {
				if (!(block instanceof BlockDirt || block instanceof BlockGrass)) {
					// Add the non-harvestable blocks to the processed list so that they can be avoided.
					processed.add(oPos);
					iQueueCount--;
					continue;
				}
				
				world.captureBlockSnapshots = true;
				world.capturedBlockSnapshots.clear();
				
				if (world.isAirBlock(oPos.up())) {
					Functions.playSound(world, oPos, SoundEvents.ITEM_HOE_TILL, SoundCategory.PLAYERS, 1.0F, world.rand.nextFloat() + 0.5F);
					
					world.setBlockState(oPos, Blocks.FARMLAND.getDefaultState().withProperty(BlockFarmland.MOISTURE, Integer.valueOf(7)), 11);
					
					if (heldItemStack != null && heldItemStack.isItemStackDamageable()) {
						heldItemStack.damageItem(1, player);
						
						if (heldItemStack.getMaxDamage() <= 0) {
							player.inventory.removeStackFromSlot(player.inventory.currentItem);
							player.openContainer.detectAndSendChanges();
						}
					}
					
					processBlockSnapshots();
					addConnectedToQueue(oPos);
				}
			} else if (this.packetID == PacketID.HarvestCrops) {
				boolean isFullyGrown = false;
				
				if (block instanceof BlockCrops)
					isFullyGrown = ((BlockCrops) block).isMaxAge(state);
				
				else if (block instanceof BlockNetherWart)
					isFullyGrown = (block.getMetaFromState(state) == 3);
				
				else if (block instanceof IGrowable || block instanceof IPlantable)
					isFullyGrown = (block.getMetaFromState(state) >= 6);
				
				if (isFullyGrown) {
					
					IBlockState newState = block.getDefaultState();
					
					int fortune = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, heldItemStack);
					
					// Get Block drops
					List<ItemStack> drops = block.getDrops(world, oPos, state, fortune);
					
					// Identify the plantable drops and spawn the non-plantable drops
					List<ItemStack> plantables = new ArrayList<ItemStack>();
					for (ItemStack item : drops) {
						if (item.getItem() instanceof IPlantable)
							plantables.add(item);
						else
							Block.spawnAsEntity(world, oPos, item);
					}
					
					// Decrease the plantable drops by one to simulate replanting
					boolean bReplanted = false;
					ItemSeeds oSeeds = null;
					for (ItemStack plantable : plantables) {
						if (!bReplanted) {
							if (plantable.getItem() instanceof ItemSeeds)
								oSeeds = (ItemSeeds) plantable.getItem();
							
							plantable.setCount(plantable.getCount() - 1);
							bReplanted = true;
						}
						if (plantable.getCount() > 0 && (settings.cropination.bHarvestSeeds() || !(plantable.getItem() instanceof ItemSeeds)))
							Block.spawnAsEntity(world, oPos, plantable);
					}
					
					// If seed harvesting is disabled and current crop requires seeds, check players inventory for
					// matching seed stacks and decrease one to simulate replanting
					if (!settings.cropination.bHarvestSeeds() && oSeeds != null)
						decreaseSeedsInInventory(oSeeds);
					
					iHarvestedCount++;
					
					world.captureBlockSnapshots = true;
					world.capturedBlockSnapshots.clear();
					
					world.setBlockState(oPos, newState, 11);
					
					// Apply Item damage every 4 crops harvested. This makes item damage 1/4 per crop
					if (iHarvestedCount > 0 && iHarvestedCount % 4 == 0
							&& heldItemStack != null && heldItemStack.isItemStackDamageable()) {
						heldItemStack.damageItem(1, player);
						
						if (heldItemStack.getMaxDamage() <= 0) {
							player.inventory.removeStackFromSlot(player.inventory.currentItem);
							player.openContainer.detectAndSendChanges();
						}
					}
					
					processBlockSnapshots();
					
					Functions.playSound(world, oPos, SoundEvents.ITEM_HOE_TILL, SoundCategory.PLAYERS, 1.0F, world.rand.nextFloat() + 0.5F);
				} else {
					// Minus one from the queue count so that the current position is not included in the block per tick
					// limit
					iQueueCount--;
				}
				addConnectedToQueue(oPos);
			}
			
			processed.add(oPos);
		}
		
		timer.reset();
		
		return (bIsComplete || queued.isEmpty());
	}
	
	private void detectFarmableLandArea() {
		BlockPos waterSource = getWaterSource();
		if (waterSource != null)
			harvestArea = new AxisAlignedBB(
					waterSource.getX() - 4, waterSource.getY(), waterSource.getZ() - 4,
					waterSource.getX() + 4, waterSource.getY(), waterSource.getZ() + 4);
		else
			harvestArea = new AxisAlignedBB(originPos, originPos);
	}
	
	private BlockPos getWaterSource() {
		// for (int yOffset = originPos.getY() - 1; yOffset <= originPos.getY() + 1; ++yOffset)
		for (int xOffset = originPos.getX() - 4; xOffset <= originPos.getX() + 4; ++xOffset)
			for (int zOffset = originPos.getZ() - 4; zOffset <= originPos.getZ() + 4; ++zOffset) {
				BlockPos oPos = new BlockPos(xOffset, originPos.getY(), zOffset);
				IBlockState state = world.getBlockState(oPos);
				if (state.getMaterial() == Material.WATER)
					return oPos;
			}
		
		return null;
	}
	
	@Override
	public void addToQueue(BlockPos oPos) {
		IBlockState state = world.getBlockState(oPos);
		Block block = state.getBlock();
		
		if (this.packetID == PacketID.TileFarmland && (block instanceof BlockDirt || block instanceof BlockGrass))
			super.addToQueue(oPos);
		
		else if (this.packetID == PacketID.HarvestCrops && (block instanceof IGrowable || block instanceof IPlantable || block instanceof BlockCrops || block instanceof BlockNetherWart))
			super.addToQueue(oPos);
	}
	
	private void decreaseSeedsInInventory(ItemSeeds oSeeds) {
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
