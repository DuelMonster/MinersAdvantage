package uk.co.duelmonster.minersadvantage.helpers;

import org.lwjgl.glfw.GLFW;

import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import uk.co.duelmonster.minersadvantage.MA;
import uk.co.duelmonster.minersadvantage.client.ClientFunctions;
import uk.co.duelmonster.minersadvantage.client.KeyBindings;
import uk.co.duelmonster.minersadvantage.common.Functions;
import uk.co.duelmonster.minersadvantage.config.MAConfig;
import uk.co.duelmonster.minersadvantage.network.packets.PacketSupremeVantage;

public class SupremeVantage {
	private static final String	CODE		= "2780872";
	private static String		vantageCode	= "";
	private static int			tickCount	= 0;
	private static boolean		isWorthy	= false;
	private static int			givenCount	= 0;
	
	@OnlyIn(Dist.CLIENT)
	public static void isWorthy(boolean bToggled) {
		if (MAConfig.CLIENT.allEnabled()) {
			tickCount++;
			if (bToggled) {
				
				if (KeyBindings.isKeyDown(GLFW.GLFW_KEY_0) && !codeEndsWith(GLFW.GLFW_KEY_0))
					vantageCode += KeyBindings.getKeyName(GLFW.GLFW_KEY_0);
				if (KeyBindings.isKeyDown(GLFW.GLFW_KEY_2) && !codeEndsWith(GLFW.GLFW_KEY_2))
					vantageCode += KeyBindings.getKeyName(GLFW.GLFW_KEY_2);
				if (KeyBindings.isKeyDown(GLFW.GLFW_KEY_7) && !codeEndsWith(GLFW.GLFW_KEY_7))
					vantageCode += KeyBindings.getKeyName(GLFW.GLFW_KEY_7);
				if (KeyBindings.isKeyDown(GLFW.GLFW_KEY_8) && !codeEndsWith(GLFW.GLFW_KEY_8))
					vantageCode += KeyBindings.getKeyName(GLFW.GLFW_KEY_8);
				
				tickCount = 0;
				
			} else if (!isWorthy && !vantageCode.isEmpty() && vantageCode.equalsIgnoreCase(CODE)) {
				
				Functions.NotifyClient(ClientFunctions.getPlayer(), Functions.localize("minersadvantage.supremevantage"));
				isWorthy = true;
				vantageCode = "";
				givenCount = 0;
				tickCount = 0;
				
			} else if (isWorthy && tickCount >= 5) {
				
				MA.NETWORK.sendToServer(new PacketSupremeVantage());
				tickCount = 0;
				
			}
			
			if ((!bToggled && vantageCode != "" && tickCount > 0) || tickCount >= 1000) {
				vantageCode = "";
				tickCount = 0;
			}
		}
		
	}
	
	private static boolean codeEndsWith(int keyCode) {
		return vantageCode.endsWith(KeyBindings.getKeyName(keyCode));
	}
	
