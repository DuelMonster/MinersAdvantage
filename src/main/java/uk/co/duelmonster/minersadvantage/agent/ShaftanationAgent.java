package uk.co.duelmonster.minersadvantage.agent;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import uk.co.duelmonster.minersadvantage.common.config.CommonConfig;
import uk.co.duelmonster.minersadvantage.common.config.MAServerRootConfig;
import uk.co.duelmonster.minersadvantage.common.config.ShaftanationConfig;
import uk.co.duelmonster.minersadvantage.common.config.VeinationConfig;
import uk.co.duelmonster.minersadvantage.common.log.LogUtils;
import uk.co.duelmonster.minersadvantage.common.registry.RegistryPredicates;
import uk.co.duelmonster.minersadvantage.common.services.utility.VeinationRuntimeService;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Queue;

/**
 * ShaftanationAgent: digs a horizontal shaft in the player's facing direction.
 */
public class ShaftanationAgent extends Agent {
    private static final int MAX_TORCH_LIGHT_WAIT_TICKS = 40;

    private final BlockPos origin;
    private final Direction direction;
    private final ShaftanationConfig config;
    private final Queue<BlockPos> queue = new LinkedList<>();
    private final int targetDepth;
    private final int shaftWidth;
    private final int shaftHeight;
    private final int blocksPerTick;
    private final int blockLimit;
    private final boolean autoIlluminate;
    private final int torchLowestLightLevel;
    private final boolean mineVeins;
    private final VeinationRuntimeService veinationRuntime;
    private final VeinationConfig veinationConfig;
    private final ItemStack veinationTriggerTool;
    private int dug = 0;
    private int torchPlacements = 0;
    private int torchLightWaitTicks = 0;

    private record TorchJob(BlockPos pos, Direction facing) {}
    private enum TorchPlacementDecision {
        PLACE,
        WAIT_FOR_LIGHT,
        DISCARD
    }
    private final Deque<TorchJob> torchQueue = new LinkedList<>();

    public ShaftanationAgent(ServerPlayer player, BlockPos origin, int depth) {
        this(
            player,
            origin,
            player.getDirection(),
            new ShaftanationConfig(true, Math.max(1, depth), 8),
            new CommonConfig(),
            MAServerRootConfig.defaults().illumination().lowestLightLevel()
        );
    }

    public ShaftanationAgent(ServerPlayer player, BlockPos origin, ShaftanationConfig config, CommonConfig commonConfig) {
        this(player, origin, player.getDirection(), config, commonConfig, MAServerRootConfig.defaults().illumination().lowestLightLevel());
    }

    public ShaftanationAgent(ServerPlayer player, BlockPos origin, Direction direction, ShaftanationConfig config, CommonConfig commonConfig) {
        this(player, origin, direction, config, commonConfig, MAServerRootConfig.defaults().illumination().lowestLightLevel());
    }

    public ShaftanationAgent(ServerPlayer player, BlockPos origin, Direction direction, ShaftanationConfig config, CommonConfig commonConfig, int torchLowestLightLevel) {
        this(player, origin, direction, config, commonConfig, torchLowestLightLevel, null, null);
    }

    public ShaftanationAgent(
        ServerPlayer player,
        BlockPos origin,
        Direction direction,
        ShaftanationConfig config,
        CommonConfig commonConfig,
        int torchLowestLightLevel,
        VeinationRuntimeService veinationRuntime,
        VeinationConfig veinationConfig
    ) {
        this(player, origin, direction, config, commonConfig, torchLowestLightLevel, veinationRuntime, veinationConfig, ItemStack.EMPTY);
    }

