package uk.co.duelmonster.minersadvantage.workers;

import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Level;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.Tags;
import uk.co.duelmonster.minersadvantage.common.Constants;
import uk.co.duelmonster.minersadvantage.common.Functions;
import uk.co.duelmonster.minersadvantage.helpers.VentilationHelper;
import uk.co.duelmonster.minersadvantage.network.packets.PacketVentilate;

public class VentilationAgent extends Agent {

  public AABB ladderArea;

  public VentilationAgent(ServerPlayer player, PacketVentilate pkt) {
    super(player, pkt);

    this.originPos   = pkt.pos;
    this.faceHit     = pkt.faceHit;
    this.originState = Block.stateById(pkt.stateID);

    if (originState == null || originState.isAir())
      Constants.LOGGER.log(Level.INFO, "Invalid BlockState ID recieved from message packet. [ " + pkt.stateID + " ]");

    this.originBlock = originState.getBlock();

    this.shouldAutoIlluminate = clientConfig.common.autoIlluminate;

    setupVent();
    placeLadder(originPos); // Fire place ladder for originPos to be sure first layer doesn't get missed

    addConnectedToQueue(originPos);
  }

  // Returns true when Ventilation is complete or cancelled
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

          addConnectedToQueue(oPos);

          processed.add(oPos);
        }
      }

      placeLadder(oPos);
    }

    return (bIsComplete || queued.isEmpty());
  }

  private void setupVent() {
    // if the vent Diameter is divisible by 2 we don't want to do anything
    double dDivision = ((clientConfig.ventilation.ventDiameter & 1) != 0 ? 0 : 0.5);
    int    dCenter   = (clientConfig.ventilation.ventDiameter / 2);

    // Shaft area info
    int xStart  = originPos.getX() + ((int) (dCenter - dDivision));
    int xEnd    = originPos.getX() - dCenter;
    int zStart  = originPos.getZ() - dCenter;
    int zEnd    = originPos.getZ() + ((int) (dCenter - dDivision));
    int yTop    = originPos.getY();
    int yBottom = originPos.getY() - (clientConfig.ventilation.ventDepth - 1);

    if (faceHit == Direction.DOWN) { // Positive Y
      yTop    = originPos.getY() + (clientConfig.ventilation.ventDepth - 1);
      yBottom = originPos.getY();
    }

    interimArea = new AABB(xStart, yTop, zStart, xEnd, yBottom, zEnd);

    ladderArea = new AABB(originPos.getX(), yTop, zEnd, originPos.getX(), yBottom, zEnd);
  }

  @Override
  public void addToQueue(BlockPos oPos) {
    BlockState state = world.getBlockState(oPos);

    if (getPlayer().hasCorrectToolForDrops(state))
      super.addToQueue(oPos);
  }

  private void placeLadder(BlockPos oPos) {
    if (clientConfig.ventilation.placeLadders &&
        Functions.isWithinArea(oPos, ladderArea) &&
        VentilationHelper.INSTANCE.playerHasLadders(player) &&
        (VentilationHelper.INSTANCE.isLadderablePosition(world, oPos) || Functions.isPosEqual(oPos, originPos))) {

      world.setBlockAndUpdate(oPos, Blocks.LADDER.defaultBlockState());
      Functions.playSound(world, oPos, SoundEvents.LADDER_PLACE, SoundSource.BLOCKS, 1.0F, world.random.nextFloat() + 0.5F);

      ItemStack ladderStack = player.getInventory().removeItem(VentilationHelper.INSTANCE.ladderIndx, 1);

      if (ladderStack.getCount() <= 0) {
        VentilationHelper.INSTANCE.ladderStackCount--;
      }

      if (VentilationHelper.INSTANCE.ladderStackCount <= 0)
        Functions.NotifyClient(player, ChatFormatting.GOLD + "Ventilation: " + ChatFormatting.WHITE + Functions.localize("minersadvantage.ventilation.no_ladders"));
    }
  }

}
