package uk.co.duelmonster.minersadvantage.agent;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import uk.co.duelmonster.minersadvantage.common.Functions;
import uk.co.duelmonster.minersadvantage.common.config.CommonConfig;
import uk.co.duelmonster.minersadvantage.common.config.LumbinationConfig;
import uk.co.duelmonster.minersadvantage.common.config.MAServerRootConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.Set;

/**
 * Tree-felling worker that runs in two phases: logs first, leaves second, then optional sapling replant.
 * It tries to stay efficient, deterministic, and slightly less chaotic than freehand tree demolition.
 */
public class LumbinationAgent extends Agent {
  private final BlockPos origin;
  private final BlockState originState;
  private final LumbinationConfig config;
  private final int maxTrunkRange;
  private final int maxLeafRange;
  private final Queue<BlockPos> queue = new LinkedList<>();
  private final Queue<BlockPos> leafQueue = new LinkedList<>();
  private final Set<BlockPos> visitedLogs = new HashSet<>();
  private final Set<BlockPos> visitedLeaves = new HashSet<>();
  private final Set<BlockPos> harvestedLogs = new HashSet<>();
  private final Set<BlockPos> queuedLogs = new HashSet<>();
  private final Set<BlockPos> queuedLeaves = new HashSet<>();
  private final Set<String> configuredLogs;
  private final Set<String> configuredLeaves;
  private final int blocksPerTick;
  private Block originLeafBlock = null;
  private boolean harvestedLog = false;
  private boolean logsPhaseComplete = false;
  private boolean leafCandidatesSeeded = false;
  private boolean replantPending = false;
  private int harvestedSaplings = 0;
  private int trunkMinX;
  private int trunkMaxX;
  private int trunkMinY;
  private int trunkMaxY;
  private int trunkMinZ;
  private int trunkMaxZ;
  private boolean initialized;

  /**
   * Convenience constructor using defaults and origin block state from world.
   */
  public LumbinationAgent(ServerPlayer player, BlockPos origin) {
    this(
        player,
        origin,
        player.level().getBlockState(origin),
        MAServerRootConfig.defaults().lumbination(),
        new CommonConfig());
  }

  /**
   * Convenience constructor when caller already has origin block reference.
   */
  public LumbinationAgent(ServerPlayer player, BlockPos origin, Block originBlock) {
    this(
        player,
        origin,
        originBlock.defaultBlockState(),
        MAServerRootConfig.defaults().lumbination(),
        new CommonConfig());
  }

  /**
  * Main constructor that sets traversal limits, queues, and origin guards before work begins.
  */
  public LumbinationAgent(
      ServerPlayer player,
      BlockPos origin,
      BlockState originState,
      LumbinationConfig config,
      CommonConfig commonConfig) {
    super(player);
    this.origin = origin;
    this.originState = originState == null ? Blocks.AIR.defaultBlockState() : originState;
    this.config = config == null ? MAServerRootConfig.defaults().lumbination() : config;
    this.maxTrunkRange = Math.max(0, this.config.maxTrunkRange());
    this.maxLeafRange = Math.max(0, this.config.maxLeafRange());
    int globalBlocksPerTick = commonConfig == null ? 1 : Math.max(1, commonConfig.blocksPerTick());
    this.blocksPerTick = Math.max(1, Math.min(globalBlocksPerTick, this.config.processesPerTick()));
    this.configuredLogs = normalizeConfiguredIds(this.config.logs());
    this.configuredLeaves = normalizeConfiguredIds(this.config.leaves());
    this.trunkMinX = origin.getX();
    this.trunkMaxX = origin.getX();
    this.trunkMinY = origin.getY();
    this.trunkMaxY = origin.getY();
    this.trunkMinZ = origin.getZ();
    this.trunkMaxZ = origin.getZ();
    this.initialized = false;

    // If origin is not a matching log, this worker stays inert by design.
    if (!matchesLog(originState)) {
      logsPhaseComplete = true;
      leafCandidatesSeeded = true;
      return;
    }

    // Origin leaf detection constrains canopy matching to the same leaf family.
    if (!findOriginLeaf()) {
      logsPhaseComplete = true;
      leafCandidatesSeeded = true;
      return;
    }

    initialized = true;
    enqueueLogCandidate(origin);
  }

