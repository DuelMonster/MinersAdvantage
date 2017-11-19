package co.uk.duelmonster.minersadvantage.handlers;

import co.uk.duelmonster.minersadvantage.common.Variables;
import co.uk.duelmonster.minersadvantage.packets.NetworkPacket;
import co.uk.duelmonster.minersadvantage.settings.Settings;
import co.uk.duelmonster.minersadvantage.workers.AgentProcessor;
import co.uk.duelmonster.minersadvantage.workers.CropinationAgent;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CropinationHandler implements IPacketHandler {
	
	public static CropinationHandler instance = new CropinationHandler();
	
	@Override
	@SideOnly(Side.CLIENT)
	public void processClientMessage(NetworkPacket message, MessageContext context) {
		if (!Settings.get().bCropinationEnabled())
			return;
		
		Variables.get().IsCropinating = true;
	}
	
	@Override
	public void processServerMessage(NetworkPacket message, MessageContext context) {
		final EntityPlayerMP player = context.getServerHandler().player;
		if (player == null)
			return;
		
		player.getServer().addScheduledTask(new Runnable() {
			@Override
			public void run() {
				AgentProcessor.instance.startProcessing(player, new CropinationAgent(player, message.getTags()));
			}
		});
	}
	
}
