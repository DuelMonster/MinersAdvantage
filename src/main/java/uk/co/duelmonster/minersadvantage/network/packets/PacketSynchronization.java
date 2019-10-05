package uk.co.duelmonster.minersadvantage.network.packets;

import java.util.UUID;
import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import uk.co.duelmonster.minersadvantage.common.Variables;
import uk.co.duelmonster.minersadvantage.network.packetids.PacketId;

public class PacketSynchronization {

	private final UUID uuid;
	private final String payload;

	public PacketSynchronization(UUID uuid, String payload) {
		this.uuid = uuid;
		this.payload = payload;
	}

	public PacketSynchronization(PacketBuffer buf) {
		this.uuid = buf.readUniqueId();
		this.payload = buf.readString();
	}

	public PacketId getPacketId() { return PacketId.Synchronization; }

	public static void encode(PacketSynchronization pkt, PacketBuffer buf) {
		buf.writeUniqueId(pkt.uuid);
		buf.writeString(pkt.payload);
	}

	public static PacketSynchronization decode(PacketBuffer buf) { return new PacketSynchronization(buf); }

	public static void handle(final PacketSynchronization pkt, Supplier<Context> ctx) {
		ctx.get().enqueueWork(() -> {
			// Work that needs to be threadsafe (most work)
			if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
				ServerPlayerEntity sender = ctx.get().getSender(); // the client that sent this packet
				// do stuff
				Variables.set(sender.getUniqueID(), pkt.payload);
			} else {
				// do stuff
				Variables.set(pkt.payload);
			}
		});
	}
}