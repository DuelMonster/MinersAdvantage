package uk.co.duelmonster.minersadvantage.agent;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import uk.co.duelmonster.minersadvantage.common.config.CommonConfig;
import uk.co.duelmonster.minersadvantage.common.config.MAServerRootConfig;
import uk.co.duelmonster.minersadvantage.common.config.VeinationConfig;
import uk.co.duelmonster.minersadvantage.common.config.VentilationConfig;
import uk.co.duelmonster.minersadvantage.common.services.utility.VeinationRuntimeService;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Queue;

/**
 * VentilationAgent: digs a vertical vent shaft upward or downward from the origin.
 */
public class VentilationAgent extends Agent {
    private final BlockPos origin;
    private final Direction direction;
    private final VentilationConfig config;
    private final Queue<BlockPos> queue = new LinkedList<>();
    private final Deque<BlockPos> ladderQueue = new LinkedList<>();
    private int dug = 0;
    private final int blocksPerTick;
    private final int blockLimit;
    private final boolean mineVeins;
    private final CommonConfig commonConfig;
    private final VeinationRuntimeService veinationRuntime;
    private final VeinationConfig veinationConfig;
    private final ItemStack veinationTriggerTool;
    private int ladderPlacements = 0;
    private BlockPos lowestDugPos;
    private boolean bottomTorchProcessed = false;

    public VentilationAgent(ServerPlayer player, BlockPos origin, int length) {
        this(player, origin, Direction.DOWN, new VentilationConfig(true, 1, Math.max(1, length), 1, 8), new CommonConfig());
    }

    public VentilationAgent(ServerPlayer player, BlockPos origin, VentilationConfig config, CommonConfig commonConfig) {
        this(player, origin, Direction.DOWN, config, commonConfig);
    }

    public VentilationAgent(ServerPlayer player, BlockPos origin, Direction direction, VentilationConfig config, CommonConfig commonConfig) {
        this(player, origin, direction, config, commonConfig, null, null);
    }

    public VentilationAgent(
        ServerPlayer player,
        BlockPos origin,
        Direction direction,
        VentilationConfig config,
        CommonConfig commonConfig,
        VeinationRuntimeService veinationRuntime,
        VeinationConfig veinationConfig
    ) {
        this(player, origin, direction, config, commonConfig, veinationRuntime, veinationConfig, ItemStack.EMPTY);
    }

    public VentilationAgent(
        ServerPlayer player,
        BlockPos origin,
        Direction direction,
        VentilationConfig config,
        CommonConfig commonConfig,
        VeinationRuntimeService veinationRuntime,
        VeinationConfig veinationConfig,
        ItemStack veinationTriggerTool
    ) {
        super(player);
        this.origin = origin;
        this.direction = direction == Direction.UP ? Direction.UP : Direction.DOWN;
        this.config = config == null ? MAServerRootConfig.defaults().ventilation() : config;
        int ventDepth = Math.max(1, this.config.height());

        int globalBlocksPerTick = commonConfig == null ? 1 : Math.max(1, commonConfig.blocksPerTick());
        this.blocksPerTick = Math.max(1, Math.min(globalBlocksPerTick, this.config.processesPerTick()));
        this.blockLimit = commonConfig == null ? 64 : Math.max(1, commonConfig.blockLimit());
        this.mineVeins = commonConfig == null || commonConfig.mineVeins();
        this.commonConfig = commonConfig;
        this.veinationRuntime = veinationRuntime;
        this.veinationConfig = veinationConfig;
        this.veinationTriggerTool = veinationTriggerTool == null ? ItemStack.EMPTY : veinationTriggerTool.copy();

        for (int depth = 1; depth <= ventDepth; depth++) {
            queue.add(origin.relative(this.direction, depth).immutable());
        }

        if (this.direction == Direction.DOWN && this.config.placeLadders()) {
            ladderQueue.addLast(origin.immutable());
        }
    }

    @Override
    public boolean tick() {
        int count = 0;
        while (!queue.isEmpty() && count < blocksPerTick && dug < blockLimit) {
            BlockPos pos = queue.poll();
            BlockState state = world.getBlockState(pos);
            if (!state.isAir()) {
                world.destroyBlock(pos, true, player);
                maybeFanOutVeination(pos, state, mineVeins, this.commonConfig, veinationRuntime, veinationConfig, veinationTriggerTool);
                dug++;
                count++;

                if (direction == Direction.DOWN) {
                    lowestDugPos = pos.immutable();
                    if (config.placeLadders()) {
                        // Prepended so ladder placement runs from far-to-near once digging completes.
                        ladderQueue.addFirst(pos.immutable());
                    }
                }
            }
        }

        boolean diggingComplete = queue.isEmpty() || dug >= blockLimit;
        if (diggingComplete) {
            if (direction == Direction.DOWN && !bottomTorchProcessed && count < blocksPerTick) {
                bottomTorchProcessed = true;
                if (lowestDugPos != null) {
                    ladderQueue.remove(lowestDugPos);
                    placeTorchWithInventory(lowestDugPos, Direction.UP);
                }
                count++;
            }

            while (count < blocksPerTick && !ladderQueue.isEmpty()) {
                BlockPos ladderPos = ladderQueue.pollFirst();
                tryPlaceLadder(ladderPos);
                count++;
            }
        }

        boolean placementComplete = ladderQueue.isEmpty() && (direction != Direction.DOWN || bottomTorchProcessed);
        if (diggingComplete && placementComplete) {
            return finish(queue.isEmpty() ? "ventilation queue exhausted" : "ventilation target reached");
        }
        return false;
    }

    private void tryPlaceLadder(BlockPos pos) {
        int ladderSlot = findLadderInInventory();
        if (ladderSlot < 0) {
            return;
        }
        if (!world.getBlockState(pos).canBeReplaced()) {
            return;
        }

        Direction[] sides = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
        for (Direction wallDir : sides) {
            BlockPos supportPos = pos.relative(wallDir);
            BlockState supportState = world.getBlockState(supportPos);
            if (supportState.isFaceSturdy(world, supportPos, wallDir.getOpposite())) {
                if (placeItemFromInventoryByUse(ladderSlot, supportPos, wallDir.getOpposite())) {
                    ladderPlacements++;
                }
                return;
            }
        }
    }

    private int findLadderInInventory() {
        return findFirstInventorySlot(Items.LADDER);
    }

}