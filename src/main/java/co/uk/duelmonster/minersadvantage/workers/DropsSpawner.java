package co.uk.duelmonster.minersadvantage.workers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;

import co.uk.duelmonster.minersadvantage.MinersAdvantage;
import co.uk.duelmonster.minersadvantage.common.Functions;
import co.uk.duelmonster.minersadvantage.config.MAConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DropsSpawner {
	
	private static List<Entity>	recordedDrops	= Collections.synchronizedList(new ArrayList<Entity>());
	private static int			recordedXPCount	= 0;
	
	public static void recordDrop(Entity entity) {
		recordedDrops.add(entity);
	}
	
	public static void addXP(int value) {
		recordedXPCount += value;
	}
	
	public static void spawnDrops(World world, BlockPos spawnPos) {
		if (spawnPos != null)
			try {
				while (!recordedDrops.isEmpty()) {
					List<Entity> dropsClone = new ArrayList<Entity>(recordedDrops);
					recordedDrops.clear();
					
					// Respawn the recorded Drops
					for (Entity entity : dropsClone) {
						if (entity != null && entity instanceof EntityItem) {
							
							if (MAConfig.get().common.bGatherDrops() && spawnPos != null)
								entity = new EntityItem(world, spawnPos.getX() + 0.5D, spawnPos.getY() + 0.5D, spawnPos.getZ() + 0.5D, ((EntityItem) entity).getEntityItem());
							
							world.spawnEntityInWorld(entity);
							
						}
					}
					
					// Respawn the recorded XP
					if (recordedXPCount > 0) {
						EntityXPOrb entity = new EntityXPOrb(world, spawnPos.getX() + 0.5D, spawnPos.getY() + 0.5D, spawnPos.getZ() + 0.5D, recordedXPCount);
						
						world.spawnEntityInWorld(entity);
						recordedXPCount = 0;
					}
					
				}
			}
			catch (ConcurrentModificationException e) {
				MinersAdvantage.logger.error("ConcurrentModification Exception Caught and Avoided : " + Functions.getStackTrace());
			}
	}
	
}