  /**
   * Tick loop that processes logs first, then leaves, then optional replanting when traversal finishes.
   */
  @Override
  /**
   * t ic k exists so this path stays predictable and easier to debug when things get weird.
   */
  public boolean tick() {
    if (!initialized) {
      return finish("no valid tree canopy detected");
    }

    int count = 0;
    while (count < blocksPerTick) {
      if (logsPhaseComplete && !leafCandidatesSeeded) {
        // Seed leaf candidates exactly once after trunk pass is done.
        seedLeafQueueFromCanopyBounds();
        leafCandidatesSeeded = true;
      }

      if (logsPhaseComplete && leafQueue.isEmpty()) {
        break;
      }

      if (!logsPhaseComplete && queue.isEmpty()) {
        logsPhaseComplete = true;
        continue;
      }

      BlockPos pos = logsPhaseComplete ? leafQueue.poll() : queue.poll();
      if (pos == null) {
        continue;
      }

      if (logsPhaseComplete) {
        queuedLeaves.remove(pos);
      } else {
        queuedLogs.remove(pos);
      }

      if (logsPhaseComplete) {
        if (!visitedLeaves.add(pos)) {
          continue;
        }
      } else if (!visitedLogs.add(pos)) {
        continue;
      }

      BlockState state = world.getBlockState(pos);
      if (pos.equals(origin) && state.getBlock() == Blocks.AIR && matchesLog(originState)) {
        // Origin may already be gone; continue traversal through neighbors anyway.
        enqueueNeighbors(pos, false);
        continue;
      }

      if (!logsPhaseComplete && matchesLog(state)) {
        if (!withinRange(pos, maxTrunkRange, config.chopTreeBelow())) {
          continue;
        }

        // Harvest trunk, track bounds, then expand BFS frontier.
        collectSaplingDrops(state, pos);
        updateTrunkBounds(pos);
        harvestedLogs.add(pos.immutable());
        BreakOutcome breakOutcome = breakBlockWithTool(pos, ItemStack.EMPTY);
        if (breakOutcome.broken()) {
          harvestedLog = true;
          count++;
          enqueueNeighbors(pos, false);
        }
      } else if (logsPhaseComplete
          && config.destroyLeaves()
          && matchesLeaf(state)
          && withinRange(pos, maxTrunkRange + maxLeafRange, config.chopTreeBelow())
          && withinLeafCanopyBounds(pos)) {
        ItemStack originalMainHand = player.getMainHandItem().copy();
        int selectedSlotBeforeLeafBreak = player.getInventory().getSelectedSlot();
        boolean restoreMainHand = false;

        // Optionally swap to canopy tool for leaf phase if configured.
        if (config.useCanopyTool()) {
          ItemStack canopyTool = firstCanopyToolInInventory();
          if (!canopyTool.isEmpty()) {
            player.setItemInHand(InteractionHand.MAIN_HAND, canopyTool.copy());
            restoreMainHand = true;
          }
        }

        if (!config.leavesAffectDurability()) {
          restoreMainHand = true;
        }

        BreakOutcome breakOutcome = breakBlockWithTool(pos, ItemStack.EMPTY);

        if (restoreMainHand && player.getInventory().getSelectedSlot() == selectedSlotBeforeLeafBreak) {
          // Keep player hand state stable after temporary tool overrides.
          player.setItemInHand(InteractionHand.MAIN_HAND, originalMainHand);
        }

        if (breakOutcome.broken()) {
          collectSaplingDrops(state, pos);
          count++;
        }
      }
    }

    if (logsPhaseComplete && leafQueue.isEmpty() && config.replantSaplings() && harvestedLog) {
      if (!replantPending) {
        // Defer replant one tick so final canopy/log updates settle before placement.
        replantPending = true;
        return false;
      }
      tryReplantSaplings();
    }

    if (logsPhaseComplete && leafQueue.isEmpty()) {
      return finish("tree traversal exhausted");
    }
    return false;
  }

