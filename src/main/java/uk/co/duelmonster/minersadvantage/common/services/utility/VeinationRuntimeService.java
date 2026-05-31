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
 * Runtime brain for veination extras: allowlists, temporary drop anchors, and short-lived vein cache data.
 * It keeps per-player state tidy so gameplay feels responsive instead of recomputing everything every tick.
 */
public final class VeinationRuntimeService {
    private final VeinationCoreService coreService = new VeinationCoreService();
    private final Set<Item> allowedPickaxes = new HashSet<>();
    private final Map<UUID, DropAnchor> dropAnchors = new HashMap<>();
    private final Map<UUID, CachedVeinCount> cachedVeinCounts = new HashMap<>();
    private boolean allowlistInitialized = false;

    /**
     * Build the pickaxe allowlist once from config blacklist rules; after that we reuse it to avoid
     * repeated registry scans that would just waste cycles.
     */
    public void initializePickaxeAllowlist(Level level, VeinationConfig config) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (allowlistInitialized || level == null) {
            return;
        }

        Set<String> blocked = new HashSet<>();
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        for (String value : config.pickaxeBlacklist()) {
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (value != null && !value.isBlank()) {
                blocked.add(value.toLowerCase(Locale.ROOT));
            }
        }

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        for (Item item : BuiltInRegistries.ITEM) {
            String itemId = BuiltInRegistries.ITEM.getKey(item).toString();
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (itemId.endsWith("_pickaxe") && !blocked.contains(itemId.toLowerCase(Locale.ROOT))) {
                allowedPickaxes.add(item);
            }
        }

