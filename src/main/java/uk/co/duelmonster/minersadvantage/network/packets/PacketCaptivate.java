package uk.co.duelmonster.minersadvantage.network.packets;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import uk.co.duelmonster.minersadvantage.common.Functions;
import uk.co.duelmonster.minersadvantage.config.MAConfig;
import uk.co.duelmonster.minersadvantage.config.MAConfig_Client;
import uk.co.duelmonster.minersadvantage.network.packetids.PacketId;

public class PacketCaptivate implements IMAPacket {
	
	public PacketCaptivate() {}
	
	public PacketCaptivate(PacketBuffer buf) {}
	
	@Override
	public PacketId getPacketId() {
		return PacketId.Captivate;
	}
	
	public static void encode(PacketCaptivate pkt, PacketBuffer buf) {}
	
	public static PacketCaptivate decode(PacketBuffer buf) {
		return new PacketCaptivate(buf);
	}
	
	public static void handle(final PacketCaptivate pkt, Supplier<Context> ctx) {
		ctx.get().enqueueWork(() -> {
			// Work that needs to be threadsafe (most work)
			ServerPlayerEntity sender = ctx.get().getSender(); // the client that sent this packet
			// do stuff
			MAConfig_Client settings = MAConfig.CLIENT;
			
			if (settings != null) {
				AxisAlignedBB captivateArea = sender.getBoundingBox().grow(settings.captivation.radiusHorizontal(), settings.captivation.radiusVertical(), settings.captivation.radiusHorizontal());
				
				if (sender.world != null) {
					List<Entity> localDrops = Functions.getNearbyEntities(sender.world, captivateArea);
					if (localDrops != null && !localDrops.isEmpty()) {
						for (Entity entity : localDrops) {
							if (entity instanceof ItemEntity) {
								ItemEntity eItem = (ItemEntity) entity;
								
								if (!eItem.cannotPickup() && (settings.captivation.blacklist() == null || settings.captivation.blacklist().size() == 0
										|| !settings.captivation.blacklist().contains(Functions.getName(eItem))))
									entity.onCollideWithPlayer(sender);
								
							} else if (entity instanceof ExperienceOrbEntity)
								entity.onCollideWithPlayer(sender);
						}
					}
				}
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
