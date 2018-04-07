package co.uk.duelmonster.minersadvantage.handlers;

import org.lwjgl.input.Keyboard;

import co.uk.duelmonster.minersadvantage.MinersAdvantage;
import co.uk.duelmonster.minersadvantage.client.ClientFunctions;
import co.uk.duelmonster.minersadvantage.common.Functions;
import co.uk.duelmonster.minersadvantage.common.PacketID;
import co.uk.duelmonster.minersadvantage.packets.NetworkPacket;
import co.uk.duelmonster.minersadvantage.settings.Settings;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class GodItems {
	private static final String	CODE		= "27807";
	private static String		sGodCode	= "";
	private static int			iTickCount	= 0;
	private static boolean		bIsWorthy	= false;
	private static int			iGivenCount	= 0;
	
	public static void isWorthy(boolean bToggled) {
		if (Settings.get().allEnabled()) {
			iTickCount++;
			if (bToggled) {
				
				sGodCode += (Keyboard.isKeyDown(Keyboard.KEY_0) && Keyboard.getEventCharacter() == '0' && !sGodCode.endsWith("0") ? "0" : "");
				sGodCode += (Keyboard.isKeyDown(Keyboard.KEY_2) && Keyboard.getEventCharacter() == '2' && !sGodCode.endsWith("2") ? "2" : "");
				sGodCode += (Keyboard.isKeyDown(Keyboard.KEY_7) && Keyboard.getEventCharacter() == '7' && !sGodCode.endsWith("7") ? "7" : "");
				sGodCode += (Keyboard.isKeyDown(Keyboard.KEY_8) && Keyboard.getEventCharacter() == '8' && !sGodCode.endsWith("8") ? "8" : "");
				
			} else if (!bIsWorthy && !sGodCode.isEmpty() && sGodCode.equalsIgnoreCase(CODE)) {
				
				Functions.NotifyClient(ClientFunctions.getPlayer(), Functions.localize("minersadvantage.excavation.goditems"));
				bIsWorthy = true;
				sGodCode = "";
				iGivenCount = 0;
				
			} else if (bIsWorthy && iTickCount >= 40) {
				
				NBTTagCompound tags = new NBTTagCompound();
				tags.setInteger("ID", PacketID.GODITEMS.value());
				MinersAdvantage.instance.network.sendToServer(new NetworkPacket(tags));
				
			}
			
			if (iTickCount >= 100) {
				sGodCode = "";
				iTickCount = 0;
			}
		}
		
	}
	
	public static void GiveGodTools(EntityPlayer player) {
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		if (null == server)
			return;
		
		World world = server.worldServerForDimension(player.dimension);
		
		iGivenCount++;
		
		switch (iGivenCount) {
		case 1:
			// Sword: "/give @p 276 1 0 {Unbreakable:1,ench:[{id:16,lvl:10},{id:20,lvl:2},{id:21,lvl:10}]}"
			ItemStack oItemStack = new ItemStack(Items.DIAMOND_SWORD);
			oItemStack.setCount(1);
			oItemStack.addEnchantment(Enchantments.SHARPNESS, 10);
			oItemStack.addEnchantment(Enchantments.FIRE_ASPECT, 2);
			oItemStack.addEnchantment(Enchantments.LOOTING, 10);
			oItemStack.setStackDisplayName("Soulblade");
			if (!oItemStack.hasTagCompound())
				oItemStack.setTagCompound(new NBTTagCompound());
			oItemStack.getTagCompound().setBoolean("Unbreakable", true);
			
			world.spawnEntity(new EntityItem(world, player.posX, player.posY, player.posZ, oItemStack));
			break;
		case 2:
			// Sword: "/give @p 276 1 0 {Unbreakable:1,ench:[{id:16,lvl:10},{id:21,lvl:10}]}"
			oItemStack = new ItemStack(Items.DIAMOND_SWORD);
			oItemStack.setCount(1);
			oItemStack.addEnchantment(Enchantments.SHARPNESS, 10);
			oItemStack.addEnchantment(Enchantments.LOOTING, 10);
			oItemStack.setStackDisplayName("Peacekeeper");
			if (!oItemStack.hasTagCompound())
				oItemStack.setTagCompound(new NBTTagCompound());
			oItemStack.getTagCompound().setBoolean("Unbreakable", true);
			
			world.spawnEntity(new EntityItem(world, player.posX, player.posY, player.posZ, oItemStack));
			break;
		case 3:
			// Pickaxe: "/give @p 278 1 0 {Unbreakable:1,ench:[{id:32,lvl:6},{id:35,lvl:10}]}"
			oItemStack = new ItemStack(Items.DIAMOND_PICKAXE);
			oItemStack.setCount(1);
			oItemStack.addEnchantment(Enchantments.EFFICIENCY, 5); // Six too fast...
			oItemStack.addEnchantment(Enchantments.FORTUNE, 10);
			oItemStack.setStackDisplayName("Minora");
			if (!oItemStack.hasTagCompound())
				oItemStack.setTagCompound(new NBTTagCompound());
			oItemStack.getTagCompound().setBoolean("Unbreakable", true);
			
			world.spawnEntity(new EntityItem(world, player.posX, player.posY, player.posZ, oItemStack));
			break;
		case 4:
			// Pickaxe: "/give @p 278 1 0 {Unbreakable:1,ench:[{id:32,lvl:6},{id:33,lvl:1}]}"
			oItemStack = new ItemStack(Items.DIAMOND_PICKAXE);
			oItemStack.setCount(1);
			oItemStack.addEnchantment(Enchantments.EFFICIENCY, 5); // Six too fast...
			oItemStack.addEnchantment(Enchantments.SILK_TOUCH, 1);
			oItemStack.setStackDisplayName("Silkar");
			if (!oItemStack.hasTagCompound())
				oItemStack.setTagCompound(new NBTTagCompound());
			oItemStack.getTagCompound().setBoolean("Unbreakable", true);
			
			world.spawnEntity(new EntityItem(world, player.posX, player.posY, player.posZ, oItemStack));
			break;
		case 5:
			// Bow: "/give @p 261 1 0 {Unbreakable:1,ench:[{id:48,lvl:10},{id:50,lvl:10},{id:51,lvl:10}]}" - KnockBack =
			// ",{id:49,lvl:10}"
			oItemStack = new ItemStack(Items.BOW);
			oItemStack.setCount(1);
			oItemStack.addEnchantment(Enchantments.POWER, 10);
			oItemStack.addEnchantment(Enchantments.FLAME, 1);
			oItemStack.addEnchantment(Enchantments.INFINITY, 1);
			oItemStack.setStackDisplayName("Firestarter");
			if (!oItemStack.hasTagCompound())
				oItemStack.setTagCompound(new NBTTagCompound());
			oItemStack.getTagCompound().setBoolean("Unbreakable", true);
			
			world.spawnEntity(new EntityItem(world, player.posX, player.posY, player.posZ, oItemStack));
			break;
		case 6:
			// Single Arrow: /give @p 262 1
			oItemStack = new ItemStack(Items.ARROW);
			oItemStack.setCount(1);
			oItemStack.setStackDisplayName("Firestarter Ammo");
			
			world.spawnEntity(new EntityItem(world, player.posX, player.posY, player.posZ, oItemStack));
			break;
		case 7:
			// Shovel: "/give @p 277 1 0 {Unbreakable:1,ench:[{id:32,lvl:10},{id:35,lvl:10}]}"
			oItemStack = new ItemStack(Items.DIAMOND_SHOVEL);
			oItemStack.setCount(1);
			oItemStack.addEnchantment(Enchantments.EFFICIENCY, 5); // Six too fast...
			oItemStack.addEnchantment(Enchantments.FORTUNE, 10);
			oItemStack.setStackDisplayName("Diggle");
			if (!oItemStack.hasTagCompound())
				oItemStack.setTagCompound(new NBTTagCompound());
			oItemStack.getTagCompound().setBoolean("Unbreakable", true);
			
			world.spawnEntity(new EntityItem(world, player.posX, player.posY, player.posZ, oItemStack));
			break;
		case 8:
			// Axe: "/give @p 279 1 0 {Unbreakable:1,ench:[{id:32,lvl:10},{id:35,lvl:10}]}"
			oItemStack = new ItemStack(Items.DIAMOND_AXE);
			oItemStack.setCount(1);
			oItemStack.addEnchantment(Enchantments.EFFICIENCY, 5); // Six too fast...
			oItemStack.addEnchantment(Enchantments.FORTUNE, 10);
			oItemStack.setStackDisplayName("Whirlwind");
			if (!oItemStack.hasTagCompound())
				oItemStack.setTagCompound(new NBTTagCompound());
			oItemStack.getTagCompound().setBoolean("Unbreakable", true);
			
			world.spawnEntity(new EntityItem(world, player.posX, player.posY, player.posZ, oItemStack));
			break;
		case 9:
			// Helmet: "/give @p 310 1 0 {Unbreakable:1,ench:[{id:0,lvl:10},{id:1,lvl:10},{id:5,lvl:10},{id:6,lvl:1}]}"
			oItemStack = new ItemStack(Items.DIAMOND_HELMET);
			oItemStack.setCount(1);
			oItemStack.addEnchantment(Enchantments.PROTECTION, 10);
			oItemStack.addEnchantment(Enchantments.FIRE_PROTECTION, 10);
			oItemStack.addEnchantment(Enchantments.RESPIRATION, 10);
			oItemStack.addEnchantment(Enchantments.AQUA_AFFINITY, 1);
			oItemStack.setStackDisplayName("Headguard of the Mage");
			if (!oItemStack.hasTagCompound())
				oItemStack.setTagCompound(new NBTTagCompound());
			oItemStack.getTagCompound().setBoolean("Unbreakable", true);
			
			world.spawnEntity(new EntityItem(world, player.posX, player.posY, player.posZ, oItemStack));
			break;
		case 10:
			// Chestplate: "/give @p 311 1 0 {Unbreakable:1,ench:[{id:0,lvl:10},{id:1,lvl:10}]}"
			oItemStack = new ItemStack(Items.DIAMOND_CHESTPLATE);
			oItemStack.setCount(1);
			oItemStack.addEnchantment(Enchantments.PROTECTION, 10);
			oItemStack.addEnchantment(Enchantments.FIRE_PROTECTION, 10);
			oItemStack.setStackDisplayName("Breastplate of Eternal Protection");
			if (!oItemStack.hasTagCompound())
				oItemStack.setTagCompound(new NBTTagCompound());
			oItemStack.getTagCompound().setBoolean("Unbreakable", true);
			
			world.spawnEntity(new EntityItem(world, player.posX, player.posY, player.posZ, oItemStack));
			break;
		case 11:
			// Leggings: "/give @p 312 1 0 {Unbreakable:1,ench:[{id:0,lvl:10},{id:1,lvl:10}]}"
			oItemStack = new ItemStack(Items.DIAMOND_LEGGINGS);
			oItemStack.setCount(1);
			oItemStack.addEnchantment(Enchantments.PROTECTION, 10);
			oItemStack.addEnchantment(Enchantments.FIRE_PROTECTION, 10);
			oItemStack.setStackDisplayName("Legplates of the Bear");
			if (!oItemStack.hasTagCompound())
				oItemStack.setTagCompound(new NBTTagCompound());
			oItemStack.getTagCompound().setBoolean("Unbreakable", true);
			
			world.spawnEntity(new EntityItem(world, player.posX, player.posY, player.posZ, oItemStack));
			break;
		case 12:
			// Boots: "/give @p 313 1 0 {Unbreakable:1,ench:[{id:0,lvl:10},{id:1,lvl:10},{id:2,lvl:10}]}"
			oItemStack = new ItemStack(Items.DIAMOND_BOOTS);
			oItemStack.setCount(1);
			oItemStack.addEnchantment(Enchantments.PROTECTION, 10);
			oItemStack.addEnchantment(Enchantments.FIRE_PROTECTION, 10);
			oItemStack.addEnchantment(Enchantments.FEATHER_FALLING, 10);
			oItemStack.addEnchantment(Enchantments.DEPTH_STRIDER, 10);
			oItemStack.setStackDisplayName("Walkers of Broken Visions");
			if (!oItemStack.hasTagCompound())
				oItemStack.setTagCompound(new NBTTagCompound());
			oItemStack.getTagCompound().setBoolean("Unbreakable", true);
			
			world.spawnEntity(new EntityItem(world, player.posX, player.posY, player.posZ, oItemStack));
			break;
		case 13:
			// Fishing Rod: "/give @p 346 64 0 {Unbreakable:1,ench:[{id:61,lvl:100},{id:62,lvl:8}]}"
			oItemStack = new ItemStack(Items.FISHING_ROD);
			oItemStack.setCount(1);
			oItemStack.addEnchantment(Enchantments.LUCK_OF_THE_SEA, 100); // 61 = Luck of the Sea
			oItemStack.addEnchantment(Enchantments.LURE, 8); // 62 = Lure
			oItemStack.setStackDisplayName("Raider of Poseidons Stash");
			if (!oItemStack.hasTagCompound())
				oItemStack.setTagCompound(new NBTTagCompound());
			oItemStack.getTagCompound().setBoolean("Unbreakable", true);
			
			world.spawnEntity(new EntityItem(world, player.posX, player.posY, player.posZ, oItemStack));
			break;
		case 14:
			// Fishing Rod: "/give @p 346 64 0 {Unbreakable:1,ench:[{id:62,lvl:8}]}"
			oItemStack = new ItemStack(Items.FISHING_ROD);
			oItemStack.setCount(1);
			oItemStack.addEnchantment(Enchantments.LURE, 8); // 62 = Lure
			oItemStack.setStackDisplayName("Dagons Rod");
			if (!oItemStack.hasTagCompound())
				oItemStack.setTagCompound(new NBTTagCompound());
			oItemStack.getTagCompound().setBoolean("Unbreakable", true);
			
			world.spawnEntity(new EntityItem(world, player.posX, player.posY, player.posZ, oItemStack));
			break;
		case 15:
			// Hoe: "/give @p 293 1 0 {Unbreakable:1}"
			oItemStack = new ItemStack(Items.DIAMOND_HOE);
			oItemStack.setCount(1);
			oItemStack.setStackDisplayName("Ten Dolla");
			if (!oItemStack.hasTagCompound())
				oItemStack.setTagCompound(new NBTTagCompound());
			oItemStack.getTagCompound().setBoolean("Unbreakable", true);
			
			world.spawnEntity(new EntityItem(world, player.posX, player.posY, player.posZ, oItemStack));
			break;
		case 16:
			// Shears: "/give @p 359 1 0 {Unbreakable:1,ench:[{id:32,lvl:10}]}"
			oItemStack = new ItemStack(Items.SHEARS);
			oItemStack.setCount(1);
			oItemStack.setStackDisplayName("Shawn");
			if (!oItemStack.hasTagCompound())
				oItemStack.setTagCompound(new NBTTagCompound());
			oItemStack.getTagCompound().setBoolean("Unbreakable", true);
			
			world.spawnEntity(new EntityItem(world, player.posX, player.posY, player.posZ, oItemStack));
			break;
		case 17:
			// Flint & Steel: "/give @p 259 1 0 {Unbreakable:1}"
			oItemStack = new ItemStack(Items.FLINT_AND_STEEL);
			oItemStack.setCount(1);
			oItemStack.setStackDisplayName("Firestarter");
			if (!oItemStack.hasTagCompound())
				oItemStack.setTagCompound(new NBTTagCompound());
			oItemStack.getTagCompound().setBoolean("Unbreakable", true);
			
			world.spawnEntity(new EntityItem(world, player.posX, player.posY, player.posZ, oItemStack));
			break;
		default:
			iGivenCount = -1;
			bIsWorthy = false;
		}
	}
}
