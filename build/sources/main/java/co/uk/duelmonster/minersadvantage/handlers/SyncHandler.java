package co.uk.duelmonster.minersadvantage.handlers;

import co.uk.duelmonster.minersadvantage.common.JsonHelper;
import co.uk.duelmonster.minersadvantage.common.Variables;
import co.uk.duelmonster.minersadvantage.packets.PacketBase;
import co.uk.duelmonster.minersadvantage.settings.Settings;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SyncHandler implements IPacketHandler {
	
	public static SyncHandler instance = new SyncHandler();
	
	@Override
	public void processClientMessage(PacketBase message, MessageContext context) {
		if (message.getTags().hasKey("variables")) {
			String json = message.getTags().getString("variables");
			Variables variables = JsonHelper.gson.fromJson(json, Variables.class);
			
			Variables.set(variables);
		}
	}
	
	@Override
	public void processServerMessage(PacketBase message, MessageContext context) {
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
