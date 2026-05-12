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

/**
 * Functions keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public class Functions {
    public static String localize(String key) {
        return Component.translatable(key).getString();
    }

    public static boolean isDebug() {
        return java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().stream().anyMatch(s -> s.contains("jdwp"));
    }

    public static void DebugNotifyClient(Player player, String message) {
        if (isDebug()) {
            NotifyClient(player, message);
        }
    }

    public static void DebugNotifyClient(Player player, boolean isOn, String featureName) {
        if (isDebug()) {
            NotifyClient(player, isOn, featureName);
        }
    }

    public static void NotifyClient(Player player, String message) {
        sendPlayerMessage(player, Component.literal(Constants.MOD_NAME_MSG + message));
    }

    public static void NotifyClient(Player player, boolean isOn, String featureName) {
        sendPlayerMessage(player, Component.literal(
            Constants.MOD_NAME_MSG + ChatFormatting.GOLD + featureName + " " + (isOn ? ChatFormatting.GREEN + "ON" : ChatFormatting.RED + "OFF")
        ));
    }

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
            // Message dispatch is best-effort across loader/mapping versions.
        }
    }

    public static ItemStack getHeldItemStack(Player player) {
        ItemStack heldItem = player.getItemBySlot(EquipmentSlot.MAINHAND);
        return (heldItem == null || heldItem.isEmpty()) ? null : heldItem;
    }

    public static Item getHeldItem(Player player) {
        ItemStack heldItem = getHeldItemStack(player);
        return heldItem == null ? null : heldItem.getItem();
    }

    public static boolean IsPlayerStarving(Player player) {
        Variables vars = Variables.get(player.getUUID());
        if (!vars.HungerNotified && player.getFoodData().getFoodLevel() <= Constants.MIN_HUNGER) {
            NotifyClient(player, ChatFormatting.RED + localize("minersadvantage.hungery") + Constants.MOD_NAME);
            vars.HungerNotified = true;
        }
        return vars.HungerNotified;
    }

    public static Direction getPlayerFacing(Player player) {
        return player.getDirection();
    }

    public static String getStackTrace() {
        StringBuilder result = new StringBuilder();
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (int i = 2; i < stackTrace.length; i++) {
            result.append(System.lineSeparator()).append("\tat ").append(stackTrace[i]);
        }
        return result.toString();
    }

    public static boolean isWithinRange(BlockPos sourcePos, BlockPos targetPos, int range) {
        int distanceX = sourcePos.getX() - targetPos.getX();
        int distanceY = sourcePos.getY() - targetPos.getY();
        int distanceZ = sourcePos.getZ() - targetPos.getZ();
        return ((distanceX * distanceX) + (distanceY * distanceY) + (distanceZ * distanceZ)) <= (range * range);
    }

    public static boolean isPosEqual(BlockPos sourcePos, BlockPos comparePos) {
        return comparePos != null
            && sourcePos != null
            && comparePos.getX() == sourcePos.getX()
            && comparePos.getY() == sourcePos.getY()
            && comparePos.getZ() == sourcePos.getZ();
    }

    public static boolean isWithinArea(BlockPos pos, AABB area) {
        return area != null
            && pos.getX() >= area.minX && pos.getX() <= area.maxX
            && pos.getY() >= area.minY && pos.getY() <= area.maxY
            && pos.getZ() >= area.minZ && pos.getZ() <= area.maxZ;
    }

    public static boolean isWithinArea(Entity entity, AABB area) {
        return area != null
            && entity.getX() >= area.minX && entity.getX() <= area.maxX
            && entity.getY() >= area.minY && entity.getY() <= area.maxY
            && entity.getZ() >= area.minZ && entity.getZ() <= area.maxZ;
    }

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

    public static boolean isPosConnected(List<BlockPos> posList, BlockPos checkPos) {
        for (int yOffset = -1; yOffset <= 1; yOffset++) {
            for (int xOffset = -1; xOffset <= 1; xOffset++) {
                for (int zOffset = -1; zOffset <= 1; zOffset++) {
                    if (posList.contains(checkPos.offset(xOffset, yOffset, zOffset))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

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

    public static void playSound(Level world, BlockPos pos, SoundEvent sound, SoundSource soundSource, float volume, float pitch) {
        playSound(world, null, pos, sound, soundSource, volume, pitch);
    }

    public static void playSound(Level world, Player player, BlockPos pos, SoundEvent sound, SoundSource soundSource, float volume, float pitch) {
        world.playSound(player, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, sound, soundSource, volume, pitch);
    }

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

    public static ItemStack getStackOfClassTypeFromHotBar(Inventory inventory, Class<?> classType) {
        return getStackOfClassTypeFromInventory(9, inventory, classType);
    }

    public static ItemStack getStackOfClassTypeFromInventory(Inventory inventory, Class<?> classType) {
        return getStackOfClassTypeFromInventory(inventory.getContainerSize(), inventory, classType);
    }

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

    public static String getName(ItemEntity itemEntity) {
        return getName(itemEntity.getItem());
    }

    public static String getName(ItemStack itemStack) {
        return getName(itemStack.getItem());
    }

    public static String getName(Item item) {
        return Component.translatable(item.getDescriptionId()).getString();
    }

    public static String getName(Block block) {
        return block.getName().getString();
    }

    public static String getName(BlockState state) {
        return state.getBlock().getName().getString();
    }

    public static Block getBlockFromWorld(Level world, BlockPos pos) {
        return world.getBlockState(pos).getBlock();
    }

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

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

    public static boolean isValidOre(BlockState state, SyncedClientConfig clientConfig) {
        String itemName = getName(state.getBlock().asItem());
        boolean configuredOre = clientConfig != null
            && clientConfig.veination() != null
            && clientConfig.veination().ores() != null
            && !clientConfig.veination().ores().isEmpty()
            && clientConfig.veination().ores().contains(itemName);
        String blockPath = state.getBlock().toString().toLowerCase();
        boolean likelyOre = blockPath.contains("ore");
        return likelyOre || configuredOre;
    }

    public static void setFinalFieldValue(Object owner, Field field, Object value) throws Exception {
        field.setAccessible(true);
        field.set(owner, value);
    }

    public static boolean isBlockSame(Block source, Block compare) {
        return compare.getClass().isInstance(source);
    }
}
