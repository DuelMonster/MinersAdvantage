package uk.co.duelmonster.minersadvantage.utils;

import net.minecraft.command.CommandSource;
import net.minecraft.command.impl.GiveCommand;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import uk.co.duelmonster.minersadvantage.common.Constants;

// Server-side-safe utilities
public final class UtilsServer {
	
	/**
	 * Give an item to the player
	 *
	 * @see GiveCommand#giveItem(net.minecraft.command.CommandSource, net.minecraft.command.arguments.ItemInput, java.util.Collection, int)
	 */
	public static void giveToInventory(ServerPlayerEntity player, ItemStack itemStack) {
		ItemStack itemStackCopy = itemStack.copy();
		boolean flag = player.inventory.addItemStackToInventory(itemStack);
		
		if (flag && itemStack.isEmpty()) {
			itemStack.setCount(1);
			
			ItemEntity entityitem = player.dropItem(itemStack, false);
			if (entityitem != null)
				entityitem.makeFakeItem();
			
			BlockPos playerPos = player.getPosition();
			player.world.playSound(null, playerPos.getX(), playerPos.getY(), playerPos.getZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ((player.getRNG().nextFloat() - player.getRNG().nextFloat()) * 0.7F + 1.0F) * 2.0F);
			player.container.detectAndSendChanges();
			
		} else {
			ItemEntity entityitem = player.dropItem(itemStack, false);
			if (entityitem != null) {
				entityitem.setNoPickupDelay();
				entityitem.setOwnerId(player.getUniqueID());
			}
		}
		
		notifyGive(player, itemStackCopy);
	}
	
	public static void setHotbarSlot(ServerPlayerEntity player, ItemStack itemStack, int hotbarSlot) {
		
		if (!PlayerInventory.isHotbar(hotbarSlot)) {
			Constants.LOGGER.error("Invaild hotbar slot requested: {}", hotbarSlot);
			return;
		}
		
		ItemStack stackInSlot = player.inventory.getStackInSlot(hotbarSlot);
		if (ItemStack.areItemStacksEqual(stackInSlot, itemStack))
			return;
		
		ItemStack itemStackCopy = itemStack.copy();
		player.inventory.setInventorySlotContents(hotbarSlot, itemStack);
		
		BlockPos playerPos = player.getPosition();
		player.world.playSound(null, playerPos.getX(), playerPos.getY(), playerPos.getZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ((player.getRNG().nextFloat() - player.getRNG().nextFloat()) * 0.7F + 1.0F) * 2.0F);
		player.container.detectAndSendChanges();
		
		notifyGive(player, itemStackCopy);
	}
	
	public static void setHeldItemSlot(ServerPlayerEntity player, int hotbarSlot) {
		
		if (!PlayerInventory.isHotbar(hotbarSlot)) {
			Constants.LOGGER.error("Invaild hotbar slot requested: {}", hotbarSlot);
			return;
		}
		
		player.inventory.currentItem = hotbarSlot;
		
		BlockPos playerPos = player.getPosition();
		player.world.playSound(null, playerPos.getX(), playerPos.getY(), playerPos.getZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ((player.getRNG().nextFloat() - player.getRNG().nextFloat()) * 0.7F + 1.0F) * 2.0F);
		player.container.detectAndSendChanges();
	}
	
	private static void notifyGive(ServerPlayerEntity ServerPlayerEntity, ItemStack stack) {
		int count = stack.getCount();
		
		CommandSource commandSource = ServerPlayerEntity.getCommandSource();
		ITextComponent stackTextComponent = stack.getTextComponent();
		ITextComponent displayName = ServerPlayerEntity.getDisplayName();
		TranslationTextComponent message = new TranslationTextComponent("commands.give.success.single", count, stackTextComponent, displayName);
		
		commandSource.sendFeedback(message, true);
	}
	
}