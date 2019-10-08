package uk.co.duelmonster.minersadvantage.helpers;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import uk.co.duelmonster.minersadvantage.client.ClientFunctions;
import uk.co.duelmonster.minersadvantage.common.Functions;
import uk.co.duelmonster.minersadvantage.network.NetworkHandler;
import uk.co.duelmonster.minersadvantage.network.packets.PacketIlluminate;

@OnlyIn(Dist.CLIENT)
public class IlluminationHelper {
	
	public static IlluminationHelper instance = new IlluminationHelper();
	
	public static BlockPos	lastTorchLocation	= null;
	public static int		torchStackCount		= 0;
	public static int		torchIndx			= -1;
	
	public static void getTorchSlot(ServerPlayerEntity player) {
		// Reset the count and index to ensure we don't use torches that the player
		// doesn't have!
		torchStackCount = 0;
		torchIndx = -1;
		
		Item torchItem = Blocks.TORCH.asItem();
		
		// Locate the players torches
		for (int i = 0; i < player.inventory.mainInventory.size(); i++) {
			ItemStack stack = player.inventory.mainInventory.get(i);
			if (stack != null && stack.getItem().equals(torchItem)) {
				torchStackCount++;
				torchIndx = Functions.getSlotFromInventory(player, stack);
			}
		}
		
		if (torchIndx == -1) {
			for (int i = 0; i < player.inventory.offHandInventory.size(); i++) {
				ItemStack stack = player.inventory.offHandInventory.get(i);
				if (stack != null && stack.getItem().equals(torchItem)) {
					torchStackCount++;
					torchIndx = Functions.getSlotFromInventory(player, stack);
				}
			}
		}
	}
	
	public static void PlaceTorch() {
		Minecraft mc = ClientFunctions.mc;
		
		if (mc.objectMouseOver != null && mc.objectMouseOver.getType() == RayTraceResult.Type.BLOCK) {
			
			BlockRayTraceResult	blockResult	= (BlockRayTraceResult) mc.objectMouseOver;
			BlockPos			oPos		= blockResult.getPos();
			
			if (mc.world.getBlockState(oPos).getBlock() != Blocks.TORCH) {
				Direction	faceHit		= blockResult.getFace();
				BlockPos	oSidePos	= oPos;
				
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
					oSidePos = oPos.up();
					break;
				default:
					return;
				
				}
				
				PacketIlluminate	packet;
				BlockState			state	= mc.world.getBlockState(oPos);
				
				if (state.getMaterial().isReplaceable() && canPlaceTorchOnFace(mc.world, oPos, Direction.UP)) {
					
					packet = new PacketIlluminate(oPos, Direction.UP);
					
				} else if (mc.world.isAirBlock(oSidePos) && canPlaceTorchOnFace(mc.world, oPos, faceHit)) {
					
					packet = new PacketIlluminate(oSidePos, faceHit);
					
				} else {
					
					packet = new PacketIlluminate(oPos, faceHit);
					
				}
				
				NetworkHandler.sendToServer(packet);
			}
		}
	}
	
	public static boolean canPlaceTorchOnFace(IBlockReader world, BlockPos pos, Direction face) {
		BlockState	state	= world.getBlockState(pos);
		Block		block	= state.getBlock();
		
		boolean validFace = (face != Direction.DOWN && Block.hasSolidSide(state, world, pos, face) && world.getBlockState(pos.offset(face)).getMaterial().isReplaceable());
		
		boolean validBockType = (block != Blocks.END_GATEWAY && block != Blocks.JACK_O_LANTERN);
		
		return validFace && validBockType;
	}
}
