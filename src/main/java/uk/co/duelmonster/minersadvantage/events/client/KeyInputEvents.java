package uk.co.duelmonster.minersadvantage.events.client;

import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import uk.co.duelmonster.minersadvantage.MA;
import uk.co.duelmonster.minersadvantage.client.ClientFunctions;
import uk.co.duelmonster.minersadvantage.client.KeyBindings;
import uk.co.duelmonster.minersadvantage.common.Variables;
import uk.co.duelmonster.minersadvantage.config.MAConfig;
import uk.co.duelmonster.minersadvantage.helpers.IlluminationHelper;
import uk.co.duelmonster.minersadvantage.network.packets.PacketAbortAgents;

public class KeyInputEvents {

  @SubscribeEvent
  public void onKeyInput(InputEvent.KeyInputEvent event) {
    // checking inGameHasFocus prevents your keys from firing when the player is typing a chat
    // message
    // NOTE that the KeyInputEvent will NOT be posted when a gui screen such as the inventory is
    // open
    if (ClientFunctions.mc != null && !ClientFunctions.mc.isPaused() && ClientFunctions.mc.isWindowActive()) {
      Variables variables = Variables.get();

      if (KeyBindings.captivation.isDown()) {
        MAConfig.CLIENT.captivation.setEnabled(!MAConfig.CLIENT.captivation.enabled());
        ClientFunctions.NotifyClient(MAConfig.CLIENT.captivation.enabled(), "Captivation");
      }

      if (KeyBindings.cropination.isDown()) {
        MAConfig.CLIENT.cropination.setEnabled(!MAConfig.CLIENT.cropination.enabled());
        ClientFunctions.NotifyClient(MAConfig.CLIENT.cropination.enabled(), "Cropination");
      }

      if (KeyBindings.excavation.isDown()) {
        MAConfig.CLIENT.excavation.setEnabled(!MAConfig.CLIENT.excavation.enabled());
        ClientFunctions.NotifyClient(MAConfig.CLIENT.excavation.enabled(), "Excavation");
      }

      if (MAConfig.CLIENT.excavation.enabled() && !MAConfig.CLIENT.excavation.toggleMode()) {

        if (variables.IsExcavationToggled != KeyBindings.excavation_toggle.isDown()) {

          variables.IsExcavationToggled = KeyBindings.excavation_toggle.isDown();
          ClientFunctions.DebugNotifyClient(variables.IsExcavationToggled, "Excavation Toggled:");

        } else if (variables.IsSingleLayerToggled != KeyBindings.excavation_layer_only_toggle.isDown()) {

          variables.IsSingleLayerToggled = KeyBindings.excavation_layer_only_toggle.isDown();
          ClientFunctions.DebugNotifyClient(variables.IsSingleLayerToggled, "Excavation Single Layer Toggled:");

        }

      } else if (MAConfig.CLIENT.excavation.enabled() && MAConfig.CLIENT.excavation.toggleMode()) {
        if (KeyBindings.excavation_toggle.isDown()) {
          if (variables.IsSingleLayerToggled) {
            variables.IsSingleLayerToggled = false;
            if (MAConfig.CLIENT.excavation.toggleMode())
              ClientFunctions.NotifyClient(variables.IsSingleLayerToggled, "Excavation Single Layer Toggled:");
          }

          variables.IsExcavationToggled = (MAConfig.CLIENT.excavation.toggleMode() ? !variables.IsExcavationToggled : true);
          if (MAConfig.CLIENT.excavation.toggleMode())
            ClientFunctions.NotifyClient(variables.IsExcavationToggled, "Excavation Toggled:");

        } else if (KeyBindings.excavation_layer_only_toggle.isDown()) {
          if (variables.IsExcavationToggled) {
            variables.IsExcavationToggled = false;
            if (MAConfig.CLIENT.excavation.toggleMode())
              ClientFunctions.NotifyClient(variables.IsExcavationToggled, "Excavation Toggled:");
          }

          variables.IsSingleLayerToggled = (MAConfig.CLIENT.excavation.toggleMode() ? !variables.IsSingleLayerToggled : true);
          if (MAConfig.CLIENT.excavation.toggleMode())
            ClientFunctions.NotifyClient(variables.IsSingleLayerToggled, "Excavation Single Layer Toggled:");
        }
      }

      if (KeyBindings.illumination.isDown()) {
        MAConfig.CLIENT.illumination.setEnabled(!MAConfig.CLIENT.illumination.enabled());
        ClientFunctions.NotifyClient(MAConfig.CLIENT.illumination.enabled(), "Illumination");
      }

      if (MAConfig.CLIENT.illumination.enabled() && KeyBindings.illumination_place.isDown()) {
        IlluminationHelper.INSTANCE.PlaceTorch();
      }

      if (MAConfig.CLIENT.illumination.enabled() && KeyBindings.illumination_area.isDown()) {
        IlluminationHelper.INSTANCE.IlluminateArea();
      }

      if (KeyBindings.lumbination.isDown()) {
        MAConfig.CLIENT.lumbination.setEnabled(!MAConfig.CLIENT.lumbination.enabled());
        ClientFunctions.NotifyClient(MAConfig.CLIENT.lumbination.enabled(), "Lumbination");
      }

      if (KeyBindings.shaftanation.isDown()) {
        MAConfig.CLIENT.shaftanation.setEnabled(!MAConfig.CLIENT.shaftanation.enabled());
        ClientFunctions.NotifyClient(MAConfig.CLIENT.shaftanation.enabled(), "Shaftanation");
      }

      if (MAConfig.CLIENT.shaftanation.enabled() && variables.IsShaftanationToggled != KeyBindings.shaftanation_toggle.isDown()) {
        variables.IsShaftanationToggled = KeyBindings.shaftanation_toggle.isDown();
        ClientFunctions.DebugNotifyClient(variables.IsShaftanationToggled, "Shaftanation Toggled:");
      }

      if (KeyBindings.substitution.isDown()) {
        MAConfig.CLIENT.substitution.setEnabled(!MAConfig.CLIENT.substitution.enabled());
        ClientFunctions.NotifyClient(MAConfig.CLIENT.substitution.enabled(), "Substitution");
      }

      if (KeyBindings.veination.isDown()) {
        MAConfig.CLIENT.veination.setEnabled(!MAConfig.CLIENT.veination.enabled());
        ClientFunctions.NotifyClient(MAConfig.CLIENT.veination.enabled(), "Veination");
      }

      if (KeyBindings.abortAgents.isDown()) {
        MA.NETWORK.sendToServer(new PacketAbortAgents());
      }

      // Record the block face being attacked
      if (ClientFunctions.mc.hitResult instanceof BlockRayTraceResult) {
        variables.faceHit = ((BlockRayTraceResult) ClientFunctions.mc.hitResult).getDirection();
      }

      // Sync modified variables to the server
      Variables.syncToServer();

      // Sync Settings with the Server
      MAConfig.CLIENT.syncPlayerConfigToServer();
    }
  }
}