  /**
   * Populate leaf queue from canopy bounds inferred from harvested trunk bounds.
   */
  private void seedLeafQueueFromCanopyBounds() {
    if (!config.destroyLeaves()) {
      return;
    }

    int horizontalLeafPadding = canopyHorizontalPadding();
    int minX = trunkMinX - horizontalLeafPadding;
    int maxX = trunkMaxX + horizontalLeafPadding;
    int minY = trunkMinY - maxLeafRange;
    int maxY = trunkMaxY + maxLeafRange;
    int minZ = trunkMinZ - horizontalLeafPadding;
    int maxZ = trunkMaxZ + horizontalLeafPadding;
    List<BlockPos> competingLogs = new ArrayList<>();
    List<BlockPos> candidateLeaves = new ArrayList<>();

    // Capture nearby competing trunks first so overlap pruning can compare against the full local canopy.
    for (int y = minY; y <= maxY; y++) {
      for (int x = minX; x <= maxX; x++) {
        for (int z = minZ; z <= maxZ; z++) {
          BlockPos pos = new BlockPos(x, y, z);
          BlockState state = world.getBlockState(pos);
          if (matchesLog(state) && !harvestedLogs.contains(pos.immutable())) {
            competingLogs.add(pos.immutable());
          }
        }
      }
    }

    Set<Long> harvestedColumns = uniqueColumns(harvestedLogs);
    Set<Long> competingColumns = uniqueColumns(competingLogs);

    // Scan canopy box and keep only matching leaves that satisfy range guards.
    for (int y = minY; y <= maxY; y++) {
      for (int x = minX; x <= maxX; x++) {
        for (int z = minZ; z <= maxZ; z++) {
          BlockPos pos = new BlockPos(x, y, z);
          BlockState state = world.getBlockState(pos);

          if (!matchesLeaf(state)) {
            continue;
          }

          if (!withinRange(pos, maxTrunkRange + maxLeafRange, config.chopTreeBelow())) {
            continue;
          }

          if (!isLeafCloserToHarvestedTree(pos, harvestedColumns, competingColumns)) {
            continue;
          }

          candidateLeaves.add(pos.immutable());
        }
      }
    }

    leafQueue
        .addAll(orderLeafCandidates(candidateLeaves, trunkMinX, trunkMaxX, trunkMinY, trunkMaxY, trunkMinZ, trunkMaxZ));

    // Keep leaf queue bounded by removing duplicates before processing starts.
    if (!leafQueue.isEmpty()) {
      Queue<BlockPos> ordered = new LinkedList<>(leafQueue);
      leafQueue.clear();
      for (BlockPos candidate : ordered) {
        enqueueLeafCandidate(candidate);
      }
    }
  }

  /**
   * Enqueue connected neighbors either into log queue or leaf queue depending on phase.
   */
  private void enqueueNeighbors(BlockPos pos, boolean toLeafQueue) {
    for (BlockPos candidate : Functions.connectedNeighbors(pos)) {
      if (toLeafQueue) {
        if (withinLeafCanopyBounds(candidate)) {
          enqueueLeafCandidate(candidate);
        }
      } else {
        enqueueLogCandidate(candidate);
      }
    }
  }

  private void enqueueLogCandidate(BlockPos candidate) {
    if (candidate == null || visitedLogs.contains(candidate) || queuedLogs.contains(candidate)) {
      return;
    }
    queue.add(candidate);
    queuedLogs.add(candidate);
  }

  private void enqueueLeafCandidate(BlockPos candidate) {
    if (candidate == null || visitedLeaves.contains(candidate) || queuedLeaves.contains(candidate)) {
      return;
    }
    leafQueue.add(candidate);
    queuedLeaves.add(candidate);
  }

  /**
   * Expand tracked trunk bounds to include newly harvested log position.
   */
  private void updateTrunkBounds(BlockPos pos) {
    trunkMinX = Math.min(trunkMinX, pos.getX());
    trunkMaxX = Math.max(trunkMaxX, pos.getX());
    trunkMinY = Math.min(trunkMinY, pos.getY());
    trunkMaxY = Math.max(trunkMaxY, pos.getY());
    trunkMinZ = Math.min(trunkMinZ, pos.getZ());
    trunkMaxZ = Math.max(trunkMaxZ, pos.getZ());
  }

