package co.uk.duelmonster.minersadvantage.handlers;

import java.util.List;

import co.uk.duelmonster.minersadvantage.common.Functions;
import co.uk.duelmonster.minersadvantage.common.JsonHelper;
import co.uk.duelmonster.minersadvantage.packets.NetworkPacket;
import co.uk.duelmonster.minersadvantage.settings.Settings;
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
		
		Settings settings = Settings.get(player.getUniqueID());
		
		AxisAlignedBB captivateArea = player.getEntityBoundingBox().expand(settings.radiusHorizontal(), settings.radiusVertical(), settings.radiusHorizontal());
		
		if (player.worldObj != null) {
			List<Entity> localDrops = Functions.getNearbyEntities(player.worldObj, captivateArea);
			if (localDrops != null && !localDrops.isEmpty()) {
				for (Entity entity : localDrops) {
					if (entity instanceof EntityItem) {
						EntityItem eItem = (EntityItem) entity;
						
						if (!eItem.cannotPickup()
								&& (settings.captivationBlacklist() == null
										|| JsonHelper.size(settings.captivationBlacklist()) == 0
										|| !settings.captivationBlacklist().has(eItem.getEntityItem().getItem().getRegistryName().toString().trim())))
							entity.onCollideWithPlayer(player);
						
					} else if (entity instanceof EntityXPOrb)
						entity.onCollideWithPlayer(player);
				}
			}
		}
	}
}
