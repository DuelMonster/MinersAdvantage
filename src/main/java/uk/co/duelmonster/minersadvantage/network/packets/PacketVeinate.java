package uk.co.duelmonster.minersadvantage.network.packets;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import uk.co.duelmonster.minersadvantage.network.packetids.PacketId;
import uk.co.duelmonster.minersadvantage.workers.AgentProcessor;
import uk.co.duelmonster.minersadvantage.workers.ExcavationAgent;

public class PacketVeinate extends BaseBlockPacket {
	
	public PacketVeinate(BlockPos _pos, Direction _sideHit, int _stateID) {
		super(_pos, _sideHit, _stateID);
	}
	
	public PacketVeinate(PacketBuffer buf) {
		super(buf);
	}
	
	@Override
	public PacketId getPacketId() {
		return PacketId.Veinate;
	}
	
	public static void encode(PacketVeinate pkt, PacketBuffer buf) {
		buf.writeBlockPos(pkt.pos);
		buf.writeEnumValue(pkt.faceHit);
		buf.writeInt(pkt.stateID);
	}
	
	public static PacketVeinate decode(PacketBuffer buf) {
		return new PacketVeinate(buf);
	}
	
	public static void handle(final PacketVeinate pkt, Supplier<Context> ctx) {
		ctx.get().enqueueWork(() -> {
			// Work that needs to be threadsafe (most work)
			ServerPlayerEntity player = ctx.get().getSender(); // the client that sent this packet
			
			// do stuff
			process(player, pkt);
		
		});
		ctx.get().setPacketHandled(true);
	}
	
	public static void process(ServerPlayerEntity player, final PacketVeinate pkt) {
		player.getServer().deferTask(new Runnable() {
			@Override
			public void run() {
				AgentProcessor.INSTANCE.startProcessing(player, new ExcavationAgent(player, pkt));
			}
		});
	}
}
