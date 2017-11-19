package co.uk.duelmonster.minersadvantage.handlers;

import co.uk.duelmonster.minersadvantage.common.JsonHelper;
import co.uk.duelmonster.minersadvantage.common.Variables;
import co.uk.duelmonster.minersadvantage.packets.NetworkPacket;
import co.uk.duelmonster.minersadvantage.settings.Settings;
import co.uk.duelmonster.minersadvantage.settings.SettingsServer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SyncHandler implements IPacketHandler {
	
	public static SyncHandler instance = new SyncHandler();
	
	@Override
	public void processClientMessage(NetworkPacket message, MessageContext context) {
		if (message.getTags().hasKey("server_settings")) {
			String json = message.getTags().getString("server_settings");
			Settings.get().serverOverrides = JsonHelper.gson.fromJson(json, SettingsServer.class);
		}
		
		if (message.getTags().hasKey("variables")) {
			String json = message.getTags().getString("variables");
			Variables variables = JsonHelper.gson.fromJson(json, Variables.class);
			
			Variables.set(variables);
		}
	}
	
	@Override
	public void processServerMessage(NetworkPacket message, MessageContext context) {
		final EntityPlayerMP player = context.getServerHandler().player;
		if (player == null)
			return;
		
		String json = null;
		
		if (message.getTags().hasKey("settings")) {
			json = message.getTags().getString("settings");
			Settings settings = JsonHelper.gson.fromJson(json, Settings.class);
			
			Settings.set(player.getUniqueID(), settings);
		}
		
		if (message.getTags().hasKey("variables")) {
			json = message.getTags().getString("variables");
			Variables variables = JsonHelper.gson.fromJson(json, Variables.class);
			
			Variables.set(player.getUniqueID(), variables);
		}
	}
	
}
