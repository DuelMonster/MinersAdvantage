package uk.co.duelmonster.minersadvantage.network.packets;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.concurrent.TickDelayedTask;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import uk.co.duelmonster.minersadvantage.network.packetids.PacketId;
import uk.co.duelmonster.minersadvantage.workers.AgentProcessor;
import uk.co.duelmonster.minersadvantage.workers.PathanationAgent;

public class PacketPathanate implements IMAPacket {

  public final BlockPos pos;

  public PacketPathanate(BlockPos _pos) {
    pos = _pos;
  }

  public PacketPathanate(PacketBuffer buf) {
    pos = buf.readBlockPos();
  }

  @Override
  public PacketId getPacketId() {
    return PacketId.Pathinate;
  }

  public static void encode(PacketPathanate pkt, PacketBuffer buf) {
    buf.writeBlockPos(pkt.pos);
  }

  public static PacketPathanate decode(PacketBuffer buf) {
    return new PacketPathanate(buf);
  }

  public static void process(ServerPlayerEntity player, final PacketPathanate pkt) {
    player.getServer().tell(new TickDelayedTask(player.getServer().getTickCount(), () -> {
      AgentProcessor.INSTANCE.startProcessing(player, new PathanationAgent(player, pkt));
    }));
  }

  public static void handle(final PacketPathanate pkt, Supplier<Context> ctx) {
    ctx.get().enqueueWork(() -> {
      // Work that needs to be threadsafe (most work)
      ServerPlayerEntity player = ctx.get().getSender(); // the client that sent this packet

      // do stuff
      player.getServer().tell(new TickDelayedTask(player.getServer().getTickCount(), () -> {
        AgentProcessor.INSTANCE.startProcessing(player, new PathanationAgent(player, pkt));
      }));

    });
    ctx.get().setPacketHandled(true);
  }
}
