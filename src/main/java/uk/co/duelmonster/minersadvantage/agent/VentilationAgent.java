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
 * Vent-shaft worker that carves vertical columns and optionally ladders/torch support for downward runs.
 */
public class VentilationAgent extends Agent {
    private final BlockPos origin;
    private final Direction direction;
    private final VentilationConfig config;
    private final Queue<BlockPos> queue = new LinkedList<>();
    private final Deque<BlockPos> ladderQueue = new LinkedList<>();
    private int dug = 0;
    private final int blocksPerTick;
    private final boolean mineVeins;
    private final CommonConfig commonConfig;
    private final VeinationRuntimeService veinationRuntime;
    private final VeinationConfig veinationConfig;
    private ItemStack veinationTriggerTool;
    private int ladderPlacements = 0;
    private BlockPos lowestDugPos;
    private boolean bottomTorchProcessed = false;

    /**
     * Convenience constructor with downward direction and default config.
     */
    public VentilationAgent(ServerPlayer player, BlockPos origin, int length) {
        this(player, origin, Direction.DOWN, new VentilationConfig(true, 1, Math.max(1, length), 1, 8), new CommonConfig());
    }

    /**
     * Convenience constructor with explicit ventilation/common config.
     */
    public VentilationAgent(ServerPlayer player, BlockPos origin, VentilationConfig config, CommonConfig commonConfig) {
        this(player, origin, Direction.DOWN, config, commonConfig);
    }

    /**
     * Constructor with explicit direction and default veination wiring.
     */
    public VentilationAgent(ServerPlayer player, BlockPos origin, Direction direction, VentilationConfig config, CommonConfig commonConfig) {
        this(player, origin, direction, config, commonConfig, null, null);
    }

    /**
     * Constructor with optional veination runtime service.
     */
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

    /**
     * Full constructor that seeds dig queue and optional ladder queue.
     */
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
        this.mineVeins = commonConfig == null || commonConfig.mineVeins();
        this.commonConfig = commonConfig;
        this.veinationRuntime = veinationRuntime;
        this.veinationConfig = veinationConfig;
        this.veinationTriggerTool = veinationTriggerTool == null ? ItemStack.EMPTY : veinationTriggerTool.copy();

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        for (int depth = 1; depth <= ventDepth; depth++) {
            queue.add(origin.relative(this.direction, depth).immutable());
        }

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (this.direction == Direction.DOWN && this.config.placeLadders()) {
            ladderQueue.addLast(origin.immutable());
        }
    }

    /**
     * Per-tick ventilation loop with carve phase then ladder/torch placement phase.
     */
    @Override
    /**
     * t ic k exists so this path stays predictable and easier to debug when things get weird.
     */
    public boolean tick() {
        int count = 0;
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        while (!queue.isEmpty() && count < blocksPerTick) {
            BlockPos pos = queue.poll();
            BlockState state = world.getBlockState(pos);
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (!state.isAir()) {
                BreakOutcome breakOutcome = breakBlockWithTool(pos, veinationTriggerTool);
                if (breakOutcome.broken()) {
                    veinationTriggerTool = breakOutcome.toolAfterBreak().copy();
                    maybeFanOutVeination(pos, state, mineVeins, this.commonConfig, veinationRuntime, veinationConfig, veinationTriggerTool);
                    dug++;
                    count++;

                    // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                    if (direction == Direction.DOWN) {
                        lowestDugPos = pos.immutable();
                        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                        if (config.placeLadders()) {
                            // Prepended so ladder placement runs from far-to-near once digging completes.
                            ladderQueue.addFirst(pos.immutable());
                        }
                    }
                }
            }
        }

        boolean diggingComplete = queue.isEmpty();
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (diggingComplete) {
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (direction == Direction.DOWN && !bottomTorchProcessed && count < blocksPerTick) {
                bottomTorchProcessed = true;
                // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                if (lowestDugPos != null) {
                    ladderQueue.remove(lowestDugPos);
                    placeTorchWithInventory(lowestDugPos, Direction.UP);
                }
                count++;
            }

            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            while (count < blocksPerTick && !ladderQueue.isEmpty()) {
                BlockPos ladderPos = ladderQueue.pollFirst();
                tryPlaceLadder(ladderPos);
                count++;
            }
        }

        boolean placementComplete = ladderQueue.isEmpty() && (direction != Direction.DOWN || bottomTorchProcessed);
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (diggingComplete && placementComplete) {
            return finish(queue.isEmpty() ? "ventilation queue exhausted" : "ventilation target reached");
        }
        return false;
    }

    /**
     * Attempt ladder placement at position using available inventory ladders.
     */
    private void tryPlaceLadder(BlockPos pos) {
        int ladderSlot = findLadderInInventory();
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (ladderSlot < 0) {
            return;
        }
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (!world.getBlockState(pos).canBeReplaced()) {
            return;
        }

        Direction[] sides = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        for (Direction wallDir : sides) {
            BlockPos supportPos = pos.relative(wallDir);
            BlockState supportState = world.getBlockState(supportPos);
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (supportState.isFaceSturdy(world, supportPos, wallDir.getOpposite())) {
                // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                if (placeItemFromInventoryByUse(ladderSlot, supportPos, wallDir.getOpposite())) {
                    ladderPlacements++;
                }
                return;
            }
        }
    }

    /**
     * Find first ladder slot in player inventory.
     */
    private int findLadderInInventory() {
        return findFirstInventorySlot(Items.LADDER);
    }

}
