package co.uk.duelmonster.minersadvantage.handlers;

import co.uk.duelmonster.minersadvantage.common.Variables;
import co.uk.duelmonster.minersadvantage.config.MAConfig;
import co.uk.duelmonster.minersadvantage.packets.NetworkPacket;
import co.uk.duelmonster.minersadvantage.workers.AgentProcessor;
import co.uk.duelmonster.minersadvantage.workers.PathanationAgent;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PathanationHandler implements IPacketHandler {
	
	public static PathanationHandler instance = new PathanationHandler();
	
	@Override
	@SideOnly(Side.CLIENT)
	public void processClientMessage(NetworkPacket message, MessageContext context) {
		if (!MAConfig.get().excavation.bEnabled())
			return;
		
		Variables.get().IsPathanating = true;
	}
	
	@Override
	public void processServerMessage(final NetworkPacket message, MessageContext context) {
		final EntityPlayerMP player = context.getServerHandler().player;
		if (player == null)
			return;
		
		player.getServer().addScheduledTask(new Runnable() {
			@Override
			public void run() {
				AgentProcessor.instance.startProcessing(player, new PathanationAgent(player, message.getTags()));
			}
		});
	}
	
}
