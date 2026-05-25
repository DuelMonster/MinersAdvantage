package uk.co.duelmonster.minersadvantage.agent;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import uk.co.duelmonster.minersadvantage.common.config.CommonConfig;
import uk.co.duelmonster.minersadvantage.common.config.MAServerRootConfig;
import uk.co.duelmonster.minersadvantage.common.config.ShaftanationConfig;

import java.util.LinkedList;
import java.util.Queue;

/**
 * ShaftanationAgent: digs a horizontal shaft in the player's facing direction.
 */
public class ShaftanationAgent extends Agent {
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
    private int dug = 0;
    private int torchPlacements = 0;

    private record WallTorchJob(BlockPos pos, Direction facing) {}
    private final Queue<WallTorchJob> wallTorchQueue = new LinkedList<>();

    public ShaftanationAgent(ServerPlayer player, BlockPos origin, int depth) {
        this(player, origin, player.getDirection(), new ShaftanationConfig(true, Math.max(1, depth), 8), new CommonConfig());
    }

    public ShaftanationAgent(ServerPlayer player, BlockPos origin, ShaftanationConfig config, CommonConfig commonConfig) {
        this(player, origin, player.getDirection(), config, commonConfig);
    }

    public ShaftanationAgent(ServerPlayer player, BlockPos origin, Direction direction, ShaftanationConfig config, CommonConfig commonConfig) {
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

        int halfWidth = shaftWidth / 2;
        boolean alongZ = this.direction.getAxis() == Direction.Axis.Z;
        for (int depth = 1; depth <= targetDepth; depth++) {
            BlockPos base = origin.relative(this.direction, depth).below();
            for (int w = -halfWidth; w <= halfWidth; w++) {
                for (int h = 0; h < shaftHeight; h++) {
                    queue.add((alongZ ? base.offset(w, h, 0) : base.offset(0, h, w)).immutable());
                }
            }
            if (autoIlluminate && depth % 5 == 0) {
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
                dug++;
                count++;
            } else if (canPlaceTorch(pos)) {
                world.setBlockAndUpdate(pos, Blocks.TORCH.defaultBlockState());
                torchPlacements++;
            }
        }

        WallTorchJob wallJob;
        while ((wallJob = wallTorchQueue.peek()) != null && world.isEmptyBlock(wallJob.pos())) {
            wallTorchQueue.poll();
            world.setBlockAndUpdate(wallJob.pos(), Blocks.WALL_TORCH.defaultBlockState()
                .setValue(WallTorchBlock.FACING, wallJob.facing()));
            torchPlacements++;
        }

        if ((queue.isEmpty() && wallTorchQueue.isEmpty()) || dug >= blockLimit) {
            return finish(queue.isEmpty() && wallTorchQueue.isEmpty() ? "shaft queue exhausted" : "shaft target reached");
        }
        return false;
    }

    private void addTorchTargets(BlockPos base, int halfWidth, boolean alongZ) {
        switch (config.torchPlacement()) {
            case FLOOR -> queue.add(base.immutable());
            case LEFT_WALL -> wallTorchQueue.add(new WallTorchJob(
                (alongZ ? base.offset(-halfWidth, 1, 0) : base.offset(0, 1, -halfWidth)).immutable(),
                alongZ ? Direction.EAST : Direction.SOUTH
            ));
            case RIGHT_WALL -> wallTorchQueue.add(new WallTorchJob(
                (alongZ ? base.offset(halfWidth, 1, 0) : base.offset(0, 1, halfWidth)).immutable(),
                alongZ ? Direction.WEST : Direction.NORTH
            ));
            case BOTH_WALLS -> {
                wallTorchQueue.add(new WallTorchJob(
                    (alongZ ? base.offset(-halfWidth, 1, 0) : base.offset(0, 1, -halfWidth)).immutable(),
                    alongZ ? Direction.EAST : Direction.SOUTH
                ));
                wallTorchQueue.add(new WallTorchJob(
                    (alongZ ? base.offset(halfWidth, 1, 0) : base.offset(0, 1, halfWidth)).immutable(),
                    alongZ ? Direction.WEST : Direction.NORTH
                ));
            }
            default -> {
            }
        }
    }

    private boolean canPlaceTorch(BlockPos pos) {
        if (!autoIlluminate) {
            return false;
        }
        if (config.torchPlacement() == null) {
            return false;
        }

        return world.isEmptyBlock(pos) && !world.isEmptyBlock(pos.below());
    }
}