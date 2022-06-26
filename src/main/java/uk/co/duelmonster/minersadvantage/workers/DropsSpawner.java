package uk.co.duelmonster.minersadvantage.workers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.Level;
import uk.co.duelmonster.minersadvantage.common.Constants;
import uk.co.duelmonster.minersadvantage.common.Functions;
import uk.co.duelmonster.minersadvantage.config.MAConfig_Client;
import uk.co.duelmonster.minersadvantage.config.SyncedClientConfig;

public class DropsSpawner {

  private static List<Entity> recordedDrops   = Collections.synchronizedList(new ArrayList<Entity>());
  private static int          recordedXPCount = 0;

  public static void recordDrop(Entity entity) {
    recordedDrops.add(entity);
  }

  public static void addXP(int value) {
    recordedXPCount += value;
  }

  public static List<Entity> getDrops() {
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
          && blockClass.isInstance(((BlockItem) ((ItemEntity) entity).getItem().getItem()).getBlock()))
        return (BlockItem) ((ItemEntity) entity).getItem().getItem();
    }
    return null;
  }

  public static void spawnDrops(UUID playerUID, Level world, BlockPos spawnPos) {
    if (spawnPos != null)
      try {
        SyncedClientConfig clientConfig = MAConfig_Client.getPlayerConfig(playerUID);

        while (!recordedDrops.isEmpty()) {
          List<Entity> dropsClone = new ArrayList<Entity>(recordedDrops);
          recordedDrops.clear();

          // Respawn the recorded Drops
          for (Entity entity : dropsClone) {
            if (entity != null && entity instanceof ItemEntity) {

              if (clientConfig.common.gatherDrops && spawnPos != null)
                entity = new ItemEntity(world, spawnPos.getX() + 0.5D, spawnPos.getY() + 0.5D, spawnPos.getZ() + 0.5D, ((ItemEntity) entity).getItem());

              world.addFreshEntity(entity);

            }
          }

          // Respawn the recorded XP
          if (recordedXPCount > 0) {
            ExperienceOrb entity = new ExperienceOrb(world, spawnPos.getX() + 0.5D, spawnPos.getY() + 0.5D, spawnPos.getZ() + 0.5D, recordedXPCount);

            world.addFreshEntity(entity);
            recordedXPCount = 0;
          }

        }
      } catch (ConcurrentModificationException e) {
        Constants.LOGGER.error("ConcurrentModification Exception Caught and Avoided : " + Functions.getStackTrace());
      }
  }

}
