package co.uk.duelmonster.minersadvantage.events;

import co.uk.duelmonster.minersadvantage.MinersAdvantage;
import co.uk.duelmonster.minersadvantage.client.ClientFunctions;
import co.uk.duelmonster.minersadvantage.client.KeyBindings;
import co.uk.duelmonster.minersadvantage.common.Constants;
import co.uk.duelmonster.minersadvantage.common.PacketID;
import co.uk.duelmonster.minersadvantage.common.Variables;
import co.uk.duelmonster.minersadvantage.config.MAConfig;
import co.uk.duelmonster.minersadvantage.handlers.IlluminationHandler;
import co.uk.duelmonster.minersadvantage.packets.NetworkPacket;
import co.uk.duelmonster.minersadvantage.settings.ConfigHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

public class KeyInputEvents {
	@SubscribeEvent
	public void onKeyInput(InputEvent.KeyInputEvent event) {
		// checking inGameHasFocus prevents your keys from firing when the player is typing a chat message
		// NOTE that the KeyInputEvent will NOT be posted when a gui screen such as the inventory is open
		if (FMLClientHandler.instance().getClient().inGameHasFocus) {
			MAConfig settings = MAConfig.get();
			Variables variables = Variables.get();
			
			if (KeyBindings.captivation.isPressed()) {
				settings.captivation.setEnabled(!settings.captivation.bEnabled());
				ConfigHandler.setValue(Constants.CAPTIVATION_ID, "enabled", settings.captivation.bEnabled());
				ClientFunctions.NotifyClient(settings.captivation.bEnabled(), "Captivation");
			}
			
			if (KeyBindings.cropination.isPressed()) {
				settings.cropination.setEnabled(!settings.cropination.bEnabled());
				ConfigHandler.setValue(Constants.CROPINATION_ID, "enabled", settings.cropination.bEnabled());
				ClientFunctions.NotifyClient(settings.cropination.bEnabled(), "Cropination");
			}
			
			if (KeyBindings.excavation.isPressed()) {
				settings.excavation.setEnabled(!settings.excavation.bEnabled());
				ConfigHandler.setValue(Constants.EXCAVATION_ID, "enabled", settings.excavation.bEnabled());
				ClientFunctions.NotifyClient(settings.excavation.bEnabled(), "Excavation");
			}
			
			if (KeyBindings.excavation_toggle.isPressed()) {
				if (variables.IsSingleLayerToggled) {
					variables.IsSingleLayerToggled = false;
					if (settings.excavation.bToggleMode())
						ClientFunctions.NotifyClient(variables.IsSingleLayerToggled, "Excavation Single Layer Toggled:");
				}
				
				variables.IsExcavationToggled = (settings.excavation.bToggleMode() ? !variables.IsExcavationToggled : true);
				if (settings.excavation.bToggleMode())
					ClientFunctions.NotifyClient(variables.IsExcavationToggled, "Excavation Toggled:");
				
			} else if (KeyBindings.excavation_layer_only_toggle.isPressed()) {
				if (variables.IsExcavationToggled) {
					variables.IsExcavationToggled = false;
					if (settings.excavation.bToggleMode())
						ClientFunctions.NotifyClient(variables.IsExcavationToggled, "Excavation Toggled:");
				}
				
				variables.IsSingleLayerToggled = (settings.excavation.bToggleMode() ? !variables.IsSingleLayerToggled : true);
				if (settings.excavation.bToggleMode())
					ClientFunctions.NotifyClient(variables.IsSingleLayerToggled, "Excavation Single Layer Toggled:");
			}
			
			if (KeyBindings.illumination.isPressed()) {
				settings.illumination.setEnabled(!settings.illumination.bEnabled());
				ConfigHandler.setValue(Constants.ILLUMINATION_ID, "enabled", settings.illumination.bEnabled());
				ClientFunctions.NotifyClient(settings.illumination.bEnabled(), "Illumination");
			}
			
			if (settings.illumination.bEnabled() && KeyBindings.illumination_place.isPressed()) {
				IlluminationHandler.PlaceTorch(ClientFunctions.getMC(), ClientFunctions.getPlayer());
			}
			
			if (KeyBindings.lumbination.isPressed()) {
				settings.lumbination.setEnabled(!settings.lumbination.bEnabled());
				ConfigHandler.setValue(Constants.LUMBINATION_ID, "enabled", settings.lumbination.bEnabled());
				ClientFunctions.NotifyClient(settings.lumbination.bEnabled(), "Lumbination");
			}
			
			if (KeyBindings.shaftanation.isPressed()) {
				settings.shaftanation.setEnabled(!settings.shaftanation.bEnabled());
				ConfigHandler.setValue(Constants.SHAFTANATION_ID, "enabled", settings.shaftanation.bEnabled());
				ClientFunctions.NotifyClient(settings.shaftanation.bEnabled(), "Shaftanation");
			}
			
			if (KeyBindings.substitution.isPressed()) {
				settings.substitution.setEnabled(!settings.substitution.bEnabled());
				ConfigHandler.setValue(Constants.SUBSTITUTION_ID, "enabled", settings.substitution.bEnabled());
				ClientFunctions.NotifyClient(settings.substitution.bEnabled(), "Substitution");
			}
			
			if (KeyBindings.veination.isPressed()) {
				settings.veination.setEnabled(!settings.veination.bEnabled());
				ConfigHandler.setValue(Constants.VEINATION_ID, "enabled", settings.veination.bEnabled());
				ClientFunctions.NotifyClient(settings.veination.bEnabled(), "Veination");
			}
			
			if (KeyBindings.abortAgents.isPressed()) {
				NBTTagCompound tags = new NBTTagCompound();
				tags.setInteger("ID", PacketID.AbortAgents.value());
				
				MinersAdvantage.instance.network.sendToServer(new NetworkPacket(tags));
			}
			
			ConfigHandler.save();
		}
	}
}