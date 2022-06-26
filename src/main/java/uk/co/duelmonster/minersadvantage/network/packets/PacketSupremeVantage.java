package uk.co.duelmonster.minersadvantage.network.packets;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;
import uk.co.duelmonster.minersadvantage.helpers.SupremeVantage;
import uk.co.duelmonster.minersadvantage.network.packetids.PacketId;

public class PacketSupremeVantage implements IMAPacket {

  private final String code;

  public PacketSupremeVantage(String _code) {
    this.code = _code;
  }

  public PacketSupremeVantage(FriendlyByteBuf buf) {
    this.code = buf.readUtf();
  }

  @Override
  public PacketId getPacketId() {
    return PacketId.SupremeVantage;
  }

  public static void encode(PacketSupremeVantage pkt, FriendlyByteBuf buf) {
    buf.writeUtf(pkt.code);
  }

  public static PacketSupremeVantage decode(FriendlyByteBuf buf) {
    return new PacketSupremeVantage(buf);
  }

  public static void handle(final PacketSupremeVantage pkt, Supplier<Context> ctx) {
    ctx.get().enqueueWork(() -> {
      // Work that needs to be threadsafe (most work)
      ServerPlayer player = ctx.get().getSender(); // the client that sent this packet
      // do stuff
      SupremeVantage.GiveSupremeVantage(player, pkt.code);
    });
    ctx.get().setPacketHandled(true);
  }
}
