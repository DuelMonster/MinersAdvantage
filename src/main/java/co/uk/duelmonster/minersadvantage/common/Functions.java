package co.uk.duelmonster.minersadvantage.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.List;

import co.uk.duelmonster.minersadvantage.MinersAdvantage;
import co.uk.duelmonster.minersadvantage.settings.Settings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPlanks.EnumType;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAreaEffectCloud;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.oredict.OreDictionary;

public class Functions {
	
	public static String localize(String key) {
		return (new TextComponentTranslation(key)).getFormattedText();
	}
	
	public static void NotifyClient(EntityPlayer player, String sMsg) {
		FMLClientHandler.instance().getClient().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(Constants.MOD_NAME_MSG + sMsg));
	}
	
	public static void NotifyClient(EntityPlayer player, boolean bIsOn, String sFeatureName) {
		FMLClientHandler.instance().getClient().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(Constants.MOD_NAME_MSG + TextFormatting.GOLD + sFeatureName + " " + (bIsOn ? TextFormatting.GREEN + "ON" : TextFormatting.RED + "OFF")));
	}
	
	public static ItemStack getHeldItemStack(EntityPlayer player) {
		ItemStack heldItem = player.getHeldItem(EnumHand.MAIN_HAND);
		return (heldItem == null || heldItem.stackSize <= 0) ? null : heldItem;
	}
	
	public static Item getHeldItem(EntityPlayer player) {
		ItemStack heldItem = getHeldItemStack(player);
		return heldItem == null ? null : heldItem.getItem();
	}
	
	public static boolean IsPlayerStarving(EntityPlayer player) {
		if (!Variables.get(player.getUniqueID()).HungerNotified && player.getFoodStats().getFoodLevel() <= Constants.MIN_HUNGER) {
			NotifyClient(player, TextFormatting.RED + localize("minersadvantage.hungery") + Constants.MOD_NAME);
			Variables.get(player.getUniqueID()).HungerNotified = true;
		}
		
		return Variables.get(player.getUniqueID()).HungerNotified;
	}
	
	public static String getStackTrace() {
		String sRtrn = "";
		StackTraceElement[] stes = Thread.currentThread().getStackTrace();
		
		for (int i = 2; i < stes.length; i++)
			sRtrn += System.getProperty("line.separator") + "	at " + stes[i].toString();
		
		return sRtrn;
	}
	
	public static boolean isWithinRange(BlockPos sourcePos, BlockPos targetPos, int range) {
		int distanceX = sourcePos.getX() - targetPos.getX();
		int distanceY = sourcePos.getY() - targetPos.getY();
		int distanceZ = sourcePos.getZ() - targetPos.getZ();
		
		return ((distanceX * distanceX) + (distanceY * distanceY) + (distanceZ * distanceZ)) <= (range * range);
	}
	
	public static boolean isWithinArea(BlockPos oPos, AxisAlignedBB area) {
		return area != null &&
				oPos.getX() >= area.minX && oPos.getX() <= area.maxX &&
				oPos.getY() >= area.minY && oPos.getY() <= area.maxY &&
				oPos.getZ() >= area.minZ && oPos.getZ() <= area.maxZ;
	}
	
	public static boolean isWithinArea(Entity entity, AxisAlignedBB area) {
		return area != null &&
				entity.getPosition().getX() >= area.minX && entity.getPosition().getX() <= area.maxX &&
				entity.getPosition().getY() >= area.minY && entity.getPosition().getY() <= area.maxY &&
				entity.getPosition().getZ() >= area.minZ && entity.getPosition().getZ() <= area.maxZ;
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
				Entity e = (Entity) o;
				if (!e.isDead && (e instanceof EntityItem || e instanceof EntityXPOrb))// && isEntityWithinArea(e,
																						// area))
					rtrn.add(e);
			}
			return rtrn;
			
		}
		catch (ConcurrentModificationException e) {
			MinersAdvantage.logger.error("ConcurrentModification Exception Avoided..."); // : " + getStackTrace());
		}
		catch (Exception ex) {
			MinersAdvantage.logger.error(ex.getClass().getName() + " Exception: " + getStackTrace());
		}
		return rtrn;
	}
	
	public static boolean isItemBlacklisted(ItemStack item) {
		if (item == null || item.stackSize <= 0)
			return Settings.get().bIsToolWhitelist();
		
		return isItemBlacklisted(item.getItem());
	}
	
	public static boolean isItemBlacklisted(Item item) {
		if (item == null)
			return Settings.get().bIsToolWhitelist();
		
		if (Settings.get().toolBlacklist().has(Item.REGISTRY.getNameForObject(item).toString()))
			return !Settings.get().bIsToolWhitelist();
		
		for (int id : OreDictionary.getOreIDs(new ItemStack(item)))
			if (Settings.get().toolBlacklist().has(OreDictionary.getOreName(id)))
				return !Settings.get().bIsToolWhitelist();
			
		return Settings.get().bIsToolWhitelist();
	}
	
	public static boolean isBlockBlacklisted(Block block) {
		if (block == null || block == Blocks.AIR)
			return Settings.get().bIsBlockWhitelist();
		
		if (Settings.get().blockBlacklist().has(Block.REGISTRY.getNameForObject(block).toString()))
			return !Settings.get().bIsBlockWhitelist();
		
		ItemStack blockStack = new ItemStack(Item.getItemFromBlock(block));
		
		if (blockStack.stackSize > 0)
			for (int id : OreDictionary.getOreIDs(blockStack))
				if (Settings.get().blockBlacklist().has(OreDictionary.getOreName(id)))
					return !Settings.get().bIsBlockWhitelist();
				
		return Settings.get().bIsBlockWhitelist();
	}
	
	public static boolean isIdInList(Object oID, ArrayList<String> lumbinationLeaves) {
		return (lumbinationLeaves != null && lumbinationLeaves.indexOf(oID) >= 0);
	}
	
	public static List<Object> IDListToArray(String[] saIDs, boolean bIsBlock) {
		return IDListToArray(Arrays.asList(saIDs), bIsBlock);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static List<Object> IDListToArray(List<String> saIDs, boolean bIsBlock) {
		RegistryNamespaced rn = bIsBlock ? Block.REGISTRY : Item.REGISTRY;
		List<Object> lReturn = new ArrayList();
		
		for (String sID : saIDs) {
			Object oEntity = null;
			sID = sID.trim();
			
			try {
				int id = Integer.parseInt(sID.trim());
				oEntity = rn.getObjectById(id);
			}
			catch (NumberFormatException e) {
				oEntity = rn.getObject(new ResourceLocation(sID));
			}
			
			if (null != oEntity && Blocks.AIR != oEntity)
				lReturn.add(oEntity);
			
		}
		return lReturn;
	}
	
	public static void playSound(World world, BlockPos oPos, SoundEvent sound, SoundCategory soundCategory, float volume, float pitch) {
		playSound(world, null, oPos, sound, soundCategory, volume, pitch);
	}
	
	public static void playSound(World world, EntityPlayer player, BlockPos oPos, SoundEvent sound, SoundCategory soundCategory, float volume, float pitch) {
		world.playSound(player, oPos.getX() + 0.5F, oPos.getY() + 0.5F, oPos.getZ() + 0.5F, sound, soundCategory, volume, pitch);
	}
	
	public static void spawnAreaEffectCloud(World world, EntityPlayer player, BlockPos oPos) {
		EntityAreaEffectCloud entityareaeffectcloud = new EntityAreaEffectCloud(world, oPos.getX() + 0.5D, oPos.getY() + 0.5D, oPos.getZ() + 0.5D);
		entityareaeffectcloud.setOwner(player);
		entityareaeffectcloud.setRadius(1.0F);
		entityareaeffectcloud.setRadiusOnUse(-0.5F);
		entityareaeffectcloud.setWaitTime(1);
		entityareaeffectcloud.setDuration(20);
		entityareaeffectcloud.setRadiusPerTick(-entityareaeffectcloud.getRadius() / entityareaeffectcloud.getDuration());
		entityareaeffectcloud.addEffect(new PotionEffect(MobEffects.WEAKNESS, 10));
		
		world.spawnEntityInWorld(entityareaeffectcloud);
	}
	
	/**
	 * Finds the stack or an equivalent one in the main inventory
	 */
	public static int getSlotFromInventory(EntityPlayer player, ItemStack stack) {
		for (int i = 0; i < player.inventory.mainInventory.length; ++i)
			if (player.inventory.mainInventory[i].stackSize > 0 && stackEqualExact(stack, player.inventory.mainInventory[i]))
				return i;
			
		return -1;
	}
	
	/**
	 * Checks item, NBT, and meta if the item is not damageable
	 */
	private static boolean stackEqualExact(ItemStack stack1, ItemStack stack2) {
		return stack1.getItem() == stack2.getItem() && (!stack1.getHasSubtypes() || stack1.getMetadata() == stack2.getMetadata()) && ItemStack.areItemStackTagsEqual(stack1, stack2);
	}
	
	public static String getItemName(ItemStack itemStack) {
		return getItemName(itemStack.getItem());
	}
	
	public static String getItemName(Item item) {
		return item.getRegistryName().toString().trim();
	}
	
	public static String getBlockName(Block block) {
		return block.getRegistryName().toString().trim();
	}
	
	public static Object getPropertyValue(IBlockState state, PropertyEnum<EnumType> variant) {
		// for (IProperty<?> prop : state.getProperties().keySet())
		// if (prop.getName().equals(variant))
		// return state.getValue(prop);
		try {
			return state.getValue(variant);// .getMetadata();
		}
		catch (Exception e) {
			return null;
		}
	}
	
	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		}
		catch (InterruptedException e) {}
	}
}
