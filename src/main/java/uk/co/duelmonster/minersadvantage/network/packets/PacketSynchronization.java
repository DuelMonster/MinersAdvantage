package uk.co.duelmonster.minersadvantage.network.packets;

import java.util.UUID;
import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import uk.co.duelmonster.minersadvantage.common.SyncType;
import uk.co.duelmonster.minersadvantage.common.Variables;
import uk.co.duelmonster.minersadvantage.config.MAConfig_Server;
import uk.co.duelmonster.minersadvantage.network.packetids.PacketId;

public class PacketSynchronization implements IMAPacket {
	
	private UUID		uuid;
	private SyncType	syncType;
	private String		payload;
	// private Variables payload;
	
	@Override
	public PacketId getPacketId() {
		return PacketId.Synchronization;
	}
	
	public PacketSynchronization(UUID uuid, SyncType syncType, String payload) { // Variables payload) {
		this.uuid = uuid;
		this.syncType = syncType;
		this.payload = payload;
	}
	
	public PacketSynchronization(PacketBuffer buf) {
		this.uuid = buf.readUniqueId();
		
		this.syncType = buf.readEnumValue(SyncType.class);
		
		int payloadLength = buf.readInt();
		this.payload = buf.readString(payloadLength);
	}
	
	public static void encode(PacketSynchronization pkt, PacketBuffer buf) {
		buf.writeUniqueId(pkt.uuid);
		
		buf.writeEnumValue(pkt.syncType);
		
		buf.writeInt(pkt.payload.length());
		buf.writeString(pkt.payload);
	}
	
	public static PacketSynchronization decode(PacketBuffer buf) {
		return new PacketSynchronization(buf);
	}
	
	public static void handle(final PacketSynchronization pkt, Supplier<Context> ctx) {
		ctx.get().enqueueWork(() -> {
			// Work that needs to be threadsafe (most work)
			if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
				ServerPlayerEntity sender = ctx.get().getSender(); // the client that sent this packet
				
				// do stuff
				if (pkt.syncType == SyncType.Variables) {
					Variables.set(sender.getUniqueID(), pkt.payload);
					
				} else if (pkt.syncType == SyncType.ClientConfig) {
					MAConfig_Server.setPlayerConfig(sender.getUniqueID(), pkt.payload);
				}
			} else {
				// do stuff
				if (pkt.syncType == SyncType.Variables) {
					Variables.set(pkt.payload);
				}
			}
		});
		ctx.get().setPacketHandled(true);
	}
}