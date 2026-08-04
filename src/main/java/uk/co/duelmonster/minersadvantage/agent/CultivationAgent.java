package uk.co.duelmonster.minersadvantage.agent;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import uk.co.duelmonster.minersadvantage.common.config.CommonConfig;
import uk.co.duelmonster.minersadvantage.common.config.CultivationConfig;
import uk.co.duelmonster.minersadvantage.common.config.MAServerRootConfig;
import uk.co.duelmonster.minersadvantage.common.registry.RegistryPredicates;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * Cultivation worker that expands a hydrated farm patch, converts dirt-like blocks to farmland,
 * and nudges block updates so plants above freshly tilled soil behave correctly.
 */
public class CultivationAgent extends Agent {
  private static final int FLOATING_UPDATE_DELAY_TICKS = 2;

  private final BlockPos origin;
  private final int hydrationDistance;
  private final int minX;
  private final int maxX;
  private final int minZ;
  private final int maxZ;
  private final Queue<BlockPos> queue = new LinkedList<>();
  private final Queue<DelayedUpdate> delayedUpdates = new LinkedList<>();
  private final Set<BlockPos> visited = new HashSet<>();
  private final int blocksPerTick;

  /**
   * Convenience constructor using default server/common config values.
   */
  public CultivationAgent(ServerPlayer player, BlockPos origin, int radius) {
    this(player, origin, radius, MAServerRootConfig.defaults().cultivation(), new CommonConfig());
  }

  /**
   * Build cultivation patch bounds around nearest water source and initialize traversal queue.
   */
  public CultivationAgent(ServerPlayer player, BlockPos origin, int radius, CultivationConfig config,
      CommonConfig commonConfig) {
    super(player);
    this.origin = origin;
    this.hydrationDistance = resolveHydrationDistance(config, radius);
    BlockPos waterSource = findClosestWaterSource(origin, this.hydrationDistance);
    BlockPos patchCenter = waterSource == null ? origin : waterSource;
    int patchRadius = waterSource == null ? 0 : this.hydrationDistance;
    this.minX = patchCenter.getX() - patchRadius;
    this.maxX = patchCenter.getX() + patchRadius;
    this.minZ = patchCenter.getZ() - patchRadius;
    this.maxZ = patchCenter.getZ() + patchRadius;
    int globalBlocksPerTick = commonConfig == null ? 1 : Math.max(1, commonConfig.blocksPerTick());
    this.blocksPerTick = globalBlocksPerTick;
    queue.add(origin);
  }

  /**
   * Resolve the effective hydration distance from config or legacy fallback radius.
   */
  static int resolveHydrationDistance(CultivationConfig config, int fallbackRadius) {
    if (config == null) {
      return Math.max(1, fallbackRadius);
    }
    return Math.max(0, config.hydrationDistance());
  }

  /**
   * Per-tick cultivation loop with bounded queue processing and delayed update flushing.
   */
  @Override
  /**
   * t ic k exists so this path stays predictable and easier to debug when things get weird.
   */
  public boolean tick() {
    int count = 0;
    while (!queue.isEmpty() && count < blocksPerTick) {
      BlockPos pos = queue.poll();
      if (pos == null || !visited.add(pos) || !withinFarmPatch(pos)) {
        continue;
      }

      BlockState state = world.getBlockState(pos);
      if (RegistryPredicates.isDirtLike(state)) {
        if (isAirOrReplaceableAbove(pos)) {
          // Clear floating replaceables, till the block, then schedule follow-up updates for stability.
          clearReplaceableBlockAbove(pos);
          world.setBlockAndUpdate(pos, Blocks.FARMLAND.defaultBlockState());
          scheduleFloatingUpdate(pos.above());
        }
        count++;
        // Add neighbors in a 3x3 area
        for (int dx = -1; dx <= 1; dx++)
          for (int dz = -1; dz <= 1; dz++)
            queue.add(pos.offset(dx, 0, dz));
      }
    }

    processDelayedUpdates();

    if (queue.isEmpty() && delayedUpdates.isEmpty()) {
      return finish("cultivation queue exhausted");
    }
    return false;
  }

  /**
   * Queue delayed update for one position so neighbor state settles after tilling.
   */
  private void scheduleFloatingUpdate(BlockPos pos) {
    delayedUpdates.add(new DelayedUpdate(pos.immutable(), FLOATING_UPDATE_DELAY_TICKS));
  }

  /**
   * Process delayed block updates and trigger neighbor notifications once countdown expires.
   */
  private void processDelayedUpdates() {
    int pending = delayedUpdates.size();
    for (int i = 0; i < pending; i++) {
      DelayedUpdate update = delayedUpdates.poll();
      if (update == null) {
        continue;
      }
      if (update.ticksRemaining() > 0) {
        delayedUpdates.add(new DelayedUpdate(update.pos(), update.ticksRemaining() - 1));
        continue;
      }

      BlockPos pos = update.pos();
      BlockState state = world.getBlockState(pos);
      world.sendBlockUpdated(pos, state, state, 3);
      world.updateNeighborsAt(pos, state.getBlock());

      BlockPos belowPos = pos.below();
      BlockState belowState = world.getBlockState(belowPos);
      world.sendBlockUpdated(belowPos, belowState, belowState, 3);
      world.updateNeighborsAt(belowPos, belowState.getBlock());

      if (!state.isAir()) {
        world.scheduleTick(pos, state.getBlock(), 1);
      }
    }
  }

  /**
   * Keep cultivation traversal constrained to the selected farm patch at origin Y.
   */
  private boolean withinFarmPatch(BlockPos pos) {
    return pos.getY() == origin.getY()
        && pos.getX() >= minX
        && pos.getX() <= maxX
        && pos.getZ() >= minZ
        && pos.getZ() <= maxZ;
  }

  /**
   * Break replaceable block above target soil so farmland conversion has breathing room.
   */
  private void clearReplaceableBlockAbove(BlockPos pos) {
    BlockPos abovePos = pos.above();
    BlockState above = world.getBlockState(abovePos);
    if (!above.isAir() && above.canBeReplaced()) {
      breakBlockWithTool(abovePos, ItemStack.EMPTY);
    }
  }

  /**
   * Search outward in square rings for nearest water (or waterlogged) block.
   */
  private BlockPos findClosestWaterSource(BlockPos start, int maxDistance) {
    for (int offset = 1; offset <= maxDistance; offset++) {
      for (int x = start.getX() - offset; x <= start.getX() + offset; x++) {
        for (int z = start.getZ() - offset; z <= start.getZ() + offset; z++) {
          BlockPos candidate = new BlockPos(x, start.getY(), z);
          BlockState state = world.getBlockState(candidate);
          if (state.getFluidState().is(Fluids.WATER)
              || (state.hasProperty(BlockStateProperties.WATERLOGGED)
                  && state.getValue(BlockStateProperties.WATERLOGGED))) {
            return candidate;
          }
        }
      }
    }
    return null;
  }

  /**
   * Small delayed-update payload for post-till block/neighbor refresh scheduling.
   */
  private record DelayedUpdate(BlockPos pos, int ticksRemaining) {
  }
}
