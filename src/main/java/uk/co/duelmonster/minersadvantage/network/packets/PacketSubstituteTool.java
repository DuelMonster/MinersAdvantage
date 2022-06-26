package uk.co.duelmonster.minersadvantage.network.packets;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent.Context;
import uk.co.duelmonster.minersadvantage.client.ClientFunctions;
import uk.co.duelmonster.minersadvantage.helpers.SubstitutionHelper;
import uk.co.duelmonster.minersadvantage.network.packetids.PacketId;

public class PacketSubstituteTool implements IMAPacket {

  public final BlockPos pos;
  public final int      optimalSlot;

  public PacketSubstituteTool(BlockPos _pos) {
    pos         = _pos;
    optimalSlot = -1;
  }

  public PacketSubstituteTool(int _optimalSlot) {
    pos         = BlockPos.ZERO;
    optimalSlot = _optimalSlot;
  }

  public PacketSubstituteTool(FriendlyByteBuf buf) {
    pos         = buf.readBlockPos();
    optimalSlot = buf.readInt();
  }

  @Override
  public PacketId getPacketId() {
    return PacketId.Substitute;
  }

  public static void encode(PacketSubstituteTool pkt, FriendlyByteBuf buf) {
    buf.writeBlockPos(pkt.pos);
    buf.writeInt(pkt.optimalSlot);
  }

  public static PacketSubstituteTool decode(FriendlyByteBuf buf) {
    return new PacketSubstituteTool(buf);
  }

  public static void handle(final PacketSubstituteTool pkt, Supplier<Context> ctx) {
    ctx.get().enqueueWork(() -> {
      // Work that needs to be threadsafe (most work)
      if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
        ServerPlayer player = ctx.get().getSender(); // the client that sent this packet

        new SubstitutionHelper().processToolSubtitution(player, pkt.pos);

      } else {

        ClientFunctions.syncCurrentPlayItem(pkt.optimalSlot);

      }
    });
    ctx.get().setPacketHandled(true);
  }
}