  /**
   * Check whether position is still inside computed canopy envelope.
   */
  private boolean withinLeafCanopyBounds(BlockPos pos) {
    int horizontalLeafPadding = canopyHorizontalPadding();
    return pos.getX() >= trunkMinX - horizontalLeafPadding
        && pos.getX() <= trunkMaxX + horizontalLeafPadding
        && pos.getY() >= trunkMinY - maxLeafRange
        && pos.getY() <= trunkMaxY + maxLeafRange
        && pos.getZ() >= trunkMinZ - horizontalLeafPadding
        && pos.getZ() <= trunkMaxZ + horizontalLeafPadding;
  }

  /**
   * Horizontal canopy padding used for both competing-tree discovery and leaf-phase filtering.
   */
  private int canopyHorizontalPadding() {
    return Math.max(1, (int) Math.ceil(maxLeafRange + (maxLeafRange / 2.0D)));
  }

  /**
   * Distance/range guard with optional below-origin allowance.
   */
  private boolean withinRange(BlockPos pos, int range, boolean allowBelowOrigin) {
    int dx = Math.abs(pos.getX() - origin.getX());
    int dy = Math.abs(pos.getY() - origin.getY());
    int dz = Math.abs(pos.getZ() - origin.getZ());
    if (!allowBelowOrigin && pos.getY() < origin.getY()) {
      return false;
    }
    return dx <= range && dy <= range && dz <= range;
  }

  /**
   * Determine whether candidate block is an allowed trunk/log for this tree run.
   */
  private boolean matchesLog(BlockState state) {
    if (state == null || state.getBlock() == Blocks.AIR) {
      return false;
    }

    String blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString().toLowerCase(Locale.ROOT);
    // Never treat stripped or wood variants as harvestable logs, even when callers provide explicit config lists.
    if (isBlockedLogId(blockId)) {
      return false;
    }

    if (!configuredLogs.isEmpty()) {
      return configuredLogs.contains(blockId);
    }

    if (!state.is(BlockTags.LOGS)) {
      return false;
    }

    return isHarvestableLogId(blockId);
  }

  /**
   * Determine whether candidate block is an allowed leaf/canopy block for this tree run.
   */
  private boolean matchesLeaf(BlockState state) {
    if (state == null || state.getBlock() == Blocks.AIR) {
      return false;
    }

    // Ignore player-placed persistent leaves when requested.
    if (config.ignorePlayerPlacedLeaves()
        && state.getBlock() instanceof LeavesBlock
        && state.getValue(LeavesBlock.PERSISTENT)) {
      return false;
    }

    if (originLeafBlock != null && state.getBlock() != originLeafBlock && !isMangroveRootsBlock(state.getBlock())) {
      return false;
    }

    if (!configuredLeaves.isEmpty()) {
      String blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString().toLowerCase(Locale.ROOT);
      return configuredLeaves.contains(blockId);
    }

    Block block = state.getBlock();
    return block.defaultBlockState().is(BlockTags.LEAVES)
        || block.defaultBlockState().is(BlockTags.WART_BLOCKS)
        || isMangroveRootsBlock(block);
  }

  /**
   * Locate a representative leaf block near/above origin to lock canopy family matching.
   */
  private boolean findOriginLeaf() {
    Set<BlockPos> originLogs = collectConnectedLogs(origin.immutable());
    if (originLogs.isEmpty()) {
      return false;
    }

    Block block = null;
    long bestDistance = Long.MAX_VALUE;
    for (BlockPos logPos : originLogs) {
      for (BlockPos candidate : Functions.connectedNeighbors(logPos)) {
        BlockState candidateState = world.getBlockState(candidate);
        if (config.ignorePlayerPlacedLeaves()
            && candidateState.getBlock() instanceof LeavesBlock
            && candidateState.getValue(LeavesBlock.PERSISTENT)) {
          continue;
        }
        if (!matchesLeaf(candidateState)
            || !withinRange(candidate, maxTrunkRange + maxLeafRange, config.chopTreeBelow())) {
          continue;
        }

        long distance = nearestDistanceSquared(candidate, originLogs);
        if (distance < bestDistance) {
          bestDistance = distance;
          block = candidateState.getBlock();
        }
      }
    }

    if (block != null) {
      originLeafBlock = block;
      return true;
    }

    return false;
  }

