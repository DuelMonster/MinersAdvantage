package uk.co.duelmonster.minersadvantage.network.packets;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import uk.co.duelmonster.minersadvantage.network.packetids.PacketId;
import uk.co.duelmonster.minersadvantage.workers.AgentProcessor;
import uk.co.duelmonster.minersadvantage.workers.CropinationAgent;

public class PacketCropinate implements IMAPacket {
	
	public final BlockPos pos;
	
	public PacketCropinate(BlockPos _pos) {
		pos = _pos;
	}
	
	public PacketCropinate(PacketBuffer buf) {
		pos = buf.readBlockPos();
	}
	
	@Override
	public PacketId getPacketId() {
		return PacketId.Cropinate;
	}
	
	public static void encode(PacketCropinate pkt, PacketBuffer buf) {
		buf.writeBlockPos(pkt.pos);
	}
	
	public static PacketCropinate decode(PacketBuffer buf) {
		return new PacketCropinate(buf);
	}
	
	public static void handle(final PacketCropinate pkt, Supplier<Context> ctx) {
		ctx.get().enqueueWork(() -> {
			// Work that needs to be threadsafe (most work)
			ServerPlayerEntity player = ctx.get().getSender(); // the client that sent this packet
			
			// do stuff
			player.getServer().deferTask(new Runnable() {
				@Override
				public void run() {
					AgentProcessor.INSTANCE.startProcessing(player, new CropinationAgent(player, pkt));
				}
			});
		
		});
		ctx.get().setPacketHandled(true);
	}
}
