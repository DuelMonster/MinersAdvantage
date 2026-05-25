package uk.co.duelmonster.minersadvantage.agent;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import uk.co.duelmonster.minersadvantage.common.config.CommonConfig;
import uk.co.duelmonster.minersadvantage.common.config.MAServerRootConfig;
import uk.co.duelmonster.minersadvantage.common.config.ShaftanationConfig;

import java.util.LinkedList;
import java.util.Queue;

/**
 * ShaftanationAgent: digs a vertical shaft down from the origin.
 */
public class ShaftanationAgent extends Agent {
    private final BlockPos origin;
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

    public ShaftanationAgent(ServerPlayer player, BlockPos origin, int depth) {
        this(player, origin, new ShaftanationConfig(true, Math.max(1, depth), 8), new CommonConfig());
    }

    public ShaftanationAgent(ServerPlayer player, BlockPos origin, ShaftanationConfig config, CommonConfig commonConfig) {
        super(player);
        this.origin = origin;
        this.config = config == null ? MAServerRootConfig.defaults().shaftanation() : config;
        this.targetDepth = Math.max(1, this.config.maxDepth());
        this.shaftWidth = Math.max(1, this.config.shaftWidth());
        this.shaftHeight = Math.max(1, this.config.shaftHeight());

        int globalBlocksPerTick = commonConfig == null ? 1 : Math.max(1, commonConfig.blocksPerTick());
        this.blocksPerTick = Math.max(1, Math.min(globalBlocksPerTick, this.config.processesPerTick()));
        this.blockLimit = commonConfig == null ? 64 : Math.max(1, commonConfig.blockLimit());
        this.autoIlluminate = commonConfig == null || commonConfig.autoIlluminate();

        int halfWidth = shaftWidth / 2;
        for (int depth = 1; depth <= targetDepth; depth++) {
            BlockPos base = origin.below(depth);
            for (int x = -halfWidth; x <= halfWidth; x++) {
                for (int y = 0; y < shaftHeight; y++) {
                    queue.add(base.offset(x, y, 0).immutable());
                }
            }

            if (autoIlluminate && depth % 5 == 0) {
                addTorchTargets(base, halfWidth);
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

        if (queue.isEmpty() || dug >= blockLimit) {
            return finish(queue.isEmpty() ? "shaft queue exhausted" : "shaft target reached");
        }
        return false;
    }

    private void addTorchTargets(BlockPos base, int halfWidth) {
        switch (config.torchPlacement()) {
            case FLOOR -> queue.add(base.above().immutable());
            case LEFT_WALL -> queue.add(base.offset(-halfWidth, 1, 0).immutable());
            case RIGHT_WALL -> queue.add(base.offset(halfWidth, 1, 0).immutable());
            case BOTH_WALLS -> {
                queue.add(base.offset(-halfWidth, 1, 0).immutable());
                queue.add(base.offset(halfWidth, 1, 0).immutable());
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

        return world.isEmptyBlock(pos)
            && (!world.isEmptyBlock(pos.below())
            || !world.isEmptyBlock(pos.north())
            || !world.isEmptyBlock(pos.south())
            || !world.isEmptyBlock(pos.east())
            || !world.isEmptyBlock(pos.west()));
    }
}