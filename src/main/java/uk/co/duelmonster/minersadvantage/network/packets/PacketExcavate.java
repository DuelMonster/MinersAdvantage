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
import uk.co.duelmonster.minersadvantage.workers.ExcavationAgent;

public class PacketExcavate extends BaseBlockPacket {

  public PacketExcavate(BlockPos _pos, Direction _sideHit, int _stateID) {
    super(_pos, _sideHit, _stateID);
  }

  public PacketExcavate(PacketBuffer buf) {
    super(buf);
  }

  @Override
  public PacketId getPacketId() {
    return PacketId.Excavate;
  }

  public static void encode(PacketExcavate pkt, PacketBuffer buf) {
    buf.writeBlockPos(pkt.pos);
    buf.writeEnum(pkt.faceHit);
    buf.writeInt(pkt.stateID);
  }

  public static PacketExcavate decode(PacketBuffer buf) {
    return new PacketExcavate(buf);
  }

  public static void handle(final PacketExcavate pkt, Supplier<Context> ctx) {
    ctx.get().enqueueWork(() -> {
      // Work that needs to be threadsafe (most work)
      ServerPlayerEntity player = ctx.get().getSender(); // the client that sent this packet

      // do stuff
      process(player, pkt);

    });
    ctx.get().setPacketHandled(true);
  }

  public static void process(ServerPlayerEntity player, final PacketExcavate pkt) {
    player.getServer().tell(new TickDelayedTask(player.getServer().getTickCount(), () -> {
      AgentProcessor.INSTANCE.startProcessing(player, new ExcavationAgent(player, pkt));
    }));
  }
}