	public static void GiveSupremeVantage(ServerPlayerEntity player) {
		givenCount++;
		ItemStack oItemStack = null;
		
		switch (givenCount) {
		case 1:
			// Sword - Fire Aspect
			oItemStack = new ItemStack(Items.DIAMOND_SWORD);
			oItemStack.setCount(1);
			oItemStack.addEnchantment(Enchantments.SHARPNESS, 10);
			oItemStack.addEnchantment(Enchantments.SWEEPING, 10);
			oItemStack.addEnchantment(Enchantments.FIRE_ASPECT, 2);
			oItemStack.addEnchantment(Enchantments.LOOTING, 10);
			oItemStack.setDisplayName(new StringTextComponent("Soulblade"));
			oItemStack.getOrCreateTag().putBoolean("Unbreakable", true);
			
			break;
		case 2:
			// Sword
			oItemStack = new ItemStack(Items.DIAMOND_SWORD);
			oItemStack.setCount(1);
			oItemStack.addEnchantment(Enchantments.SHARPNESS, 10);
			oItemStack.addEnchantment(Enchantments.SWEEPING, 10);
			oItemStack.addEnchantment(Enchantments.LOOTING, 10);
			oItemStack.setDisplayName(new StringTextComponent("Peacekeeper"));
			oItemStack.getOrCreateTag().putBoolean("Unbreakable", true);
			break;
		case 3:
			// Pickaxe - Fortune
			oItemStack = new ItemStack(Items.DIAMOND_PICKAXE);
			oItemStack.setCount(1);
			oItemStack.addEnchantment(Enchantments.EFFICIENCY, 5); // Six too fast...
			oItemStack.addEnchantment(Enchantments.FORTUNE, 10);
			oItemStack.setDisplayName(new StringTextComponent("Minora"));
			oItemStack.getOrCreateTag().putBoolean("Unbreakable", true);
			break;
		case 4:
			// Pickaxe - Silktouch
			oItemStack = new ItemStack(Items.DIAMOND_PICKAXE);
			oItemStack.setCount(1);
			oItemStack.addEnchantment(Enchantments.EFFICIENCY, 5); // Six too fast...
			oItemStack.addEnchantment(Enchantments.SILK_TOUCH, 1);
			oItemStack.setDisplayName(new StringTextComponent("Silkar"));
			oItemStack.getOrCreateTag().putBoolean("Unbreakable", true);
			break;
		case 5:
			// Shovel
			oItemStack = new ItemStack(Items.DIAMOND_SHOVEL);
			oItemStack.setCount(1);
			oItemStack.addEnchantment(Enchantments.EFFICIENCY, 5); // Six too fast...
			oItemStack.addEnchantment(Enchantments.FORTUNE, 10);
			oItemStack.setDisplayName(new StringTextComponent("Diggle"));
			oItemStack.getOrCreateTag().putBoolean("Unbreakable", true);
			break;
		case 6:
			// Axe
			oItemStack = new ItemStack(Items.DIAMOND_AXE);
			oItemStack.setCount(1);
			oItemStack.addEnchantment(Enchantments.EFFICIENCY, 5); // Six too fast...
			oItemStack.addEnchantment(Enchantments.FORTUNE, 10);
			oItemStack.setDisplayName(new StringTextComponent("Whirlwind"));
			oItemStack.getOrCreateTag().putBoolean("Unbreakable", true);
			break;
		case 7:
			// Helmet
			oItemStack = new ItemStack(Items.DIAMOND_HELMET);
			oItemStack.setCount(1);
			oItemStack.addEnchantment(Enchantments.FIRE_PROTECTION, 10);
			oItemStack.addEnchantment(Enchantments.RESPIRATION, 10);
			oItemStack.addEnchantment(Enchantments.AQUA_AFFINITY, 1);
			oItemStack.setDisplayName(new StringTextComponent("Tadpols Scuba Helm"));
			oItemStack.getOrCreateTag().putBoolean("Unbreakable", true);
			break;
		case 8:
			// Chestplate
			oItemStack = new ItemStack(Items.DIAMOND_CHESTPLATE);
			oItemStack.setCount(1);
			oItemStack.addEnchantment(Enchantments.FIRE_PROTECTION, 10);
			oItemStack.setDisplayName(new StringTextComponent("Breastplate of Black Bones"));
			oItemStack.getOrCreateTag().putBoolean("Unbreakable", true);
			break;
		case 9:
			// Elytra
			oItemStack = new ItemStack(Items.ELYTRA);
			oItemStack.setCount(1);
			oItemStack.setDisplayName(new StringTextComponent("Pegasus' Wings"));
			oItemStack.getOrCreateTag().putBoolean("Unbreakable", true);
			break;
		case 10:
			// Leggings
			oItemStack = new ItemStack(Items.DIAMOND_LEGGINGS);
			oItemStack.setCount(1);
			oItemStack.addEnchantment(Enchantments.FIRE_PROTECTION, 10);
			oItemStack.setDisplayName(new StringTextComponent("Wizadora's Legplates"));
			oItemStack.getOrCreateTag().putBoolean("Unbreakable", true);
			break;
		case 11:
			// Boots
			oItemStack = new ItemStack(Items.DIAMOND_BOOTS);
			oItemStack.setCount(1);
			oItemStack.addEnchantment(Enchantments.FIRE_PROTECTION, 10);
			oItemStack.addEnchantment(Enchantments.FEATHER_FALLING, 10);
			oItemStack.addEnchantment(Enchantments.DEPTH_STRIDER, 10);
			oItemStack.setDisplayName(new StringTextComponent("Victims Souls"));
			oItemStack.getOrCreateTag().putBoolean("Unbreakable", true);
			break;
		case 12:
			// Bow
			oItemStack = new ItemStack(Items.BOW);
			oItemStack.setCount(1);
			oItemStack.addEnchantment(Enchantments.POWER, 10);
			oItemStack.addEnchantment(Enchantments.FLAME, 1);
			oItemStack.addEnchantment(Enchantments.INFINITY, 1);
			oItemStack.setDisplayName(new StringTextComponent("Firestarter"));
			oItemStack.getOrCreateTag().putBoolean("Unbreakable", true);
			break;
		case 13:
			// Single Arrow
			oItemStack = new ItemStack(Items.ARROW);
			oItemStack.setCount(1);
			oItemStack.setDisplayName(new StringTextComponent("Firestarter Ammo"));
			break;
		case 14:
			// Crossbow
			oItemStack = new ItemStack(Items.CROSSBOW);
			oItemStack.setCount(1);
			oItemStack.addEnchantment(Enchantments.MULTISHOT, 1);
			oItemStack.addEnchantment(Enchantments.PIERCING, 5);
			oItemStack.addEnchantment(Enchantments.QUICK_CHARGE, 5);
			oItemStack.setDisplayName(new StringTextComponent("Penertrator"));
			oItemStack.getOrCreateTag().putBoolean("Unbreakable", true);
			break;
		case 15:
			// Trident
			oItemStack = new ItemStack(Items.TRIDENT);
			oItemStack.setCount(1);
			oItemStack.addEnchantment(Enchantments.RIPTIDE, 5);
			oItemStack.addEnchantment(Enchantments.IMPALING, 5);
			oItemStack.setDisplayName(new StringTextComponent("Poseidons Fork"));
			oItemStack.getOrCreateTag().putBoolean("Unbreakable", true);
			break;
		case 16:
			// Hoe
			oItemStack = new ItemStack(Items.DIAMOND_HOE);
			oItemStack.setCount(1);
			oItemStack.setDisplayName(new StringTextComponent("Ten Dolla"));
			oItemStack.getOrCreateTag().putBoolean("Unbreakable", true);
			break;
		case 17:
			// Shears
			oItemStack = new ItemStack(Items.SHEARS);
			oItemStack.setCount(1);
			oItemStack.setDisplayName(new StringTextComponent("Shawn"));
			oItemStack.getOrCreateTag().putBoolean("Unbreakable", true);
			break;
		case 18:
			// Flint & Steel
			oItemStack = new ItemStack(Items.FLINT_AND_STEEL);
			oItemStack.setCount(1);
			oItemStack.setDisplayName(new StringTextComponent("Pyro"));
			oItemStack.getOrCreateTag().putBoolean("Unbreakable", true);
			break;
		case 19:
			// Fishing Rod - Luck of the Sea
			oItemStack = new ItemStack(Items.FISHING_ROD);
			oItemStack.setCount(1);
			oItemStack.addEnchantment(Enchantments.LUCK_OF_THE_SEA, 100); // 61 = Luck of the Sea
			oItemStack.setDisplayName(new StringTextComponent("Raider of Poseidons Stash"));
			oItemStack.getOrCreateTag().putBoolean("Unbreakable", true);
			break;
		case 20:
			// Fishing Rod - Lure
			oItemStack = new ItemStack(Items.FISHING_ROD);
			oItemStack.setCount(1);
			oItemStack.addEnchantment(Enchantments.LURE, 8); // 62 = Lure
			oItemStack.setDisplayName(new StringTextComponent("Rodney"));
			oItemStack.getOrCreateTag().putBoolean("Unbreakable", true);
			break;
		default:
			givenCount = -1;
			isWorthy = false;
		}
		
		if (oItemStack != null)
			player.world.addEntity(new ItemEntity(player.world, player.posX, player.posY, player.posZ, oItemStack));
		
	}
}
