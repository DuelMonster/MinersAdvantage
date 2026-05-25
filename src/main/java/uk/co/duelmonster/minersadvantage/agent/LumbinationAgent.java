package uk.co.duelmonster.minersadvantage.agent;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
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
 * Modernized LumbinationAgent: fells trees by breaking connected logs and leaves.
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

    public LumbinationAgent(ServerPlayer player, BlockPos origin) {
        this(
            player,
            origin,
            player.level().getBlockState(origin),
            MAServerRootConfig.defaults().lumbination(),
            new CommonConfig()
        );
    }

    public LumbinationAgent(ServerPlayer player, BlockPos origin, Block originBlock) {
        this(
            player,
            origin,
            originBlock.defaultBlockState(),
            MAServerRootConfig.defaults().lumbination(),
            new CommonConfig()
        );
    }

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

        if (!matchesLog(originState)) {
            return;
        }

        if (!findOriginLeaf()) {
            return;
        }

        queue.add(origin);
    }

    @Override
    public boolean tick() {
        int count = 0;
        while (count < blocksPerTick) {
            if (logsPhaseComplete && !leafCandidatesSeeded) {
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
                if (!visitedLeaves.add(pos)) {
                    continue;
                }
            } else if (!visitedLogs.add(pos)) {
                continue;
            }

            BlockState state = world.getBlockState(pos);
            if (pos.equals(origin) && state.getBlock() == Blocks.AIR && matchesLog(originState)) {
                enqueueNeighbors(pos, false);
                continue;
            }

            if (!logsPhaseComplete && matchesLog(state)) {
                if (!withinRange(pos, maxTrunkRange, config.chopTreeBelow())) {
                    continue;
                }

                collectSaplingDrops(state, pos);
                updateTrunkBounds(pos);
                harvestedLogs.add(pos.immutable());
                world.destroyBlock(pos, true, player);
                harvestedLog = true;
                count++;
                enqueueNeighbors(pos, false);
            } else if (logsPhaseComplete
                    && config.destroyLeaves()
                    && matchesLeaf(state)
                    && withinRange(pos, maxTrunkRange + maxLeafRange, config.chopTreeBelow())
                    && withinLeafCanopyBounds(pos)) {
                ItemStack originalMainHand = player.getMainHandItem().copy();
                boolean restoreMainHand = false;

                if (config.useShearsOnLeaves()) {
                    ItemStack shears = firstShearsInInventory();
                    if (!shears.isEmpty()) {
                        player.setItemInHand(InteractionHand.MAIN_HAND, shears.copy());
                        restoreMainHand = true;
                    }
                }

                if (!config.leavesAffectDurability()) {
                    restoreMainHand = true;
                }

                world.destroyBlock(pos, true, player);

                if (restoreMainHand) {
                    player.setItemInHand(InteractionHand.MAIN_HAND, originalMainHand);
                }

                collectSaplingDrops(state, pos);
                count++;
            }
        }

        if (logsPhaseComplete && leafQueue.isEmpty() && config.replantSaplings() && harvestedLog) {
            tryReplantSaplings();
        }

        if (logsPhaseComplete && leafQueue.isEmpty()) {
            return finish("tree traversal exhausted");
        }
        return false;
    }

    private void seedLeafQueueFromCanopyBounds() {
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

                    leafQueue.add(pos.immutable());
                }
            }
        }
    }

    private void enqueueNeighbors(BlockPos pos, boolean toLeafQueue) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) {
                        continue;
                    }
                    if (toLeafQueue) {
                        BlockPos candidate = pos.offset(dx, dy, dz).immutable();
                        if (withinLeafCanopyBounds(candidate)) {
                            leafQueue.add(candidate);
                        }
                    } else {
                        queue.add(pos.offset(dx, dy, dz).immutable());
                    }
                }
            }
        }
    }

    private void updateTrunkBounds(BlockPos pos) {
        trunkMinX = Math.min(trunkMinX, pos.getX());
        trunkMaxX = Math.max(trunkMaxX, pos.getX());
        trunkMinY = Math.min(trunkMinY, pos.getY());
        trunkMaxY = Math.max(trunkMaxY, pos.getY());
        trunkMinZ = Math.min(trunkMinZ, pos.getZ());
        trunkMaxZ = Math.max(trunkMaxZ, pos.getZ());
    }

    private boolean withinLeafCanopyBounds(BlockPos pos) {
        int horizontalLeafPadding = Math.max(1, maxLeafRange / 2);
        return pos.getX() >= trunkMinX - horizontalLeafPadding
            && pos.getX() <= trunkMaxX + horizontalLeafPadding
            && pos.getY() >= trunkMinY - maxLeafRange
            && pos.getY() <= trunkMaxY + maxLeafRange
            && pos.getZ() >= trunkMinZ - horizontalLeafPadding
            && pos.getZ() <= trunkMaxZ + horizontalLeafPadding;
    }

    private boolean withinRange(BlockPos pos, int range, boolean allowBelowOrigin) {
        int dx = Math.abs(pos.getX() - origin.getX());
        int dy = Math.abs(pos.getY() - origin.getY());
        int dz = Math.abs(pos.getZ() - origin.getZ());
        if (!allowBelowOrigin && pos.getY() < origin.getY()) {
            return false;
        }
        return dx <= range && dy <= range && dz <= range;
    }

    private boolean matchesLog(BlockState state) {
        if (state == null || state.getBlock() == Blocks.AIR) {
            return false;
        }

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

    private boolean matchesLeaf(BlockState state) {
        if (state == null || state.getBlock() == Blocks.AIR) {
            return false;
        }

        if (config.ignorePlayerPlacedLeaves()
                && state.getBlock() instanceof LeavesBlock
                && state.getValue(LeavesBlock.PERSISTENT)) {
            return false;
        }

        if (originLeafBlock != null && state.getBlock() != originLeafBlock) {
            return false;
        }

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

    private boolean findOriginLeaf() {
        int maxY = origin.getY() + maxTrunkRange + maxLeafRange;
        for (int y = origin.getY(); y <= maxY; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    BlockState checkState = world.getBlockState(new BlockPos(origin.getX() + x, y, origin.getZ() + z));
                    if (isLeafBlock(checkState.getBlock())) {
                        originLeafBlock = checkState.getBlock();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isLeafBlock(Block block) {
        return block.defaultBlockState().is(BlockTags.LEAVES)
            || block.defaultBlockState().is(BlockTags.WART_BLOCKS);
    }

    private ItemStack firstShearsInInventory() {
        if (player == null || player.getInventory() == null) {
            return ItemStack.EMPTY;
        }

        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.isEmpty() && stack.getItem() instanceof ShearsItem) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

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
        if (requiredSaplings == 4 && !hasAvailableSaplings(saplingBlock, 4)) {
            return;
        }
        if (requiredSaplings == 1 && !hasAvailableSaplings(saplingBlock, 1)) {
            return;
        }

        BlockState saplingState = saplingBlock.defaultBlockState();
        if (!allTargetsPlantable(targets, saplingState)) {
            return;
        }

        if (!consumeSaplingsForReplant(saplingBlock, requiredSaplings)) {
            return;
        }

        for (BlockPos target : targets) {
            world.setBlockAndUpdate(target, saplingState);
        }
    }

    private List<BlockPos> findReplantTargets() {
        if (harvestedLogs.isEmpty()) {
            return List.of();
        }

        int baseY = trunkMinY;
        Set<BlockPos> baseLogs = new HashSet<>();
        for (BlockPos pos : harvestedLogs) {
            if (pos.getY() == baseY) {
                baseLogs.add(pos.immutable());
            }
        }

        if (baseLogs.isEmpty()) {
            return List.of();
        }

        List<BlockPos> twoByTwo = findTwoByTwoBase(baseLogs, baseY);
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

    private List<BlockPos> findTwoByTwoBase(Set<BlockPos> baseLogs, int baseY) {
        for (BlockPos candidate : baseLogs) {
            int x = candidate.getX();
            int z = candidate.getZ();

            BlockPos p1 = new BlockPos(x, baseY, z);
            BlockPos p2 = new BlockPos(x + 1, baseY, z);
            BlockPos p3 = new BlockPos(x, baseY, z + 1);
            BlockPos p4 = new BlockPos(x + 1, baseY, z + 1);

            if (baseLogs.contains(p1) && baseLogs.contains(p2) && baseLogs.contains(p3) && baseLogs.contains(p4)) {
                return List.of(p1, p2, p3, p4);
            }
        }

        return List.of();
    }

    private boolean allTargetsPlantable(List<BlockPos> targets, BlockState saplingState) {
        for (BlockPos target : targets) {
            if (!world.getBlockState(target).isAir()) {
                return false;
            }
            if (!saplingState.canSurvive(world, target)) {
                return false;
            }
        }
        return true;
    }

    private boolean hasAvailableSaplings(Block saplingBlock, int required) {
        int inventoryCount = countInventorySaplings(saplingBlock);
        if (config.useShearsOnLeaves()) {
            return inventoryCount >= required;
        }
        return inventoryCount + harvestedSaplings >= required;
    }

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

    private boolean consumeSaplingsForReplant(Block saplingBlock, int required) {
        int availableInventory = countInventorySaplings(saplingBlock);
        int availableDrops = harvestedSaplings;
        boolean shearsModeActive = config.useShearsOnLeaves();

        if (shearsModeActive) {
            if (availableInventory < required) {
                return false;
            }
            shrinkInventorySaplings(saplingBlock, required);
            return true;
        }

        if (availableInventory + availableDrops < required) {
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

    private boolean isSaplingStackForBlock(ItemStack stack, Block saplingBlock) {
        return stack != null
            && !stack.isEmpty()
            && stack.getItem() instanceof BlockItem blockItem
            && blockItem.getBlock() == saplingBlock;
    }

    private void collectSaplingDrops(BlockState state, BlockPos pos) {
        var drops = Block.getDrops(state, (net.minecraft.server.level.ServerLevel) world, pos, null, player, player.getMainHandItem());
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
}