    public ShaftanationAgent(
        ServerPlayer player,
        BlockPos origin,
        Direction direction,
        ShaftanationConfig config,
        CommonConfig commonConfig,
        int torchLowestLightLevel,
        VeinationRuntimeService veinationRuntime,
        VeinationConfig veinationConfig,
        ItemStack veinationTriggerTool
    ) {
        super(player);
        this.origin = origin;
        this.direction = direction != null && direction.getAxis().isHorizontal() ? direction : player.getDirection();
        this.config = config == null ? MAServerRootConfig.defaults().shaftanation() : config;
        this.targetDepth = Math.max(1, this.config.maxDepth());
        this.shaftWidth = Math.max(1, this.config.shaftWidth());
        this.shaftHeight = Math.max(1, this.config.shaftHeight());

        int globalBlocksPerTick = commonConfig == null ? 1 : Math.max(1, commonConfig.blocksPerTick());
        this.blocksPerTick = Math.max(1, Math.min(globalBlocksPerTick, this.config.processesPerTick()));
        this.blockLimit = commonConfig == null ? 64 : Math.max(1, commonConfig.blockLimit());
        this.autoIlluminate = commonConfig == null || commonConfig.autoIlluminate();
        this.torchLowestLightLevel = Math.max(0, torchLowestLightLevel);
        this.mineVeins = commonConfig == null || commonConfig.mineVeins();
        this.veinationRuntime = veinationRuntime;
        this.veinationConfig = veinationConfig;
        this.veinationTriggerTool = veinationTriggerTool == null ? ItemStack.EMPTY : veinationTriggerTool.copy();

        int halfWidth = shaftWidth / 2;
        boolean alongZ = this.direction.getAxis() == Direction.Axis.Z;
        BlockPos floorOrigin = new BlockPos(origin.getX(), player.blockPosition().getY(), origin.getZ());
        for (int depth = 0; depth < targetDepth; depth++) {
            BlockPos base = floorOrigin.relative(this.direction, depth);
            for (int w = -halfWidth; w <= halfWidth; w++) {
                for (int h = 0; h < shaftHeight; h++) {
                    queue.add((alongZ ? base.offset(w, h, 0) : base.offset(0, h, w)).immutable());
                }
            }
            if (autoIlluminate && depth > 0) {
                addTorchTargets(base, halfWidth, alongZ);
            }
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
                maybeFanOutVeination(pos, state);
                dug++;
                count++;
            }
        }

        if (queue.isEmpty()) {
            boolean madeProgress = false;
            int scanBudget = torchQueue.size();
            while (scanBudget-- > 0 && !torchQueue.isEmpty()) {
                TorchJob torchJob = torchQueue.peekFirst();
                TorchPlacementDecision decision = evaluateTorchPlacement(torchJob);
                if (decision == TorchPlacementDecision.PLACE) {
                    torchQueue.pollFirst();
                    if (playerHasTorches()) {
                        placeTorch(torchJob);
                        madeProgress = true;
                    } else {
                        LogUtils.logDebug("Shaft torch skipped: no torches in inventory player={} pos={}", player.getScoreboardName(), torchJob.pos());
                        torchQueue.clear();
                    }
                    break;
                }
                if (decision == TorchPlacementDecision.DISCARD) {
                    torchQueue.pollFirst();
                    LogUtils.logDebug("Discarded shaft torch job player={} pos={} facing={} reason=unplaceable", player.getScoreboardName(), torchJob.pos(), torchJob.facing());
                    madeProgress = true;
                    continue;
                }

                // Defer bright candidates so darker ones deeper in the queue can be considered this tick.
                torchQueue.addLast(torchQueue.pollFirst());
            }

            if (madeProgress) {
                torchLightWaitTicks = 0;
            } else if (!torchQueue.isEmpty()) {
                torchLightWaitTicks++;
                if (torchLightWaitTicks > MAX_TORCH_LIGHT_WAIT_TICKS) {
                    int dropped = torchQueue.size();
                    TorchJob head = torchQueue.peekFirst();
                    torchQueue.clear();
                    LogUtils.logDebug(
                        "Discarded stalled shaft torch queue player={} headPos={} headFacing={} reason=light_wait_timeout waitedTicks={} threshold={} droppedJobs={}",
                        player.getScoreboardName(),
                        head == null ? null : head.pos(),
                        head == null ? null : head.facing(),
                        torchLightWaitTicks,
                        torchLowestLightLevel,
                        dropped
                    );
                    torchLightWaitTicks = 0;
                }
            }
        }

