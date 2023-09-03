package uk.co.duelmonster.minersadvantage.common;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.Tags;
import uk.co.duelmonster.minersadvantage.config.MAConfig;
import uk.co.duelmonster.minersadvantage.config.SyncedClientConfig;

public class Functions {

  public static String localize(String key) {
    return Component.translatable(key).toString();
  }

  public static boolean isDebug() {
    return java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().stream().anyMatch(s -> s.contains("jdwp"));
  }

  public static void DebugNotifyClient(Player player, String sMsg) {
    if (isDebug()) {
      NotifyClient(player, sMsg);
    }
  }

  public static void DebugNotifyClient(Player player, boolean bIsOn, String sFeatureName) {
    if (isDebug()) {
      NotifyClient(player, bIsOn, sFeatureName);
    }
  }

  public static void NotifyClient(Player player, String sMsg) {
    player.displayClientMessage(Component.literal(Constants.MOD_NAME_MSG + sMsg), false);
  }

  public static void NotifyClient(Player player, boolean bIsOn, String sFeatureName) {
    player.displayClientMessage(Component.literal(Constants.MOD_NAME_MSG + ChatFormatting.GOLD + sFeatureName + " " + (bIsOn ? ChatFormatting.GREEN + "ON" : ChatFormatting.RED + "OFF")), false);
  }

  public static ItemStack getHeldItemStack(Player player) {
    ItemStack heldItem = player.getItemBySlot(EquipmentSlot.MAINHAND);
    return (heldItem == null || heldItem.isEmpty()) ? null : heldItem;
  }

  public static Item getHeldItem(Player player) {
    ItemStack heldItem = getHeldItemStack(player);
    return heldItem == null ? null : heldItem.getItem();
  }

  public static boolean IsPlayerStarving(Player player) {
    if (!Variables.get(player.getUUID()).HungerNotified && player.getFoodData().getFoodLevel() <= Constants.MIN_HUNGER) {
      NotifyClient(player, ChatFormatting.RED + localize("minersadvantage.hungery") + Constants.MOD_NAME);
      Variables.get(player.getUUID()).HungerNotified = true;
    }

    return Variables.get(player.getUUID()).HungerNotified;
  }

  public static Direction getPlayerFacing(Player player) {
    Vec3 lookVec = player.getLookAngle();
    return Direction.getNearest((float) lookVec.x, (float) lookVec.y, (float) lookVec.z);
  }

