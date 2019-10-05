package uk.co.duelmonster.minersadvantage.helpers;

import org.lwjgl.glfw.GLFW;

import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.text.StringTextComponent;
import uk.co.duelmonster.minersadvantage.client.ClientFunctions;
import uk.co.duelmonster.minersadvantage.client.KeyBindings;
import uk.co.duelmonster.minersadvantage.common.Functions;
import uk.co.duelmonster.minersadvantage.config.MAConfig;
import uk.co.duelmonster.minersadvantage.network.NetworkHandler;
import uk.co.duelmonster.minersadvantage.network.packets.PacketSupremeVantage;

public class SupremeVantage {
	private static final String CODE = "2780872";
	private static String vantageCode = "";
	private static int tickCount = 0;
	private static boolean isWorthy = false;
	private static int givenCount = 0;

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

				Functions.NotifyClient(ClientFunctions.getPlayer(), Functions.localize("minersadvantage.excavation.supremevantage"));
				isWorthy = true;
				vantageCode = "";
				givenCount = 0;
				tickCount = 0;

			} else if (isWorthy && tickCount >= 5) {

				NetworkHandler.sendToServer(new PacketSupremeVantage());
				tickCount = 0;

			}

			if ((!bToggled && vantageCode != "" && tickCount > 0) || tickCount >= 1000) {
				vantageCode = "";
				tickCount = 0;
			}
		}

	}

	private static boolean codeEndsWith(int keyCode) { return vantageCode.endsWith(KeyBindings.getKeyName(keyCode)); }

	public static void GiveSupremeVantage(PlayerEntity player) {
		givenCount++;
		ItemStack oItemStack = null;

		switch (givenCount) {
		case 1:
			// Sword: "/give @p 276 1 0
			// {Unbreakable:1,ench:[{id:16,lvl:10},{id:20,lvl:2},{id:21,lvl:10}]}"
			oItemStack = new ItemStack(Items.DIAMOND_SWORD);
			oItemStack.setCount(1);
			oItemStack.addEnchantment(Enchantments.SHARPNESS, 10);
			oItemStack.addEnchantment(Enchantments.FIRE_ASPECT, 2);
			oItemStack.addEnchantment(Enchantments.LOOTING, 10);
			oItemStack.setDisplayName(new StringTextComponent("Soulblade"));
			oItemStack.getOrCreateTag().putBoolean("Unbreakable", true);

			break;
		case 2:
			// Sword: "/give @p 276 1 0
			// {Unbreakable:1,ench:[{id:16,lvl:10},{id:21,lvl:10}]}"
			oItemStack = new ItemStack(Items.DIAMOND_SWORD);
			oItemStack.setCount(1);
			oItemStack.addEnchantment(Enchantments.SHARPNESS, 10);
			oItemStack.addEnchantment(Enchantments.LOOTING, 10);
			oItemStack.setDisplayName(new StringTextComponent("Peacekeeper"));
			oItemStack.getOrCreateTag().putBoolean("Unbreakable", true);
			break;
		case 3:
			// Pickaxe: "/give @p 278 1 0
			// {Unbreakable:1,ench:[{id:32,lvl:6},{id:35,lvl:10}]}"
			oItemStack = new ItemStack(Items.DIAMOND_PICKAXE);
			oItemStack.setCount(1);
			oItemStack.addEnchantment(Enchantments.EFFICIENCY, 5); // Six too fast...
			oItemStack.addEnchantment(Enchantments.FORTUNE, 10);
			oItemStack.setDisplayName(new StringTextComponent("Minora"));
			oItemStack.getOrCreateTag().putBoolean("Unbreakable", true);
			break;
		case 4:
			// Pickaxe: "/give @p 278 1 0
			// {Unbreakable:1,ench:[{id:32,lvl:6},{id:33,lvl:1}]}"
			oItemStack = new ItemStack(Items.DIAMOND_PICKAXE);
			oItemStack.setCount(1);
			oItemStack.addEnchantment(Enchantments.EFFICIENCY, 5); // Six too fast...
			oItemStack.addEnchantment(Enchantments.SILK_TOUCH, 1);
			oItemStack.setDisplayName(new StringTextComponent("Silkar"));
			oItemStack.getOrCreateTag().putBoolean("Unbreakable", true);
			break;
		case 5:
			// Bow: "/give @p 261 1 0
			// {Unbreakable:1,ench:[{id:48,lvl:10},{id:50,lvl:10},{id:51,lvl:10}]}" -
			// KnockBack =
			// ",{id:49,lvl:10}"
			oItemStack = new ItemStack(Items.BOW);
			oItemStack.setCount(1);
			oItemStack.addEnchantment(Enchantments.POWER, 10);
			oItemStack.addEnchantment(Enchantments.FLAME, 1);
			oItemStack.addEnchantment(Enchantments.INFINITY, 1);
			oItemStack.setDisplayName(new StringTextComponent("Firestarter"));
			oItemStack.getOrCreateTag().putBoolean("Unbreakable", true);
			break;
		case 6:
			// Single Arrow: /give @p 262 1
			oItemStack = new ItemStack(Items.ARROW);
			oItemStack.setCount(1);
			oItemStack.setDisplayName(new StringTextComponent("Firestarter Ammo"));
			break;
		case 7:
			// Shovel: "/give @p 277 1 0
			// {Unbreakable:1,ench:[{id:32,lvl:10},{id:35,lvl:10}]}"
			oItemStack = new ItemStack(Items.DIAMOND_SHOVEL);
			oItemStack.setCount(1);
			oItemStack.addEnchantment(Enchantments.EFFICIENCY, 5); // Six too fast...
			oItemStack.addEnchantment(Enchantments.FORTUNE, 10);
			oItemStack.setDisplayName(new StringTextComponent("Diggle"));
			oItemStack.getOrCreateTag().putBoolean("Unbreakable", true);
			break;
		case 8:
			// Axe: "/give @p 279 1 0 {Unbreakable:1,ench:[{id:32,lvl:10},{id:35,lvl:10}]}"
			oItemStack = new ItemStack(Items.DIAMOND_AXE);
			oItemStack.setCount(1);
			oItemStack.addEnchantment(Enchantments.EFFICIENCY, 5); // Six too fast...
			oItemStack.addEnchantment(Enchantments.FORTUNE, 10);
			oItemStack.setDisplayName(new StringTextComponent("Whirlwind"));
			oItemStack.getOrCreateTag().putBoolean("Unbreakable", true);
			break;
		case 9:
			// Helmet: "/give @p 310 1 0
			// {Unbreakable:1,ench:[{id:0,lvl:10},{id:1,lvl:10},{id:5,lvl:10},{id:6,lvl:1}]}"
			oItemStack = new ItemStack(Items.DIAMOND_HELMET);
			oItemStack.setCount(1);
			oItemStack.addEnchantment(Enchantments.PROTECTION, 10);
			oItemStack.addEnchantment(Enchantments.FIRE_PROTECTION, 10);
			oItemStack.addEnchantment(Enchantments.RESPIRATION, 10);
			oItemStack.addEnchantment(Enchantments.AQUA_AFFINITY, 1);
			oItemStack.setDisplayName(new StringTextComponent("Headguard of the Mage"));
			oItemStack.getOrCreateTag().putBoolean("Unbreakable", true);
			break;
		case 10:
			// Chestplate: "/give @p 311 1 0
			// {Unbreakable:1,ench:[{id:0,lvl:10},{id:1,lvl:10}]}"
			oItemStack = new ItemStack(Items.DIAMOND_CHESTPLATE);
			oItemStack.setCount(1);
			oItemStack.addEnchantment(Enchantments.PROTECTION, 10);
			oItemStack.addEnchantment(Enchantments.FIRE_PROTECTION, 10);
			oItemStack.setDisplayName(new StringTextComponent("Breastplate of Eternal Protection"));
			oItemStack.getOrCreateTag().putBoolean("Unbreakable", true);
			break;
		case 11:
			// Leggings: "/give @p 312 1 0
			// {Unbreakable:1,ench:[{id:0,lvl:10},{id:1,lvl:10}]}"
			oItemStack = new ItemStack(Items.DIAMOND_LEGGINGS);
			oItemStack.setCount(1);
			oItemStack.addEnchantment(Enchantments.PROTECTION, 10);
			oItemStack.addEnchantment(Enchantments.FIRE_PROTECTION, 10);
			oItemStack.setDisplayName(new StringTextComponent("Legplates of the Bear"));
			oItemStack.getOrCreateTag().putBoolean("Unbreakable", true);
			break;
		case 12:
			// Boots: "/give @p 313 1 0
			// {Unbreakable:1,ench:[{id:0,lvl:10},{id:1,lvl:10},{id:2,lvl:10}]}"
			oItemStack = new ItemStack(Items.DIAMOND_BOOTS);
			oItemStack.setCount(1);
			oItemStack.addEnchantment(Enchantments.PROTECTION, 10);
			oItemStack.addEnchantment(Enchantments.FIRE_PROTECTION, 10);
			oItemStack.addEnchantment(Enchantments.FEATHER_FALLING, 10);
			oItemStack.addEnchantment(Enchantments.DEPTH_STRIDER, 10);
			oItemStack.setDisplayName(new StringTextComponent("Walkers of Broken Visions"));
			oItemStack.getOrCreateTag().putBoolean("Unbreakable", true);
			break;
		case 13:
			// Fishing Rod: "/give @p 346 64 0
			// {Unbreakable:1,ench:[{id:61,lvl:100},{id:62,lvl:8}]}"
			oItemStack = new ItemStack(Items.FISHING_ROD);
			oItemStack.setCount(1);
			oItemStack.addEnchantment(Enchantments.LUCK_OF_THE_SEA, 100); // 61 = Luck of the Sea
			oItemStack.addEnchantment(Enchantments.LURE, 8); // 62 = Lure
			oItemStack.setDisplayName(new StringTextComponent("Raider of Poseidons Stash"));
			oItemStack.getOrCreateTag().putBoolean("Unbreakable", true);
			break;
		case 14:
			// Fishing Rod: "/give @p 346 64 0 {Unbreakable:1,ench:[{id:62,lvl:8}]}"
			oItemStack = new ItemStack(Items.FISHING_ROD);
			oItemStack.setCount(1);
			oItemStack.addEnchantment(Enchantments.LURE, 8); // 62 = Lure
			oItemStack.setDisplayName(new StringTextComponent("Dagons Rod"));
			oItemStack.getOrCreateTag().putBoolean("Unbreakable", true);
			break;
		case 15:
			// Hoe: "/give @p 293 1 0 {Unbreakable:1}"
			oItemStack = new ItemStack(Items.DIAMOND_HOE);
			oItemStack.setCount(1);
			oItemStack.setDisplayName(new StringTextComponent("Ten Dolla"));
			oItemStack.getOrCreateTag().putBoolean("Unbreakable", true);
			break;
		case 16:
			// Shears: "/give @p 359 1 0 {Unbreakable:1,ench:[{id:32,lvl:10}]}"
			oItemStack = new ItemStack(Items.SHEARS);
			oItemStack.setCount(1);
			oItemStack.setDisplayName(new StringTextComponent("Shawn"));
			oItemStack.getOrCreateTag().putBoolean("Unbreakable", true);
			break;
		case 17:
			// Flint & Steel: "/give @p 259 1 0 {Unbreakable:1}"
			oItemStack = new ItemStack(Items.FLINT_AND_STEEL);
			oItemStack.setCount(1);
			oItemStack.setDisplayName(new StringTextComponent("Firestarter"));
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
