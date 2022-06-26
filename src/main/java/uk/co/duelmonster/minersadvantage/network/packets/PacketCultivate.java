package uk.co.duelmonster.minersadvantage.network.packets;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.concurrent.TickDelayedTask;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import uk.co.duelmonster.minersadvantage.network.packetids.PacketId;
import uk.co.duelmonster.minersadvantage.workers.AgentProcessor;
import uk.co.duelmonster.minersadvantage.workers.CultivationAgent;

public class PacketCultivate implements IMAPacket {

  public final BlockPos pos;

  public PacketCultivate(BlockPos _pos) {
    pos = _pos;
  }

  public PacketCultivate(PacketBuffer buf) {
    pos = buf.readBlockPos();
  }

  @Override
  public PacketId getPacketId() {
    return PacketId.Cultivate;
  }

  public static void encode(PacketCultivate pkt, PacketBuffer buf) {
    buf.writeBlockPos(pkt.pos);
  }

  public static PacketCultivate decode(PacketBuffer buf) {
    return new PacketCultivate(buf);
  }

  public static void handle(final PacketCultivate pkt, Supplier<Context> ctx) {
    ctx.get().enqueueWork(() -> {
      // Work that needs to be threadsafe (most work)
      ServerPlayerEntity player = ctx.get().getSender(); // the client that sent this packet

      // do stuff
      process(player, pkt);

    });
    ctx.get().setPacketHandled(true);
  }

  public static void process(ServerPlayerEntity player, final PacketCultivate pkt) {
    player.getServer().tell(new TickDelayedTask(player.getServer().getTickCount(), () -> {
      AgentProcessor.INSTANCE.startProcessing(player, new CultivationAgent(player, pkt));
    }));
  }
}
