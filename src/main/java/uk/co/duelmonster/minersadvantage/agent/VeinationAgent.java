package uk.co.duelmonster.minersadvantage.agent;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import uk.co.duelmonster.minersadvantage.common.config.CommonConfig;
import uk.co.duelmonster.minersadvantage.common.config.VeinationConfig;
import uk.co.duelmonster.minersadvantage.common.services.utility.VeinationCoreService;
import uk.co.duelmonster.minersadvantage.common.services.utility.VeinationRuntimeService;

import java.util.LinkedList;
import java.util.Queue;

/**
 * VeinationAgent: breaks all connected ore blocks (vein mining).
 */
public class VeinationAgent extends Agent {
  private static final int MIN_DISCOVERY_BATCH_SIZE = 8;

  private final Queue<BlockPos> queue = new LinkedList<>();
  private final VeinationCoreService.VeinDiscoveryCursor discoveryCursor;
  private final VeinationRuntimeService runtime;
  private final VeinationConfig config;
  private final int blocksPerTick;
  private final int discoveryBatchSize;
  private ItemStack breakTool;

  /**
   * v ei na ti on ag en t exists so this path stays predictable and easier to debug when things get weird.
   */
  public VeinationAgent(ServerPlayer player, BlockPos origin, CommonConfig commonConfig,
      VeinationRuntimeService runtime, VeinationConfig config) {
    this(player, origin, null, commonConfig, runtime, config, ItemStack.EMPTY);
  }

  /**
   * v ei na ti on ag en t exists so this path stays predictable and easier to debug when things get weird.
   */
  public VeinationAgent(ServerPlayer player, BlockPos origin, BlockState originStateHint, CommonConfig commonConfig,
      VeinationRuntimeService runtime, VeinationConfig config) {
    this(player, origin, originStateHint, commonConfig, runtime, config, ItemStack.EMPTY);
  }

  /**
   * v ei na ti on ag en t exists so this path stays predictable and easier to debug when things get weird.
   */
  public VeinationAgent(
      ServerPlayer player,
      BlockPos origin,
      BlockState originStateHint,
      CommonConfig commonConfig,
      VeinationRuntimeService runtime,
      VeinationConfig config,
      ItemStack breakTool) {
    super(player);
    this.runtime = runtime;
    this.config = config;
    this.blocksPerTick = commonConfig == null ? 1 : Math.max(1, commonConfig.blocksPerTick());
    this.discoveryBatchSize = Math.max(MIN_DISCOVERY_BATCH_SIZE, this.blocksPerTick * 2);
    this.breakTool = breakTool == null ? ItemStack.EMPTY : breakTool.copy();
    this.discoveryCursor = runtime.beginVeinDiscovery(world, origin, originStateHint, config);
    queue.addAll(discoveryCursor.drainNext(discoveryBatchSize));
  }

  @Override
  /**
   * t ic k exists so this path stays predictable and easier to debug when things get weird.
   */
  public boolean tick() {
    if (queue.isEmpty() && !discoveryCursor.isComplete()) {
      queue.addAll(discoveryCursor.drainNext(discoveryBatchSize));
    }

    int count = 0;
    while (!queue.isEmpty() && count < blocksPerTick) {
      BlockPos pos = queue.poll();
      if (world.getBlockState(pos).isAir()) {
        continue;
      }

      BreakOutcome breakOutcome = breakBlockWithTool(pos, breakTool);
      if (breakOutcome.broken()) {
        breakTool = breakOutcome.toolAfterBreak().copy();
        count++;
      }

      if (queue.isEmpty() && !discoveryCursor.isComplete() && count < blocksPerTick) {
        queue.addAll(discoveryCursor.drainNext(discoveryBatchSize));
      }
    }

    if (queue.isEmpty() && discoveryCursor.isComplete()) {
      return finish("vein queue exhausted");
    }
    return false;
  }
}
