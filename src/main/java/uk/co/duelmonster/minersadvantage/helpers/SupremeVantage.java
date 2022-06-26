package uk.co.duelmonster.minersadvantage.helpers;

import org.lwjgl.glfw.GLFW;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import uk.co.duelmonster.minersadvantage.MA;
import uk.co.duelmonster.minersadvantage.client.ClientFunctions;
import uk.co.duelmonster.minersadvantage.client.KeyBindings;
import uk.co.duelmonster.minersadvantage.common.Functions;
import uk.co.duelmonster.minersadvantage.config.MAConfig;
import uk.co.duelmonster.minersadvantage.network.packets.PacketSupremeVantage;

public class SupremeVantage {
  private static final String CODE_N      = "2780872";
  private static final String CODE_D      = "3780873";
  private static String       vantageCode = "";
  private static int          tickCount   = 0;
  private static boolean      isWorthy    = false;
  private static int          givenCount  = 0;

  @OnlyIn(Dist.CLIENT)
  public static void isWorthy(boolean bToggled) {
    if (MAConfig.CLIENT.allEnabled()) {
      tickCount++;
      if (bToggled) {

        if (KeyBindings.isDown(GLFW.GLFW_KEY_0) && !codeEndsWith(GLFW.GLFW_KEY_0))
          vantageCode += KeyBindings.getKeyName(GLFW.GLFW_KEY_0);
        if (KeyBindings.isDown(GLFW.GLFW_KEY_2) && !codeEndsWith(GLFW.GLFW_KEY_2))
          vantageCode += KeyBindings.getKeyName(GLFW.GLFW_KEY_2);
        if (KeyBindings.isDown(GLFW.GLFW_KEY_7) && !codeEndsWith(GLFW.GLFW_KEY_7))
          vantageCode += KeyBindings.getKeyName(GLFW.GLFW_KEY_7);
        if (KeyBindings.isDown(GLFW.GLFW_KEY_8) && !codeEndsWith(GLFW.GLFW_KEY_8))
          vantageCode += KeyBindings.getKeyName(GLFW.GLFW_KEY_8);

        tickCount = 0;

      } else if (!isWorthy && !vantageCode.isEmpty() && (vantageCode.equalsIgnoreCase(CODE_N) || vantageCode.equalsIgnoreCase(CODE_D))) {

        Functions.NotifyClient(ClientFunctions.getPlayer(), Functions.localize("minersadvantage.supremevantage"));
        isWorthy   = true;
        givenCount = 0;
        tickCount  = 0;

      } else if (isWorthy && tickCount >= 5) {

        MA.NETWORK.sendToServer(new PacketSupremeVantage(vantageCode));
        tickCount = 0;

      }

      if ((!bToggled && !isWorthy && vantageCode != "" && tickCount > 0 && tickCount <= 5) || tickCount >= 1000) {
        vantageCode = "";
        tickCount   = 0;
      }
    }

  }

  private static boolean codeEndsWith(int keyCode) {
    return vantageCode.endsWith(KeyBindings.getKeyName(keyCode));
  }

