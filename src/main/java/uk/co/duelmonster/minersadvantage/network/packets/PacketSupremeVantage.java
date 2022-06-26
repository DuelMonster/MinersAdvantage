package uk.co.duelmonster.minersadvantage.network.packets;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import uk.co.duelmonster.minersadvantage.helpers.SupremeVantage;
import uk.co.duelmonster.minersadvantage.network.packetids.PacketId;

public class PacketSupremeVantage implements IMAPacket {

  private final String code;

  public PacketSupremeVantage(String _code) {
    this.code = _code;
  }

  public PacketSupremeVantage(PacketBuffer buf) {
    this.code = buf.readUtf();
  }

  @Override
  public PacketId getPacketId() {
    return PacketId.SupremeVantage;
  }

  public static void encode(PacketSupremeVantage pkt, PacketBuffer buf) {
    buf.writeUtf(pkt.code);
  }

  public static PacketSupremeVantage decode(PacketBuffer buf) {
    return new PacketSupremeVantage(buf);
  }

  public static void handle(final PacketSupremeVantage pkt, Supplier<Context> ctx) {
    ctx.get().enqueueWork(() -> {
      // Work that needs to be threadsafe (most work)
      ServerPlayerEntity player = ctx.get().getSender(); // the client that sent this packet
      // do stuff
      SupremeVantage.GiveSupremeVantage(player, pkt.code);
    });
    ctx.get().setPacketHandled(true);
  }
}
