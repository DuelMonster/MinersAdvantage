package co.uk.duelmonster.minersadvantage.handlers;

import co.uk.duelmonster.minersadvantage.packets.NetworkPacket;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IPacketHandler {
	
	@SideOnly(Side.CLIENT)
	void processClientMessage(NetworkPacket message, MessageContext context);
	
	void processServerMessage(NetworkPacket message, MessageContext context);
	
}
