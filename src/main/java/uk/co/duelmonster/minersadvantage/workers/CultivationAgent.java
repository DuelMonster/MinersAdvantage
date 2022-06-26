package uk.co.duelmonster.minersadvantage.workers;

import java.util.concurrent.TimeUnit;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import uk.co.duelmonster.minersadvantage.common.Constants;
import uk.co.duelmonster.minersadvantage.common.Functions;
import uk.co.duelmonster.minersadvantage.helpers.FarmingHelper;
import uk.co.duelmonster.minersadvantage.network.packets.PacketCultivate;

public class CultivationAgent extends Agent {

  public CultivationAgent(ServerPlayer player, final PacketCultivate pkt) {
    super(player, pkt);

    this.originPos = pkt.pos;

    this.interimArea = FarmingHelper.getFarmableLand(world, originPos);

    addConnectedToQueue(originPos);
  }

  // Returns true when Cultivation is complete or cancelled
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

      Block block = world.getBlockState(oPos).getBlock();
      if (!Constants.DIRT_BLOCKS.contains(block) && !block.equals(Blocks.DIRT_PATH)) {
        // Add the non-harvestable blocks to the processed list so that they can be avoided.
        processed.add(oPos);
        iQueueCount--;
        continue;
      }

      world.captureBlockSnapshots = true;
      world.capturedBlockSnapshots.clear();

      if (world.isEmptyBlock(oPos.above()) || world.getBlockState(oPos.above()).getMaterial().isReplaceable()) {
        Functions.playSound(world, oPos, SoundEvents.HOE_TILL, SoundSource.PLAYERS, 1.0F, world.random.nextFloat() + 0.5F);

        if (!world.isEmptyBlock(oPos.above())) {
          world.setBlockAndUpdate(oPos.above(), Blocks.AIR.defaultBlockState());
        }

        world.setBlockAndUpdate(oPos, Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, Integer.valueOf(7)));

        if (heldItemStack != null && heldItemStack.isDamageableItem()) {
          heldItemStack.hurtAndBreak(1, player, null);

          // if (heldItemStack.getMaxDamage() <= 0) {
          // player.getInventory().removeStackFromSlot(player.getInventory().selected);
          // player.openContainer.detectAndSendChanges();
          // }
        }

        processBlockSnapshots();
        addConnectedToQueue(oPos);
      }

      processed.add(oPos);
    }

    return (bIsComplete || queued.isEmpty());
  }

  @Override
  public void addToQueue(BlockPos oPos) {
    Block block = world.getBlockState(oPos).getBlock();
    if (Constants.DIRT_BLOCKS.contains(block) || block.equals(Blocks.DIRT_PATH))
      super.addToQueue(oPos);
  }
}
