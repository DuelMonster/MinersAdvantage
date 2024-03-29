package uk.co.duelmonster.minersadvantage.workers;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Lists;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.IPlantable;
import uk.co.duelmonster.minersadvantage.common.Functions;
import uk.co.duelmonster.minersadvantage.helpers.FarmingHelper;
import uk.co.duelmonster.minersadvantage.network.packets.PacketCropinate;

public class CropinationAgent extends Agent {

  private int iHarvestedCount = 0;

  public CropinationAgent(ServerPlayer player, final PacketCropinate pkt) {
    super(player, pkt);

    this.originPos = pkt.pos;

    // Removed to simplify harvesting crops. This may be an issue for a MASSIVE farm and should be
    // replaced.
    this.interimArea = FarmingHelper.getCropPatch(world, originPos);

    addConnectedToQueue(originPos);
  }

  // Returns true when Cropination is complete or cancelled
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
      Block      block = state.getBlock();

      if (isFullyGrown(state)) {

        BlockState newState = block.defaultBlockState();

        // Get Block drops
        List<ItemStack> drops = Block.getDrops(state, (ServerLevel) world, oPos, null, player, Functions.getHeldItemStack(player));

        // Identify the plantable drops and spawn the non-plantable drops
        List<ItemStack> plantables = Lists.newArrayList();
        for (ItemStack item : drops) {
          if (item.getItem() instanceof IPlantable)
            plantables.add(item);
          else
            Block.popResource(world, oPos, item);
        }

        // Decrease the plantable drops by one to simulate re-planting
        boolean   bReplanted = false;
        BlockItem oSeeds     = null;
        for (ItemStack plantable : plantables) {
          if (!bReplanted) {
            if (plantable.getItem() instanceof BlockItem)
              oSeeds = (BlockItem) plantable.getItem();

            plantable.setCount(plantable.getCount() - 1);
            bReplanted = true;
          }
          // Spawn drop if it's not a seed or harvestSeeds is enabled
          if (plantable.getCount() > 0 && (clientConfig.cropination.harvestSeeds || !(plantable.getItem() instanceof BlockItem)))
            Block.popResource(world, oPos, plantable);
        }

        // If seed harvesting is disabled and current crop requires seeds, check players
        // inventory for
        // matching seed stacks and decrease by one to simulate re-planting
        if (!clientConfig.cropination.harvestSeeds && oSeeds != null)
          decreaseSeedsInInventory(oSeeds);

        iHarvestedCount++;

        world.captureBlockSnapshots = true;
        world.capturedBlockSnapshots.clear();

        world.setBlock(oPos, newState, 1 | 2 | 8);

        // Apply Item damage every 4 crops harvested. This makes item damage 1/4 per
        // crop
        if (iHarvestedCount > 0 && iHarvestedCount % 4 == 0
            && heldItemStack != null && heldItemStack.isDamageableItem()) {
          heldItemStack.hurtAndBreak(1, player, (player) -> {
            player.broadcastBreakEvent(EquipmentSlot.MAINHAND);
          });
        }

        processBlockSnapshots();

        Functions.playSound(world, oPos, SoundEvents.HOE_TILL, SoundSource.PLAYERS, 1.0F, world.random.nextFloat() + 0.5F);
      } else {
        // Minus one from the queue count so that the current position is not included
        // in the block per tick
        // limit
        iQueueCount--;
      }

      addConnectedToQueue(oPos);
      processed.add(oPos);
    }

    return (bIsComplete || queued.isEmpty());
  }

  private boolean isFullyGrown(BlockState state) {
    return (state.hasProperty(BlockStateProperties.AGE_1) && state.getValue(BlockStateProperties.AGE_1).equals(1)) ||
        (state.hasProperty(BlockStateProperties.AGE_2) && state.getValue(BlockStateProperties.AGE_2).equals(2)) ||
        (state.hasProperty(BlockStateProperties.AGE_3) && state.getValue(BlockStateProperties.AGE_3).equals(3)) ||
        (state.hasProperty(BlockStateProperties.AGE_5) && state.getValue(BlockStateProperties.AGE_5).equals(5)) ||
        (state.hasProperty(BlockStateProperties.AGE_7) && state.getValue(BlockStateProperties.AGE_7).equals(7)) ||
        (state.hasProperty(BlockStateProperties.AGE_15) && state.getValue(BlockStateProperties.AGE_15).equals(15)) ||
        (state.hasProperty(BlockStateProperties.AGE_25) && state.getValue(BlockStateProperties.AGE_25).equals(25));
  }

  @Override
  public void addToQueue(BlockPos oPos) {
    BlockState state = world.getBlockState(oPos);
    Block      block = state.getBlock();

    if ((block instanceof BonemealableBlock && block instanceof IPlantable) || block instanceof CropBlock || block instanceof NetherWartBlock)
      super.addToQueue(oPos);
  }

  private void decreaseSeedsInInventory(BlockItem oSeeds) {
    ItemStack stack = null;

    // Locate the players seeds in the main inventory
    for (int i = 0; i < player.getInventory().items.size(); i++) {
      stack = player.getInventory().items.get(i);
      if (stack != null && stack.getItem().equals(oSeeds))
        break;
    }

    if (stack == null) {
      // Seeds not found in the main inventory check the off hand inventory
      for (int i = 0; i < player.getInventory().offhand.size(); i++) {
        stack = player.getInventory().offhand.get(i);
        if (stack != null && stack.getItem().equals(oSeeds))
          break;
      }
    }

    // If seeds were found then decrease the size of the stack by one
    if (stack != null && stack.getCount() > 0)
      player.getInventory().removeItem(Functions.getSlotFromInventory(player, stack), 1);

  }
}
