package uk.co.duelmonster.minersadvantage.network.packets;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import uk.co.duelmonster.minersadvantage.network.packetids.PacketId;
import uk.co.duelmonster.minersadvantage.workers.AgentProcessor;

public class PacketAbortAgents implements IMAPacket {

	public PacketAbortAgents() {}
	public PacketAbortAgents(PacketBuffer buf) {}

	@Override
	public PacketId getPacketId() { return PacketId.Captivate; }

	public static void encode(PacketAbortAgents pkt, PacketBuffer buf) {}

	public static PacketAbortAgents decode(PacketBuffer buf) { return new PacketAbortAgents(buf); }

	public static void handle(final PacketAbortAgents pkt, Supplier<Context> ctx) {
		ctx.get().enqueueWork(() -> {
			// Work that needs to be threadsafe (most work)
			ServerPlayerEntity sender = ctx.get().getSender(); // the client that sent this packet
			// do stuff
			AgentProcessor.INSTANCE.stopProcessing(sender);
		});
	}
}
