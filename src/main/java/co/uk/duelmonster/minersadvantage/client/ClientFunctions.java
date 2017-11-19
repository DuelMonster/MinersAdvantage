package co.uk.duelmonster.minersadvantage.client;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import co.uk.duelmonster.minersadvantage.MinersAdvantage;
import co.uk.duelmonster.minersadvantage.common.Constants;
import co.uk.duelmonster.minersadvantage.common.Functions;
import co.uk.duelmonster.minersadvantage.common.JsonHelper;
import co.uk.duelmonster.minersadvantage.common.PacketID;
import co.uk.duelmonster.minersadvantage.common.Variables;
import co.uk.duelmonster.minersadvantage.packets.NetworkPacket;
import co.uk.duelmonster.minersadvantage.settings.Settings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.ForgeVersion.CheckResult;
import net.minecraftforge.common.ForgeVersion.Status;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class ClientFunctions {
	
	private static boolean updateNotified = false;
	
	public static void syncSettings() {
		
		boolean hasChanged = false;
		
		NBTTagCompound tags = new NBTTagCompound();
		tags.setInteger("ID", PacketID.SyncSettings.value());
		
		Settings settings = Settings.get();
		if (settings.hasChanged()) {
			tags.setString("settings", JsonHelper.gson.toJson(settings));
			hasChanged = true;
		}
		
		Variables variables = Variables.get();
		if (variables.hasChanged()) {
			tags.setString("variables", JsonHelper.gson.toJson(variables));
			hasChanged = true;
		}
		
		if (hasChanged)
			MinersAdvantage.instance.network.sendToServer(new NetworkPacket(tags));
	}
	
	public static Minecraft getMC() {
		return FMLClientHandler.instance().getClient();
	}
	
	public static EntityPlayer getPlayer() {
		return getMC().player;
	}
	
	public static boolean isAttacking() {
		return getMC().gameSettings.keyBindAttack.isKeyDown();
	}
	
	public static boolean isUsingItem() {
		return getMC().gameSettings.keyBindUseItem.isKeyDown();
	}
	
	public static int getKeyIndexEx(String name) {
		int index = Mouse.getButtonIndex(name);
		if (index == -1) {
			return Keyboard.getKeyIndex(name);
		} else {
			return index - 100;
		}
	}
	
	public static boolean isKeyDownEx(String name) {
		return isKeyDownEx(getKeyIndexEx(name));
	}
	
	public static boolean isKeyDownEx(int index) {
		if (index < 0) {
			return Mouse.isButtonDown(index + 100);
		} else {
			return Keyboard.isKeyDown(index);
		}
	}
	
	public static void NotifyClient(String sMsg) {
		Functions.NotifyClient(getPlayer(), sMsg);
	}
	
	public static void NotifyClient(boolean bIsOn, String sFeatureName) {
		Functions.NotifyClient(getPlayer(), bIsOn, sFeatureName);
	}
	
	public static void playSound(World world, SoundEvent sound, BlockPos oPos) {
		world.playSound(getPlayer(), oPos.getX() + 0.5F, oPos.getY() + 0.5F, oPos.getZ() + 0.5F, sound, SoundCategory.BLOCKS, 2.0F, 1.0F);
	}
	
	public static void doJoinWorldEventStuff() {
		if (!updateNotified) {
			updateNotified = true;
			
			CheckResult verCheck = ForgeVersion.getResult(FMLCommonHandler.instance().findContainerFor(Constants.MOD_ID));
			if (verCheck.status == Status.OUTDATED)
				Functions.NotifyClient(getPlayer(), "§7" + Functions.localize("minersadvantage.version.update") + ": v§a" + verCheck.target.toString());
		}
	}
	
	/**
	 * Sync's the players currently held item to the server
	 */
	public static void syncCurrentPlayItem(int iSlotIndx) {
		((NetHandlerPlayClient) FMLCommonHandler.instance().getClientPlayHandler()).sendPacket(new CPacketHeldItemChange(iSlotIndx));
		// Pause the current Thread to allow the server to catch up and realize that we have changed the slot.
		// Without this sleep, we can end up with phantom blocks.
		Functions.sleep(100);
	}
}
