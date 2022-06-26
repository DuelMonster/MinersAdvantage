package uk.co.duelmonster.minersadvantage.network.packets;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;
import uk.co.duelmonster.minersadvantage.network.packetids.PacketId;
import uk.co.duelmonster.minersadvantage.workers.AgentProcessor;
import uk.co.duelmonster.minersadvantage.workers.CultivationAgent;

public class PacketCultivate implements IMAPacket {

  public final BlockPos pos;

  public PacketCultivate(BlockPos _pos) {
    pos = _pos;
  }

  public PacketCultivate(FriendlyByteBuf buf) {
    pos = buf.readBlockPos();
  }

  @Override
  public PacketId getPacketId() {
    return PacketId.Cultivate;
  }

  public static void encode(PacketCultivate pkt, FriendlyByteBuf buf) {
    buf.writeBlockPos(pkt.pos);
  }

  public static PacketCultivate decode(FriendlyByteBuf buf) {
    return new PacketCultivate(buf);
  }

  public static void handle(final PacketCultivate pkt, Supplier<Context> ctx) {
    ctx.get().enqueueWork(() -> {
      // Work that needs to be threadsafe (most work)
      ServerPlayer player = ctx.get().getSender(); // the client that sent this packet

      // do stuff
      process(player, pkt);

    });
    ctx.get().setPacketHandled(true);
  }

  public static void process(ServerPlayer player, final PacketCultivate pkt) {
    player.getServer().tell(new TickTask(player.getServer().getTickCount(), () -> {
      AgentProcessor.INSTANCE.startProcessing(player, new CultivationAgent(player, pkt));
    }));
  }
}
