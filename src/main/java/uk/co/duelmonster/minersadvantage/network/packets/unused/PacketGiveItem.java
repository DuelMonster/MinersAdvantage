package uk.co.duelmonster.minersadvantage.network.packets.unused;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import uk.co.duelmonster.minersadvantage.network.packetids.PacketId;
import uk.co.duelmonster.minersadvantage.utils.UtilsServer;

public class PacketGiveItem {
  private final ItemStack itemStack;

  public PacketGiveItem(PacketBuffer buf) {
    this.itemStack = buf.readItem();
  }

  public PacketId getPacketId() {
    return PacketId.GiveItem;
  }

  public static void encode(PacketGiveItem pkt, PacketBuffer buf) {
    CompoundNBT nbt = pkt.itemStack.serializeNBT();
    buf.writeNbt(nbt);
  }

  public static PacketGiveItem decode(PacketBuffer buf) {
    return new PacketGiveItem(buf);
  }

  public static void handle(final PacketGiveItem pkt, Supplier<Context> ctx) {
    ctx.get().enqueueWork(() -> {
      // Work that needs to be threadsafe (most work)
      ServerPlayerEntity sender = ctx.get().getSender(); // the client that sent this packet
      // do stuff
      ItemStack itemStack = pkt.itemStack;
      if (!itemStack.isEmpty())
        UtilsServer.giveToInventory(sender, itemStack);
    });
  }
}