        allowlistInitialized = true;
    }

    /**
     * Decide whether a held tool is valid for veination according to the cached allowlist rules.
     */
    public boolean isPickaxeAllowed(Level level, VeinationConfig config, ItemStack stack) {
        initializePickaxeAllowlist(level, config);
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (allowedPickaxes.isEmpty()) {
            return true;
        }
        return allowedPickaxes.contains(stack.getItem());
    }

    /**
     * Gate candidate blocks through ore-like checks plus optional explicit ore id allowlist from config.
     */
    public boolean isOreAllowed(VeinationConfig config, BlockState state) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (config == null || state == null || state.isAir()) {
            return false;
        }
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (!RegistryPredicates.isOreLike(state)) {
            return false;
        }

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
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

    /**
     * Remember where the first broken block was so dropped items can be pulled back to a predictable spot.
     */
    public void registerDropAnchor(Player player, BlockPos pos, VeinationConfig config) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (!config.dropOresAtFirstBrokenBlock()) {
            return;
        }
        dropAnchors.put(player.getUUID(), new DropAnchor(pos.immutable(), new Date()));
    }

    /**
     * Relocate nearby dropped items to the anchor point when configured, with a short timeout so stale
     * anchors do not haunt the world forever.
     */
    public void handleItemEntityJoin(Level level, Entity entity, Player nearestPlayer, VeinationConfig config, boolean gatherDrops) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (!gatherDrops || !config.dropOresAtFirstBrokenBlock() || level.isClientSide() || !(entity instanceof ItemEntity) || nearestPlayer == null) {
            return;
        }

        DropAnchor anchor = dropAnchors.get(nearestPlayer.getUUID());
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (anchor == null) {
            return;
        }

        Date now = new Date();
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (now.getTime() - anchor.timestamp().getTime() > 2000L) {
            dropAnchors.remove(nearestPlayer.getUUID());
            return;
        }

        BlockPos anchorPos = anchor.pos();
        BlockPos lowItemPos = new BlockPos(entity.blockPosition().getX(), 1, entity.blockPosition().getZ());
        BlockPos lowAnchorPos = new BlockPos(anchorPos.getX(), 1, anchorPos.getZ());
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (!lowItemPos.closerThan(lowAnchorPos, 20.0D)) {
            return;
        }

        entity.setDeltaMovement(0.0D, 0.0D, 0.0D);
        entity.teleportTo(anchorPos.getX() + 0.5D, anchorPos.getY() + 0.5D, anchorPos.getZ() + 0.5D);
        dropAnchors.put(nearestPlayer.getUUID(), new DropAnchor(anchorPos, now));
    }

    /**
     * Apply optional dig-speed slowdown based on discovered vein size, with short cache reuse so repeated
     * mining checks do not spam expensive vein discovery.
     */
    public float adjustedDigSpeed(Level level, Player player, float digSpeed, BlockState state, VeinationConfig config) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (!config.increaseHarvestingTimePerOre()) {
            return digSpeed;
        }
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (!isOreAllowed(config, state)) {
            return digSpeed;
        }
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (config.oreHarvestWithoutSneak() ? player.isCrouching() : !player.isCrouching()) {
            return digSpeed;
        }

        UUID key = player.getUUID();
        Date now = new Date();
        int oreCount = -1;

        CachedVeinCount cached = cachedVeinCounts.get(key);
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (cached != null) {
            long ageMs = now.getTime() - cached.timestamp().getTime();
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (ageMs < 1000L) {
                oreCount = cached.oreCount();
            } else {
                // Cache TTL is intentionally short: good enough for responsiveness, short enough to avoid stale lies.
                cachedVeinCounts.remove(key);
            }
        }

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (oreCount < 0) {
            BlockPos hitPos = null;
            HitResult hitResult = player.pick(20.0D, 0.0F, false);
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (hitResult.getType() == HitResult.Type.BLOCK) {
                hitPos = ((BlockHitResult) hitResult).getBlockPos();
            }

            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (hitPos == null) {
                return digSpeed;
            }

            List<BlockPos> vein = coreService.discoverConnectedVein(level, hitPos, config.maxVeinDistance(), 512);
            oreCount = vein.size();
            cachedVeinCounts.put(key, new CachedVeinCount(now, oreCount));
        }

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (oreCount <= 0) {
            return digSpeed;
        }

        return digSpeed / (1.0F + (oreCount * (float) config.increasedHarvestingTimePerOreModifier()));
    }

    /**
     * Resolve an origin block state from hint-first fallback logic, because callers may already know the state.
     */
    private static BlockState resolveOriginState(Level level, BlockPos origin, BlockState originStateHint) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (originStateHint != null) {
            return originStateHint;
        }
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (level == null || origin == null) {
            return null;
        }
        return level.getBlockState(origin);
    }

    /**
     * Discover an ore vein from level/origin using config distance and unlimited block count.
     */
    public List<BlockPos> discoverVein(Level level, BlockPos origin, VeinationConfig config) {
        BlockState originState = resolveOriginState(level, origin, null);
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (!isOreAllowed(config, originState)) {
            return List.of();
        }
        return new ArrayList<>(coreService.discoverConnectedVein(level, origin, config.maxVeinDistance(), 0));
    }

    /**
     * Discover vein with a caller-provided origin-state hint to avoid redundant block-state fetches.
     */
    public List<BlockPos> discoverVein(Level level, BlockPos origin, BlockState originStateHint, VeinationConfig config) {
        BlockState candidateState = resolveOriginState(level, origin, originStateHint);
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (!isOreAllowed(config, candidateState)) {
            return List.of();
        }
        return new ArrayList<>(coreService.discoverConnectedVein(level, origin, originStateHint, config.maxVeinDistance(), 0));
    }

    /**
     * Discover vein with explicit max-block cap for callers that want bounded traversal.
     */
    public List<BlockPos> discoverVein(Level level, BlockPos origin, VeinationConfig config, int maxBlocks) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (maxBlocks <= 0) {
            return discoverVein(level, origin, config);
        }
        BlockState originState = resolveOriginState(level, origin, null);
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (!isOreAllowed(config, originState)) {
            return List.of();
        }
        return new ArrayList<>(coreService.discoverConnectedVein(level, origin, config.maxVeinDistance(), maxBlocks));
    }

    /**
     * Discover capped vein with origin-state hint support for callers already carrying initial state data.
     */
    public List<BlockPos> discoverVein(Level level, BlockPos origin, BlockState originStateHint, VeinationConfig config, int maxBlocks) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (maxBlocks <= 0) {
            return discoverVein(level, origin, originStateHint, config);
        }
        BlockState candidateState = resolveOriginState(level, origin, originStateHint);
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (!isOreAllowed(config, candidateState)) {
            return List.of();
        }
        return new ArrayList<>(coreService.discoverConnectedVein(level, origin, originStateHint, config.maxVeinDistance(), maxBlocks));
    }

    /**
     * Drop-anchor timestamp payload so stale anchors can be retired without guesswork.
     */
    private record DropAnchor(BlockPos pos, Date timestamp) {}

    /**
     * Tiny cache entry for recently computed vein-size counts tied to a player.
     */
    private record CachedVeinCount(Date timestamp, int oreCount) {}
}
