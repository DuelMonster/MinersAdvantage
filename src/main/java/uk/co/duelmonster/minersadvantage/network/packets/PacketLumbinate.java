package uk.co.duelmonster.minersadvantage.network.packets;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;
import uk.co.duelmonster.minersadvantage.network.packetids.PacketId;
import uk.co.duelmonster.minersadvantage.workers.AgentProcessor;
import uk.co.duelmonster.minersadvantage.workers.LumbinationAgent;

public class PacketLumbinate extends BaseBlockPacket {

  public PacketLumbinate(BlockPos _pos, Direction _sideHit, int _stateID) {
    super(_pos, _sideHit, _stateID);
  }

  public PacketLumbinate(FriendlyByteBuf buf) {
    super(buf);
  }

  @Override
  public PacketId getPacketId() {
    return PacketId.Lumbinate;
  }

  public static void encode(PacketLumbinate pkt, FriendlyByteBuf buf) {
    buf.writeBlockPos(pkt.pos);
    buf.writeEnum(pkt.faceHit);
    buf.writeInt(pkt.stateID);
  }

  public static PacketLumbinate decode(FriendlyByteBuf buf) {
    return new PacketLumbinate(buf);
  }

  public static void handle(final PacketLumbinate pkt, Supplier<Context> ctx) {
    ctx.get().enqueueWork(() -> {
      // Work that needs to be threadsafe (most work)
      ServerPlayer player = ctx.get().getSender(); // the client that sent this packet

      // do stuff
      process(player, pkt);

    });
    ctx.get().setPacketHandled(true);
  }

  public static void process(ServerPlayer player, final PacketLumbinate pkt) {
    player.getServer().tell(new TickTask(player.getServer().getTickCount(), () -> {
      AgentProcessor.INSTANCE.startProcessing(player, new LumbinationAgent(player, pkt));
    }));
  }
}
