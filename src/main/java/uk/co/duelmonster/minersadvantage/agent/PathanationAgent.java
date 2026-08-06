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
 * Pathanation worker that stamps a directional strip of dirt paths with configurable length and width.
 */
public class PathanationAgent extends Agent {
  private final BlockPos origin;
  private final Direction direction;
  private final int length;
  private final int pathWidth;
  private final Queue<BlockPos> queue = new LinkedList<>();
  private int placed = 0;
  private final int ticksPerBlock;
  private final int maxBlocksPerTick;

  /**
   * Convenience constructor using player facing and default configs.
   */
  public PathanationAgent(ServerPlayer player, BlockPos origin, int length) {
    this(player, origin, player.getDirection(), new PathanationConfig(true, Math.max(1, length), 3),
        new CommonConfig());
  }

  /**
   * Build a queued ribbon of candidate positions for path conversion.
   */
  public PathanationAgent(ServerPlayer player, BlockPos origin, Direction direction, PathanationConfig config,
      CommonConfig commonConfig) {
    super(player);
    this.origin = origin;
    this.direction = direction != null && direction.getAxis().isHorizontal() ? direction : player.getDirection();
    PathanationConfig effectiveConfig = config == null ? MAServerRootConfig.defaults().pathanation() : config;
    this.length = Math.max(1, effectiveConfig.targetBlockRange());
    this.pathWidth = Math.max(1, effectiveConfig.pathWidth());
    this.ticksPerBlock = commonConfig == null ? 1 : Math.max(1, commonConfig.ticksPerBlock());
    this.maxBlocksPerTick = commonConfig == null ? 1 : Math.max(1, commonConfig.maxBlocksPerTick());

    // Pre-seed queue with full footprint so tick loop stays simple and predictable.
    int halfWidth = pathWidth / 2;
    boolean alongZ = this.direction.getAxis() == Direction.Axis.Z;
    for (int i = 0; i < this.length; i++) {
      BlockPos base = origin.relative(this.direction, i);
      for (int offset = -halfWidth; offset <= halfWidth; offset++) {
        queue.add((alongZ ? base.offset(offset, 0, 0) : base.offset(0, 0, offset)).immutable());
      }
    }
  }

  /**
   * Constructor variant that accepts explicit config while still deriving direction from player facing.
   */
  public PathanationAgent(ServerPlayer player, BlockPos origin, PathanationConfig config, CommonConfig commonConfig) {
    this(player, origin, player.getDirection(), config, commonConfig);
  }

  /**
   * Per-tick path placement loop with blocks-per-tick and global block-limit guards.
   */
  @Override
  /**
   * t ic k exists so this path stays predictable and easier to debug when things get weird.
   */
  public boolean tick() {
    if (!shouldProcessThisTick(ticksPerBlock)) {
      return false;
    }

    int count = 0;
    while (!queue.isEmpty() && count < maxBlocksPerTick) {
      BlockPos pos = queue.poll();
      BlockState state = world.getBlockState(pos);
      // Only convert dirt-like surfaces with open/replaceable headspace above.
      if (state.is(BlockTags.DIRT) && isAirOrReplaceableAbove(pos)) {
        world.setBlockAndUpdate(pos, Blocks.DIRT_PATH.defaultBlockState());
        placed++;
        count++;
      }
    }
    int targetPlacements = length * pathWidth;
    // Finish when queue is consumed or target reached.
    if (queue.isEmpty() || placed >= targetPlacements) {
      return finish(queue.isEmpty() ? "path queue exhausted" : "path target reached");
    }
    return false;
  }
}
