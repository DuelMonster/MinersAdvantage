package uk.co.duelmonster.minersadvantage.network.packets.unused;

import java.util.function.Supplier;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent.Context;
import uk.co.duelmonster.minersadvantage.network.packetids.PacketId;
import uk.co.duelmonster.minersadvantage.utils.UtilsServer;

public class PacketGiveItem {
  private final ItemStack itemStack;

  public PacketGiveItem(FriendlyByteBuf buf) {
    this.itemStack = buf.readItem();
  }

  public PacketId getPacketId() {
    return PacketId.GiveItem;
  }

  public static void encode(PacketGiveItem pkt, FriendlyByteBuf buf) {
    CompoundTag nbt = pkt.itemStack.serializeNBT();
    buf.writeNbt(nbt);
  }

  public static PacketGiveItem decode(FriendlyByteBuf buf) {
    return new PacketGiveItem(buf);
  }

  public static void handle(final PacketGiveItem pkt, Supplier<Context> ctx) {
    ctx.get().enqueueWork(() -> {
      // Work that needs to be threadsafe (most work)
      ServerPlayer sender = ctx.get().getSender(); // the client that sent this packet
      // do stuff
      ItemStack itemStack = pkt.itemStack;
      if (!itemStack.isEmpty())
        UtilsServer.giveToInventory(sender, itemStack);
    });
  }
}