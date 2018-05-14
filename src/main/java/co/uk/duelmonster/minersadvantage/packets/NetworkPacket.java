package co.uk.duelmonster.minersadvantage.packets;

import org.apache.logging.log4j.Level;

import co.uk.duelmonster.minersadvantage.MinersAdvantage;
import co.uk.duelmonster.minersadvantage.client.KeyBindings;
import co.uk.duelmonster.minersadvantage.common.PacketID;
import co.uk.duelmonster.minersadvantage.common.Variables;
import co.uk.duelmonster.minersadvantage.handlers.CaptivationHandler;
import co.uk.duelmonster.minersadvantage.handlers.CropinationHandler;
import co.uk.duelmonster.minersadvantage.handlers.ExcavationHandler;
import co.uk.duelmonster.minersadvantage.handlers.GodItems;
import co.uk.duelmonster.minersadvantage.handlers.IlluminationHandler;
import co.uk.duelmonster.minersadvantage.handlers.LumbinationHandler;
import co.uk.duelmonster.minersadvantage.handlers.PathanationHandler;
import co.uk.duelmonster.minersadvantage.handlers.ShaftanationHandler;
import co.uk.duelmonster.minersadvantage.handlers.SyncHandler;
import co.uk.duelmonster.minersadvantage.settings.Settings;
import co.uk.duelmonster.minersadvantage.workers.AgentProcessor;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class NetworkPacket implements IMessage {
	
	protected NBTTagCompound tags = new NBTTagCompound();
	
	public NetworkPacket() {}
	
	public NetworkPacket(NBTTagCompound tags) {
		this.tags = tags;
	}
	
	public NBTTagCompound getTags() {
		return tags;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		tags = ByteBufUtils.readTag(buf);
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeTag(buf, tags);
	}
	
	@SideOnly(Side.CLIENT)
	public static class ClientHandler implements IMessageHandler<NetworkPacket, NetworkPacket> {
		@Override
		public NetworkPacket onMessage(NetworkPacket message, MessageContext context) {
			PacketID pID = PacketID.valueOf(message.tags.getInteger("ID"));
			NetworkPacket rtrnPacket = null;
			
			if (pID != null) {
				int iStateID = 0;
				IBlockState state = null;
				
				if (message.getTags().hasKey("stateID")) {
					iStateID = message.getTags().getInteger("stateID");
					state = Block.getStateById(iStateID);
					
					if (state == null || state.getBlock() == Blocks.AIR) {
						MinersAdvantage.logger.log(Level.INFO, "Invalid BlockState ID recieved from message packet. [ " + iStateID + " ]");
						return null;
					}
				}
				
				Settings settings = Settings.get();
				Variables variables = Variables.get();
				
				switch (pID) {
				case SyncSettings:
				case SyncVariables:
					SyncHandler.instance.processClientMessage(message, context);
					break;
				
				case Excavate:
				case Veinate:
					boolean isOreVein = settings.bVeinationEnabled() && settings.veinationOres().has(state.getBlock().getRegistryName().toString().trim());
					
					if (isOreVein || settings.bExcavationEnabled() && (KeyBindings.excavation_toggle.isKeyDown() || KeyBindings.excavation_layer_only_toggle.isKeyDown())) {
						if (isOreVein)
							message.tags.setInteger("ID", PacketID.Veinate.value());
						
						if (!settings.bToggleMode())
							variables.IsSingleLayerToggled = KeyBindings.excavation_layer_only_toggle.isKeyDown();
						
						ExcavationHandler.instance.processClientMessage(message, context);
						rtrnPacket = new NetworkPacket(message.tags);
					}
					break;
				
				case Shaftanate:
					if (settings.bShaftanationEnabled() && KeyBindings.shaftanation_toggle.isKeyDown()) {
						
						ShaftanationHandler.instance.processClientMessage(message, context);
						rtrnPacket = new NetworkPacket(message.tags);
						
					}
					break;
				
				case Illuminate:
					if (settings.bIlluminationEnabled())
						rtrnPacket = new NetworkPacket(message.tags);
					break;
				
				case Lumbinate:
					if (settings.bLumbinationEnabled()) {
						LumbinationHandler.instance.processClientMessage(message, context);
						rtrnPacket = new NetworkPacket(message.tags);
					}
					break;
				
				case LayPath:
					if (settings.bCropinationEnabled()) {
						PathanationHandler.instance.processClientMessage(rtrnPacket, context);
						rtrnPacket = new NetworkPacket(message.tags);
					}
					break;
				
				case TileFarmland:
				case HarvestCrops:
					if (settings.bCropinationEnabled()) {
						CropinationHandler.instance.processClientMessage(rtrnPacket, context);
						rtrnPacket = new NetworkPacket(message.tags);
					}
					break;
				
				case AboutAgents:
					rtrnPacket = new NetworkPacket(message.tags);
					
				default:
					break;
				}
			}
			
			return rtrnPacket;
		}
	}
	
	public static class ServerHandler implements IMessageHandler<NetworkPacket, NetworkPacket> {
		@Override
		public NetworkPacket onMessage(NetworkPacket message, MessageContext context) {
			final EntityPlayerMP player = context.getServerHandler().playerEntity;
			
			PacketID pID = PacketID.valueOf(message.tags.getInteger("ID"));
			
			if (pID != null) {
				switch (pID) {
				case GODITEMS:
					GodItems.GiveGodTools(player);
					break;
				
				case SyncSettings:
					SyncHandler.instance.processServerMessage(message, context);
					break;
				
				case Captivate:
					CaptivationHandler.instance.processServerMessage(message, context);
					break;
				
				case Excavate:
				case Veinate:
					ExcavationHandler.instance.processServerMessage(message, context);
					break;
				
				case Shaftanate:
					ShaftanationHandler.instance.processServerMessage(message, context);
					break;
				
				case Illuminate:
					IlluminationHandler.instance.processServerMessage(message, context);
					break;
				
				case Lumbinate:
					LumbinationHandler.instance.processServerMessage(message, context);
					break;
				
				case LayPath:
					PathanationHandler.instance.processServerMessage(message, context);
					break;
				
				case TileFarmland:
				case HarvestCrops:
					CropinationHandler.instance.processServerMessage(message, context);
					break;
				
				case Substitution:
					break;
				
				case AboutAgents:
					AgentProcessor.instance.stopProcessing(player);
					
				default:
					break;
				}
			}
			
			return null;
		}
	}
}
