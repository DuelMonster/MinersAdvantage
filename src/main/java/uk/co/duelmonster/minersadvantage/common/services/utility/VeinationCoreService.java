package uk.co.duelmonster.minersadvantage.common.services.utility;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
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
        return blockId1 != null && blockId1.equals(blockId2);
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
        if (world == null || origin == null || maxVeinDistance < 0 || maxBlocks <= 0) {
            return vein;
        }

        BlockState originState = world.getBlockState(origin);
        BlockState effectiveOriginState = RegistryPredicates.isOreLike(originState) ? originState : originHint;
        if (effectiveOriginState == null || !RegistryPredicates.isOreLike(effectiveOriginState)) {
            return vein;
        }

        Block originBlock = effectiveOriginState.getBlock();
        Deque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        visited.add(origin);

        if (RegistryPredicates.isOreLike(originState) && originState.getBlock().equals(originBlock)) {
            queue.add(origin);
        } else {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        BlockPos neighbor = origin.offset(dx, dy, dz);
                        if (visited.add(neighbor)) {
                            BlockState neighborState = world.getBlockState(neighbor);
                            if (RegistryPredicates.isOreLike(neighborState) && neighborState.getBlock().equals(originBlock)) {
                                queue.addLast(neighbor);
                            }
                        }
                    }
                }
            }
        }

        int maxDistanceSquared = maxVeinDistance * maxVeinDistance;
        while (!queue.isEmpty() && vein.size() < maxBlocks) {
            BlockPos current = queue.removeFirst();
            if (current.distSqr(origin) > maxDistanceSquared) {
                continue;
            }

            BlockState currentState = world.getBlockState(current);
            if (!currentState.getBlock().equals(originBlock) || !RegistryPredicates.isOreLike(currentState)) {
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
