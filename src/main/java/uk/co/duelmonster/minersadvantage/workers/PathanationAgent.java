package uk.co.duelmonster.minersadvantage.workers;

import java.util.concurrent.TimeUnit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import uk.co.duelmonster.minersadvantage.common.Constants;
import uk.co.duelmonster.minersadvantage.common.Functions;
import uk.co.duelmonster.minersadvantage.network.packets.PacketPathanate;

public class PathanationAgent extends Agent {

  public PathanationAgent(ServerPlayer player, PacketPathanate pkt) {
    super(player, pkt);

    this.originPos = pkt.pos;

    setupPath();

    addConnectedToQueue(originPos);
  }

  // Returns true when Pathanation is complete or cancelled
  @Override
  public boolean tick() {
    if (originPos == null || player == null || !player.isAlive() || processed.size() >= clientConfig.common.blockLimit)
      return true;

    boolean bIsComplete = false;

    for (int iQueueCount = 0; queued.size() > 0; iQueueCount++) {
      if (iQueueCount >= clientConfig.common.blocksPerTick
          || processed.size() >= clientConfig.common.blockLimit
          || (clientConfig.common.tpsGuard && timer.elapsed(TimeUnit.MILLISECONDS) > 40))
        break;

      if (Functions.IsPlayerStarving(player)) {
        bIsComplete = true;
        break;
      }

      BlockPos oPos = queued.remove(0);
      if (oPos == null)
        continue;

      BlockState state = world.getBlockState(oPos);

      if (!Constants.DIRT_BLOCKS.contains(state.getBlock())) {
        // Add the non-harvestable blocks to the processed list so that they can be avoided.
        processed.add(oPos);
        continue;
      }

      world.captureBlockSnapshots = true;
      world.capturedBlockSnapshots.clear();

      if (world.isEmptyBlock(oPos.above()) || world.getBlockState(oPos.above()).canBeReplaced()) {
        world.playSound(player, oPos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1.0F, 1.0F);

        if (!world.isEmptyBlock(oPos.above())) {
          world.setBlockAndUpdate(oPos.above(), Blocks.AIR.defaultBlockState());
        }

        world.setBlockAndUpdate(oPos, Blocks.DIRT_PATH.defaultBlockState());

        if (heldItemStack != null && heldItemStack.isDamageableItem()) {
          heldItemStack.hurtAndBreak(1, player, null);
        }

        SoundType soundtype = state.getSoundType(world, oPos, null);
        reportProgessToClient(oPos, soundtype.getHitSound());

        processBlockSnapshots();
        addConnectedToQueue(oPos);
      }

      processed.add(oPos);
    }

    return (bIsComplete || queued.isEmpty());
  }

  @Override
  public void addToQueue(BlockPos oPos) {
    if (Constants.DIRT_BLOCKS.contains(world.getBlockState(oPos).getBlock()))
      super.addToQueue(oPos);
  }

  private void setupPath() {
    // Shaft area info
    int xStart  = 0;
    int xEnd    = 0;
    int yBottom = originPos.getY() - 1;
    int yTop    = originPos.getY() + 1;
    int zStart  = 0;
    int zEnd    = 0;

    int iLength = clientConfig.pathanation.pathLength - 1;

    // if the ShaftWidth is divisible by 2 we don't want to do anything
    double dDivision = ((clientConfig.pathanation.pathWidth & 1) != 0 ? 0 : 1);// 0.5);

    Direction direction = player.getDirection().getOpposite();

    switch (direction) {
      case SOUTH: // Positive Z
        xStart = originPos.getX() + ((int) ((clientConfig.pathanation.pathWidth / 2) - dDivision));
        xEnd = originPos.getX() - (clientConfig.pathanation.pathWidth / 2);
        zStart = originPos.getZ();
        zEnd = originPos.getZ() - iLength;
        break;
      case NORTH: // Negative Z
        xStart = originPos.getX() - (clientConfig.pathanation.pathWidth / 2);
        xEnd = originPos.getX() + ((int) ((clientConfig.pathanation.pathWidth / 2) - dDivision));
        zStart = originPos.getZ();
        zEnd = originPos.getZ() + iLength;
        break;
      case EAST: // Positive X
        xStart = originPos.getX();
        xEnd = originPos.getX() - iLength;
        zStart = originPos.getZ() + ((int) ((clientConfig.pathanation.pathWidth / 2) - dDivision));
        zEnd = originPos.getZ() - (clientConfig.pathanation.pathWidth / 2);
        break;
      case WEST: // Negative X
        xStart = originPos.getX();
        xEnd = originPos.getX() + iLength;
        zStart = originPos.getZ() - (clientConfig.pathanation.pathWidth / 2);
        zEnd = originPos.getZ() + ((int) ((clientConfig.pathanation.pathWidth / 2) - dDivision));
        break;
      default:
        break;
    }

    interimArea = new AABB(
        xStart, yBottom, zStart,
        xEnd, yTop, zEnd);
  }
}
