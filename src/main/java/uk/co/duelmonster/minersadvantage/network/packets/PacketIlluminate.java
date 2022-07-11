package uk.co.duelmonster.minersadvantage.network.packets;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;
import uk.co.duelmonster.minersadvantage.common.TorchPlacement;
import uk.co.duelmonster.minersadvantage.network.packetids.PacketId;
import uk.co.duelmonster.minersadvantage.workers.AgentProcessor;
import uk.co.duelmonster.minersadvantage.workers.IlluminationAgent;

public class PacketIlluminate implements IMAPacket {

  public final PacketId       originPacket;
  public final BlockPos       areaStartPos;
  public final BlockPos       areaEndPos;
  public final Direction      faceHit;
  public final boolean        singleTorch;
  public final TorchPlacement torchPlacement;

  public PacketIlluminate(BlockPos pos, Direction _faceHit) {
    this.originPacket   = PacketId.Illuminate;
    this.areaStartPos   = pos;
    this.areaEndPos     = pos;
    this.faceHit        = _faceHit;
    this.singleTorch    = true;
    this.torchPlacement = TorchPlacement.FLOOR;
  }

  public PacketIlluminate(PacketId originPacket, BlockPos areaStartPos, BlockPos areaEndPos, Direction _faceHit) {
    this.originPacket   = originPacket;
    this.areaStartPos   = areaStartPos;
    this.areaEndPos     = areaEndPos;
    this.faceHit        = _faceHit;
    this.singleTorch    = false;
    this.torchPlacement = TorchPlacement.FLOOR;
  }

  public PacketIlluminate(PacketId originPacket, BlockPos areaStartPos, BlockPos areaEndPos, TorchPlacement torchPlacement) {
    this.originPacket   = originPacket;
    this.areaStartPos   = areaStartPos;
    this.areaEndPos     = areaEndPos;
    this.faceHit        = Direction.UP;
    this.singleTorch    = false;
    this.torchPlacement = torchPlacement;
  }

  public PacketIlluminate(PacketId originPacket, BlockPos areaStartPos, BlockPos areaEndPos, Direction _faceHit, TorchPlacement torchPlacement) {
    this.originPacket   = originPacket;
    this.areaStartPos   = areaStartPos;
    this.areaEndPos     = areaEndPos;
    this.faceHit        = _faceHit;
    this.singleTorch    = false;
    this.torchPlacement = torchPlacement;
  }

  public PacketIlluminate(FriendlyByteBuf buf) {
    this.originPacket   = buf.readEnum(PacketId.class);
    this.areaStartPos   = buf.readBlockPos();
    this.areaEndPos     = buf.readBlockPos();
    this.faceHit        = buf.readEnum(Direction.class);
    this.singleTorch    = buf.readBoolean();
    this.torchPlacement = buf.readEnum(TorchPlacement.class);
  }

  @Override
  public PacketId getPacketId() {
    return PacketId.Illuminate;
  }

  public static void encode(PacketIlluminate pkt, FriendlyByteBuf buf) {
    buf.writeEnum(pkt.originPacket);
    buf.writeBlockPos(pkt.areaStartPos);
    buf.writeBlockPos(pkt.areaEndPos);
    buf.writeEnum(pkt.faceHit);
    buf.writeBoolean(pkt.singleTorch);
    buf.writeEnum(pkt.torchPlacement);
  }

  public static PacketIlluminate decode(FriendlyByteBuf buf) {
    return new PacketIlluminate(buf);
  }

  public static void handle(final PacketIlluminate pkt, Supplier<Context> ctx) {
    ctx.get().enqueueWork(() -> {
      // Work that needs to be threadsafe (most work)
      final ServerPlayer player = ctx.get().getSender(); // the client that sent this packet

      // do stuff
      process(player, pkt);

    });
    ctx.get().setPacketHandled(true);
  }

  public static void process(ServerPlayer player, final PacketIlluminate pkt) {
    player.getServer().tell(new TickTask(player.getServer().getTickCount(), () -> {
      AgentProcessor.INSTANCE.startProcessing(player, new IlluminationAgent(player, pkt));
    }));
  }
}
