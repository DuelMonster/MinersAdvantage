package uk.co.duelmonster.minersadvantage.network.packets.unused;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent.Context;
import uk.co.duelmonster.minersadvantage.network.packetids.PacketId;
import uk.co.duelmonster.minersadvantage.utils.UtilsServer;

public class PacketSetHotbarItem {

  private final ItemStack itemStack;
  private final int       hotbarSlot;

  public PacketSetHotbarItem(ItemStack _itemStack, int _hotbarSlot) {
    this.itemStack  = _itemStack;
    this.hotbarSlot = _hotbarSlot;
  }

  public PacketSetHotbarItem(FriendlyByteBuf buf) {
    this.itemStack  = buf.readItem();
    this.hotbarSlot = buf.readVarInt();
  }

  public PacketId getPacketId() {
    return PacketId.SetHotbarItem;
  }

  public static void encode(PacketSetHotbarItem pkt, FriendlyByteBuf buf) {
    buf.writeItem(pkt.itemStack);
    buf.writeVarInt(pkt.hotbarSlot);
  }

  public static PacketSetHotbarItem decode(FriendlyByteBuf buf) {
    return new PacketSetHotbarItem(buf);
  }

  public static void handle(final PacketSetHotbarItem pkt, Supplier<Context> ctx) {
    ctx.get().enqueueWork(() -> {
      // Work that needs to be threadsafe (most work)
      ServerPlayer sender = ctx.get().getSender(); // the client that sent this packet
      // do stuff
      ItemStack itemStack = pkt.itemStack;
      if (!itemStack.isEmpty())
        UtilsServer.setHotbarSlot(sender, itemStack, pkt.hotbarSlot);
    });
  }
}