  public static void GiveSupremeVantage(ServerPlayer player, String code) {
    givenCount++;
    ItemStack oItemStack = null;

    switch (givenCount) {
      case 1:
        // Sword - Fire Aspect
        oItemStack = new ItemStack(code.equals(CODE_N) ? Items.NETHERITE_SWORD : Items.DIAMOND_SWORD);
        oItemStack.setCount(1);
        oItemStack.enchant(Enchantments.SHARPNESS, 10);
        oItemStack.enchant(Enchantments.SWEEPING_EDGE, 10);
        oItemStack.enchant(Enchantments.FIRE_ASPECT, 2);
        oItemStack.enchant(Enchantments.MOB_LOOTING, 10);
        oItemStack.setHoverName(new TextComponent("Soulblade" + (code.equals(CODE_N) ? " Rite" : "")));
        oItemStack.getOrCreateTag().putBoolean("Unbreakable", true);

        break;
      case 2:
        // Sword
        oItemStack = new ItemStack(code.equals(CODE_N) ? Items.NETHERITE_SWORD : Items.DIAMOND_SWORD);
        oItemStack.setCount(1);
        oItemStack.enchant(Enchantments.SHARPNESS, 10);
        oItemStack.enchant(Enchantments.SWEEPING_EDGE, 10);
        oItemStack.enchant(Enchantments.MOB_LOOTING, 10);
        oItemStack.setHoverName(new TextComponent("Peacekeeper" + (code.equals(CODE_N) ? " Rite" : "")));
        oItemStack.getOrCreateTag().putBoolean("Unbreakable", true);
        break;
      case 3:
        // Pickaxe - Fortune
        oItemStack = new ItemStack(code.equals(CODE_N) ? Items.NETHERITE_PICKAXE : Items.DIAMOND_PICKAXE);
        oItemStack.setCount(1);
        oItemStack.enchant(Enchantments.BLOCK_EFFICIENCY, 5); // Six too fast...
        oItemStack.enchant(Enchantments.BLOCK_FORTUNE, 10);
        oItemStack.setHoverName(new TextComponent("Minora" + (code.equals(CODE_N) ? " Rite" : "")));
        oItemStack.getOrCreateTag().putBoolean("Unbreakable", true);
        break;
      case 4:
        // Pickaxe - Silktouch
        oItemStack = new ItemStack(code.equals(CODE_N) ? Items.NETHERITE_PICKAXE : Items.DIAMOND_PICKAXE);
        oItemStack.setCount(1);
        oItemStack.enchant(Enchantments.BLOCK_EFFICIENCY, 5); // Six too fast...
        oItemStack.enchant(Enchantments.SILK_TOUCH, 1);
        oItemStack.setHoverName(new TextComponent("Silkar" + (code.equals(CODE_N) ? " Rite" : "")));
        oItemStack.getOrCreateTag().putBoolean("Unbreakable", true);
        break;
      case 5:
        // Shovel
        oItemStack = new ItemStack(code.equals(CODE_N) ? Items.NETHERITE_SHOVEL : Items.DIAMOND_SHOVEL);
        oItemStack.setCount(1);
        oItemStack.enchant(Enchantments.BLOCK_EFFICIENCY, 5); // Six too fast...
        oItemStack.enchant(Enchantments.BLOCK_FORTUNE, 10);
        oItemStack.setHoverName(new TextComponent("Diggle" + (code.equals(CODE_N) ? " Rite" : "")));
        oItemStack.getOrCreateTag().putBoolean("Unbreakable", true);
        break;
      case 6:
        // Axe
        oItemStack = new ItemStack(code.equals(CODE_N) ? Items.NETHERITE_AXE : Items.DIAMOND_AXE);
        oItemStack.setCount(1);
        oItemStack.enchant(Enchantments.BLOCK_EFFICIENCY, 5); // Six too fast...
        oItemStack.enchant(Enchantments.BLOCK_FORTUNE, 10);
        oItemStack.setHoverName(new TextComponent("Whirlwind" + (code.equals(CODE_N) ? " Rite" : "")));
        oItemStack.getOrCreateTag().putBoolean("Unbreakable", true);
        break;
      case 7:
        // Helmet
        oItemStack = new ItemStack(code.equals(CODE_N) ? Items.NETHERITE_HELMET : Items.DIAMOND_HELMET);
        oItemStack.setCount(1);
        oItemStack.enchant(Enchantments.FIRE_PROTECTION, 10);
        oItemStack.enchant(Enchantments.RESPIRATION, 10);
        oItemStack.enchant(Enchantments.AQUA_AFFINITY, 1);
        oItemStack.setHoverName(new TextComponent("Tadpols Scuba Helm" + (code.equals(CODE_N) ? " Rite" : "")));
        oItemStack.getOrCreateTag().putBoolean("Unbreakable", true);
        break;
      case 8:
        // Chestplate
        oItemStack = new ItemStack(code.equals(CODE_N) ? Items.NETHERITE_CHESTPLATE : Items.DIAMOND_CHESTPLATE);
        oItemStack.setCount(1);
        oItemStack.enchant(Enchantments.FIRE_PROTECTION, 10);
        oItemStack.setHoverName(new TextComponent("Breastplate of Black Bones" + (code.equals(CODE_N) ? " Rite" : "")));
        oItemStack.getOrCreateTag().putBoolean("Unbreakable", true);
        break;
      case 9:
        // Elytra
        oItemStack = new ItemStack(Items.ELYTRA);
        oItemStack.setCount(1);
        oItemStack.setHoverName(new TextComponent("Pegasus' Wings"));
        oItemStack.getOrCreateTag().putBoolean("Unbreakable", true);
        break;
      case 10:
        // Leggings
        oItemStack = new ItemStack(code.equals(CODE_N) ? Items.NETHERITE_LEGGINGS : Items.DIAMOND_LEGGINGS);
        oItemStack.setCount(1);
        oItemStack.enchant(Enchantments.FIRE_PROTECTION, 10);
        oItemStack.setHoverName(new TextComponent("Wizadora's Legplates" + (code.equals(CODE_N) ? " Rite" : "")));
        oItemStack.getOrCreateTag().putBoolean("Unbreakable", true);
        break;
      case 11:
        // Boots
        oItemStack = new ItemStack(code.equals(CODE_N) ? Items.NETHERITE_BOOTS : Items.DIAMOND_BOOTS);
        oItemStack.setCount(1);
        oItemStack.enchant(Enchantments.FIRE_PROTECTION, 10);
        oItemStack.enchant(Enchantments.FALL_PROTECTION, 10);
        oItemStack.enchant(Enchantments.DEPTH_STRIDER, 10);
        oItemStack.setHoverName(new TextComponent("Victims Souls" + (code.equals(CODE_N) ? " Rite" : "")));
        oItemStack.getOrCreateTag().putBoolean("Unbreakable", true);
        break;
      case 12:
        // Bow
        oItemStack = new ItemStack(Items.BOW);
        oItemStack.setCount(1);
        oItemStack.enchant(Enchantments.POWER_ARROWS, 10);
        oItemStack.enchant(Enchantments.FLAMING_ARROWS, 1);
        oItemStack.enchant(Enchantments.INFINITY_ARROWS, 1);
        oItemStack.setHoverName(new TextComponent("Firestarter"));
        oItemStack.getOrCreateTag().putBoolean("Unbreakable", true);
        break;
      case 13:
        // Single Arrow
        oItemStack = new ItemStack(Items.ARROW);
        oItemStack.setCount(1);
        oItemStack.setHoverName(new TextComponent("Firestarter Ammo"));
        break;
      case 14:
        // Crossbow
        oItemStack = new ItemStack(Items.CROSSBOW);
        oItemStack.setCount(1);
        oItemStack.enchant(Enchantments.MULTISHOT, 1);
        oItemStack.enchant(Enchantments.PIERCING, 5);
        oItemStack.enchant(Enchantments.QUICK_CHARGE, 5);
        oItemStack.setHoverName(new TextComponent("Penertrator"));
        oItemStack.getOrCreateTag().putBoolean("Unbreakable", true);
        break;
      case 15:
        // Trident
        oItemStack = new ItemStack(Items.TRIDENT);
        oItemStack.setCount(1);
        oItemStack.enchant(Enchantments.RIPTIDE, 10);
        oItemStack.enchant(Enchantments.IMPALING, 10);
        oItemStack.setHoverName(new TextComponent("Poseidons Rocket"));
        oItemStack.getOrCreateTag().putBoolean("Unbreakable", true);
        break;
      case 16:
        // Trident
        oItemStack = new ItemStack(Items.TRIDENT);
        oItemStack.setCount(1);
        oItemStack.enchant(Enchantments.LOYALTY, 10);
        oItemStack.enchant(Enchantments.IMPALING, 10);
        oItemStack.enchant(Enchantments.CHANNELING, 1);
        oItemStack.setHoverName(new TextComponent("Poseidons Fork"));
        oItemStack.getOrCreateTag().putBoolean("Unbreakable", true);
        break;
      case 17:
        // Hoe
        oItemStack = new ItemStack(code.equals(CODE_N) ? Items.NETHERITE_HOE : Items.DIAMOND_HOE);
        oItemStack.setCount(1);
        oItemStack.setHoverName(new TextComponent("Ten Dolla" + (code.equals(CODE_N) ? " Rite" : "")));
        oItemStack.getOrCreateTag().putBoolean("Unbreakable", true);
        break;
      case 18:
        // Shears
        oItemStack = new ItemStack(Items.SHEARS);
        oItemStack.setCount(1);
        oItemStack.setHoverName(new TextComponent("Shawn"));
        oItemStack.getOrCreateTag().putBoolean("Unbreakable", true);
        break;
      case 19:
        // Flint & Steel
        oItemStack = new ItemStack(Items.FLINT_AND_STEEL);
        oItemStack.setCount(1);
        oItemStack.setHoverName(new TextComponent("Pyro"));
        oItemStack.getOrCreateTag().putBoolean("Unbreakable", true);
        break;
      case 20:
        // Fishing Rod - Luck of the Sea
        oItemStack = new ItemStack(Items.FISHING_ROD);
        oItemStack.setCount(1);
        oItemStack.enchant(Enchantments.FISHING_LUCK, 100); // 61 = Luck of the Sea
        oItemStack.setHoverName(new TextComponent("Raider of Poseidons Stash"));
        oItemStack.getOrCreateTag().putBoolean("Unbreakable", true);
        break;
      case 21:
        // Fishing Rod - Lure
        oItemStack = new ItemStack(Items.FISHING_ROD);
        oItemStack.setCount(1);
        oItemStack.enchant(Enchantments.FISHING_SPEED, 8); // 62 = Lure
        oItemStack.setHoverName(new TextComponent("Rodney"));
        oItemStack.getOrCreateTag().putBoolean("Unbreakable", true);
        break;
      default:
        givenCount = -1;
        vantageCode = "";
        isWorthy = false;
    }

    if (oItemStack != null) {
      BlockPos playerPos = player.blockPosition();
      player.level.addFreshEntity(new ItemEntity(player.level, playerPos.getX(), playerPos.getY(), playerPos.getZ(), oItemStack));
    }
  }
}
