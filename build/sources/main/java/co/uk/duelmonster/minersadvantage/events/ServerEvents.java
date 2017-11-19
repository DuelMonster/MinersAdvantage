package co.uk.duelmonster.minersadvantage.events;

import java.util.List;

import co.uk.duelmonster.minersadvantage.common.Variables;
import co.uk.duelmonster.minersadvantage.workers.AgentProcessor;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ServerEvents {
	
	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase != TickEvent.Phase.END)
			return;
		
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		if (null == server)
			return;
		
		List<EntityPlayerMP> players = server.getPlayerList().getPlayers();
		if (players != null && !players.isEmpty()) {
			for (EntityPlayerMP player : players) {
				Variables vars = Variables.get(player.getUniqueID());
				
				if (vars != null) {
					if (vars.skipNext) {
						vars.skipNext = false;
						return;
					} else {
						AgentProcessor.instance.fireAgentTicks(player.world);
						AgentProcessor.instance.setCurrentAgent(player.getUniqueID(), null);
					}
				}
			}
		}
	}
	
}
