package uk.co.duelmonster.minersadvantage.utils;

import java.util.Random;
import java.util.function.Supplier;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.GiveCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import uk.co.duelmonster.minersadvantage.common.Constants;

// Server-side-safe utilities
public final class UtilsServer {
  private final static Random random = new Random();

  /**
   * Give an item to the player
   *
   * @see GiveCommand#giveItem(net.minecraft.command.CommandSource,
   *      net.minecraft.command.arguments.ItemInput, java.util.Collection, int)
   */
  public static void giveToInventory(ServerPlayer player, ItemStack itemStack) {
    ItemStack itemStackCopy = itemStack.copy();
    boolean   flag          = player.getInventory().add(itemStack);

    if (flag && itemStack.isEmpty()) {
      itemStack.setCount(1);

      ItemEntity entityitem = player.drop(itemStack, false);
      if (entityitem != null)
        entityitem.makeFakeItem();

      player.playSound(SoundEvents.ITEM_PICKUP, 0.2F, ((random.nextFloat() - random.nextFloat()) * 0.7F + 1.0F) * 2.0F);

    } else {
      ItemEntity entityitem = player.drop(itemStack, false);
      if (entityitem != null) {
        entityitem.setNoPickUpDelay();
        entityitem.setThrower(player.getUUID());
      }
    }

    notifyGive(player, itemStackCopy);
  }

  public static void setHotbarSlot(ServerPlayer player, ItemStack itemStack, int hotbarSlot) {

    if (!Inventory.isHotbarSlot(hotbarSlot)) {
      Constants.LOGGER.error("Invaild hotbar slot requested: {}", hotbarSlot);
      return;
    }

    ItemStack stackInSlot = player.getInventory().getItem(hotbarSlot);
    if (ItemStack.isSameItem(stackInSlot, itemStack))
      return;

    ItemStack itemStackCopy = itemStack.copy();
    player.getInventory().setItem(hotbarSlot, itemStack);

    player.playSound(SoundEvents.ITEM_PICKUP, 0.2F, ((random.nextFloat() - random.nextFloat()) * 0.7F + 1.0F) * 2.0F);

    notifyGive(player, itemStackCopy);
  }

  public static void setHeldItemSlot(ServerPlayer player, int hotbarSlot) {

    if (!Inventory.isHotbarSlot(hotbarSlot)) {
      Constants.LOGGER.error("Invaild hotbar slot requested: {}", hotbarSlot);
      return;
    }

    player.getInventory().selected = hotbarSlot;

    player.playSound(SoundEvents.ITEM_PICKUP, 0.2F, ((random.nextFloat() - random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
  }

  private static void notifyGive(ServerPlayer ServerPlayer, ItemStack stack) {
    int count = stack.getCount();

    CommandSourceStack commandSource = ServerPlayer.createCommandSourceStack();
    Component          stackName     = stack.getHoverName();
    Component          displayName   = ServerPlayer.getDisplayName();
    Component          message       = Component.translatable("commands.give.success.single", count, stackName, displayName);

    commandSource.sendSuccess((Supplier<Component>) message, true);
  }

}