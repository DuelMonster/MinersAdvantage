package uk.co.duelmonster.minersadvantage.network.packets;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import uk.co.duelmonster.minersadvantage.client.ClientFunctions;
import uk.co.duelmonster.minersadvantage.helpers.SubstitutionHelper;
import uk.co.duelmonster.minersadvantage.network.packetids.PacketId;

public class PacketSubstituteTool implements IMAPacket {

	public final BlockPos pos;
	public final int optimalSlot;

	public PacketSubstituteTool(BlockPos _pos) {
		pos = _pos;
		optimalSlot = -1;
	}
	public PacketSubstituteTool(int _optimalSlot) {
		pos = BlockPos.ZERO;
		optimalSlot = _optimalSlot;
	}
	public PacketSubstituteTool(PacketBuffer buf) {
		pos = buf.readBlockPos();
		optimalSlot = buf.readInt();
	}

	@Override
	public PacketId getPacketId() { return PacketId.Substitute; }

	public static void encode(PacketSubstituteTool pkt, PacketBuffer buf) {
		buf.writeBlockPos(pkt.pos);
		buf.writeInt(pkt.optimalSlot);
	}

	public static PacketSubstituteTool decode(PacketBuffer buf) { return new PacketSubstituteTool(buf); }

	public static void handle(final PacketSubstituteTool pkt, Supplier<Context> ctx) {
		ctx.get().enqueueWork(() -> {
			// Work that needs to be threadsafe (most work)
			if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
				ServerPlayerEntity player = ctx.get().getSender(); // the client that sent this packet

				new SubstitutionHelper().processToolSubtitution(player, pkt.pos);

			} else {

				ClientFunctions.syncCurrentPlayItem(pkt.optimalSlot);

			}
		});
	}
}
