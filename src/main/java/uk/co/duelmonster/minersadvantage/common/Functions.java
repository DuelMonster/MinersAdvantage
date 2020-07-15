package uk.co.duelmonster.minersadvantage.common;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.Tags;
import uk.co.duelmonster.minersadvantage.config.MAConfig;
import uk.co.duelmonster.minersadvantage.config.SyncedClientConfig;

public class Functions {
	
	public static String localize(String key) {
		return (new TranslationTextComponent(key)).getString();
	}
	
	public static boolean isDebug() {
		return java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().stream().anyMatch(s -> s.contains("jdwp"));
	}
	
	public static void DebugNotifyClient(PlayerEntity player, String sMsg) {
		if (isDebug()) {
			NotifyClient(player, sMsg);
		}
	}
	
	public static void DebugNotifyClient(PlayerEntity player, boolean bIsOn, String sFeatureName) {
		if (isDebug()) {
			NotifyClient(player, bIsOn, sFeatureName);
		}
	}
	
	public static void NotifyClient(PlayerEntity player, String sMsg) {
		player.sendStatusMessage(new StringTextComponent(Constants.MOD_NAME_MSG + sMsg), false);
	}
	
	public static void NotifyClient(PlayerEntity player, boolean bIsOn, String sFeatureName) {
		player.sendStatusMessage(new StringTextComponent(Constants.MOD_NAME_MSG + TextFormatting.GOLD + sFeatureName + " " + (bIsOn ? TextFormatting.GREEN + "ON" : TextFormatting.RED + "OFF")), false);
	}
	
	public static ItemStack getHeldItemStack(PlayerEntity player) {
		ItemStack heldItem = player.getHeldItem(Hand.MAIN_HAND);
		return (heldItem == null || heldItem.isEmpty()) ? null : heldItem;
	}
	
	public static Item getHeldItem(PlayerEntity player) {
		ItemStack heldItem = getHeldItemStack(player);
		return heldItem == null ? null : heldItem.getItem();
	}
	
	public static boolean IsPlayerStarving(PlayerEntity player) {
		if (!Variables.get(player.getUniqueID()).HungerNotified && player.getFoodStats().getFoodLevel() <= Constants.MIN_HUNGER) {
			NotifyClient(player, TextFormatting.RED + localize("minersadvantage.hungery") + Constants.MOD_NAME);
			Variables.get(player.getUniqueID()).HungerNotified = true;
		}
		
		return Variables.get(player.getUniqueID()).HungerNotified;
	}
	
	public static Direction getPlayerFacing(PlayerEntity player) {
		Vector3d lookVec = player.getLookVec();
		return Direction.getFacingFromVector((float) lookVec.x, (float) lookVec.y, (float) lookVec.z);
	}
	
	public static String getStackTrace() {
		String				sRtrn	= "";
		StackTraceElement[]	stes	= Thread.currentThread().getStackTrace();
		
		for (int i = 2; i < stes.length; i++)
			sRtrn += System.getProperty("line.separator") + "	at " + stes[i].toString();
		
		return sRtrn;
	}
	
	public static boolean isWithinRange(BlockPos sourcePos, BlockPos targetPos, int range) {
		int	distanceX	= sourcePos.getX() - targetPos.getX();
		int	distanceY	= sourcePos.getY() - targetPos.getY();
		int	distanceZ	= sourcePos.getZ() - targetPos.getZ();
		
		return ((distanceX * distanceX) + (distanceY * distanceY) + (distanceZ * distanceZ)) <= (range * range);
	}
	
	public static boolean isWithinArea(BlockPos pos, AxisAlignedBB area) {
		return area != null &&
				pos.getX() >= area.minX && pos.getX() <= area.maxX &&
				pos.getY() >= area.minY && pos.getY() <= area.maxY &&
				pos.getZ() >= area.minZ && pos.getZ() <= area.maxZ;
	}
	
	public static boolean isWithinArea(Entity entity, AxisAlignedBB area) {
		return area != null &&
				entity.getPosX() >= area.minX && entity.getPosX() <= area.maxX &&
				entity.getPosY() >= area.minY && entity.getPosY() <= area.maxY &&
				entity.getPosZ() >= area.minZ && entity.getPosZ() <= area.maxZ;
	}
	
	public static List<BlockPos> getAllPositionsInArea(AxisAlignedBB area) {
		List<BlockPos> positions = new ArrayList<BlockPos>();
		
		for (double y = area.minY; y <= area.maxY; y++)
			for (double x = area.minX; x <= area.maxX; x++)
				for (double z = area.minZ; z <= area.maxZ; z++)
					positions.add(new BlockPos(x, y, z));
				
		return positions;
	}
	
