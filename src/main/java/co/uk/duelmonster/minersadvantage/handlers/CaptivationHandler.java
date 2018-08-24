package co.uk.duelmonster.minersadvantage.handlers;

import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import co.uk.duelmonster.minersadvantage.common.Functions;
import co.uk.duelmonster.minersadvantage.config.MAConfig;
import co.uk.duelmonster.minersadvantage.packets.NetworkPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class CaptivationHandler implements IPacketHandler {
	
	public static CaptivationHandler instance = new CaptivationHandler();
	
	@Override
	public void processClientMessage(NetworkPacket message, MessageContext context) {}
	
	@Override
	public void processServerMessage(NetworkPacket message, MessageContext context) {
		final EntityPlayerMP player = context.getServerHandler().playerEntity;
		if (player == null)
			return;
		
		MAConfig settings = MAConfig.get(player.getUniqueID());
		
		AxisAlignedBB captivateArea = player.getEntityBoundingBox().expand(settings.captivation.radiusHorizontal(), settings.captivation.radiusVertical(), settings.captivation.radiusHorizontal());
		
		if (player.world != null) {
			List<Entity> localDrops = Functions.getNearbyEntities(player.world, captivateArea);
			if (localDrops != null && !localDrops.isEmpty()) {
				for (Entity entity : localDrops) {
					if (entity instanceof EntityItem) {
						EntityItem eItem = (EntityItem) entity;
						
						if (!eItem.cannotPickup()
								&& (settings.captivation.blacklist() == null
										|| settings.captivation.blacklist().length == 0
										|| !ArrayUtils.contains(settings.captivation.blacklist(), eItem.getEntityItem().getItem().getRegistryName().toString().trim())))
							entity.onCollideWithPlayer(player);
						
					} else if (entity instanceof EntityXPOrb)
						entity.onCollideWithPlayer(player);
				}
			}
		}
	}
}
