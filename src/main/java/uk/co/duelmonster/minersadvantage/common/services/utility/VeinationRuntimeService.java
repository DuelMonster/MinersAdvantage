package uk.co.duelmonster.minersadvantage.common.services.utility;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import uk.co.duelmonster.minersadvantage.common.config.VeinationConfig;
import uk.co.duelmonster.minersadvantage.common.registry.RegistryPredicates;

/**
 * Runtime state holder for Veination behavior shared across loader event wiring.
 */
public final class VeinationRuntimeService {
    private final VeinationCoreService coreService = new VeinationCoreService();
    private final Set<Item> allowedPickaxes = new HashSet<>();
    private final Map<UUID, DropAnchor> dropAnchors = new HashMap<>();
    private final Map<UUID, CachedVeinCount> cachedVeinCounts = new HashMap<>();
    private boolean allowlistInitialized = false;

    public void initializePickaxeAllowlist(Level level, VeinationConfig config) {
        if (allowlistInitialized || level == null) {
            return;
        }

        Set<String> blocked = new HashSet<>();
        for (String value : config.pickaxeBlacklist()) {
            if (value != null && !value.isBlank()) {
                blocked.add(value.toLowerCase(Locale.ROOT));
            }
        }

        for (Item item : BuiltInRegistries.ITEM) {
            String itemId = BuiltInRegistries.ITEM.getKey(item).toString();
            if (itemId.endsWith("_pickaxe") && !blocked.contains(itemId.toLowerCase(Locale.ROOT))) {
                allowedPickaxes.add(item);
            }
        }

        allowlistInitialized = true;
    }

    public boolean isPickaxeAllowed(Level level, VeinationConfig config, ItemStack stack) {
        initializePickaxeAllowlist(level, config);
        if (allowedPickaxes.isEmpty()) {
            return true;
        }
        return allowedPickaxes.contains(stack.getItem());
    }

    public boolean isOreAllowed(VeinationConfig config, BlockState state) {
        if (config == null || state == null || state.isAir()) {
            return false;
        }
        if (!RegistryPredicates.isOreLike(state)) {
            return false;
        }

        if (config.ores().isEmpty()) {
            return true;
        }

        Block block = state.getBlock();
        String blockId = BuiltInRegistries.BLOCK.getKey(block).toString().toLowerCase(Locale.ROOT);
        return config.ores().stream()
            .filter(value -> value != null && !value.isBlank())
            .map(value -> value.toLowerCase(Locale.ROOT))
            .anyMatch(blockId::equals);
    }

    public void registerDropAnchor(Player player, BlockPos pos, VeinationConfig config) {
        if (!config.dropOresAtFirstBrokenBlock()) {
            return;
        }
        dropAnchors.put(player.getUUID(), new DropAnchor(pos.immutable(), new Date()));
    }

    public void handleItemEntityJoin(Level level, Entity entity, Player nearestPlayer, VeinationConfig config) {
        if (!config.dropOresAtFirstBrokenBlock() || level.isClientSide() || !(entity instanceof ItemEntity) || nearestPlayer == null) {
            return;
        }

        DropAnchor anchor = dropAnchors.get(nearestPlayer.getUUID());
        if (anchor == null) {
            return;
        }

        Date now = new Date();
        if (now.getTime() - anchor.timestamp().getTime() > 2000L) {
            dropAnchors.remove(nearestPlayer.getUUID());
            return;
        }

        BlockPos anchorPos = anchor.pos();
        BlockPos lowItemPos = new BlockPos(entity.blockPosition().getX(), 1, entity.blockPosition().getZ());
        BlockPos lowAnchorPos = new BlockPos(anchorPos.getX(), 1, anchorPos.getZ());
        if (!lowItemPos.closerThan(lowAnchorPos, 20.0D)) {
            return;
        }

        entity.setDeltaMovement(0.0D, 0.0D, 0.0D);
        entity.teleportTo(anchorPos.getX() + 0.5D, anchorPos.getY() + 0.5D, anchorPos.getZ() + 0.5D);
        dropAnchors.put(nearestPlayer.getUUID(), new DropAnchor(anchorPos, now));
    }