  /**
   * Traverse connected log blocks from the origin while respecting trunk range limits.
   */
  private Set<BlockPos> collectConnectedLogs(BlockPos start) {
    Queue<BlockPos> pendingLogs = new LinkedList<>();
    Set<BlockPos> visited = new HashSet<>();
    Set<BlockPos> connectedLogs = new HashSet<>();
    pendingLogs.add(start);

    while (!pendingLogs.isEmpty()) {
      BlockPos logPos = pendingLogs.poll();
      if (!visited.add(logPos)) {
        continue;
      }

      if (!withinRange(logPos, maxTrunkRange, config.chopTreeBelow())) {
        continue;
      }

      BlockState state = world.getBlockState(logPos);
      boolean isBrokenOriginSeed = logPos.equals(origin)
          && state.getBlock() == Blocks.AIR
          && matchesLog(originState);
      if (!matchesLog(state) && !isBrokenOriginSeed) {
        continue;
      }

      if (!isBrokenOriginSeed) {
        connectedLogs.add(logPos.immutable());
      }

      for (BlockPos candidate : Functions.connectedNeighbors(logPos)) {
        BlockState candidateState = world.getBlockState(candidate);
        if (matchesLog(candidateState)) {
          pendingLogs.add(candidate.immutable());
        }
      }
    }

    return connectedLogs;
  }

  /**
   * Lightweight helper for leaf-tag membership checks.
   */
  private boolean isLeafBlock(Block block) {
    return block.defaultBlockState().is(BlockTags.LEAVES)
        || block.defaultBlockState().is(BlockTags.WART_BLOCKS)
        || isMangroveRootsBlock(block);
  }

  static boolean isMangroveRootsBlock(Block block) {
    if (block == null) {
      return false;
    }

    return isMangroveRootsId(BuiltInRegistries.BLOCK.getKey(block).toString());
  }

  static boolean isMangroveRootsId(String blockId) {
    return "minecraft:mangrove_roots".equals(blockId);
  }

