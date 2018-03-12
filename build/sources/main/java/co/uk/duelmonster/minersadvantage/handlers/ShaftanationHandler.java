package co.uk.duelmonster.minersadvantage.handlers;

import co.uk.duelmonster.minersadvantage.client.KeyBindings;
import co.uk.duelmonster.minersadvantage.common.Variables;
import co.uk.duelmonster.minersadvantage.packets.NetworkPacket;
import co.uk.duelmonster.minersadvantage.settings.Settings;
import co.uk.duelmonster.minersadvantage.workers.AgentProcessor;
import co.uk.duelmonster.minersadvantage.workers.ShaftanationAgent;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ShaftanationHandler implements IPacketHandler {
	
	public static ShaftanationHandler instance = new ShaftanationHandler();
	
	@Override
	@SideOnly(Side.CLIENT)
	public void processClientMessage(NetworkPacket message, MessageContext context) {
		if (!Settings.get().bShaftanationEnabled() || KeyBindings.shaftanation_toggle.getKeyCode() == 0 || !KeyBindings.shaftanation_toggle.isKeyDown())
			return;
		
		Variables.get().IsShaftanating = true;
	}
	
	@Override
	public void processServerMessage(NetworkPacket message, MessageContext context) {
		final EntityPlayerMP player = context.getServerHandler().player;
		if (player == null)
			return;
		
		//		if (message.getTags().getBoolean("cancel")) {
		//			player.getServer().addScheduledTask(new Runnable() {
		//				@Override
		//				public void run() {
		//					AgentProcessor.instance.stopProcessing(player);
		//				}
		//			});
		//			return;
		//		}
		
		player.getServer().addScheduledTask(new Runnable() {
			@Override
			public void run() {
				AgentProcessor.instance.startProcessing(player, new ShaftanationAgent(player, message.getTags()));
			}
		});
	}
}
