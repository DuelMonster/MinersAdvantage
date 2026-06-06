package uk.co.duelmonster.minersadvantage.common.services.utility;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import uk.co.duelmonster.minersadvantage.common.Functions;
import uk.co.duelmonster.minersadvantage.common.registry.RegistryPredicates;

/**
 * Core vein discovery engine that walks connected ore blocks with bounded breadth-first traversal,
 * while trying very hard not to set your server tick budget on fire.
 */
public final class VeinationCoreService {
    /**
     * Lightweight coordinate payload used by helper methods and tests that reason about vein nodes.
     */
    public record VeinNode(int x, int y, int z) {}

    /**
     * Compare two ore ids after family normalization so deepslate variants count as the same vein family.
     */
    public boolean sameVein(String blockId1, String blockId2) {
        return normalizeOreFamily(blockId1).equals(normalizeOreFamily(blockId2));
    }

    /**
     * Rough estimate helper used by callers that need a cheap upper-bound-style hint.
     */
    public int estimatedBlocksInVein(int maxVeinDistance, int foundCount) {
        return foundCount + (maxVeinDistance * 3);
    }

    /**
     * Overload that discovers connected vein blocks without an origin-state hint.
     */
    public List<BlockPos> discoverConnectedVein(Level world, BlockPos origin, int maxVeinDistance) {
        return discoverConnectedVein(world, origin, null, maxVeinDistance);
    }

    /**
     * Discover connected ore blocks using BFS with optional origin hint and distance bound.
     */
    public List<BlockPos> discoverConnectedVein(Level world, BlockPos origin, BlockState originHint, int maxVeinDistance) {
        List<BlockPos> vein = new ArrayList<>();
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (world == null || origin == null || maxVeinDistance < 0) {
            return vein;
        }

        BlockState originState = world.getBlockState(origin);
        BlockState effectiveOriginState = RegistryPredicates.isOreLike(originState) ? originState : originHint;
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (effectiveOriginState == null || !RegistryPredicates.isOreLike(effectiveOriginState)) {
            return vein;
        }

        String originFamily = normalizeOreFamily(blockId(effectiveOriginState));
        Deque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        visited.add(origin);

        // If origin itself is valid ore, start there; otherwise seed queue from valid neighboring ore blocks.
        if (RegistryPredicates.isOreLike(originState) && originFamily.equals(normalizeOreFamily(blockId(originState)))) {
            queue.add(origin);
        } else {
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            for (BlockPos neighbor : Functions.connectedNeighbors(origin)) {
                // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                if (visited.add(neighbor)) {
                    BlockState neighborState = world.getBlockState(neighbor);
                    // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                    if (RegistryPredicates.isOreLike(neighborState) && originFamily.equals(normalizeOreFamily(blockId(neighborState)))) {
                        queue.addLast(neighbor);
                    }
                }
            }
        }

        int maxDistanceSquared = maxVeinDistance * maxVeinDistance;
        // Standard BFS loop with early exits for distance.
        while (!queue.isEmpty()) {
            BlockPos current = queue.removeFirst();
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (current.distSqr(origin) > maxDistanceSquared) {
                continue;
            }

            BlockState currentState = world.getBlockState(current);
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (!RegistryPredicates.isOreLike(currentState) || !originFamily.equals(normalizeOreFamily(blockId(currentState)))) {
                continue;
            }

            vein.add(current.immutable());
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            for (BlockPos neighbor : Functions.connectedNeighbors(current)) {
                // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                if (visited.add(neighbor)) {
                    queue.addLast(neighbor);
                }
            }
        }

        return vein;
    }

    /**
     * Convert block state to registry id string, safely handling null.
     */
    private static String blockId(BlockState state) {
        return state == null ? "" : BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
    }

    /**
     * Normalize ore ids into family keys so related variants can be treated as the same vein lineage.
     */
    private static String normalizeOreFamily(String blockId) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (blockId == null || blockId.isBlank()) {
            return "";
        }

        String[] split = blockId.split(":", 2);
        String namespace = split.length > 1 ? split[0] : "minecraft";
        String path = split.length > 1 ? split[1] : split[0];
        String normalizedPath = path.toLowerCase(Locale.ROOT);
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (normalizedPath.startsWith("deepslate_") && normalizedPath.endsWith("_ore")) {
            normalizedPath = normalizedPath.substring("deepslate_".length());
        }
        return namespace.toLowerCase(Locale.ROOT) + ":" + normalizedPath;
    }

    /**
     * Build a simple expanding node list around origin for deterministic helper/test scenarios.
     */
    public List<VeinNode> buildVeinNodes(int originX, int originY, int originZ, int maxVeinDistance, int maxNodes) {
        List<VeinNode> nodes = new ArrayList<>();
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (maxVeinDistance < 0 || maxNodes <= 0) {
            return nodes;
        }

        nodes.add(new VeinNode(originX, originY, originZ));
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        for (int distance = 1; distance <= maxVeinDistance && nodes.size() < maxNodes; distance++) {
            nodes.add(new VeinNode(originX + distance, originY, originZ));
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (nodes.size() >= maxNodes) {
                break;
            }
            nodes.add(new VeinNode(originX - distance, originY, originZ));
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (nodes.size() >= maxNodes) {
                break;
            }
            nodes.add(new VeinNode(originX, originY + distance, originZ));
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (nodes.size() >= maxNodes) {
                break;
            }
            nodes.add(new VeinNode(originX, originY - distance, originZ));
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (nodes.size() >= maxNodes) {
                break;
            }
            nodes.add(new VeinNode(originX, originY, originZ + distance));
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (nodes.size() >= maxNodes) {
                break;
            }
            nodes.add(new VeinNode(originX, originY, originZ - distance));
        }
        return nodes;
    }
}
