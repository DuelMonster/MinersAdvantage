package uk.co.duelmonster.minersadvantage.workers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.minecraft.block.Blocks;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import uk.co.duelmonster.minersadvantage.common.Functions;
import uk.co.duelmonster.minersadvantage.common.TorchPlacement;
import uk.co.duelmonster.minersadvantage.helpers.IlluminationHelper;
import uk.co.duelmonster.minersadvantage.network.packets.PacketIlluminate;

public class IlluminationAgent extends Agent {

  private List<BlockPos> illuminationPositions = null;
  private boolean        singleTorch           = false;
  private TorchPlacement torchPlacement        = TorchPlacement.FLOOR;

  public IlluminationAgent(ServerPlayerEntity player, PacketIlluminate pkt) {
    super(player, pkt);

    this.originPos      = pkt.areaStartPos;
    this.faceHit        = pkt.faceHit;
    this.singleTorch    = pkt.singleTorch;
    this.torchPlacement = pkt.torchPlacement;

    if (singleTorch) {

      illuminationPositions = new ArrayList<BlockPos>();
      illuminationPositions.add(this.originPos);

    } else {

      refinedArea           = new AxisAlignedBB(pkt.areaStartPos, pkt.areaEndPos);
      illuminationPositions = IlluminationHelper.INSTANCE.getTorchablePositionsInArea(world, refinedArea);

      if (!illuminationPositions.isEmpty()) {
        // If the first torchable position is close to the player we reverse the positions list.
        // This helps make it look as though we are working our way back to illuminate the area.
        AxisAlignedBB playerArea = player.getBoundingBox().inflate(4);
        if (Functions.isWithinArea(illuminationPositions.get(0), playerArea)) {
          Collections.reverse(illuminationPositions);
        }
      }
    }

  }

  // Returns true when Illumination is complete or cancelled
  @Override
  public boolean tick() {
    if (illuminationPositions.isEmpty() || player == null || !player.isAlive())
      return true;

    if (clientConfig.common.tpsGuard && timer.elapsed(TimeUnit.MILLISECONDS) > 40)
      return false;

    BlockPos oPos = illuminationPositions.remove(0);
    if (oPos == null)
      return false;

    autoIlluminate(oPos);

    return illuminationPositions.isEmpty();
  }

  public boolean autoIlluminate(BlockPos lightCheckPos) {
    Direction placeOnFace   = faceHit;
    BlockPos  torchPlacePos = lightCheckPos;

    if (torchPlacement != TorchPlacement.FLOOR) {
      torchPlacePos = lightCheckPos.above();

      switch (playerFacing) {
        case NORTH:
          if (torchPlacement == TorchPlacement.LEFT_WALL || torchPlacement == TorchPlacement.BOTH_WALLS) {
            placeOnFace = Direction.EAST;
          } else if (torchPlacement == TorchPlacement.RIGHT_WALL) {
            placeOnFace = Direction.WEST;
          }
          break;
        case EAST:
          if (torchPlacement == TorchPlacement.LEFT_WALL || torchPlacement == TorchPlacement.BOTH_WALLS) {
            placeOnFace = Direction.SOUTH;
          } else if (torchPlacement == TorchPlacement.RIGHT_WALL) {
            placeOnFace = Direction.NORTH;
          }
          break;
        case SOUTH:
          if (torchPlacement == TorchPlacement.LEFT_WALL || torchPlacement == TorchPlacement.BOTH_WALLS) {
            placeOnFace = Direction.WEST;
          } else if (torchPlacement == TorchPlacement.RIGHT_WALL) {
            placeOnFace = Direction.EAST;
          }
          break;
        case WEST:
          if (torchPlacement == TorchPlacement.LEFT_WALL || torchPlacement == TorchPlacement.BOTH_WALLS) {
            placeOnFace = Direction.NORTH;
          } else if (torchPlacement == TorchPlacement.RIGHT_WALL) {
            placeOnFace = Direction.SOUTH;
          }
          break;
        default:
          break;
      }
    }

    // Offset the torch placement position to allow block updates to calculate light levels
    BlockPos torchPlaceOnFacePos            = torchPlacePos.relative(placeOnFace.getOpposite());
    boolean  canTorchBePlacedOnFace         = IlluminationHelper.INSTANCE.canPlaceTorchOnFace(world, torchPlaceOnFacePos, placeOnFace);
    boolean  canTorchBePlacedOnOppositeFace = torchPlacement == TorchPlacement.BOTH_WALLS && IlluminationHelper.INSTANCE.canPlaceTorchOnFace(world, torchPlacePos.relative(placeOnFace), placeOnFace.getOpposite());

    world.getChunkSource().getLightEngine().checkBlock(torchPlacePos);

    // Max light value between block and sky values
    int lightLevel = world.getLightEngine().getRawBrightness(lightCheckPos, 0);

    if ((canTorchBePlacedOnFace || canTorchBePlacedOnOppositeFace)
        && (singleTorch || lightLevel <= clientConfig.illumination.lowestLightLevel)
        && (world.isEmptyBlock(torchPlacePos) || world.getBlockState(torchPlacePos).getMaterial().isReplaceable())) {

      // System.out.print("[PLACING] canPlace = " + canTorchBePlacedOnFace);
      // System.out.print(" -> canPlaceOpp = " + canTorchBePlacedOnOppositeFace);
      // System.out.print(" -> singleTorch = " + singleTorch);
      // System.out.print(" -> lightLevel: " + lightLevel + " <= " +
      // clientConfig.illumination.lowestLightLevel + " = " + (lightLevel <=
      // clientConfig.illumination.lowestLightLevel));
      // System.out.print(" -> isEmpty = " + world.isEmptyBlock(torchPlacePos));
      // System.out.print(" -> isReplaceable = " +
      // world.getBlockState(torchPlacePos).getMaterial().isReplaceable());
      // System.out.println();

      placeTorchInWorld(torchPlacePos, canTorchBePlacedOnFace ? placeOnFace : placeOnFace.getOpposite());
      return true;
    }
    return false;
  }

  private void placeTorchInWorld(BlockPos pos, Direction placeOnFace) {
    final World world = player.level;

    IlluminationHelper.INSTANCE.getTorchSlot(player);

    if (IlluminationHelper.INSTANCE.torchIndx >= 0) {
      IlluminationHelper.INSTANCE.lastTorchLocation = new BlockPos(pos);

      if (placeOnFace == Direction.UP) {
        world.setBlockAndUpdate(pos, Blocks.TORCH.defaultBlockState());
      } else {
        world.setBlockAndUpdate(pos, Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, placeOnFace));
      }
      Functions.playSound(world, pos, SoundEvents.WOOD_HIT, SoundCategory.BLOCKS, 1.0F, world.random.nextFloat() + 0.5F);

      ItemStack torchStack = player.inventory.removeItem(IlluminationHelper.INSTANCE.torchIndx, 1);

      if (torchStack.getCount() <= 0) {
        IlluminationHelper.INSTANCE.lastTorchLocation = null;
        IlluminationHelper.INSTANCE.torchStackCount--;
      }

      if (IlluminationHelper.INSTANCE.torchStackCount == 0)
        Functions.NotifyClient(player, TextFormatting.GOLD + "Illumination: " + TextFormatting.WHITE + Functions.localize("minersadvantage.illumination.no_torches"));
    }
  }

}
