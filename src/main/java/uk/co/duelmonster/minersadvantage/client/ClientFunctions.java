package uk.co.duelmonster.minersadvantage.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.VersionChecker;
import net.minecraftforge.fml.VersionChecker.CheckResult;
import net.minecraftforge.fml.VersionChecker.Status;
import uk.co.duelmonster.minersadvantage.common.Constants;
import uk.co.duelmonster.minersadvantage.common.Functions;

@OnlyIn(Dist.CLIENT)
public class ClientFunctions {
	
	public static Minecraft	mc				= null;
	private static boolean	updateNotified	= false;
	
	public static ClientPlayerEntity getPlayer() {
		return mc != null ? mc.player : null;
	}
	
	public static boolean isAttacking() {
		return mc.gameSettings.keyBindAttack.isKeyDown();
	}
	
	public static boolean isUsingItem() {
		return mc.gameSettings.keyBindUseItem.isKeyDown();
	}
	
	// public static int getKeyIndexEx(String name) {
	// int index = Mouse.getButtonIndex(name);
	// if (index == -1) {
	// return Keyboard.getKeyIndex(name);
	// } else {
	// return index - 100;
	// }
	// }
	//
	// public static boolean isKeyDownEx(String name) {
	// return isKeyDownEx(getKeyIndexEx(name));
	// }
	//
	// public static boolean isKeyDownEx(int index) {
	// if (index < 0) {
	// return Mouse.isButtonDown(index + 100);
	// } else {
	// return Keyboard.isKeyDown(index);
	// }
	// }
	
	public static void DebugNotifyClient(String sMsg) {
		Functions.DebugNotifyClient(getPlayer(), sMsg);
	}
	
	public static void DebugNotifyClient(boolean bIsOn, String sFeatureName) {
		Functions.DebugNotifyClient(getPlayer(), bIsOn, sFeatureName);
	}
	
	public static void NotifyClient(String sMsg) {
		Functions.NotifyClient(getPlayer(), sMsg);
	}
	
	public static void NotifyClient(boolean bIsOn, String sFeatureName) {
		Functions.NotifyClient(getPlayer(), bIsOn, sFeatureName);
	}
	
	public static void playSound(World world, SoundEvent sound, BlockPos pos) {
		world.playSound(getPlayer(), pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, sound, SoundCategory.BLOCKS, 2.0F, 1.0F);
	}
	
	public static void doJoinWorldEventStuff() {
		if (!updateNotified) {
			updateNotified = true;
			
			CheckResult verCheck = VersionChecker.getResult(ModList.get().getModFileById(Constants.MOD_ID).getMods().get(0));
			if (verCheck.status == Status.OUTDATED)
				Functions.NotifyClient(getPlayer(), TextFormatting.GRAY + Functions.localize("minersadvantage.version.update") + ": " + TextFormatting.GREEN + verCheck.target.toString());
		}
	}
	
	/**
	 * Sync's the players currently held item to the server
	 */
	public static void syncCurrentPlayItem(int iSlotIndx) {
		getPlayer().inventory.currentItem = iSlotIndx;
		
		// MA.NETWORK.sendToServer(new CHeldItemChangePacket(iSlotIndx));
		mc.playerController.tick();
		
		// Pause the current Thread to allow the server to catch up and realize that we
		// have changed the slot.
		// Without this sleep, we can end up with phantom blocks.
		Functions.sleep(100);
	}
}
