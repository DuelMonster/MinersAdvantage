package uk.co.duelmonster.minersadvantage.network.packets;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.concurrent.TickDelayedTask;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import uk.co.duelmonster.minersadvantage.common.TorchPlacement;
import uk.co.duelmonster.minersadvantage.network.packetids.PacketId;
import uk.co.duelmonster.minersadvantage.workers.AgentProcessor;
import uk.co.duelmonster.minersadvantage.workers.IlluminationAgent;

public class PacketIlluminate implements IMAPacket {

  public final BlockPos       areaStartPos;
  public final BlockPos       areaEndPos;
  public final Direction      faceHit;
  public final boolean        singleTorch;
  public final TorchPlacement torchPlacement;

  public PacketIlluminate(BlockPos pos, Direction _faceHit) {
    this.areaStartPos   = pos;
    this.areaEndPos     = pos;
    this.faceHit        = _faceHit;
    this.singleTorch    = true;
    this.torchPlacement = TorchPlacement.FLOOR;
  }

  public PacketIlluminate(BlockPos areaStartPos, BlockPos areaEndPos, Direction _faceHit) {
    this.areaStartPos   = areaStartPos;
    this.areaEndPos     = areaEndPos;
    this.faceHit        = _faceHit;
    this.singleTorch    = false;
    this.torchPlacement = TorchPlacement.FLOOR;
  }

  public PacketIlluminate(BlockPos areaStartPos, BlockPos areaEndPos, TorchPlacement torchPlacement) {
    this.areaStartPos   = areaStartPos;
    this.areaEndPos     = areaEndPos;
    this.faceHit        = Direction.UP;
    this.singleTorch    = false;
    this.torchPlacement = torchPlacement;
  }

  public PacketIlluminate(PacketBuffer buf) {
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

  public static void encode(PacketIlluminate pkt, PacketBuffer buf) {
    buf.writeBlockPos(pkt.areaStartPos);
    buf.writeBlockPos(pkt.areaEndPos);
    buf.writeEnum(pkt.faceHit);
    buf.writeBoolean(pkt.singleTorch);
    buf.writeEnum(pkt.torchPlacement);
  }

  public static PacketIlluminate decode(PacketBuffer buf) {
    return new PacketIlluminate(buf);
  }

  public static void handle(final PacketIlluminate pkt, Supplier<Context> ctx) {
    ctx.get().enqueueWork(() -> {
      // Work that needs to be threadsafe (most work)
      final ServerPlayerEntity player = ctx.get().getSender(); // the client that sent this packet

      // do stuff
      process(player, pkt);

    });
    ctx.get().setPacketHandled(true);
  }

  public static void process(ServerPlayerEntity player, final PacketIlluminate pkt) {
    player.getServer().tell(new TickDelayedTask(player.getServer().getTickCount(), () -> {
      AgentProcessor.INSTANCE.startProcessing(player, new IlluminationAgent(player, pkt));
    }));
  }
}
