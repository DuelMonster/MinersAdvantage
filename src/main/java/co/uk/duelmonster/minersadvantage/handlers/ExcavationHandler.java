package co.uk.duelmonster.minersadvantage.handlers;

import org.apache.logging.log4j.Level;

import co.uk.duelmonster.minersadvantage.MinersAdvantage;
import co.uk.duelmonster.minersadvantage.common.PacketID;
import co.uk.duelmonster.minersadvantage.common.Variables;
import co.uk.duelmonster.minersadvantage.packets.NetworkPacket;
import co.uk.duelmonster.minersadvantage.settings.Settings;
import co.uk.duelmonster.minersadvantage.workers.AgentProcessor;
import co.uk.duelmonster.minersadvantage.workers.ExcavationAgent;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ExcavationHandler implements IPacketHandler {
	
	public static ExcavationHandler instance = new ExcavationHandler();
	
	@Override
	@SideOnly(Side.CLIENT)
	public void processClientMessage(NetworkPacket message, MessageContext context) {
		int ID = message.getTags().getInteger("ID");
		if (ID == PacketID.Excavate.value()) {
			// if (!Settings.get().bExcavationEnabled() || KeyBindings.excavation_toggle.getKeyCode() == 0 ||
			// !KeyBindings.excavation_toggle.isKeyDown())
			// return;
			
			Variables.get().IsExcavating = true;
		} else if (ID == PacketID.Veinate.value() && Settings.get().bVeinationEnabled()) {
			Variables.get().IsVeinating = true;
		}
	}
	
	@Override
	public void processServerMessage(final NetworkPacket message, MessageContext context) {
		final EntityPlayerMP player = context.getServerHandler().playerEntity;
		if (player == null)
			return;
		
		final int iStateID = message.getTags().getInteger("stateID");
		final IBlockState state = Block.getStateById(iStateID);
		
		if (state == null || state.getBlock() == Blocks.AIR)
			MinersAdvantage.logger.log(Level.INFO, "Invalid BlockState ID recieved from message packet. [ " + iStateID + " ]");
		
		player.getServer().addScheduledTask(new Runnable() {
			@Override
			public void run() {
				AgentProcessor.instance.startProcessing(player, new ExcavationAgent(player, message.getTags()));
			}
		});
	}
}
