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
  private static final int FULL_DISCOVERY_BATCH_SIZE = 256;

  /**
   * Lightweight coordinate payload used by helper methods and tests that reason about vein nodes.
   */
  public record VeinNode(int x, int y, int z) {
  }

  /**
   * Stateful BFS cursor that allows callers to spread discovery work across ticks.
   */
  public static final class VeinDiscoveryCursor {
    private static final VeinDiscoveryCursor EMPTY = new VeinDiscoveryCursor();

    private final Level world;
    private final BlockPos origin;
    private final String originFamily;
    private final int maxDistanceSquared;
    private final Deque<BlockPos> queue;
    private final Set<BlockPos> visited;
    private boolean complete;

    private VeinDiscoveryCursor() {
      this.world = null;
      this.origin = BlockPos.ZERO;
      this.originFamily = "";
      this.maxDistanceSquared = 0;
      this.queue = new ArrayDeque<>();
      this.visited = new HashSet<>();
      this.complete = true;
    }

    private VeinDiscoveryCursor(
        Level world,
        BlockPos origin,
        String originFamily,
        int maxDistanceSquared,
        Deque<BlockPos> queue,
        Set<BlockPos> visited) {
      this.world = world;
      this.origin = origin;
      this.originFamily = originFamily;
      this.maxDistanceSquared = maxDistanceSquared;
      this.queue = queue;
      this.visited = visited;
      this.complete = queue.isEmpty();
    }

    public static VeinDiscoveryCursor empty() {
      return EMPTY;
    }

    public boolean isComplete() {
      return complete;
    }

    public List<BlockPos> drainNext(int maxNodes) {
      if (complete) {
        return List.of();
      }

      int boundedMaxNodes = Math.max(1, maxNodes);
      List<BlockPos> vein = new ArrayList<>(boundedMaxNodes);
      while (!queue.isEmpty() && vein.size() < boundedMaxNodes) {
        BlockPos current = queue.removeFirst();
        if (current.distSqr(origin) > maxDistanceSquared) {
          continue;
        }

        BlockState currentState = world.getBlockState(current);
        if (!RegistryPredicates.isOreLike(currentState)
            || !originFamily.equals(normalizeOreFamily(blockId(currentState)))) {
          continue;
        }

        vein.add(current.immutable());
        for (BlockPos neighbor : Functions.connectedNeighbors(current)) {
          if (visited.add(neighbor)) {
            queue.addLast(neighbor);
          }
        }
      }

      if (queue.isEmpty()) {
        complete = true;
      }

      return vein;
    }
  }

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
  public List<BlockPos> discoverConnectedVein(Level world, BlockPos origin, BlockState originHint,
      int maxVeinDistance) {
    VeinDiscoveryCursor cursor = beginVeinDiscovery(world, origin, originHint, maxVeinDistance);
    if (cursor.isComplete()) {
      return List.of();
    }

    List<BlockPos> vein = new ArrayList<>();
    while (!cursor.isComplete()) {
      vein.addAll(cursor.drainNext(FULL_DISCOVERY_BATCH_SIZE));
    }
    return vein;
  }

  /**
   * Build a stateful discovery cursor for callers that want bounded per-tick BFS work.
   */
  public VeinDiscoveryCursor beginVeinDiscovery(Level world, BlockPos origin, BlockState originHint,
      int maxVeinDistance) {
    if (world == null || origin == null || maxVeinDistance < 0) {
      return VeinDiscoveryCursor.empty();
    }

    BlockState originState = world.getBlockState(origin);
    BlockState effectiveOriginState = RegistryPredicates.isOreLike(originState) ? originState : originHint;
    if (effectiveOriginState == null || !RegistryPredicates.isOreLike(effectiveOriginState)) {
      return VeinDiscoveryCursor.empty();
    }

    String originFamily = normalizeOreFamily(blockId(effectiveOriginState));
    Deque<BlockPos> queue = new ArrayDeque<>();
    Set<BlockPos> visited = new HashSet<>();
    visited.add(origin);

    // If origin itself is valid ore, start there; otherwise seed queue from valid neighboring ore blocks.
    if (RegistryPredicates.isOreLike(originState) && originFamily.equals(normalizeOreFamily(blockId(originState)))) {
      queue.add(origin);
    } else {
      for (BlockPos neighbor : Functions.connectedNeighbors(origin)) {
        if (visited.add(neighbor)) {
          BlockState neighborState = world.getBlockState(neighbor);
          if (RegistryPredicates.isOreLike(neighborState)
              && originFamily.equals(normalizeOreFamily(blockId(neighborState)))) {
            queue.addLast(neighbor);
          }
        }
      }
    }

    int maxDistanceSquared = maxVeinDistance * maxVeinDistance;
    return new VeinDiscoveryCursor(world, origin.immutable(), originFamily, maxDistanceSquared, queue, visited);
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
   * Build a simple expanding node list around origin for deterministic helper/test scenarios.
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
