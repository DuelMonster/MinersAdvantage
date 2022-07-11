package uk.co.duelmonster.minersadvantage.helpers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import uk.co.duelmonster.minersadvantage.common.Functions;

public class VentilationHelper {

  public static VentilationHelper INSTANCE = new VentilationHelper();

  public BlockPos lastLadderLocation = null;
  public int      ladderStackCount   = 0;
  public int      ladderIndx         = -1;

  public boolean playerHasLadders(ServerPlayer player) {
    getLadderSlot(player);

    return ladderIndx >= 0;
  }

  public void getLadderSlot(ServerPlayer player) {
    // Reset the count and index to ensure we don't use ladders that the player
    // doesn't have!
    ladderStackCount = 0;
    ladderIndx       = -1;

    Item ladderItem = Blocks.LADDER.asItem();

    // Locate the players ladders
    for (int i = 0; i < player.getInventory().items.size(); i++) {
      ItemStack stack = player.getInventory().items.get(i);
      if (stack != null && stack.getItem().equals(ladderItem)) {
        ladderStackCount++;
        ladderIndx = Functions.getSlotFromInventory(player, stack);
      }
    }

    if (ladderIndx == -1) {
      for (int i = 0; i < player.getInventory().offhand.size(); i++) {
        ItemStack stack = player.getInventory().offhand.get(i);
        if (stack != null && stack.getItem().equals(ladderItem)) {
          ladderStackCount++;
          ladderIndx = Functions.getSlotFromInventory(player, stack);
        }
      }
    }
  }

  public boolean isLadderablePosition(Level world, BlockPos pos) {
    return world.isEmptyBlock(pos) && !world.isEmptyBlock(pos.south()) && canPlaceLadderOnFace(world, pos.south());
  }

  private boolean canPlaceLadderOnFace(Level world, BlockPos pos) {
    BlockState state = world.getBlockState(pos);
    Block      block = state.getBlock();

    boolean validFace = (state.isFaceSturdy(world, pos, Direction.NORTH) && world.getBlockState(pos.relative(Direction.NORTH)).getMaterial().isReplaceable());

    boolean validBockType = (block != Blocks.END_GATEWAY && block != Blocks.JACK_O_LANTERN);

    return validFace && validBockType;
  }
}
