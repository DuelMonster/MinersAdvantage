package uk.co.duelmonster.minersadvantage.network.packets;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import uk.co.duelmonster.minersadvantage.network.packetids.PacketId;
import uk.co.duelmonster.minersadvantage.workers.AgentProcessor;
import uk.co.duelmonster.minersadvantage.workers.LumbinationAgent;

public class PacketLumbinate extends BaseBlockPacket {
	
	public PacketLumbinate(BlockPos _pos, Direction _sideHit, int _stateID) {
		super(_pos, _sideHit, _stateID);
	}
	
	public PacketLumbinate(PacketBuffer buf) {
		super(buf);
	}
	
	@Override
	public PacketId getPacketId() {
		return PacketId.Lumbinate;
	}
	
	public static void encode(PacketLumbinate pkt, PacketBuffer buf) {
		buf.writeBlockPos(pkt.pos);
		buf.writeEnumValue(pkt.faceHit);
		buf.writeInt(pkt.stateID);
	}
	
	public static PacketLumbinate decode(PacketBuffer buf) {
		return new PacketLumbinate(buf);
	}
	
	public static void handle(final PacketLumbinate pkt, Supplier<Context> ctx) {
		ctx.get().enqueueWork(() -> {
			// Work that needs to be threadsafe (most work)
			ServerPlayerEntity player = ctx.get().getSender(); // the client that sent this packet
			
			// do stuff
			player.getServer().deferTask(new Runnable() {
				@Override
				public void run() {
					AgentProcessor.INSTANCE.startProcessing(player, new LumbinationAgent(player, pkt));
				}
			});
		
		});
		ctx.get().setPacketHandled(true);
	}
}
