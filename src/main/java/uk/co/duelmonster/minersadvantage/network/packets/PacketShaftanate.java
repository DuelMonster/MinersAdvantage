package uk.co.duelmonster.minersadvantage.network.packets;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.concurrent.TickDelayedTask;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import uk.co.duelmonster.minersadvantage.network.packetids.PacketId;
import uk.co.duelmonster.minersadvantage.workers.AgentProcessor;
import uk.co.duelmonster.minersadvantage.workers.ShaftanationAgent;

public class PacketShaftanate extends BaseBlockPacket {

  public PacketShaftanate(BlockPos _pos, Direction _sideHit, int _stateID) {
    super(_pos, _sideHit, _stateID);
  }

  public PacketShaftanate(PacketBuffer buf) {
    super(buf);
  }

  @Override
  public PacketId getPacketId() {
    return PacketId.Shaftanate;
  }

  public static void encode(PacketShaftanate pkt, PacketBuffer buf) {
    buf.writeBlockPos(pkt.pos);
    buf.writeEnum(pkt.faceHit);
    buf.writeInt(pkt.stateID);
  }

  public static PacketShaftanate decode(PacketBuffer buf) {
    return new PacketShaftanate(buf);
  }

  public static void handle(final PacketShaftanate pkt, Supplier<Context> ctx) {
    ctx.get().enqueueWork(() -> {
      // Work that needs to be threadsafe (most work)
      ServerPlayerEntity player = ctx.get().getSender(); // the client that sent this packet

      // do stuff
      process(player, pkt);

    });
    ctx.get().setPacketHandled(true);
  }

  public static void process(ServerPlayerEntity player, final PacketShaftanate pkt) {
    player.getServer().tell(new TickDelayedTask(player.getServer().getTickCount(), () -> {
      AgentProcessor.INSTANCE.startProcessing(player, new ShaftanationAgent(player, pkt));
    }));
  }
}
