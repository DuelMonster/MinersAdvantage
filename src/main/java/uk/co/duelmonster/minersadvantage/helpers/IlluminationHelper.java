package uk.co.duelmonster.minersadvantage.helpers;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import uk.co.duelmonster.minersadvantage.MA;
import uk.co.duelmonster.minersadvantage.client.ClientFunctions;
import uk.co.duelmonster.minersadvantage.common.Functions;
import uk.co.duelmonster.minersadvantage.common.TorchPlacement;
import uk.co.duelmonster.minersadvantage.network.packets.PacketIlluminate;

public class IlluminationHelper {

  public static IlluminationHelper INSTANCE = new IlluminationHelper();

  public BlockPos lastTorchLocation = null;
  public int      torchStackCount   = 0;
  public int      torchIndx         = -1;

  public void getTorchSlot(ServerPlayerEntity player) {
    // Reset the count and index to ensure we don't use torches that the player
    // doesn't have!
    torchStackCount = 0;
    torchIndx       = -1;

    Item torchItem = Blocks.TORCH.asItem();

    // Locate the players torches
    for (int i = 0; i < player.inventory.items.size(); i++) {
      ItemStack stack = player.inventory.items.get(i);
      if (stack != null && stack.getItem().equals(torchItem)) {
        torchStackCount++;
        torchIndx = Functions.getSlotFromInventory(player, stack);
      }
    }

    if (torchIndx == -1) {
      for (int i = 0; i < player.inventory.offhand.size(); i++) {
        ItemStack stack = player.inventory.offhand.get(i);
        if (stack != null && stack.getItem().equals(torchItem)) {
          torchStackCount++;
          torchIndx = Functions.getSlotFromInventory(player, stack);
        }
      }
    }
  }

  public List<BlockPos> getTorchablePositionsInArea(World world, AxisAlignedBB area) {
    List<BlockPos> positions   = new ArrayList<BlockPos>();
    BlockPos       previousPos = null;

    for (double y = area.minY; y <= area.maxY; y++)
      for (double x = area.minX; x <= area.maxX; x++)
        for (double z = area.minZ; z <= area.maxZ; z++) {
          BlockPos pos = new BlockPos(x, y, z);
          if (world.isEmptyBlock(pos) && !world.isEmptyBlock(pos.below())
              && (previousPos == null || !Functions.isWithinRange(previousPos, pos, 5))) {
            positions.add(pos);

            previousPos = pos;
          }
        }
    return positions;
  }

  @OnlyIn(Dist.CLIENT)
  public void PlaceTorch() {
    Minecraft mc = ClientFunctions.mc;

    if (mc.hitResult != null && mc.hitResult.getType() == RayTraceResult.Type.BLOCK) {

      BlockRayTraceResult blockResult = (BlockRayTraceResult) mc.hitResult;
      BlockPos            oPos        = blockResult.getBlockPos();

      if (mc.level.getBlockState(oPos).getBlock() != Blocks.TORCH) {
        Direction faceHit  = blockResult.getDirection();
        BlockPos  oSidePos = oPos;

        switch (faceHit) {
          case NORTH:
            oSidePos = oPos.north();
            break;
          case SOUTH:
            oSidePos = oPos.south();
            break;
          case EAST:
            oSidePos = oPos.east();
            break;
          case WEST:
            oSidePos = oPos.west();
            break;
          case UP:
            oSidePos = oPos.above();
            break;
          default:
            return;

        }

        BlockState state = mc.level.getBlockState(oPos);

        if (state.getMaterial().isReplaceable() && canPlaceTorchOnFace(mc.level, oPos.below(), Direction.UP)) {

          MA.NETWORK.sendToServer(new PacketIlluminate(oPos, Direction.UP));

        } else if (mc.level.isEmptyBlock(oSidePos)) {
          if (canPlaceTorchOnFace(mc.level, oPos, faceHit)) {

            MA.NETWORK.sendToServer(new PacketIlluminate(oSidePos, faceHit));

          } else {

            MA.NETWORK.sendToServer(new PacketIlluminate(oPos, faceHit));

          }
        }
      }
    }
  }

  @OnlyIn(Dist.CLIENT)
  public void IlluminateArea() {
    AxisAlignedBB illuminationArea = ClientFunctions.mc.player.getBoundingBox().inflate(8);
    BlockPos      startPos         = new BlockPos(illuminationArea.minX, illuminationArea.minY, illuminationArea.minZ);
    BlockPos      endPos           = new BlockPos(illuminationArea.maxX, illuminationArea.maxY, illuminationArea.maxZ);

    MA.NETWORK.sendToServer(new PacketIlluminate(startPos, endPos, TorchPlacement.FLOOR));
  }

  public boolean canPlaceTorchOnFace(World world, BlockPos pos, Direction face) {
    BlockState state = world.getBlockState(pos);
    Block      block = state.getBlock();

    boolean validFace = (face != Direction.DOWN && state.isFaceSturdy(world, pos, face) && world.getBlockState(pos.relative(face)).getMaterial().isReplaceable());

    boolean validBockType = (block != Blocks.END_GATEWAY && block != Blocks.JACK_O_LANTERN);

    return validFace && validBockType;
  }
}
