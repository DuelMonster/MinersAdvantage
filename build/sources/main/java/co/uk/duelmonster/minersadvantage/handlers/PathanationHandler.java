package co.uk.duelmonster.minersadvantage.handlers;

import co.uk.duelmonster.minersadvantage.common.Variables;
import co.uk.duelmonster.minersadvantage.packets.PacketBase;
import co.uk.duelmonster.minersadvantage.settings.Settings;
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
	public void processClientMessage(PacketBase message, MessageContext context) {
		if (!Settings.get().bExcavationEnabled)
			return;
		
		Variables.get().IsPathanating = true;
	}
	
	@Override
	public void processServerMessage(PacketBase message, MessageContext context) {
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
