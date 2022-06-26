package uk.co.duelmonster.minersadvantage.network.packets;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;
import uk.co.duelmonster.minersadvantage.network.packetids.PacketId;
import uk.co.duelmonster.minersadvantage.workers.AgentProcessor;

public class PacketAbortAgents implements IMAPacket {

  public PacketAbortAgents() {}

  public PacketAbortAgents(FriendlyByteBuf buf) {}

  @Override
  public PacketId getPacketId() {
    return PacketId.Captivate;
  }

  public static void encode(PacketAbortAgents pkt, FriendlyByteBuf buf) {}

  public static PacketAbortAgents decode(FriendlyByteBuf buf) {
    return new PacketAbortAgents(buf);
  }

  public static void handle(final PacketAbortAgents pkt, Supplier<Context> ctx) {
    ctx.get().enqueueWork(() -> {
      // Work that needs to be threadsafe (most work)
      ServerPlayer sender = ctx.get().getSender(); // the client that sent this packet
      // do stuff
      AgentProcessor.INSTANCE.stopProcessing(sender);
    });
    ctx.get().setPacketHandled(true);
  }
}
