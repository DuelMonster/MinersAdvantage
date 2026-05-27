package uk.co.duelmonster.minersadvantage.common;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import uk.co.duelmonster.minersadvantage.common.config.SyncedClientConfig;
import uk.co.duelmonster.minersadvantage.common.registry.RegistryPredicates;
import uk.co.duelmonster.minersadvantage.common.log.LogUtils;

/**
 * Functions keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public class Functions {
    /**
     * localize exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static String localize(String key) {
        return Component.translatable(key).getString();
    }

    /**
     * isDebug exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static boolean isDebug() {
        return LogUtils.isDebugLoggingEnabled();
    }

    /**
     * DebugNotifyClient exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static void DebugNotifyClient(Player player, String message) {
        if (isDebug()) {
            NotifyClient(player, message);
        }
    }

    /**
     * DebugNotifyClient exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static void DebugNotifyClient(Player player, boolean isOn, String featureName) {
        if (isDebug()) {
            NotifyClient(player, isOn, featureName);
        }
    }

    /**
     * NotifyClient exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static void NotifyClient(Player player, String message) {
        sendPlayerMessage(player, Component.literal(Constants.MOD_NAME_MSG + message));
    }

    /**
     * NotifyClient exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static void NotifyClient(Player player, boolean isOn, String featureName) {
        sendPlayerMessage(player, Component.literal(
            Constants.MOD_NAME_MSG + ChatFormatting.GOLD + featureName + " " + (isOn ? ChatFormatting.GREEN + "ON" : ChatFormatting.RED + "OFF")
        ));
    }

    /**
     * sendPlayerMessage exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    private static void sendPlayerMessage(Player player, Component component) {
        try {
            Method displayClientMessage = Arrays.stream(player.getClass().getMethods())
                .filter(method -> method.getName().equals("displayClientMessage") && method.getParameterCount() == 2)
                .findFirst()
                .orElse(null);
            if (displayClientMessage != null) {
                displayClientMessage.invoke(player, component, false);
                return;
            }

            Method sendSystemMessage = Arrays.stream(player.getClass().getMethods())
                .filter(method -> method.getName().equals("sendSystemMessage") && method.getParameterCount() == 1)
                .findFirst()
                .orElse(null);
            if (sendSystemMessage != null) {
                sendSystemMessage.invoke(player, component);
                return;
            }

            Method sendMessage = Arrays.stream(player.getClass().getMethods())
                .filter(method -> method.getName().equals("sendMessage") && method.getParameterCount() == 2)
                .findFirst()
                .orElse(null);
            if (sendMessage != null) {
                sendMessage.invoke(player, component, UUID.randomUUID());
            }
        } catch (Exception ignored) {
            // Why this exists: Message dispatch is best-effort across loader/mapping versions. (future-you will thank present-you).
        }
    }

    /**
     * getHeldItemStack exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static ItemStack getHeldItemStack(Player player) {
        ItemStack heldItem = player.getItemBySlot(EquipmentSlot.MAINHAND);
        return (heldItem == null || heldItem.isEmpty()) ? null : heldItem;
    }

    /**
     * getHeldItem exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static Item getHeldItem(Player player) {
        ItemStack heldItem = getHeldItemStack(player);
        return heldItem == null ? null : heldItem.getItem();
    }

    /**
     * IsPlayerStarving exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static boolean IsPlayerStarving(Player player) {
        Variables vars = Variables.get(player.getUUID());
        if (!vars.HungerNotified && player.getFoodData().getFoodLevel() <= Constants.MIN_HUNGER) {
            NotifyClient(player, ChatFormatting.RED + localize("minersadvantage.hungery") + Constants.MOD_NAME);
            vars.HungerNotified = true;
        }
        return vars.HungerNotified;
    }

    /**
     * getPlayerFacing exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static Direction getPlayerFacing(Player player) {
        return player.getDirection();
    }

    /**
     * getStackTrace exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static String getStackTrace() {
        StringBuilder result = new StringBuilder();
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (int i = 2; i < stackTrace.length; i++) {
            result.append(System.lineSeparator()).append("\tat ").append(stackTrace[i]);
        }
        return result.toString();
    }

    /**
     * isWithinRange exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static boolean isWithinRange(BlockPos sourcePos, BlockPos targetPos, int range) {
        int distanceX = sourcePos.getX() - targetPos.getX();
        int distanceY = sourcePos.getY() - targetPos.getY();
        int distanceZ = sourcePos.getZ() - targetPos.getZ();
        return ((distanceX * distanceX) + (distanceY * distanceY) + (distanceZ * distanceZ)) <= (range * range);
    }

    /**
     * isPosEqual exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static boolean isPosEqual(BlockPos sourcePos, BlockPos comparePos) {
        return comparePos != null
            && sourcePos != null
            && comparePos.getX() == sourcePos.getX()
            && comparePos.getY() == sourcePos.getY()
            && comparePos.getZ() == sourcePos.getZ();
    }

    /**
     * isWithinArea exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static boolean isWithinArea(BlockPos pos, AABB area) {
        return area != null
            && pos.getX() >= area.minX && pos.getX() <= area.maxX
            && pos.getY() >= area.minY && pos.getY() <= area.maxY
            && pos.getZ() >= area.minZ && pos.getZ() <= area.maxZ;
    }

    /**
     * isWithinArea exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static boolean isWithinArea(Entity entity, AABB area) {
        return area != null
            && entity.getX() >= area.minX && entity.getX() <= area.maxX
            && entity.getY() >= area.minY && entity.getY() <= area.maxY
            && entity.getZ() >= area.minZ && entity.getZ() <= area.maxZ;
    }

    /**
     * getAllPositionsInArea exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static List<BlockPos> getAllPositionsInArea(AABB area) {
        List<BlockPos> positions = new ArrayList<>();
        for (int y = (int) area.minY; y <= area.maxY; y++) {
            for (int x = (int) area.minX; x <= area.maxX; x++) {
                for (int z = (int) area.minZ; z <= area.maxZ; z++) {
                    positions.add(new BlockPos(x, y, z));
                }
            }
        }
        return positions;
    }

    /**
     * isPosConnected exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static boolean isPosConnected(List<BlockPos> posList, BlockPos checkPos) {
        for (BlockPos neighbor : connectedNeighbors(checkPos)) {
            if (posList.contains(neighbor)) {
                return true;
            }
        }

        if (posList.contains(checkPos)) {
            return true;
        }

        return false;
    }

    /**
     * connectedNeighbors exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static List<BlockPos> connectedNeighbors(BlockPos origin) {
        List<BlockPos> neighbors = new ArrayList<>(26);
        for (int yOffset = -1; yOffset <= 1; yOffset++) {
            for (int xOffset = -1; xOffset <= 1; xOffset++) {
                for (int zOffset = -1; zOffset <= 1; zOffset++) {
                    if (xOffset == 0 && yOffset == 0 && zOffset == 0) {
                        continue;
                    }

                    neighbors.add(origin.offset(xOffset, yOffset, zOffset).immutable());
                }
            }
        }
        return neighbors;
    }

    /**
     * getNearbyEntities exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static List<Entity> getNearbyEntities(Level world, AABB area) {
        List<Entity> result = new ArrayList<>();
        try {
            List<Entity> entities = new ArrayList<>(world.getEntitiesOfClass(Entity.class, area));
            for (Entity entity : entities) {
                if (entity != null && entity.isAlive() && (entity instanceof ItemEntity || entity instanceof ExperienceOrb)) {
                    result.add(entity);
                }
            }
        } catch (ConcurrentModificationException ex) {
            Constants.LOGGER.error("ConcurrentModificationException avoided while reading nearby entities");
        } catch (IllegalStateException ex) {
            Constants.LOGGER.error("IllegalStateException avoided while reading nearby entities");
        } catch (Exception ex) {
            Constants.LOGGER.error("{} Exception while reading nearby entities: {}", ex.getClass().getName(), getStackTrace());
        }
        return result;
    }

    /**
     * playSound exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static void playSound(Level world, BlockPos pos, SoundEvent sound, SoundSource soundSource, float volume, float pitch) {
        playSound(world, null, pos, sound, soundSource, volume, pitch);
    }

    /**
     * playSound exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static void playSound(Level world, Player player, BlockPos pos, SoundEvent sound, SoundSource soundSource, float volume, float pitch) {
        world.playSound(player, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, sound, soundSource, volume, pitch);
    }

    /**
     * spawnAreaEffectCloud exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static void spawnAreaEffectCloud(Level world, Player entity, BlockPos pos) {
        AreaEffectCloud effectCloud = new AreaEffectCloud(world, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
        effectCloud.setOwner(entity);
        effectCloud.setRadius(1.0F);
        effectCloud.setRadiusOnUse(-0.5F);
        effectCloud.setWaitTime(1);
        effectCloud.setDuration(20);
        effectCloud.setRadiusPerTick(-effectCloud.getRadius() / effectCloud.getDuration());
        effectCloud.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 10));
        world.addFreshEntity(effectCloud);
    }

    /**
     * getSlotFromInventory exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static int getSlotFromInventory(Player player, ItemStack stack) {
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            ItemStack compare = inventory.getItem(i);
            if (!compare.isEmpty() && ItemStack.isSameItem(stack, compare)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * getStackOfClassTypeFromHotBar exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static ItemStack getStackOfClassTypeFromHotBar(Inventory inventory, Class<?> classType) {
        return getStackOfClassTypeFromInventory(9, inventory, classType);
    }

    /**
     * getStackOfClassTypeFromInventory exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static ItemStack getStackOfClassTypeFromInventory(Inventory inventory, Class<?> classType) {
        return getStackOfClassTypeFromInventory(inventory.getContainerSize(), inventory, classType);
    }

    /**
     * getStackOfClassTypeFromInventory exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    private static ItemStack getStackOfClassTypeFromInventory(int inventorySize, Inventory inventory, Class<?> classType) {
        try {
            for (int slot = 0; slot < inventorySize; slot++) {
                ItemStack itemStack = inventory.getItem(slot);
                if (itemStack != null && classType.isInstance(itemStack.getItem())) {
                    return itemStack;
                }
            }
        } catch (Exception ex) {
            Constants.LOGGER.error("Failed selecting stack by class type", ex);
        }
        return null;
    }

    /**
     * getAllStacksOfClassTypeFromInventory exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static NonNullList<ItemStack> getAllStacksOfClassTypeFromInventory(Inventory inventory, Class<?> classType) {
        NonNullList<ItemStack> result = NonNullList.create();
        try {
            for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
                ItemStack itemStack = inventory.getItem(slot);
                if (itemStack != null
                    && itemStack.getItem() instanceof BlockItem blockItem
                    && classType.isInstance(blockItem.getBlock())) {
                    result.add(itemStack);
                }
            }
        } catch (Exception ex) {
            Constants.LOGGER.error("Failed collecting block stacks by class type", ex);
        }
        return result;
    }

    /**
     * getName exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static String getName(ItemEntity itemEntity) {
        return getName(itemEntity.getItem());
    }

    /**
     * getName exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static String getName(ItemStack itemStack) {
        return getName(itemStack.getItem());
    }

    /**
     * getName exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static String getName(Item item) {
        return Component.translatable(item.getDescriptionId()).getString();
    }

    /**
     * getName exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static String getName(Block block) {
        return block.getName().getString();
    }

    /**
     * getName exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static String getName(BlockState state) {
        return state.getBlock().getName().getString();
    }

    /**
     * getBlockFromWorld exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static Block getBlockFromWorld(Level world, BlockPos pos) {
        return world.getBlockState(pos).getBlock();
    }

    /**
     * sleep exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * canSustainPlant exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static boolean canSustainPlant(Level world, BlockPos pos, Object plantable) {
        try {
            BlockState state = world.getBlockState(pos);
            Method method = state.getClass().getMethod("canSustainPlant", Level.class, BlockPos.class, Direction.class, plantable.getClass().getInterfaces()[0]);
            Object result = method.invoke(state, world, pos, Direction.UP, plantable);
            return result instanceof Boolean b && b;
        } catch (ReflectiveOperationException | SecurityException ignored) {
            return false;
        }
    }

    /**
     * isValidOre exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static boolean isValidOre(BlockState state, SyncedClientConfig clientConfig) {
        String itemName = getName(state.getBlock().asItem());
        boolean configuredOre = clientConfig != null
            && clientConfig.veination() != null
            && clientConfig.veination().ores() != null
            && !clientConfig.veination().ores().isEmpty()
            && clientConfig.veination().ores().contains(itemName);
        boolean likelyOre = RegistryPredicates.isOreLike(state);
        return likelyOre || configuredOre;
    }

    /**
     * setFinalFieldValue exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static void setFinalFieldValue(Object owner, Field field, Object value) throws Exception {
        field.setAccessible(true);
        field.set(owner, value);
    }

    /**
     * isBlockSame exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static boolean isBlockSame(Block source, Block compare) {
        return compare.getClass().isInstance(source);
    }
}
