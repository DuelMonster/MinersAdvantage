package uk.co.duelmonster.minersadvantage.network.packets;

import uk.co.duelmonster.minersadvantage.network.packetids.IPacketId;

public interface IMAPacket {

	//public IMAPacket(PacketBuffer buf);

	public IPacketId getPacketId();

	// public static void encode(IMAPacket pkt, PacketBuffer buf);

	// public static PacketGiveItem decode(PacketBuffer buf);

	// public static void handle(final PacketGiveItem pkt, Supplier<Context> ctx);
}
