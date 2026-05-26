package uk.co.duelmonster.minersadvantage.agent;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import uk.co.duelmonster.minersadvantage.common.config.CommonConfig;
import uk.co.duelmonster.minersadvantage.common.config.MAServerRootConfig;
import uk.co.duelmonster.minersadvantage.common.config.PathanationConfig;

import java.util.LinkedList;
import java.util.Queue;

/**
 * PathanationAgent: creates a path (dirt path blocks) in a line from the player.
 */
public class PathanationAgent extends Agent {
    private final BlockPos origin;
    private final Direction direction;
    private final int length;
    private final int pathWidth;
    private final Queue<BlockPos> queue = new LinkedList<>();
    private int placed = 0;
    private final int blocksPerTick;
    private final int blockLimit;

    public PathanationAgent(ServerPlayer player, BlockPos origin, int length) {
        this(player, origin, player.getDirection(), new PathanationConfig(true, Math.max(1, length), 3), new CommonConfig());
    }

    public PathanationAgent(ServerPlayer player, BlockPos origin, Direction direction, PathanationConfig config, CommonConfig commonConfig) {
        super(player);
        this.origin = origin;
        this.direction = direction != null && direction.getAxis().isHorizontal() ? direction : player.getDirection();
        PathanationConfig effectiveConfig = config == null ? MAServerRootConfig.defaults().pathanation() : config;
        this.length = Math.max(1, effectiveConfig.targetBlockRange());
        this.pathWidth = Math.max(1, effectiveConfig.pathWidth());
        this.blocksPerTick = commonConfig == null ? 1 : Math.max(1, commonConfig.blocksPerTick());
        this.blockLimit = commonConfig == null ? 64 : Math.max(1, commonConfig.blockLimit());

        int halfWidth = pathWidth / 2;
        boolean alongZ = this.direction.getAxis() == Direction.Axis.Z;
        for (int i = 0; i < this.length; i++) {
            BlockPos base = origin.relative(this.direction, i);
            for (int offset = -halfWidth; offset <= halfWidth; offset++) {
                queue.add((alongZ ? base.offset(offset, 0, 0) : base.offset(0, 0, offset)).immutable());
            }
        }
    }

    public PathanationAgent(ServerPlayer player, BlockPos origin, PathanationConfig config, CommonConfig commonConfig) {
        this(player, origin, player.getDirection(), config, commonConfig);
    }

    @Override
    public boolean tick() {
        int count = 0;
        while (!queue.isEmpty() && count < blocksPerTick && placed < blockLimit) {
            BlockPos pos = queue.poll();
            BlockState state = world.getBlockState(pos);
            if (state.is(BlockTags.DIRT) && isAirOrReplaceableAbove(pos)) {
                world.setBlockAndUpdate(pos, Blocks.DIRT_PATH.defaultBlockState());
                placed++;
                count++;
            }
        }
        int targetPlacements = length * pathWidth;
        if (queue.isEmpty() || placed >= targetPlacements || placed >= blockLimit) {
            return finish(queue.isEmpty() ? "path queue exhausted" : placed >= blockLimit ? "path block limit reached" : "path target reached");
        }
        return false;
    }
}