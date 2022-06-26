package uk.co.duelmonster.minersadvantage.utils;

import java.util.Random;

import net.minecraft.command.CommandSource;
import net.minecraft.command.impl.GiveCommand;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
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
  public static void giveToInventory(ServerPlayerEntity player, ItemStack itemStack) {
    ItemStack itemStackCopy = itemStack.copy();
    boolean   flag          = player.inventory.add(itemStack);

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
        entityitem.setOwner(player.getUUID());
      }
    }

    notifyGive(player, itemStackCopy);
  }

  public static void setHotbarSlot(ServerPlayerEntity player, ItemStack itemStack, int hotbarSlot) {

    if (!PlayerInventory.isHotbarSlot(hotbarSlot)) {
      Constants.LOGGER.error("Invaild hotbar slot requested: {}", hotbarSlot);
      return;
    }

    ItemStack stackInSlot = player.inventory.getItem(hotbarSlot);
    if (ItemStack.isSame(stackInSlot, itemStack))
      return;

    ItemStack itemStackCopy = itemStack.copy();
    player.inventory.setItem(hotbarSlot, itemStack);

    player.playSound(SoundEvents.ITEM_PICKUP, 0.2F, ((random.nextFloat() - random.nextFloat()) * 0.7F + 1.0F) * 2.0F);

    notifyGive(player, itemStackCopy);
  }

  public static void setHeldItemSlot(ServerPlayerEntity player, int hotbarSlot) {

    if (!PlayerInventory.isHotbarSlot(hotbarSlot)) {
      Constants.LOGGER.error("Invaild hotbar slot requested: {}", hotbarSlot);
      return;
    }

    player.inventory.selected = hotbarSlot;

    player.playSound(SoundEvents.ITEM_PICKUP, 0.2F, ((random.nextFloat() - random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
  }

  private static void notifyGive(ServerPlayerEntity ServerPlayerEntity, ItemStack stack) {
    int count = stack.getCount();

    CommandSource            commandSource = ServerPlayerEntity.createCommandSourceStack();
    ITextComponent           stackName     = stack.getHoverName();
    ITextComponent           displayName   = ServerPlayerEntity.getDisplayName();
    TranslationTextComponent message       = new TranslationTextComponent("commands.give.success.single", count, stackName, displayName);

    commandSource.sendSuccess(message, true);
  }

}