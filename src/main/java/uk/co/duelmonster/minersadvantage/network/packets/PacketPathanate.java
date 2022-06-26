package uk.co.duelmonster.minersadvantage.network.packets;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;
import uk.co.duelmonster.minersadvantage.network.packetids.PacketId;
import uk.co.duelmonster.minersadvantage.workers.AgentProcessor;
import uk.co.duelmonster.minersadvantage.workers.PathanationAgent;

public class PacketPathanate implements IMAPacket {

  public final BlockPos pos;

  public PacketPathanate(BlockPos _pos) {
    pos = _pos;
  }

  public PacketPathanate(FriendlyByteBuf buf) {
    pos = buf.readBlockPos();
  }

  @Override
  public PacketId getPacketId() {
    return PacketId.Pathinate;
  }

  public static void encode(PacketPathanate pkt, FriendlyByteBuf buf) {
    buf.writeBlockPos(pkt.pos);
  }

  public static PacketPathanate decode(FriendlyByteBuf buf) {
    return new PacketPathanate(buf);
  }

  public static void process(ServerPlayer player, final PacketPathanate pkt) {
    player.getServer().tell(new TickTask(player.getServer().getTickCount(), () -> {
      AgentProcessor.INSTANCE.startProcessing(player, new PathanationAgent(player, pkt));
    }));
  }

  public static void handle(final PacketPathanate pkt, Supplier<Context> ctx) {
    ctx.get().enqueueWork(() -> {
      // Work that needs to be threadsafe (most work)
      ServerPlayer player = ctx.get().getSender(); // the client that sent this packet

      // do stuff
      player.getServer().tell(new TickTask(player.getServer().getTickCount(), () -> {
        AgentProcessor.INSTANCE.startProcessing(player, new PathanationAgent(player, pkt));
      }));

    });
    ctx.get().setPacketHandled(true);
  }
}