  public static String getStackTrace() {
    String              sRtrn = "";
    StackTraceElement[] stes  = Thread.currentThread().getStackTrace();

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

  public static boolean isPosEqual(BlockPos sourcePos, BlockPos comparePos) {
    return comparePos != null && sourcePos != null &&
        comparePos.getX() == sourcePos.getX() &&
        comparePos.getY() == sourcePos.getY() &&
        comparePos.getZ() == sourcePos.getZ();
  }

  public static boolean isWithinArea(BlockPos pos, AABB area) {
    return area != null &&
        pos.getX() >= area.minX && pos.getX() <= area.maxX &&
        pos.getY() >= area.minY && pos.getY() <= area.maxY &&
        pos.getZ() >= area.minZ && pos.getZ() <= area.maxZ;
  }

  public static boolean isWithinArea(Entity entity, AABB area) {
    return area != null &&
        entity.getX() >= area.minX && entity.getX() <= area.maxX &&
        entity.getY() >= area.minY && entity.getY() <= area.maxY &&
        entity.getZ() >= area.minZ && entity.getZ() <= area.maxZ;
  }

  public static List<BlockPos> getAllPositionsInArea(AABB area) {
    List<BlockPos> positions = new ArrayList<BlockPos>();

    for (int y = (int) area.minY; y <= area.maxY; y++)
      for (int x = (int) area.minX; x <= area.maxX; x++)
        for (int z = (int) area.minZ; z <= area.maxZ; z++)
          positions.add(new BlockPos(x, y, z));

    return positions;
  }

  public static boolean isPosConnected(List<BlockPos> posList, BlockPos checkPos) {
    // Ensure we have a direct connection to the current Tree.
    for (int yOffset = -1; yOffset <= 1; yOffset++)
      for (int xOffset = -1; xOffset <= 1; xOffset++)
        for (int zOffset = -1; zOffset <= 1; zOffset++) {
          BlockPos oConPos = checkPos.offset(xOffset, yOffset, zOffset);

          if (posList.contains(oConPos))
            return true;
        }

    return false;
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public static List<Entity> getNearbyEntities(Level world, AABB area) {
    List<Entity> rtrn = new ArrayList();
    try {
      List<?> list = new ArrayList(world.getEntitiesOfClass(Entity.class, area));
      if (null == list || list.isEmpty())
        return null;

      for (Object o : list) {
        if (o != null) {
          Entity e = (Entity) o;

          if (e.isAlive() && (e instanceof ItemEntity || e instanceof ExperienceOrb))
            // && isEntityWithinArea(e, area))
            rtrn.add(e);
        }
      }
      return rtrn;

    } catch (ConcurrentModificationException e) {
      Constants.LOGGER.error("ConcurrentModification Exception Avoided...");
    } catch (IllegalStateException e) {
      Constants.LOGGER.error("IllegalStateException Exception Avoided...");
    } catch (Exception ex) {
      Constants.LOGGER.error(ex.getClass().getName() + " Exception: " + getStackTrace());
    }
    return rtrn;
  }

  public static void playSound(Level world, BlockPos pos, SoundEvent sound, SoundSource soundSource, float volume, float pitch) {
    playSound(world, null, pos, sound, soundSource, volume, pitch);
  }

  public static void playSound(Level world, Player player, BlockPos pos, SoundEvent sound, SoundSource soundSource, float volume, float pitch) {
    world.playSound(player, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, sound, soundSource, volume, pitch);
  }

  public static void spawnAreaEffectCloud(Level world, Player entity, BlockPos pos) {
    if (!MAConfig.CLIENT.disableParticleEffects()) {
      AreaEffectCloud effectCloud = new AreaEffectCloud(world, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
      effectCloud.setOwner(entity);
      effectCloud.setRadius(1.0F);
      effectCloud.setRadiusOnUse(-0.5F);
      effectCloud.setWaitTime(1);
      effectCloud.setDuration(20);
      effectCloud.setRadiusPerTick(-effectCloud.getRadius() / effectCloud.getDuration());
      effectCloud.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 10));

      world.addFreshEntity(effectCloud);
    }
  }

  /**
   * Finds the stack or an equivalent one in the main inventory
   */
  public static int getSlotFromInventory(Player player, ItemStack stack) {
    for (int i = 0; i < player.getInventory().items.size(); ++i) {
      ItemStack compare = player.getInventory().items.get(i);
      if (!compare.isEmpty() && ItemStack.isSameItem(stack, compare))
        return i;
    }
    return -1;
  }

  public static ItemStack getStackOfClassTypeFromHotBar(Inventory inventory, Class<?> classType) {
    return getStackOfClassTypeFromInventory(9, inventory, classType);
  }

  public static ItemStack getStackOfClassTypeFromInventory(Inventory inventory, Class<?> classType) {
    return getStackOfClassTypeFromInventory(inventory.getContainerSize(), inventory, classType);
  }

  private static ItemStack getStackOfClassTypeFromInventory(int iInventorySize, Inventory inventory, Class<?> classType) {
    try {
      for (int iSlot = 0; iSlot < iInventorySize; iSlot++) {
        ItemStack itemStack = inventory.getItem(iSlot);
        if (itemStack != null && classType.isInstance(itemStack.getItem()))
          return itemStack;
      }
    } catch (Exception ex) {
      Constants.LOGGER.error(ex);
    }
    return null;
  }

  public static NonNullList<ItemStack> getAllStacksOfClassTypeFromInventory(Inventory inventory, Class<?> classType) {
    NonNullList<ItemStack> rtnStacks = NonNullList.create();
    try {
      for (int iSlot = 0; iSlot < inventory.getContainerSize(); iSlot++) {
        ItemStack itemStack = inventory.getItem(iSlot);
        if (itemStack != null && itemStack.getItem() instanceof BlockItem && classType.isInstance(((BlockItem) itemStack.getItem()).getBlock()))
          rtnStacks.add(itemStack);
      }
    } catch (Exception ex) {
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
    return item.getDescription().getString();
  }

  public static String getName(Block block) {
    return block.getName().getString();
  }

  public static String getName(BlockState state) {
    return state.getBlock().getName().getString();
  }

  public static Block getBlockFromWorld(Level world, BlockPos pos) {
    return world.getBlockState(pos).getBlock();
  }

  public static void sleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {}
  }

  public static boolean canSustainPlant(Level world, BlockPos pos, IPlantable plantable) {
    return world.getBlockState(pos).canSustainPlant(world, pos, Direction.UP, plantable);
  }

  public static boolean isValidOre(BlockState state, SyncedClientConfig clientConfig) {
    return state.is(Tags.Blocks.ORES) ||
        (clientConfig.veination.ores != null && clientConfig.veination.ores.isEmpty() == false
            && clientConfig.veination.ores.contains(Functions.getName(state.getBlock().asItem())));
  }

  public static void setFinalFieldValue(Object owner, Field field, Object value) throws Exception {
    field.setAccessible(true);
    field.set(owner, value);
  }

  public static boolean isBlockSame(Block source, Block compare) {
    return compare.getClass().isInstance(source);
  }

}
