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
    private final int blocksPerTick;
    private Block originLeafBlock = null;
    private boolean harvestedLog = false;
    private boolean logsPhaseComplete = false;
    private boolean leafCandidatesSeeded = false;
    private int harvestedSaplings = 0;
    private int trunkMinX;
    private int trunkMaxX;
    private int trunkMinY;
    private int trunkMaxY;
    private int trunkMinZ;
    private int trunkMaxZ;

    /**
     * Convenience constructor using defaults and origin block state from world.
     */
    public LumbinationAgent(ServerPlayer player, BlockPos origin) {
        this(
            player,
            origin,
            player.level().getBlockState(origin),
            MAServerRootConfig.defaults().lumbination(),
            new CommonConfig()
        );
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
            new CommonConfig()
        );
    }

    /**
    * Main constructor that sets traversal limits, queues, and origin guards before work begins.
    */
    public LumbinationAgent(
        ServerPlayer player,
        BlockPos origin,
        BlockState originState,
        LumbinationConfig config,
        CommonConfig commonConfig
    ) {
        super(player);
        this.origin = origin;
        this.originState = originState == null ? Blocks.AIR.defaultBlockState() : originState;
        this.config = config == null ? MAServerRootConfig.defaults().lumbination() : config;
        this.maxTrunkRange = Math.max(0, this.config.maxTrunkRange());
        this.maxLeafRange = Math.max(0, this.config.maxLeafRange());
        int globalBlocksPerTick = commonConfig == null ? 1 : Math.max(1, commonConfig.blocksPerTick());
        this.blocksPerTick = Math.max(1, Math.min(globalBlocksPerTick, this.config.processesPerTick()));
        this.trunkMinX = origin.getX();
        this.trunkMaxX = origin.getX();
        this.trunkMinY = origin.getY();
        this.trunkMaxY = origin.getY();
        this.trunkMinZ = origin.getZ();
        this.trunkMaxZ = origin.getZ();

        // If origin is not a matching log, this worker stays inert by design.
        if (!matchesLog(originState)) {
            return;
        }

        // Origin leaf detection constrains canopy matching to the same leaf family.
        if (!findOriginLeaf()) {
            return;
        }

        queue.add(origin);
    }

    /**
     * Tick loop that processes logs first, then leaves, then optional replanting when traversal finishes.
     */
    @Override
    /**
     * t ic k exists so this path stays predictable and easier to debug when things get weird.
     */
    public boolean tick() {
        int count = 0;
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        while (count < blocksPerTick) {
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (logsPhaseComplete && !leafCandidatesSeeded) {
                // Seed leaf candidates exactly once after trunk pass is done.
                seedLeafQueueFromCanopyBounds();
                leafCandidatesSeeded = true;
            }

            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (logsPhaseComplete && leafQueue.isEmpty()) {
                break;
            }

            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (!logsPhaseComplete && queue.isEmpty()) {
                logsPhaseComplete = true;
                continue;
            }

            BlockPos pos = logsPhaseComplete ? leafQueue.poll() : queue.poll();
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (pos == null) {
                continue;
            }

            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (logsPhaseComplete) {
                // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                if (!visitedLeaves.add(pos)) {
                    continue;
                }
            } else if (!visitedLogs.add(pos)) {
                continue;
            }

            BlockState state = world.getBlockState(pos);
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (pos.equals(origin) && state.getBlock() == Blocks.AIR && matchesLog(originState)) {
                // Origin may already be gone; continue traversal through neighbors anyway.
                enqueueNeighbors(pos, false);
                continue;
            }

            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (!logsPhaseComplete && matchesLog(state)) {
                // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
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
                boolean restoreMainHand = false;

                // Optionally swap to canopy tool for leaf phase if configured.
                if (config.useCanopyTool()) {
                    ItemStack canopyTool = firstCanopyToolInInventory();
                    // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                    if (!canopyTool.isEmpty()) {
                        player.setItemInHand(InteractionHand.MAIN_HAND, canopyTool.copy());
                        restoreMainHand = true;
                    }
                }

                // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                if (!config.leavesAffectDurability()) {
                    restoreMainHand = true;
                }

                BreakOutcome breakOutcome = breakBlockWithTool(pos, ItemStack.EMPTY);

                // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                if (restoreMainHand) {
                    // Keep player hand state stable after temporary tool overrides.
                    player.setItemInHand(InteractionHand.MAIN_HAND, originalMainHand);
                }

                if (breakOutcome.broken()) {
                    collectSaplingDrops(state, pos);
                    count++;
                }
            }
        }

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (logsPhaseComplete && leafQueue.isEmpty() && config.replantSaplings() && harvestedLog) {
            tryReplantSaplings();
        }

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (logsPhaseComplete && leafQueue.isEmpty()) {
            return finish("tree traversal exhausted");
        }
        return false;
    }

    /**
     * Populate leaf queue from canopy bounds inferred from harvested trunk bounds.
     */
    private void seedLeafQueueFromCanopyBounds() {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (!config.destroyLeaves()) {
            return;
        }

        int horizontalLeafPadding = Math.max(1, maxLeafRange / 2);
        int minX = trunkMinX - horizontalLeafPadding;
        int maxX = trunkMaxX + horizontalLeafPadding;
        int minY = trunkMinY - maxLeafRange;
        int maxY = trunkMaxY + maxLeafRange;
        int minZ = trunkMinZ - horizontalLeafPadding;
        int maxZ = trunkMaxZ + horizontalLeafPadding;

        // Scan canopy box and keep only matching leaves that satisfy range guards.
        for (int y = minY; y <= maxY; y++) {
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            for (int x = minX; x <= maxX; x++) {
                // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = world.getBlockState(pos);

                    // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                    if (!matchesLeaf(state)) {
                        continue;
                    }

                    // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                    if (!withinRange(pos, maxTrunkRange + maxLeafRange, config.chopTreeBelow())) {
                        continue;
                    }

                    leafQueue.add(pos.immutable());
                }
            }
        }
    }

    /**
     * Enqueue connected neighbors either into log queue or leaf queue depending on phase.
     */
    private void enqueueNeighbors(BlockPos pos, boolean toLeafQueue) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        for (BlockPos candidate : Functions.connectedNeighbors(pos)) {
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (toLeafQueue) {
                // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                if (withinLeafCanopyBounds(candidate)) {
                    leafQueue.add(candidate);
                }
            } else {
                queue.add(candidate);
            }
        }
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
        int horizontalLeafPadding = Math.max(1, maxLeafRange / 2);
        return pos.getX() >= trunkMinX - horizontalLeafPadding
            && pos.getX() <= trunkMaxX + horizontalLeafPadding
            && pos.getY() >= trunkMinY - maxLeafRange
            && pos.getY() <= trunkMaxY + maxLeafRange
            && pos.getZ() >= trunkMinZ - horizontalLeafPadding
            && pos.getZ() <= trunkMaxZ + horizontalLeafPadding;
    }

    /**
     * Distance/range guard with optional below-origin allowance.
     */
    private boolean withinRange(BlockPos pos, int range, boolean allowBelowOrigin) {
        int dx = Math.abs(pos.getX() - origin.getX());
        int dy = Math.abs(pos.getY() - origin.getY());
        int dz = Math.abs(pos.getZ() - origin.getZ());
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (!allowBelowOrigin && pos.getY() < origin.getY()) {
            return false;
        }
        return dx <= range && dy <= range && dz <= range;
    }

    /**
     * Determine whether candidate block is an allowed trunk/log for this tree run.
     */
    private boolean matchesLog(BlockState state) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (state == null || state.getBlock() == Blocks.AIR) {
            return false;
        }

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (!config.logs().isEmpty()) {
            String blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString().toLowerCase(Locale.ROOT);
            return config.logs().stream()
                .filter(value -> value != null && !value.isBlank())
                .map(value -> value.toLowerCase(Locale.ROOT))
                .anyMatch(blockId::equals);
        }

        Block block = state.getBlock();
        return block.defaultBlockState().is(BlockTags.LOGS)
            && block == originState.getBlock();
    }

    /**
     * Determine whether candidate block is an allowed leaf/canopy block for this tree run.
     */
    private boolean matchesLeaf(BlockState state) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (state == null || state.getBlock() == Blocks.AIR) {
            return false;
        }

        // Ignore player-placed persistent leaves when requested.
        if (config.ignorePlayerPlacedLeaves()
                && state.getBlock() instanceof LeavesBlock
                && state.getValue(LeavesBlock.PERSISTENT)) {
            return false;
        }

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (originLeafBlock != null && state.getBlock() != originLeafBlock) {
            return false;
        }

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (!config.leaves().isEmpty()) {
            String blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString().toLowerCase(Locale.ROOT);
            return config.leaves().stream()
                .filter(value -> value != null && !value.isBlank())
                .map(value -> value.toLowerCase(Locale.ROOT))
                .anyMatch(blockId::equals);
        }

        Block block = state.getBlock();
        return block.defaultBlockState().is(BlockTags.LEAVES)
            || block.defaultBlockState().is(BlockTags.WART_BLOCKS);
    }

    /**
     * Locate a representative leaf block near/above origin to lock canopy family matching.
     */
    private boolean findOriginLeaf() {
        int maxY = origin.getY() + maxTrunkRange + maxLeafRange;
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        for (int y = origin.getY(); y <= maxY; y++) {
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            for (int x = -1; x <= 1; x++) {
                // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                for (int z = -1; z <= 1; z++) {
                    BlockState checkState = world.getBlockState(new BlockPos(origin.getX() + x, y, origin.getZ() + z));
                    // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                    if (isLeafBlock(checkState.getBlock())) {
                        originLeafBlock = checkState.getBlock();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Lightweight helper for leaf-tag membership checks.
     */
    private boolean isLeafBlock(Block block) {
        return block.defaultBlockState().is(BlockTags.LEAVES)
            || block.defaultBlockState().is(BlockTags.WART_BLOCKS);
    }

    /**
     * Find first usable canopy tool (shears/hoe) in inventory.
     */
    private ItemStack firstCanopyToolInInventory() {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (player == null || player.getInventory() == null) {
            return ItemStack.EMPTY;
        }

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
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
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
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

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (saplingPath.equals(path)) {
            return;
        }

        String saplingId = namespace + ":" + saplingPath;
        Block saplingBlock = Blocks.AIR;
        // Registry scan resolves derived sapling id without hardcoding every tree family.
        for (Block block : BuiltInRegistries.BLOCK) {
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (saplingId.equals(BuiltInRegistries.BLOCK.getKey(block).toString())) {
                saplingBlock = block;
                break;
            }
        }

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (saplingBlock == null || saplingBlock == Blocks.AIR) {
            return;
        }

        List<BlockPos> targets = findReplantTargets();
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (targets.isEmpty()) {
            return;
        }

        int requiredSaplings = targets.size();
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (requiredSaplings == 4 && !hasAvailableSaplings(saplingBlock, 4)) {
            return;
        }
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (requiredSaplings == 1 && !hasAvailableSaplings(saplingBlock, 1)) {
            return;
        }

        BlockState saplingState = saplingBlock.defaultBlockState();
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (!allTargetsPlantable(targets, saplingState)) {
            return;
        }

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
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
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (harvestedLogs.isEmpty()) {
            return List.of();
        }

        int baseY = trunkMinY;
        Set<BlockPos> baseLogs = new HashSet<>();
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        for (BlockPos pos : harvestedLogs) {
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (pos.getY() == baseY) {
                baseLogs.add(pos.immutable());
            }
        }

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (baseLogs.isEmpty()) {
            return List.of();
        }

        List<BlockPos> twoByTwo = findTwoByTwoBase(baseLogs, baseY);
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (!twoByTwo.isEmpty()) {
            return twoByTwo;
        }

        BlockPos best = null;
        int bestDistance = Integer.MAX_VALUE;
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        for (BlockPos candidate : baseLogs) {
            int distance = Math.abs(candidate.getX() - origin.getX()) + Math.abs(candidate.getZ() - origin.getZ());
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (best == null || distance < bestDistance) {
                best = candidate;
                bestDistance = distance;
            }
        }

        return best == null ? List.of() : List.of(best);
    }

    /**
     * Detect 2x2 trunk base cluster for giant-tree style replanting.
     */
    private List<BlockPos> findTwoByTwoBase(Set<BlockPos> baseLogs, int baseY) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        for (BlockPos candidate : baseLogs) {
            int x = candidate.getX();
            int z = candidate.getZ();

            BlockPos p1 = new BlockPos(x, baseY, z);
            BlockPos p2 = new BlockPos(x + 1, baseY, z);
            BlockPos p3 = new BlockPos(x, baseY, z + 1);
            BlockPos p4 = new BlockPos(x + 1, baseY, z + 1);

            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (baseLogs.contains(p1) && baseLogs.contains(p2) && baseLogs.contains(p3) && baseLogs.contains(p4)) {
                return List.of(p1, p2, p3, p4);
            }
        }

        return List.of();
    }

    /**
     * Validate all target positions are empty and can support sapling survival.
     */
    private boolean allTargetsPlantable(List<BlockPos> targets, BlockState saplingState) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        for (BlockPos target : targets) {
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (!world.getBlockState(target).isAir()) {
                return false;
            }
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
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
        int inventoryCount = countInventorySaplings(saplingBlock);
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (config.useCanopyTool()) {
            return inventoryCount >= required;
        }
        return inventoryCount + harvestedSaplings >= required;
    }

    /**
     * Count saplings in player inventory matching specific sapling block.
     */
    private int countInventorySaplings(Block saplingBlock) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (player == null || player.getInventory() == null) {
            return 0;
        }

        int count = 0;
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
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
        boolean canopyToolModeActive = config.useCanopyTool();

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (canopyToolModeActive) {
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (availableInventory < required) {
                return false;
            }
            shrinkInventorySaplings(saplingBlock, required);
            return true;
        }

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (availableInventory + availableDrops < required) {
            return false;
        }

        int remaining = required;

        int fromInventory = Math.min(remaining, availableInventory);
        shrinkInventorySaplings(saplingBlock, fromInventory);
        remaining -= fromInventory;

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
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
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (countToRemove <= 0 || player == null || player.getInventory() == null) {
            return;
        }

        int remaining = countToRemove;
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        for (int slot = 0; slot < player.getInventory().getContainerSize() && remaining > 0; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
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
        var drops = Block.getDrops(state, (net.minecraft.server.level.ServerLevel) world, pos, null, player, player.getMainHandItem());
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        for (ItemStack drop : drops) {
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (drop == null || drop.isEmpty()) {
                continue;
            }
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (drop.getItem() instanceof BlockItem blockItem) {
                String blockId = BuiltInRegistries.BLOCK.getKey(blockItem.getBlock()).toString();
                // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                if (blockId.endsWith("_sapling")) {
                    harvestedSaplings += drop.getCount();
                }
            }
        }
    }
}