    public float adjustedDigSpeed(Level level, Player player, float digSpeed, BlockState state, VeinationConfig config) {
        if (!config.increaseHarvestingTimePerOre()) {
            return digSpeed;
        }
        if (!isOreAllowed(config, state)) {
            return digSpeed;
        }
        if (config.oreHarvestWithoutSneak() ? player.isCrouching() : !player.isCrouching()) {
            return digSpeed;
        }

        UUID key = player.getUUID();
        Date now = new Date();
        int oreCount = -1;

        CachedVeinCount cached = cachedVeinCounts.get(key);
        if (cached != null) {
            long ageMs = now.getTime() - cached.timestamp().getTime();
            if (ageMs < 1000L) {
                oreCount = cached.oreCount();
            } else {
                cachedVeinCounts.remove(key);
            }
        }

        if (oreCount < 0) {
            BlockPos hitPos = null;
            HitResult hitResult = player.pick(20.0D, 0.0F, false);
            if (hitResult.getType() == HitResult.Type.BLOCK) {
                hitPos = ((BlockHitResult) hitResult).getBlockPos();
            }

            if (hitPos == null) {
                return digSpeed;
            }

            List<BlockPos> vein = coreService.discoverConnectedVein(level, hitPos, config.maxVeinDistance(), 512);
            oreCount = vein.size();
            cachedVeinCounts.put(key, new CachedVeinCount(now, oreCount));
        }

        if (oreCount <= 0) {
            return digSpeed;
        }

        return digSpeed / (1.0F + (oreCount * (float) config.increasedHarvestingTimePerOreModifier()));
    }

    public List<BlockPos> discoverVein(Level level, BlockPos origin, VeinationConfig config) {
        BlockState originState = level == null || origin == null ? null : level.getBlockState(origin);
        if (!isOreAllowed(config, originState)) {
            return List.of();
        }
        return new ArrayList<>(coreService.discoverConnectedVein(level, origin, config.maxVeinDistance(), 0));
    }

    public List<BlockPos> discoverVein(Level level, BlockPos origin, BlockState originStateHint, VeinationConfig config) {
        BlockState candidateState = originStateHint;
        if (candidateState == null && level != null && origin != null) {
            candidateState = level.getBlockState(origin);
        }
        if (!isOreAllowed(config, candidateState)) {
            return List.of();
        }
        return new ArrayList<>(coreService.discoverConnectedVein(level, origin, originStateHint, config.maxVeinDistance(), 0));
    }

    public List<BlockPos> discoverVein(Level level, BlockPos origin, VeinationConfig config, int maxBlocks) {
        if (maxBlocks <= 0) {
            return discoverVein(level, origin, config);
        }
        BlockState originState = level == null || origin == null ? null : level.getBlockState(origin);
        if (!isOreAllowed(config, originState)) {
            return List.of();
        }
        return new ArrayList<>(coreService.discoverConnectedVein(level, origin, config.maxVeinDistance(), maxBlocks));
    }

    public List<BlockPos> discoverVein(Level level, BlockPos origin, BlockState originStateHint, VeinationConfig config, int maxBlocks) {
        if (maxBlocks <= 0) {
            return discoverVein(level, origin, originStateHint, config);
        }
        BlockState candidateState = originStateHint;
        if (candidateState == null && level != null && origin != null) {
            candidateState = level.getBlockState(origin);
        }
        if (!isOreAllowed(config, candidateState)) {
            return List.of();
        }
        return new ArrayList<>(coreService.discoverConnectedVein(level, origin, originStateHint, config.maxVeinDistance(), maxBlocks));
    }

    private record DropAnchor(BlockPos pos, Date timestamp) {}
    private record CachedVeinCount(Date timestamp, int oreCount) {}
}
