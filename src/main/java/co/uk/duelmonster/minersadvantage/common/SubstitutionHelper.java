package co.uk.duelmonster.minersadvantage.common;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SubstitutionHelper {
	private static final Minecraft mc = Minecraft.getMinecraft();
	
	public static final ItemStack EMPTY_ITEMSTACK;
	
	private static ItemStack prevHeldItem;
	
	static {
		ItemStack tempEmpty;
		try {
			tempEmpty = null;
		}
		catch (NoSuchFieldError err) {
			tempEmpty = null;
		}
		EMPTY_ITEMSTACK = tempEmpty;
		prevHeldItem = EMPTY_ITEMSTACK;
	}
	
	private static void fakeItemForPlayer(ItemStack itemstack) {
		prevHeldItem = mc.thePlayer.inventory.getStackInSlot(mc.thePlayer.inventory.currentItem);
		mc.thePlayer.inventory.setInventorySlotContents(mc.thePlayer.inventory.currentItem, itemstack);
		if (!isItemStackEmpty(prevHeldItem)) {
			mc.thePlayer.getAttributeMap().removeAttributeModifiers(prevHeldItem.getAttributeModifiers(EntityEquipmentSlot.MAINHAND));
		}
		if (!isItemStackEmpty(itemstack)) {
			mc.thePlayer.getAttributeMap().applyAttributeModifiers(itemstack.getAttributeModifiers(EntityEquipmentSlot.MAINHAND));
		}
	}
	
	public static boolean isItemStackEmpty(ItemStack itemstack) {
		if (EMPTY_ITEMSTACK == null)
			return itemstack == null;
		return itemstack.stackSize <= 0;
	}
	
	public static double getFullItemStackDamage(ItemStack itemStack, EntityLivingBase entity) {
		fakeItemForPlayer(itemStack);
		double damage = mc.thePlayer.getEntityAttribute(
				SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
		
		double enchDamage = EnchantmentHelper.getModifierForCreature(itemStack, entity.getCreatureAttribute());
		
		if (damage > 0.0D || enchDamage > 0.0D) {
			boolean critical = mc.thePlayer.fallDistance > 0.0F
					&& !mc.thePlayer.onGround && !mc.thePlayer.isOnLadder()
					&& !mc.thePlayer.isInWater()
					&& !mc.thePlayer.isPotionActive(Potion.getPotionFromResourceLocation("blindness"))
					&& !mc.thePlayer.isRiding();
			
			if (critical && damage > 0) {
				
				damage *= 1.5D;
			}
			damage += enchDamage;
		}
		unFakeItemForPlayer();
		return damage;
	}
	
	public static Set<Enchantment> getNonstandardNondamageEnchantmentsOnBothStacks(
			ItemStack stack1, ItemStack stack2) {
		
		Set<Enchantment> bothItemsEnchantments = new HashSet<Enchantment>();
		
		if (!isItemStackEmpty(stack1)) {
			bothItemsEnchantments.addAll(EnchantmentHelper.getEnchantments(
					stack1).keySet());
		}
		if (!isItemStackEmpty(stack2)) {
			bothItemsEnchantments.addAll(EnchantmentHelper.getEnchantments(
					stack2).keySet());
		}
		
		Iterator<Enchantment> iterator = bothItemsEnchantments.iterator();
		while (iterator.hasNext()) {
			Enchantment enchantment = iterator.next();
			if (enchantment == null) {
				iterator.remove();
				continue;
			}
			ResourceLocation location = Enchantment.REGISTRY.getNameForObject(enchantment);
			if (location == null || "minecraft".equals(location.getResourceDomain())) {
				iterator.remove();
			}
		}
		
		return bothItemsEnchantments;
	}
	
	public static boolean isItemStackDamageable(ItemStack itemstack) {
		return !isItemStackEmpty(itemstack) && itemstack.isItemStackDamageable();
	}
	
	public static boolean isSword(ItemStack itemstack) {
		if (isItemStackEmpty(itemstack)) {
			return false;
		}
		if (itemstack.getItem() instanceof ItemSword) {
			return true;
		}
		String name = Item.REGISTRY.getNameForObject(itemstack.getItem()).toString();
		if (name.endsWith("sword")) {
			return true;
		}
		return false;
	}
	
	private static void unFakeItemForPlayer() {
		ItemStack fakedStack = mc.thePlayer.inventory.getStackInSlot(mc.thePlayer.inventory.currentItem);
		mc.thePlayer.inventory.setInventorySlotContents(mc.thePlayer.inventory.currentItem, prevHeldItem);
		if (!isItemStackEmpty(fakedStack)) {
			mc.thePlayer.getAttributeMap().removeAttributeModifiers(
					fakedStack.getAttributeModifiers(EntityEquipmentSlot.MAINHAND));
		}
		if (!isItemStackEmpty(prevHeldItem)) {
			mc.thePlayer.getAttributeMap().applyAttributeModifiers(
					prevHeldItem.getAttributeModifiers(EntityEquipmentSlot.MAINHAND));
		}
	}
}
