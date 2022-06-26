package uk.co.duelmonster.minersadvantage.network.packets;

import uk.co.duelmonster.minersadvantage.network.packetids.IPacketId;

public interface IMAPacket {

  // public IMAPacket(FriendlyByteBuf buf);

  public IPacketId getPacketId();

  // public static void encode(IMAPacket pkt, FriendlyByteBuf buf);

  // public static PacketGiveItem decode(FriendlyByteBuf buf);

  // public static void handle(final PacketGiveItem pkt, Supplier<Context> ctx);
}
