package co.uk.duelmonster.minersadvantage.events;

import co.uk.duelmonster.minersadvantage.client.ClientFunctions;
import co.uk.duelmonster.minersadvantage.client.KeyBindings;
import co.uk.duelmonster.minersadvantage.common.Constants;
import co.uk.duelmonster.minersadvantage.common.Functions;
import co.uk.duelmonster.minersadvantage.common.Variables;
import co.uk.duelmonster.minersadvantage.handlers.IlluminationHandler;
import co.uk.duelmonster.minersadvantage.settings.ConfigHandler;
import co.uk.duelmonster.minersadvantage.settings.Settings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

public class KeyInputEvents {
	@SubscribeEvent
	public void onKeyInput(InputEvent.KeyInputEvent event) {
		// checking inGameHasFocus prevents your keys from firing when the player is typing a chat message
		// NOTE that the KeyInputEvent will NOT be posted when a gui screen such as the inventory is open
		if (FMLClientHandler.instance().getClient().inGameHasFocus) {
			Settings settings = Settings.get();
			Variables variables = Variables.get();
			
			if (KeyBindings.captivation.isPressed()) {
				settings.setCaptivationEnabled(!settings.bCaptivationEnabled());
				ConfigHandler.setValue(Constants.CAPTIVATION_ID, "enabled", settings.bCaptivationEnabled());
				ClientFunctions.NotifyClient(settings.bCaptivationEnabled(), "Captivation");
			}
			
			if (KeyBindings.excavation.isPressed()) {
				settings.setExcavationEnabled(!settings.bExcavationEnabled());
				ConfigHandler.setValue(Constants.EXCAVATION_ID, "enabled", settings.bExcavationEnabled());
				ClientFunctions.NotifyClient(settings.bExcavationEnabled(), "Excavation");
			}
			
			EntityPlayer player = ClientFunctions.getPlayer();
			if (settings.bToggleMode()) {
				if (KeyBindings.excavation_toggle.isPressed()) {
					if (variables.IsSingleLayerToggled) {
						variables.IsSingleLayerToggled = false;
						Functions.NotifyClient(player, "Excavation Single Layer Toggled: OFF");
					}
					
					variables.IsExcavationToggled = !variables.IsExcavationToggled;
					Functions.NotifyClient(player, "Excavation Toggled: " + (variables.IsExcavationToggled ? "ON" : "OFF"));
					
				} else if (KeyBindings.excavation_layer_only_toggle.isPressed()) {
					if (variables.IsExcavationToggled) {
						variables.IsExcavationToggled = false;
						Functions.NotifyClient(player, "Excavation Toggled: OFF");
					}
					
					variables.IsSingleLayerToggled = !variables.IsSingleLayerToggled;
					Functions.NotifyClient(player, "Excavation Single Layer Toggled: " + (variables.IsSingleLayerToggled ? "ON" : "OFF"));
				}
			}
			
			if (KeyBindings.illumination.isPressed()) {
				settings.setIlluminationEnabled(!settings.bIlluminationEnabled());
				ConfigHandler.setValue(Constants.ILLUMINATION_ID, "enabled", settings.bIlluminationEnabled());
				ClientFunctions.NotifyClient(settings.bIlluminationEnabled(), "Illumination");
			}
			
			if (settings.bIlluminationEnabled() && KeyBindings.illumination_place.isPressed()) {
				IlluminationHandler.PlaceTorch(ClientFunctions.getMC(), ClientFunctions.getPlayer());
			}
			
			if (KeyBindings.lumbination.isPressed()) {
				settings.setLumbinationEnabled(!settings.bLumbinationEnabled());
				ConfigHandler.setValue(Constants.LUMBINATION_ID, "enabled", settings.bLumbinationEnabled());
				ClientFunctions.NotifyClient(settings.bLumbinationEnabled(), "Lumbination");
			}
			
			if (KeyBindings.shaftanation.isPressed()) {
				settings.setShaftanationEnabled(!settings.bShaftanationEnabled());
				ConfigHandler.setValue(Constants.SHAFTANATION_ID, "enabled", settings.bShaftanationEnabled());
				ClientFunctions.NotifyClient(settings.bShaftanationEnabled(), "Shaftanation");
			}
			
			if (KeyBindings.substitution.isPressed()) {
				settings.setSubstitutionEnabled(!settings.bSubstitutionEnabled());
				ConfigHandler.setValue(Constants.SUBSTITUTION_ID, "enabled", settings.bSubstitutionEnabled());
				ClientFunctions.NotifyClient(settings.bSubstitutionEnabled(), "Substitution");
			}
			
			if (KeyBindings.veination.isPressed()) {
				settings.setVeinationEnabled(!settings.bVeinationEnabled());
				ConfigHandler.setValue(Constants.VEINATION_ID, "enabled", settings.bVeinationEnabled());
				ClientFunctions.NotifyClient(settings.bVeinationEnabled(), "Veination");
			}
			
			ConfigHandler.save();
		}
	}
}