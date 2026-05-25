package uk.co.duelmonster.minersadvantage.agent;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import uk.co.duelmonster.minersadvantage.common.config.CommonConfig;
import uk.co.duelmonster.minersadvantage.common.config.MAServerRootConfig;
import uk.co.duelmonster.minersadvantage.common.config.VentilationConfig;

import java.util.LinkedList;
import java.util.Queue;

/**
 * VentilationAgent: digs a horizontal tunnel for ventilation.
 */
public class VentilationAgent extends Agent {
    private final BlockPos origin;
    private final VentilationConfig config;
    private final int horizontalRange;
    private final int verticalRange;
    private final Queue<BlockPos> queue = new LinkedList<>();
    private int dug = 0;
    private final int blocksPerTick;
    private final int blockLimit;
    private int ladderPlacements = 0;

    public VentilationAgent(ServerPlayer player, BlockPos origin, int length) {
        this(player, origin, new VentilationConfig(true, Math.max(1, length), 1, 8), new CommonConfig());
    }

    public VentilationAgent(ServerPlayer player, BlockPos origin, VentilationConfig config, CommonConfig commonConfig) {
        super(player);
        this.origin = origin;
        this.config = config == null ? MAServerRootConfig.defaults().ventilation() : config;
        this.horizontalRange = Math.max(1, this.config.radiusHorizontal());
        this.verticalRange = Math.max(0, this.config.radiusVertical());

        int globalBlocksPerTick = commonConfig == null ? 1 : Math.max(1, commonConfig.blocksPerTick());
        this.blocksPerTick = Math.max(1, Math.min(globalBlocksPerTick, this.config.processesPerTick()));
        this.blockLimit = commonConfig == null ? 64 : Math.max(1, commonConfig.blockLimit());

        for (int x = 1; x <= horizontalRange; x++) {
            for (int y = -verticalRange; y <= verticalRange; y++) {
                queue.add(origin.offset(x, y, 0).immutable());
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

                if (config.placeLadders() && dug % 3 == 0 && world.isEmptyBlock(pos)) {
                    BlockPos anchor = pos.west();
                    if (!world.isEmptyBlock(anchor)) {
                        world.setBlockAndUpdate(pos, Blocks.LADDER.defaultBlockState());
                        ladderPlacements++;
                    }
                }
            }
        }

        if (queue.isEmpty() || dug >= blockLimit) {
            return finish(queue.isEmpty() ? "ventilation queue exhausted" : "ventilation target reached");
        }
        return false;
    }
}