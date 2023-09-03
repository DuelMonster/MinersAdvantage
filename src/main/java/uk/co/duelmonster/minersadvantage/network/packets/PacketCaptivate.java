package uk.co.duelmonster.minersadvantage.network.packets;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.NetworkEvent.Context;
import uk.co.duelmonster.minersadvantage.common.Functions;
import uk.co.duelmonster.minersadvantage.config.MAConfig_Client;
import uk.co.duelmonster.minersadvantage.config.SyncedClientConfig;
import uk.co.duelmonster.minersadvantage.network.packetids.PacketId;

public class PacketCaptivate implements IMAPacket {

  public PacketCaptivate() {}

  public PacketCaptivate(FriendlyByteBuf buf) {}

  @Override
  public PacketId getPacketId() {
    return PacketId.Captivate;
  }

  public static void encode(PacketCaptivate pkt, FriendlyByteBuf buf) {}

  public static PacketCaptivate decode(FriendlyByteBuf buf) {
    return new PacketCaptivate(buf);
  }

  public static void handle(final PacketCaptivate pkt, Supplier<Context> ctx) {
    ctx.get().enqueueWork(() -> {
      // Work that needs to be threadsafe (most work)
      ServerPlayer sender = ctx.get().getSender(); // the client that sent this packet

      // do stuff
      SyncedClientConfig clientConfig = MAConfig_Client.getPlayerConfig(sender.getUUID());
      if (clientConfig != null) {
        AABB captivateArea = sender.getBoundingBox().inflate(clientConfig.captivation.radiusHorizontal, clientConfig.captivation.radiusVertical, clientConfig.captivation.radiusHorizontal);

        if (sender.level() != null) {
          List<Entity> localDrops = Functions.getNearbyEntities(sender.level(), captivateArea);
          if (localDrops != null && !localDrops.isEmpty()) {
            for (Entity entity : localDrops) {
              if (entity instanceof ItemEntity) {
                ItemEntity eItem = (ItemEntity) entity;

                if (!eItem.hasPickUpDelay() && (clientConfig.captivation.blacklist == null || clientConfig.captivation.blacklist.size() == 0
                    || !clientConfig.captivation.blacklist.contains(Functions.getName(eItem))))
                  entity.playerTouch(sender);

              } else if (entity instanceof ExperienceOrb)
                entity.playerTouch(sender);
            }
          }
        }
      }
    });
    ctx.get().setPacketHandled(true);
  }
}
