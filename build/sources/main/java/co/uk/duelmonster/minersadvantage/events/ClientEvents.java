package co.uk.duelmonster.minersadvantage.events;

import co.uk.duelmonster.minersadvantage.MinersAdvantage;
import co.uk.duelmonster.minersadvantage.client.ClientFunctions;
import co.uk.duelmonster.minersadvantage.client.KeyBindings;
import co.uk.duelmonster.minersadvantage.common.PacketID;
import co.uk.duelmonster.minersadvantage.common.Variables;
import co.uk.duelmonster.minersadvantage.handlers.GodItems;
import co.uk.duelmonster.minersadvantage.handlers.SubstitutionHandler;
import co.uk.duelmonster.minersadvantage.packets.PacketBase;
import co.uk.duelmonster.minersadvantage.settings.Settings;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ClientEvents {
	private static int iTickCount = 10;
	
	@SubscribeEvent
	public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		ClientFunctions.doJoinWorldEventStuff();
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onItemPickup(EntityItemPickupEvent event) {
		Settings settings = Settings.get();
		
		event.setCanceled(settings.bCaptivationEnabled
				&& settings.bIsWhitelist
				&& settings.captivationBlacklist != null
				&& settings.captivationBlacklist.size() > 0
				&& settings.captivationBlacklist.has(event.getItem().getItem().getItem().getRegistryName().toString().trim()));
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onClientTick(TickEvent.ClientTickEvent event) {
		if (!TickEvent.Phase.END.equals(event.phase))
			return;
		
		iTickCount = (iTickCount + 1) % 10;
		
		Minecraft mc = ClientFunctions.getMC();
		EntityPlayer player = ClientFunctions.getPlayer();
		
		if (iTickCount <= 0 || player == null) // || mc.playerController.isInCreativeMode()
			return;
		
		Settings settings = Settings.get();
		Variables variables = Variables.get();
		if (!settings.bToggleMode)
			variables.IsExcavationToggled = KeyBindings.excavation_toggle.isKeyDown();
		variables.IsShaftanationToggled = KeyBindings.shaftanation_toggle.isKeyDown();
		variables.IsPlayerAttacking = ClientFunctions.isAttacking();
		
		ClientFunctions.syncSettings();
		
		GodItems.isWorthy(variables.IsExcavationToggled);
		
		// Fire Illumination if enabled and the player is attacking
		if (settings.bIlluminationEnabled && variables.IsPlayerAttacking) {
			NBTTagCompound tags = new NBTTagCompound();
			tags.setInteger("ID", PacketID.Illuminate.value());
			
			MinersAdvantage.instance.network.sendToServer(new PacketBase(tags));
		}
		
		// Record the block face being attacked
		if (variables.IsPlayerAttacking && mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK)
			variables.sideHit = mc.objectMouseOver.sideHit;
		
		// Cancel the running Excavation agents when player lifts the key binding
		if (settings.bExcavationEnabled && !variables.IsPlayerAttacking && !variables.IsExcavationToggled) {
			GodItems.isWorthy(false);
			
			if (variables.IsExcavating && !variables.IsVeinating) {
				variables.IsExcavating = false;
				
				NBTTagCompound tags = new NBTTagCompound();
				tags.setInteger("ID", PacketID.Excavate.value());
				tags.setBoolean("cancel", true);
				
				MinersAdvantage.instance.network.sendToServer(new PacketBase(tags));
			}
		}
		
		// Fire Captivation if enabled
		if (settings.bCaptivationEnabled && !mc.isGamePaused() && mc.inGameHasFocus) {
			NBTTagCompound tags = new NBTTagCompound();
			tags.setInteger("ID", PacketID.Captivate.value());
			
			MinersAdvantage.instance.network.sendToServer(new PacketBase(tags));
		}
		
		// Switch back to previously held item if Substitution is enabled
		if (settings.bSubstitutionEnabled && !variables.IsPlayerAttacking && settings.bSwitchBack && !variables.IsProcessing()
				&& SubstitutionHandler.instance.iPrevSlot >= 0 && player.inventory.currentItem != SubstitutionHandler.instance.iPrevSlot) {
			player.inventory.currentItem = SubstitutionHandler.instance.iPrevSlot;
			SubstitutionHandler.instance.reset();
			player.openContainer.detectAndSendChanges();
		}
	}
}
