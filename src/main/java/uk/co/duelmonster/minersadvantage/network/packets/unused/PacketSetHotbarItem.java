package uk.co.duelmonster.minersadvantage.network.packets.unused;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import uk.co.duelmonster.minersadvantage.network.packetids.PacketId;
import uk.co.duelmonster.minersadvantage.utils.UtilsServer;

public class PacketSetHotbarItem {
	
	private final ItemStack itemStack;
	private final int hotbarSlot;

	public PacketSetHotbarItem(ItemStack _itemStack, int _hotbarSlot) {
		this.itemStack = _itemStack;
		this.hotbarSlot = _hotbarSlot;
	}
	public PacketSetHotbarItem(PacketBuffer buf) {
		this.itemStack = buf.readItemStack();
		this.hotbarSlot = buf.readVarInt();
	}

	public PacketId getPacketId() { return PacketId.SetHotbarItem; }

	public static void encode(PacketSetHotbarItem pkt, PacketBuffer buf) {
		buf.writeItemStack(pkt.itemStack);
		buf.writeVarInt(pkt.hotbarSlot);
	}

	public static PacketSetHotbarItem decode(PacketBuffer buf) { return new PacketSetHotbarItem(buf); }

	public static void handle(final PacketSetHotbarItem pkt, Supplier<Context> ctx) {
		ctx.get().enqueueWork(() -> {
			// Work that needs to be threadsafe (most work)
			ServerPlayerEntity sender = ctx.get().getSender(); // the client that sent this packet
			// do stuff
			ItemStack itemStack = pkt.itemStack;
			if (!itemStack.isEmpty())
				UtilsServer.setHotbarSlot(sender, itemStack, pkt.hotbarSlot);
		});
	}
}