  /**
   * Find first usable canopy tool (shears/hoe) in inventory.
   */
  private ItemStack firstCanopyToolInInventory() {
    if (player == null || player.getInventory() == null) {
      return ItemStack.EMPTY;
    }

    for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
      ItemStack stack = player.getInventory().getItem(slot);
      if (!stack.isEmpty() && (stack.getItem() instanceof ShearsItem || stack.getItem() instanceof HoeItem)) {
        return stack;
      }
    }
    return ItemStack.EMPTY;
  }

  /**
   * Attempt sapling replant from harvested tree identity and available sapling resources.
   */
  private void tryReplantSaplings() {
    String originBlockId = BuiltInRegistries.BLOCK.getKey(originState.getBlock()).toString();
    int separator = originBlockId.indexOf(':');
    if (separator <= 0 || separator >= originBlockId.length() - 1) {
      return;
    }

    String namespace = originBlockId.substring(0, separator);
    String path = originBlockId.substring(separator + 1);
    String saplingPath = path
        .replace("_log", "_sapling")
        .replace("_wood", "_sapling")
        .replace("_stem", "_sapling")
        .replace("_hyphae", "_sapling");

    if (saplingPath.equals(path)) {
      return;
    }

    String saplingId = namespace + ":" + saplingPath;
    Block saplingBlock = Blocks.AIR;
    // Registry scan resolves derived sapling id without hardcoding every tree family.
    for (Block block : BuiltInRegistries.BLOCK) {
      if (saplingId.equals(BuiltInRegistries.BLOCK.getKey(block).toString())) {
        saplingBlock = block;
        break;
      }
    }

    if (saplingBlock == null || saplingBlock == Blocks.AIR) {
      return;
    }

    List<BlockPos> targets = findReplantTargets();
    if (targets.isEmpty()) {
      return;
    }

    int requiredSaplings = targets.size();
    if (requiredSaplings <= 0 || !hasAvailableSaplings(saplingBlock, requiredSaplings)) {
      return;
    }

    BlockState saplingState = saplingBlock.defaultBlockState();
    if (!allTargetsPlantable(targets, saplingState)) {
      return;
    }

    if (!consumeSaplingsForReplant(saplingBlock, requiredSaplings)) {
      return;
    }

    // Final commit: place saplings only after all validations and resource checks pass.
    for (BlockPos target : targets) {
      world.setBlockAndUpdate(target, saplingState);
    }
  }

  /**
   * Pick replant targets from harvested trunk footprint (prefer 2x2 base when present).
   */
  private List<BlockPos> findReplantTargets() {
    return selectReplantTargets(harvestedLogs, origin);
  }

  /**
   * Validate all target positions are empty and can support sapling survival.
   */
  private boolean allTargetsPlantable(List<BlockPos> targets, BlockState saplingState) {
    for (BlockPos target : targets) {
      BlockState existing = world.getBlockState(target);
      if (!existing.isAir() && !existing.canBeReplaced()) {
        return false;
      }
      if (!saplingState.canSurvive(world, target)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Check if enough saplings exist from inventory plus optional harvested-drop pool.
   */
  private boolean hasAvailableSaplings(Block saplingBlock, int required) {
    return hasAvailableSaplings(countInventorySaplings(saplingBlock), harvestedSaplings, required);
  }

  /**
   * Count saplings in player inventory matching specific sapling block.
   */
  private int countInventorySaplings(Block saplingBlock) {
    if (player == null || player.getInventory() == null) {
      return 0;
    }

    int count = 0;
    for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
      ItemStack stack = player.getInventory().getItem(slot);
      if (isSaplingStackForBlock(stack, saplingBlock)) {
        count += stack.getCount();
      }
    }

    return count;
  }

  /**
   * Consume saplings from inventory (and optionally harvested-drop buffer) for replant operation.
   */
  private boolean consumeSaplingsForReplant(Block saplingBlock, int required) {
    int availableInventory = countInventorySaplings(saplingBlock);
    int availableDrops = harvestedSaplings;

    if (!hasAvailableSaplings(availableInventory, availableDrops, required)) {
      return false;
    }

    int remaining = required;

    int fromInventory = Math.min(remaining, availableInventory);
    shrinkInventorySaplings(saplingBlock, fromInventory);
    remaining -= fromInventory;

    if (remaining > 0) {
      harvestedSaplings = Math.max(0, harvestedSaplings - remaining);
      remaining = 0;
    }

    return remaining == 0;
  }

  /**
   * Remove requested number of matching saplings from inventory stacks.
   */
  private void shrinkInventorySaplings(Block saplingBlock, int countToRemove) {
    if (countToRemove <= 0 || player == null || player.getInventory() == null) {
      return;
    }

    int remaining = countToRemove;
    for (int slot = 0; slot < player.getInventory().getContainerSize() && remaining > 0; slot++) {
      ItemStack stack = player.getInventory().getItem(slot);
      if (!isSaplingStackForBlock(stack, saplingBlock)) {
        continue;
      }

      int remove = Math.min(remaining, stack.getCount());
      stack.shrink(remove);
      remaining -= remove;
    }
  }

  /**
   * Check whether stack is a sapling item for the requested block.
   */
  private boolean isSaplingStackForBlock(ItemStack stack, Block saplingBlock) {
    return stack != null
        && !stack.isEmpty()
        && stack.getItem() instanceof BlockItem blockItem
        && blockItem.getBlock() == saplingBlock;
  }

  /**
   * Collect sapling drops from broken block into harvestedSaplings counter for optional replant use.
   */
  private void collectSaplingDrops(BlockState state, BlockPos pos) {
    var drops = Block.getDrops(state, (net.minecraft.server.level.ServerLevel) world, pos, null, player,
        player.getMainHandItem());
    for (ItemStack drop : drops) {
      if (drop == null || drop.isEmpty()) {
        continue;
      }
      if (drop.getItem() instanceof BlockItem blockItem) {
        String blockId = BuiltInRegistries.BLOCK.getKey(blockItem.getBlock()).toString();
        if (blockId.endsWith("_sapling")) {
          harvestedSaplings += drop.getCount();
        }
      }
    }
  }

  static boolean isHarvestableLogId(String blockId) {
    if (blockId == null || blockId.isBlank()) {
      return false;
    }

    String normalized = blockId.toLowerCase(Locale.ROOT);
    return (normalized.endsWith("_log") || normalized.endsWith("_stem") || normalized.endsWith("_hyphae"))
        && !isBlockedLogId(normalized);
  }

  static boolean isBlockedLogId(String blockId) {
    if (blockId == null || blockId.isBlank()) {
      return false;
    }

    String normalized = blockId.toLowerCase(Locale.ROOT);
    return normalized.contains("stripped")
        || normalized.endsWith("_wood")
        || normalized.contains("_wood_");
  }

  static boolean hasAvailableSaplings(int inventoryCount, int harvestedSaplings, int required) {
    return inventoryCount + harvestedSaplings >= required;
  }

  static boolean isLeafCloserToHarvestedTree(BlockPos candidate, Collection<BlockPos> harvestedLogs,
      Collection<BlockPos> competingLogs) {
    if (candidate == null) {
      return false;
    }

    Set<Long> harvestedColumns = uniqueColumns(harvestedLogs);
    Set<Long> competingColumns = uniqueColumns(competingLogs);
    return isLeafCloserToHarvestedTree(candidate, harvestedColumns, competingColumns);
  }

  private static boolean isLeafCloserToHarvestedTree(BlockPos candidate, Set<Long> harvestedColumns,
      Set<Long> competingColumns) {
    double harvestedDistance = nearestColumnHorizontalDistanceSquared(candidate, harvestedColumns);
    double competingDistance = nearestColumnHorizontalDistanceSquared(candidate, competingColumns);
    return competingDistance == Long.MAX_VALUE || harvestedDistance < competingDistance;
  }

  static List<BlockPos> orderLeafCandidates(List<BlockPos> candidates, int trunkMinX, int trunkMaxX, int trunkMinY,
      int trunkMaxY, int trunkMinZ, int trunkMaxZ) {
    if (candidates.isEmpty()) {
      return List.of();
    }

    double centerX = (trunkMinX + trunkMaxX) / 2.0D;
    double centerZ = (trunkMinZ + trunkMaxZ) / 2.0D;
    return candidates.stream()
        .sorted(Comparator
            .comparingInt((BlockPos pos) -> (pos.getY() - trunkMinY) + leafRing(pos, centerX, centerZ))
            .thenComparingInt(pos -> -leafRing(pos, centerX, centerZ))
            .thenComparingDouble(pos -> clockwiseAngle(pos, centerX, centerZ))
            .thenComparingInt(BlockPos::getX)
            .thenComparingInt(BlockPos::getZ))
        .toList();
  }

  static List<BlockPos> selectReplantTargets(Collection<BlockPos> harvestedLogs, BlockPos origin) {
    if (harvestedLogs == null || harvestedLogs.isEmpty() || origin == null) {
      return List.of();
    }

    List<BlockPos> footprint = new ArrayList<>();
    for (BlockPos pos : harvestedLogs) {
      footprint.add(pos.immutable());
    }
    footprint.add(origin.immutable());

    int baseY = footprint.stream().mapToInt(BlockPos::getY).min().orElse(origin.getY());
    Set<BlockPos> baseLogs = new HashSet<>();
    for (BlockPos pos : footprint) {
      if (pos.getY() == baseY) {
        baseLogs.add(pos.immutable());
      }
    }

    if (baseLogs.isEmpty()) {
      return List.of();
    }

    List<BlockPos> twoByTwo = findTwoByTwoBase(footprint, baseY, origin);
    if (!twoByTwo.isEmpty()) {
      return twoByTwo;
    }

    BlockPos best = null;
    int bestDistance = Integer.MAX_VALUE;
    for (BlockPos candidate : baseLogs) {
      int distance = Math.abs(candidate.getX() - origin.getX()) + Math.abs(candidate.getZ() - origin.getZ());
      if (best == null || distance < bestDistance) {
        best = candidate;
        bestDistance = distance;
      }
    }

    return best == null ? List.of() : List.of(best);
  }

  private static List<BlockPos> findTwoByTwoBase(Collection<BlockPos> footprint, int baseY, BlockPos origin) {
    Set<Long> columns = new HashSet<>();
    for (BlockPos pos : footprint) {
      columns.add(columnKey(pos.getX(), pos.getZ()));
    }

    int[] candidateX = new int[] { origin.getX() - 1, origin.getX() };
    int[] candidateZ = new int[] { origin.getZ() - 1, origin.getZ() };
    for (int baseX : candidateX) {
      for (int baseZ : candidateZ) {
        long p1 = columnKey(baseX, baseZ);
        long p2 = columnKey(baseX + 1, baseZ);
        long p3 = columnKey(baseX, baseZ + 1);
        long p4 = columnKey(baseX + 1, baseZ + 1);
        if (columns.contains(p1) && columns.contains(p2) && columns.contains(p3) && columns.contains(p4)) {
          return List.of(
              new BlockPos(baseX, baseY, baseZ),
              new BlockPos(baseX + 1, baseY, baseZ),
              new BlockPos(baseX, baseY, baseZ + 1),
              new BlockPos(baseX + 1, baseY, baseZ + 1));
        }
      }
    }

    return List.of();
  }

  private static long columnKey(int x, int z) {
    return (((long) x) << 32) ^ (z & 0xffffffffL);
  }

  private static Set<String> normalizeConfiguredIds(List<String> configuredValues) {
    Set<String> normalized = new HashSet<>();
    if (configuredValues == null || configuredValues.isEmpty()) {
      return normalized;
    }

    for (String value : configuredValues) {
      if (value != null && !value.isBlank()) {
        normalized.add(value.toLowerCase(Locale.ROOT));
      }
    }
    return normalized;
  }

  private static long nearestDistanceSquared(BlockPos candidate, Collection<BlockPos> positions) {
    if (positions == null || positions.isEmpty()) {
      return Long.MAX_VALUE;
    }

    long best = Long.MAX_VALUE;
    for (BlockPos pos : positions) {
      long dx = candidate.getX() - pos.getX();
      long dy = candidate.getY() - pos.getY();
      long dz = candidate.getZ() - pos.getZ();
      long distance = dx * dx + dy * dy + dz * dz;
      if (distance < best) {
        best = distance;
      }
    }
    return best;
  }

  private static double nearestColumnHorizontalDistanceSquared(BlockPos candidate, Collection<BlockPos> positions) {
    if (positions == null || positions.isEmpty()) {
      return Long.MAX_VALUE;
    }

    return nearestColumnHorizontalDistanceSquared(candidate, uniqueColumns(positions));
  }

  private static double nearestColumnHorizontalDistanceSquared(BlockPos candidate, Set<Long> columns) {
    if (columns == null || columns.isEmpty()) {
      return Long.MAX_VALUE;
    }

    double best = Long.MAX_VALUE;
    for (long column : columns) {
      int columnX = (int) (column >> 32);
      int columnZ = (int) column;
      double dx = candidate.getX() - columnX;
      double dz = candidate.getZ() - columnZ;
      double distance = dx * dx + dz * dz;
      if (distance < best) {
        best = distance;
      }
    }

    return best;
  }

  private static Set<Long> uniqueColumns(Collection<BlockPos> positions) {
    Set<Long> columns = new HashSet<>();
    if (positions == null || positions.isEmpty()) {
      return columns;
    }

    for (BlockPos pos : positions) {
      columns.add(columnKey(pos.getX(), pos.getZ()));
    }
    return columns;
  }

  private static int leafRing(BlockPos pos, double centerX, double centerZ) {
    return (int) Math.round(Math.max(Math.abs(pos.getX() - centerX), Math.abs(pos.getZ() - centerZ)));
  }

  private static double clockwiseAngle(BlockPos pos, double centerX, double centerZ) {
    double dx = pos.getX() - centerX;
    double dz = pos.getZ() - centerZ;
    double angle = Math.atan2(dx, -dz);
    return angle < 0.0D ? angle + (Math.PI * 2.0D) : angle;
  }
}
