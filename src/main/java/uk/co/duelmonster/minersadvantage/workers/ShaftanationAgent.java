package uk.co.duelmonster.minersadvantage.workers;

import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Level;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.Tags;
import uk.co.duelmonster.minersadvantage.common.Constants;
import uk.co.duelmonster.minersadvantage.common.Functions;
import uk.co.duelmonster.minersadvantage.network.packets.PacketShaftanate;

public class ShaftanationAgent extends Agent {

  public ShaftanationAgent(ServerPlayer player, PacketShaftanate pkt) {
    super(player, pkt);

    this.originPos   = pkt.pos;
    this.faceHit     = pkt.faceHit;
    this.originState = Block.stateById(pkt.stateID);

    if (originState == null || originState.isAir())
      Constants.LOGGER.log(Level.INFO, "Invalid BlockState ID recieved from message packet. [ " + pkt.stateID + " ]");

    this.originBlock = originState.getBlock();

    this.shouldAutoIlluminate = clientConfig.common.autoIlluminate;

    setupShaft();

    addConnectedToQueue(originPos);
  }

  // Returns true when Shaftanation is complete or cancelled
  @Override
  public boolean tick() {
    if (originPos == null || player == null || !player.isAlive() || processed.size() >= clientConfig.common.blockLimit)
      return true;

    boolean bIsComplete = false;

    for (int iQueueCount = 0; queued.size() > 0; iQueueCount++) {
      if (/*
           * (clientConfig.common.breakAtToolSpeeds && iQueueCount > 0)
           * ||
           */ iQueueCount >= clientConfig.common.blocksPerTick
          || processed.size() >= clientConfig.common.blockLimit
          || (/* !clientConfig.common.breakAtToolSpeeds && */ clientConfig.common.tpsGuard && timer.elapsed(TimeUnit.MILLISECONDS) > 40))
        break;

      if (Functions.IsPlayerStarving(player)) {
        bIsComplete = true;
        break;
      }

      BlockPos oPos = queued.remove(0);
      if (oPos == null || !Functions.isWithinArea(oPos, interimArea) || world.getBlockState(oPos).getBlock() == Blocks.TORCH)
        continue;

      BlockState state = world.getBlockState(oPos);
      Block      block = state.getBlock();

      if (!getPlayer().hasCorrectToolForDrops(state)) {
        // Avoid the non-harvestable blocks.
        processed.add(oPos);
        continue;
      }

      // Process the current block if it is valid.
      if (clientConfig.common.mineVeins && state.is(Tags.Blocks.ORES)) {
        processed.add(oPos);
        excavateOreVein(state, oPos);
      } else {
        world.captureBlockSnapshots = true;
        world.capturedBlockSnapshots.clear();

        boolean bBlockHarvested = HarvestBlock(oPos);

        if (bBlockHarvested) {
          processBlockSnapshots();

          SoundType soundtype = block.getSoundType(state, world, oPos, null);
          reportProgessToClient(oPos, soundtype.getBreakSound());

          // autoIlluminate((new BlockPos(oPos.getX(), feetPos,
          // oPos.getZ())).offset(faceHit.getOpposite()),
          // clientConfig.shaftanation.torchPlacement);

          addConnectedToQueue(oPos);

          processed.add(oPos);
        }
      }
    }

    return (bIsComplete || queued.isEmpty());
  }

  private void setupShaft() {
    // Shaft area info
    int xStart  = 0;
    int xEnd    = 0;
    int yBottom = feetPos;
    int yTop    = feetPos + (clientConfig.shaftanation.shaftHeight - 1);
    int zStart  = 0;
    int zEnd    = 0;

    // if the ShaftWidth is divisible by 2 we don't want to do anything
    double dDivision = ((clientConfig.shaftanation.shaftWidth & 1) != 0 ? 0 : 0.5);

    switch (faceHit) {
      case SOUTH: // Positive Z
        xStart = originPos.getX() + ((int) ((clientConfig.shaftanation.shaftWidth / 2) - dDivision));
        xEnd = originPos.getX() - (clientConfig.shaftanation.shaftWidth / 2);
        zStart = originPos.getZ();
        zEnd = originPos.getZ() - (clientConfig.shaftanation.shaftLength - 1);
        break;
      case NORTH: // Negative Z
        xStart = originPos.getX() - (clientConfig.shaftanation.shaftWidth / 2);
        xEnd = originPos.getX() + ((int) ((clientConfig.shaftanation.shaftWidth / 2) - dDivision));
        zStart = originPos.getZ();
        zEnd = originPos.getZ() + (clientConfig.shaftanation.shaftLength - 1);
        break;
      case EAST: // Positive X
        xStart = originPos.getX();
        xEnd = originPos.getX() - (clientConfig.shaftanation.shaftLength - 1);
        zStart = originPos.getZ() + ((int) ((clientConfig.shaftanation.shaftWidth / 2) - dDivision));
        zEnd = originPos.getZ() - (clientConfig.shaftanation.shaftWidth / 2);
        break;
      case WEST: // Negative X
        xStart = originPos.getX();
        xEnd = originPos.getX() + (clientConfig.shaftanation.shaftLength - 1);
        zStart = originPos.getZ() - (clientConfig.shaftanation.shaftWidth / 2);
        zEnd = originPos.getZ() + ((int) ((clientConfig.shaftanation.shaftWidth / 2) - dDivision));
        break;
      default:
        break;
    }

    interimArea = new AABB(
        xStart, yBottom, zStart,
        xEnd, yTop, zEnd);

  }

  @Override
  public void addToQueue(BlockPos oPos) {
    BlockState state = world.getBlockState(oPos);

    if (getPlayer().hasCorrectToolForDrops(state))
      super.addToQueue(oPos);
  }

}