	public static boolean isPosConnected(List<BlockPos> posList, BlockPos checkPos) {
		// Ensure we have a direct connection to the current Tree.
		for (int yOffset = -1; yOffset <= 1; yOffset++)
			for (int xOffset = -1; xOffset <= 1; xOffset++)
				for (int zOffset = -1; zOffset <= 1; zOffset++) {
					BlockPos oConPos = checkPos.add(xOffset, yOffset, zOffset);
					
					if (posList.contains(oConPos))
						return true;
				}
			
		return false;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List<Entity> getNearbyEntities(World world, AxisAlignedBB area) {
		List<Entity> rtrn = new ArrayList();
		try {
			List<?> list = new ArrayList(world.getEntitiesWithinAABB(Entity.class, area));
			if (null == list || list.isEmpty())
				return null;
			
			for (Object o : list) {
				if (o != null) {
					Entity e = (Entity) o;
					
					if (e.isAlive() && (e instanceof ItemEntity || e instanceof ExperienceOrbEntity))
						// && isEntityWithinArea(e, area))
						rtrn.add(e);
				}
			}
			return rtrn;
			
		}
		catch (ConcurrentModificationException e) {
			Constants.LOGGER.error("ConcurrentModification Exception Avoided...");
		}
		catch (IllegalStateException e) {
			Constants.LOGGER.error("IllegalStateException Exception Avoided...");
		}
		catch (Exception ex) {
			Constants.LOGGER.error(ex.getClass().getName() + " Exception: " + getStackTrace());
		}
		return rtrn;
	}
	
	public static void playSound(World world, BlockPos pos, SoundEvent sound, SoundCategory soundCategory, float volume, float pitch) {
		playSound(world, null, pos, sound, soundCategory, volume, pitch);
	}
	
	public static void playSound(World world, PlayerEntity player, BlockPos pos, SoundEvent sound, SoundCategory soundCategory, float volume, float pitch) {
		world.playSound(player, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, sound, soundCategory, volume, pitch);
	}
	
	public static void spawnAreaEffectCloud(World world, PlayerEntity player, BlockPos pos) {
		if (!MAConfig.CLIENT.disableParticleEffects()) {
			AreaEffectCloudEntity effectCloud = new AreaEffectCloudEntity(world, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
			effectCloud.setOwner(player);
			effectCloud.setRadius(1.0F);
			effectCloud.setRadiusOnUse(-0.5F);
			effectCloud.setWaitTime(1);
			effectCloud.setDuration(20);
			effectCloud.setRadiusPerTick(-effectCloud.getRadius() / effectCloud.getDuration());
			effectCloud.addEffect(new EffectInstance(Effects.WEAKNESS, 10));
			
			world.addEntity(effectCloud);
		}
	}
	
	/**
	 * Finds the stack or an equivalent one in the main inventory
	 */
	public static int getSlotFromInventory(PlayerEntity player, ItemStack stack) {
		for (int i = 0; i < player.inventory.mainInventory.size(); ++i)
			if (!player.inventory.mainInventory.get(i).isEmpty() && ItemStack.areItemStacksEqual(stack, player.inventory.mainInventory.get(i)))
				return i;
		
		return -1;
	}
	
	public static ItemStack getStackOfClassTypeFromHotBar(PlayerInventory inventory, Class<?> classType) {
		return getStackOfClassTypeFromInventory(PlayerInventory.getHotbarSize(), inventory, classType);
	}
	
	public static ItemStack getStackOfClassTypeFromInventory(PlayerInventory inventory, Class<?> classType) {
		return getStackOfClassTypeFromInventory(inventory.getSizeInventory(), inventory, classType);
	}
	
	private static ItemStack getStackOfClassTypeFromInventory(int iInventorySize, PlayerInventory inventory, Class<?> classType) {
		try {
			for (int iSlot = 0; iSlot < iInventorySize; iSlot++) {
				ItemStack itemStack = inventory.getStackInSlot(iSlot);
				if (itemStack != null && classType.isInstance(itemStack.getItem()))
					return itemStack;
			}
		}
		catch (Exception ex) {
			Constants.LOGGER.error(ex);
		}
		return null;
	}
	
	public static NonNullList<ItemStack> getAllStacksOfClassTypeFromInventory(PlayerInventory inventory, Class<?> classType) {
		NonNullList<ItemStack> rtnStacks = NonNullList.create();
		try {
			for (int iSlot = 0; iSlot < inventory.getSizeInventory(); iSlot++) {
				ItemStack itemStack = inventory.getStackInSlot(iSlot);
				if (itemStack != null && itemStack.getItem() instanceof BlockItem && classType.isInstance(((BlockItem) itemStack.getItem()).getBlock()))
					rtnStacks.add(itemStack);
			}
		}
		catch (Exception ex) {
			Constants.LOGGER.error(ex);
		}
		return rtnStacks;
	}
	
	public static String getName(ItemEntity itemEntity) {
		return getName(itemEntity.getItem());
	}
	
	public static String getName(ItemStack itemStack) {
		return getName(itemStack.getItem());
	}
	
	public static String getName(Item item) {
		return item.getRegistryName().toString().trim();
	}
	
	public static String getName(Block block) {
		return block.getRegistryName().toString().trim();
	}
	
	public static String getName(BlockState state) {
		return state.getBlock().getRegistryName().toString().trim();
	}
	
	public static Block getBlockFromWorld(World world, BlockPos pos) {
		return world.getBlockState(pos).getBlock();
	}
	
	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		}
		catch (InterruptedException e) {}
	}
	
	public static boolean canSustainPlant(World world, BlockPos pos, IPlantable plantable) {
		return world.getBlockState(pos).canSustainPlant(world, pos, Direction.UP, plantable);
	}
	
	public static boolean isValidOre(BlockState state, SyncedClientConfig clientConfig) {
		return state.isIn(Tags.Blocks.ORES) ||
				(clientConfig.veination.ores != null && clientConfig.veination.ores.isEmpty() == false
						&& clientConfig.veination.ores.contains(Functions.getName(state.getBlock().asItem())));
	}
	
	public static void setFinalFieldValue(Object owner, Field field, Object value) throws Exception {
		field.setAccessible(true);
		field.set(owner, value);
	}
	
}