        if ((queue.isEmpty() && torchQueue.isEmpty()) || dug >= blockLimit) {
            return finish(queue.isEmpty() && torchQueue.isEmpty() ? "shaft queue exhausted" : "shaft target reached");
        }
        return false;
    }

    private void addTorchTargets(BlockPos base, int halfWidth, boolean alongZ) {
        switch (config.torchPlacement()) {
            case FLOOR -> enqueueTorchJob(new TorchJob(base.immutable(), null));
            case LEFT_WALL -> enqueueTorchJob(new TorchJob(
                (alongZ ? base.offset(-halfWidth, 1, 0) : base.offset(0, 1, -halfWidth)).immutable(),
                alongZ ? Direction.EAST : Direction.SOUTH
            ));
            case RIGHT_WALL -> enqueueTorchJob(new TorchJob(
                (alongZ ? base.offset(halfWidth, 1, 0) : base.offset(0, 1, halfWidth)).immutable(),
                alongZ ? Direction.WEST : Direction.NORTH
            ));
            case BOTH_WALLS -> {
                enqueueTorchJob(new TorchJob(
                    (alongZ ? base.offset(-halfWidth, 1, 0) : base.offset(0, 1, -halfWidth)).immutable(),
                    alongZ ? Direction.EAST : Direction.SOUTH
                ));
                enqueueTorchJob(new TorchJob(
                    (alongZ ? base.offset(halfWidth, 1, 0) : base.offset(0, 1, halfWidth)).immutable(),
                    alongZ ? Direction.WEST : Direction.NORTH
                ));
            }
            default -> {
            }
        }
    }

    private void enqueueTorchJob(TorchJob job) {
        // Torch jobs are prepended so placement runs from far-to-near after carving completes.
        torchQueue.addFirst(job);
    }

    private TorchPlacementDecision evaluateTorchPlacement(TorchJob torchJob) {
        if (!autoIlluminate) {
            return TorchPlacementDecision.DISCARD;
        }
        if (config.torchPlacement() == null) {
            return TorchPlacementDecision.DISCARD;
        }

        BlockPos pos = torchJob.pos();
        if (!world.isEmptyBlock(pos)) {
            return TorchPlacementDecision.DISCARD;
        }

        int lightLevel = effectiveTorchLight(pos);
        if (lightLevel > torchLowestLightLevel) {
            return TorchPlacementDecision.WAIT_FOR_LIGHT;
        }

        if (torchJob.facing() == null) {
            return world.isEmptyBlock(pos.below()) ? TorchPlacementDecision.DISCARD : TorchPlacementDecision.PLACE;
        }

        return world.isEmptyBlock(pos.relative(torchJob.facing().getOpposite()))
            ? TorchPlacementDecision.DISCARD
            : TorchPlacementDecision.PLACE;
    }

    private void placeTorch(TorchJob torchJob) {
        if (placeTorchWithInventory(torchJob.pos(), torchJob.facing())) {
            torchPlacements++;
        }
    }

    private int effectiveTorchLight(BlockPos pos) {
        int atTorch = world.getBrightness(LightLayer.BLOCK, pos);
        int aboveTorch = world.getBrightness(LightLayer.BLOCK, pos.above());
        return Math.min(atTorch, aboveTorch);
    }

    private void maybeFanOutVeination(BlockPos pos, BlockState brokenState) {
        if (!mineVeins || veinationRuntime == null || veinationConfig == null || !veinationConfig.enabled()) {
            return;
        }

        ItemStack toolStack = veinationTriggerTool.isEmpty() ? player.getMainHandItem() : veinationTriggerTool;

        if (!RegistryPredicates.isPickaxeTool(toolStack)) {
            return;
        }

        if (!veinationRuntime.isPickaxeAllowed(world, veinationConfig, toolStack)) {
            return;
        }

        if (!veinationRuntime.isOreAllowed(veinationConfig, brokenState)) {
            return;
        }

        AgentManager agentManager = AgentManager.get();
        veinationRuntime.registerDropAnchor(player, pos, veinationConfig);
        if (!agentManager.hasAgentType(player, VeinationAgent.class)) {
            agentManager.addAgent(player, new VeinationAgent(player, pos, brokenState, veinationRuntime, veinationConfig));
        }
    }
}