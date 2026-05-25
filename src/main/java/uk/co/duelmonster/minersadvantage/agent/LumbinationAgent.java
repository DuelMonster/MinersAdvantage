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
import net.minecraft.world.level.block.state.BlockState;
import uk.co.duelmonster.minersadvantage.common.config.CommonConfig;
import uk.co.duelmonster.minersadvantage.common.config.LumbinationConfig;
import uk.co.duelmonster.minersadvantage.common.config.MAServerRootConfig;

import java.util.HashSet;
import java.util.LinkedList;
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
    private final Set<BlockPos> visited = new HashSet<>();
    private final int blocksPerTick;
    private final int blockLimit;
    private boolean harvestedLog = false;
    private int harvestedSaplings = 0;
    private int processed = 0;

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
        this.blockLimit = commonConfig == null ? 64 : Math.max(1, commonConfig.blockLimit());

        if (!matchesLog(originState)) {
            return;
        }

        queue.add(origin);
    }

    @Override
    public boolean tick() {
        int count = 0;
        while (!queue.isEmpty() && count < blocksPerTick && processed < blockLimit) {
            BlockPos pos = queue.poll();
            if (pos == null || !visited.add(pos)) {
                continue;
            }

            BlockState state = world.getBlockState(pos);
            if (pos.equals(origin) && state.getBlock() == Blocks.AIR && matchesLog(originState)) {
                enqueueNeighbors(pos);
                continue;
            }

            if (matchesLog(state)) {
                if (!withinRange(pos, maxTrunkRange, config.chopTreeBelow())) {
                    continue;
                }

                collectSaplingDrops(state, pos);
                world.destroyBlock(pos, true, player);
                harvestedLog = true;
                processed++;
                count++;
                enqueueNeighbors(pos);
            } else if (config.destroyLeaves() && matchesLeaf(state) && withinRange(pos, maxLeafRange, config.chopTreeBelow())) {
                if (config.useShearsOnLeaves() && !playerHasShears()) {
                    continue;
                }

                ItemStack originalMainHand = ItemStack.EMPTY;
                if (!config.leavesAffectDurability()) {
                    originalMainHand = player.getMainHandItem().copy();
                }

                world.destroyBlock(pos, true, player);

                if (!config.leavesAffectDurability() && !originalMainHand.isEmpty()) {
                    player.setItemInHand(InteractionHand.MAIN_HAND, originalMainHand);
                }

                collectSaplingDrops(state, pos);
                processed++;
                count++;
            }
        }

        if (queue.isEmpty() && config.replantSaplings() && harvestedLog && harvestedSaplings > 0) {
            tryReplantSapling();
        }

        if (queue.isEmpty() || processed >= blockLimit) {
            return finish(queue.isEmpty() ? "tree traversal exhausted" : "tree block limit reached");
        }
        return false;
    }

    private void enqueueNeighbors(BlockPos pos) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) {
                        continue;
                    }
                    queue.add(pos.offset(dx, dy, dz).immutable());
                }
            }
        }
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
        return block.defaultBlockState().is(BlockTags.LOGS);
    }

    private boolean matchesLeaf(BlockState state) {
        if (state == null || state.getBlock() == Blocks.AIR) {
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
        return block.defaultBlockState().is(BlockTags.LEAVES) || block.defaultBlockState().is(BlockTags.WART_BLOCKS);
    }

    private boolean playerHasShears() {
        if (player == null || player.getInventory() == null) {
            return false;
        }

        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.isEmpty() && stack.getItem() instanceof ShearsItem) {
                return true;
            }
        }
        return false;
    }

    private void tryReplantSapling() {
        if (origin == null || !world.getBlockState(origin).isAir()) {
            return;
        }

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

        BlockState saplingState = saplingBlock.defaultBlockState();
        if (saplingState.canSurvive(world, origin)) {
            world.setBlockAndUpdate(origin, saplingState);
            harvestedSaplings = Math.max(0, harvestedSaplings - 1);
        }
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
