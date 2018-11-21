package co.uk.duelmonster.minersadvantage.handlers;

import co.uk.duelmonster.minersadvantage.MinersAdvantage;
import co.uk.duelmonster.minersadvantage.common.Functions;
import co.uk.duelmonster.minersadvantage.common.PacketID;
import co.uk.duelmonster.minersadvantage.config.MAConfig;
import co.uk.duelmonster.minersadvantage.packets.NetworkPacket;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class IlluminationHandler implements IPacketHandler {
	
	public static IlluminationHandler instance = new IlluminationHandler();
	
	private BlockPos	lastTorchLocation	= null;
	private int			iTorchStackCount	= 0;
	private int			iTorchIndx			= -1;
	
	@Override
	public void processClientMessage(NetworkPacket message, MessageContext context) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void processServerMessage(NetworkPacket message, MessageContext context) {
		final EntityPlayerMP player = context.getServerHandler().player;
		if (player == null)
			return;
		
		World world = player.getEntityWorld();
		if (world == null)
			return;
		
		final MAConfig settings = MAConfig.get(player.getUniqueID());
		
		BlockPos oPos = player.getPosition();
		if (message.getTags().hasKey("x"))
			oPos = new BlockPos(
					message.getTags().getInteger("x"),
					message.getTags().getInteger("y"),
					message.getTags().getInteger("z"));
		
		// Get Block below the current position and check to see if we can place a torch on the top of it.
		BlockPos	oHitPos					= oPos.down();
		IBlockState	state					= world.getBlockState(oHitPos);
		EnumFacing	sideHit					= EnumFacing.UP;
		boolean		bCanTorchBePlacedOnFace	= state.getBlock().canPlaceTorchOnTop(state, world, oHitPos);
		final int	iBlockLightLevel		= world.getLightFor((settings.illumination.bUseBlockLight() ? EnumSkyBlock.BLOCK : EnumSkyBlock.SKY), oPos);
		
		// Override the above if we are being told which face the torch should be placed upon.
		if (message.getTags().hasKey("sideHit")) {
			sideHit = EnumFacing.getFront(message.getTags().getInteger("sideHit"));
			if (sideHit != EnumFacing.UP && sideHit != EnumFacing.DOWN) {
				oHitPos = oPos.up().offset(sideHit);
				state = world.getBlockState(oHitPos);
				
				bCanTorchBePlacedOnFace = Blocks.TORCH.canPlaceBlockOnSide(world, oHitPos, sideHit.getOpposite());
			}
		}
		
		if ((message.getTags().hasKey("ForceTorchPlacement") || iBlockLightLevel <= settings.illumination.iLowestLightLevel())
				&& (world.isAirBlock(oPos) || state.getBlock().isReplaceable(world, oPos))
				&& bCanTorchBePlacedOnFace) {
			getTorchSlot(player);
			
			if (iTorchIndx >= 0 && (message.getTags().hasKey("ForceTorchPlacement") || !oPos.equals(lastTorchLocation))) {
				lastTorchLocation = new BlockPos(oPos);
				
				world.setBlockState(oPos, Blocks.TORCH.getDefaultState().withProperty(BlockTorch.FACING, sideHit));
				Functions.playSound(world, oPos, SoundEvents.BLOCK_WOOD_HIT, SoundCategory.BLOCKS, 1.0F, world.rand.nextFloat() + 0.5F);
				
				ItemStack torchStack = player.inventory.decrStackSize(iTorchIndx, 1);
				
				if (torchStack.getCount() <= 0) {
					lastTorchLocation = null;
					iTorchStackCount--;
				}
				
				if (iTorchStackCount == 0)
					Functions.NotifyClient(player, TextFormatting.GOLD + "Illumination: " + TextFormatting.WHITE + Functions.localize("minersadvantage.illumination.no_torches"));
			}
		}
	}
	
	private void getTorchSlot(EntityPlayerMP player) {
		// Reset the count and index to ensure we don't use torches that the player doesn't have!
		iTorchStackCount = 0;
		iTorchIndx = -1;
		
		// Locate the players torches
		for (int i = 0; i < player.inventory.mainInventory.size(); i++) {
			ItemStack stack = player.inventory.mainInventory.get(i);
			if (stack != null && stack.getItem().equals(Item.getItemFromBlock(Blocks.TORCH))) {
				iTorchStackCount++;
				iTorchIndx = Functions.getSlotFromInventory(player, stack);
			}
		}
		
		if (iTorchIndx == -1) {
			for (int i = 0; i < player.inventory.offHandInventory.size(); i++) {
				ItemStack stack = player.inventory.offHandInventory.get(i);
				if (stack != null && stack.getItem().equals(Item.getItemFromBlock(Blocks.TORCH))) {
					iTorchStackCount++;
					iTorchIndx = Functions.getSlotFromInventory(player, stack);
				}
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	public static void PlaceTorch(Minecraft mc, EntityPlayer player) {
		if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK) {
			BlockPos oPos = mc.objectMouseOver.getBlockPos();
			if (mc.world.getBlockState(oPos).getBlock() != Blocks.TORCH) {
				EnumFacing	sideHit		= mc.objectMouseOver.sideHit;
				BlockPos	oSidePos	= mc.objectMouseOver.getBlockPos();
				
				switch (sideHit) {
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
				
				NBTTagCompound tags = new NBTTagCompound();
				tags.setInteger("ID", PacketID.Illuminate.value());
				tags.setBoolean("ForceTorchPlacement", true);
				
				IBlockState	state		= mc.world.getBlockState(oPos);
				IBlockState	stateDown	= mc.world.getBlockState(oPos.down());
				
				if (state.getBlock().isReplaceable(mc.world, oPos) && stateDown.getBlock().canPlaceTorchOnTop(stateDown, mc.world, oPos.down())) {
					tags.setInteger("x", oPos.getX());
					tags.setInteger("y", oPos.getY());
					tags.setInteger("z", oPos.getZ());
					tags.setInteger("sideHit", EnumFacing.UP.getIndex());
				} else if (mc.world.isAirBlock(oSidePos) && (state.isSideSolid(mc.world, oPos, sideHit) || state.getBlock().canPlaceTorchOnTop(state, mc.world, oPos))) {
					tags.setInteger("x", oSidePos.getX());
					tags.setInteger("y", oSidePos.getY());
					tags.setInteger("z", oSidePos.getZ());
					tags.setInteger("sideHit", sideHit.getIndex());
				} else {
					tags.setInteger("x", oPos.getX());
					tags.setInteger("y", oPos.getY());
					tags.setInteger("z", oPos.getZ());
					tags.setInteger("sideHit", sideHit.getIndex());
				}
				
				MinersAdvantage.instance.network.sendToServer(new NetworkPacket(tags));
			}
		}
	}
}
