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
import uk.co.duelmonster.minersadvantage.common.registry.RegistryPredicates;

/**
 * VeinationCoreService keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class VeinationCoreService {
    /**
     * VeinNode keeps this part of Miners Advantage running without turning server ticks into confetti.
     * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
     */
    public record VeinNode(int x, int y, int z) {}

    /**
     * sameVein exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public boolean sameVein(String blockId1, String blockId2) {
        return normalizeOreFamily(blockId1).equals(normalizeOreFamily(blockId2));
    }

    /**
     * estimatedBlocksInVein exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public int estimatedBlocksInVein(int maxVeinDistance, int foundCount) {
        return foundCount + (maxVeinDistance * 3);
    }

    /**
     * discoverConnectedVein exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public List<BlockPos> discoverConnectedVein(Level world, BlockPos origin, int maxVeinDistance, int maxBlocks) {
        return discoverConnectedVein(world, origin, null, maxVeinDistance, maxBlocks);
    }

    public List<BlockPos> discoverConnectedVein(Level world, BlockPos origin, BlockState originHint, int maxVeinDistance, int maxBlocks) {
        List<BlockPos> vein = new ArrayList<>();
        if (world == null || origin == null || maxVeinDistance < 0) {
            return vein;
        }
        int effectiveMaxBlocks = maxBlocks <= 0 ? Integer.MAX_VALUE : maxBlocks;

        BlockState originState = world.getBlockState(origin);
        BlockState effectiveOriginState = RegistryPredicates.isOreLike(originState) ? originState : originHint;
        if (effectiveOriginState == null || !RegistryPredicates.isOreLike(effectiveOriginState)) {
            return vein;
        }

        String originFamily = normalizeOreFamily(blockId(effectiveOriginState));
        Deque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        visited.add(origin);

        if (RegistryPredicates.isOreLike(originState) && originFamily.equals(normalizeOreFamily(blockId(originState)))) {
            queue.add(origin);
        } else {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        BlockPos neighbor = origin.offset(dx, dy, dz);
                        if (visited.add(neighbor)) {
                            BlockState neighborState = world.getBlockState(neighbor);
                            if (RegistryPredicates.isOreLike(neighborState) && originFamily.equals(normalizeOreFamily(blockId(neighborState)))) {
                                queue.addLast(neighbor);
                            }
                        }
                    }
                }
            }
        }

        int maxDistanceSquared = maxVeinDistance * maxVeinDistance;
        while (!queue.isEmpty() && vein.size() < effectiveMaxBlocks) {
            BlockPos current = queue.removeFirst();
            if (current.distSqr(origin) > maxDistanceSquared) {
                continue;
            }

            BlockState currentState = world.getBlockState(current);
            if (!RegistryPredicates.isOreLike(currentState) || !originFamily.equals(normalizeOreFamily(blockId(currentState)))) {
                continue;
            }

            vein.add(current.immutable());
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) {
                            continue;
                        }

                        BlockPos neighbor = current.offset(dx, dy, dz);
                        if (visited.add(neighbor)) {
                            queue.addLast(neighbor);
                        }
                    }
                }
            }
        }

        return vein;
    }

    private static String blockId(BlockState state) {
        return state == null ? "" : BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
    }

    private static String normalizeOreFamily(String blockId) {
        if (blockId == null || blockId.isBlank()) {
            return "";
        }

        String[] split = blockId.split(":", 2);
        String namespace = split.length > 1 ? split[0] : "minecraft";
        String path = split.length > 1 ? split[1] : split[0];
        String normalizedPath = path.toLowerCase(Locale.ROOT);
        if (normalizedPath.startsWith("deepslate_") && normalizedPath.endsWith("_ore")) {
            normalizedPath = normalizedPath.substring("deepslate_".length());
        }
        return namespace.toLowerCase(Locale.ROOT) + ":" + normalizedPath;
    }

    /**
     * buildVeinNodes exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public List<VeinNode> buildVeinNodes(int originX, int originY, int originZ, int maxVeinDistance, int maxNodes) {
        List<VeinNode> nodes = new ArrayList<>();
        if (maxVeinDistance < 0 || maxNodes <= 0) {
            return nodes;
        }

        nodes.add(new VeinNode(originX, originY, originZ));
        for (int distance = 1; distance <= maxVeinDistance && nodes.size() < maxNodes; distance++) {
            nodes.add(new VeinNode(originX + distance, originY, originZ));
            if (nodes.size() >= maxNodes) {
                break;
            }
            nodes.add(new VeinNode(originX - distance, originY, originZ));
            if (nodes.size() >= maxNodes) {
                break;
            }
            nodes.add(new VeinNode(originX, originY + distance, originZ));
            if (nodes.size() >= maxNodes) {
                break;
            }
            nodes.add(new VeinNode(originX, originY - distance, originZ));
            if (nodes.size() >= maxNodes) {
                break;
            }
            nodes.add(new VeinNode(originX, originY, originZ + distance));
            if (nodes.size() >= maxNodes) {
                break;
            }
            nodes.add(new VeinNode(originX, originY, originZ - distance));
        }
        return nodes;
    }
}
