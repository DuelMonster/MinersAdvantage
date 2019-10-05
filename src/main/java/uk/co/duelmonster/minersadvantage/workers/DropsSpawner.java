package uk.co.duelmonster.minersadvantage.workers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import uk.co.duelmonster.minersadvantage.MinersAdvantage;
import uk.co.duelmonster.minersadvantage.common.Functions;
import uk.co.duelmonster.minersadvantage.config.MAConfig;

public class DropsSpawner {
	
	private static List<Entity>	recordedDrops			= Collections.synchronizedList(new ArrayList<Entity>());
	private static int			recordedXPCount			= 0;
	
	public static void recordDrop(Entity entity) {
		recordedDrops.add(entity);
	}
	
	public static void addXP(int value) {
		recordedXPCount += value;
	}
	
	public static List<Entity> getDrops(){
		return recordedDrops;
	}
	
	public static BlockItem getDropOfBlockType(Class<?> blockClass) {
		return getDropOfBlockTypeFromList(blockClass, recordedDrops);
	}
	public static BlockItem getDropOfBlockTypeFromList(Class<?> blockClass, List<Entity> drops) {
		for (Entity entity : drops) {
			if (entity != null 
					&& entity instanceof ItemEntity 
					&& ((ItemEntity) entity).getItem().getItem() instanceof BlockItem
					&& blockClass.isInstance(((BlockItem)((ItemEntity) entity).getItem().getItem()).getBlock()))
				return (BlockItem)((ItemEntity) entity).getItem().getItem();
		}
		return null;
	}
	
	public static void spawnDrops(World world, BlockPos spawnPos) {
		if (spawnPos != null)
			try {
				while (!recordedDrops.isEmpty()) {
					List<Entity> dropsClone = new ArrayList<Entity>(recordedDrops);
					recordedDrops.clear();
					
					// Respawn the recorded Drops
					for (Entity entity : dropsClone) {
						if (entity != null && entity instanceof ItemEntity) {
							
							if (MAConfig.CLIENT.common.gatherDrops() && spawnPos != null)
								entity = new ItemEntity(world, spawnPos.getX() + 0.5D, spawnPos.getY() + 0.5D, spawnPos.getZ() + 0.5D, ((ItemEntity) entity).getItem());
							
							world.addEntity(entity);
							
						}
					}
					
					// Respawn the recorded XP
					if (recordedXPCount > 0) {
						ExperienceOrbEntity entity = new ExperienceOrbEntity(world, spawnPos.getX() + 0.5D, spawnPos.getY() + 0.5D, spawnPos.getZ() + 0.5D, recordedXPCount);
						
						world.addEntity(entity);
						recordedXPCount = 0;
					}
					
				}
			}
			catch (ConcurrentModificationException e) {
				MinersAdvantage.LOGGER.error("ConcurrentModification Exception Caught and Avoided : " + Functions.getStackTrace());
			}
	}
	
}
