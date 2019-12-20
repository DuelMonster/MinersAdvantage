package uk.co.duelmonster.minersadvantage.workers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.minecraft.block.BlockState;
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
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import uk.co.duelmonster.minersadvantage.common.Functions;
import uk.co.duelmonster.minersadvantage.common.TorchPlacement;
import uk.co.duelmonster.minersadvantage.helpers.IlluminationHelper;
import uk.co.duelmonster.minersadvantage.network.packets.PacketIlluminate;

public class IlluminationAgent extends Agent {
	
	private List<BlockPos>	illuminationPositions	= null;
	private boolean			singleTorch				= false;
	private TorchPlacement	torchPlacement			= TorchPlacement.FLOOR;
	
	public IlluminationAgent(ServerPlayerEntity player, PacketIlluminate pkt) {
		super(player, pkt);
		
		this.originPos = pkt.areaStartPos;
		this.faceHit = pkt.faceHit;
		this.singleTorch = pkt.singleTorch;
		this.torchPlacement = pkt.torchPlacement;
		
		if (singleTorch) {
			
			illuminationPositions = new ArrayList<BlockPos>();
			illuminationPositions.add(this.originPos);
			
		} else {
			
			harvestArea = new AxisAlignedBB(pkt.areaStartPos, pkt.areaEndPos);
			illuminationPositions = IlluminationHelper.INSTANCE.getTorchablePositionsInArea(world, harvestArea);
			
			// If the first torchable position is close to the player we reverse the positions list.
			// This helps make it look as though the fakePlayer is working it's way back to illuminate the area.
			AxisAlignedBB playerArea = player.getBoundingBox().grow(4);
			if (Functions.isWithinArea(illuminationPositions.get(0), playerArea)) {
				Collections.reverse(illuminationPositions);
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
	
	public boolean autoIlluminate(BlockPos pos) {
		Direction placeOnFace = faceHit;
		BlockPos finalPos = pos;
		
		if (torchPlacement != TorchPlacement.FLOOR) {
			finalPos = pos.up();
			
			switch (playerFacing) {
			case NORTH:
				if (torchPlacement == TorchPlacement.RIGHT_WALL) {
					placeOnFace = Direction.WEST;
				}
				if (torchPlacement == TorchPlacement.LEFT_WALL) {
					placeOnFace = Direction.EAST;
				}
				break;
			case EAST:
				if (torchPlacement == TorchPlacement.RIGHT_WALL) {
					placeOnFace = Direction.NORTH;
				}
				if (torchPlacement == TorchPlacement.LEFT_WALL) {
					placeOnFace = Direction.SOUTH;
				}
				break;
			case SOUTH:
				if (torchPlacement == TorchPlacement.RIGHT_WALL) {
					placeOnFace = Direction.EAST;
				}
				if (torchPlacement == TorchPlacement.LEFT_WALL) {
					placeOnFace = Direction.WEST;
				}
				break;
			case WEST:
				if (torchPlacement == TorchPlacement.RIGHT_WALL) {
					placeOnFace = Direction.SOUTH;
				}
				if (torchPlacement == TorchPlacement.LEFT_WALL) {
					placeOnFace = Direction.NORTH;
				}
				break;
			default:
				break;
			}
		}
		
		// Offset the torch placement position to allow block updates to calculate light levels
		BlockPos lightCheckPos = pos;		// (packetId == PacketId.Shaftanate ? pos.offset(faceHit, 3) : pos);
		BlockPos torchPlacePos = finalPos;	// (packetId == PacketId.Shaftanate ? finalPos.offset(faceHit, 3) : finalPos);
		BlockPos torchPlaceOnFacePos = torchPlacePos.offset(placeOnFace.getOpposite());
		// BlockState statePlaceOnFace = world.getBlockState(torchPlaceOnFacePos);
		BlockState statePlace = world.getBlockState(torchPlacePos);
		
		boolean canTorchBePlacedOnFace = IlluminationHelper.INSTANCE.canPlaceTorchOnFace(world, torchPlaceOnFacePos, placeOnFace);
		
		// int blockLightLevel = world.getLightFor((clientConfig.illumination.useBlockLight ? LightType.BLOCK : LightType.SKY), lightCheckPos);
		int blockLightLevel = world.func_226658_a_((clientConfig.illumination.useBlockLight ? LightType.BLOCK : LightType.SKY), lightCheckPos);
		
		if (canTorchBePlacedOnFace
				&& (singleTorch || blockLightLevel <= clientConfig.illumination.lowestLightLevel)
				&& (world.isAirBlock(torchPlacePos) || statePlace.getMaterial().isReplaceable())) {
			
			placeTorchInWorld(torchPlacePos, placeOnFace);
			return true;
		}
		return false;
	}
	
	private void placeTorchInWorld(BlockPos pos, Direction placeOnFace) {
		final World world = player.getEntityWorld();
		
		IlluminationHelper.INSTANCE.getTorchSlot(player);
		
		if (IlluminationHelper.INSTANCE.torchIndx >= 0) {
			IlluminationHelper.INSTANCE.lastTorchLocation = new BlockPos(pos);
			
			if (placeOnFace == Direction.UP) {
				world.setBlockState(pos, Blocks.TORCH.getDefaultState());
			} else {
				world.setBlockState(pos, Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.HORIZONTAL_FACING, placeOnFace));
			}
			Functions.playSound(world, pos, SoundEvents.BLOCK_WOOD_HIT, SoundCategory.BLOCKS, 1.0F, world.rand.nextFloat() + 0.5F);
			
			ItemStack torchStack = player.inventory.decrStackSize(IlluminationHelper.INSTANCE.torchIndx, 1);
			
			if (torchStack.getCount() <= 0) {
				IlluminationHelper.INSTANCE.lastTorchLocation = null;
				IlluminationHelper.INSTANCE.torchStackCount--;
			}
			
			if (IlluminationHelper.INSTANCE.torchStackCount == 0)
				Functions.NotifyClient(player, TextFormatting.GOLD + "Illumination: " + TextFormatting.WHITE + Functions.localize("minersadvantage.illumination.no_torches"));
		}
	}
	
}
