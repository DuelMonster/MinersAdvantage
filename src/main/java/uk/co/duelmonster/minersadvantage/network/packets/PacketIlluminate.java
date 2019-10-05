package uk.co.duelmonster.minersadvantage.network.packets;

import java.util.function.Supplier;

import net.minecraft.block.Blocks;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import uk.co.duelmonster.minersadvantage.common.Functions;
import uk.co.duelmonster.minersadvantage.helpers.IlluminationHelper;
import uk.co.duelmonster.minersadvantage.network.packetids.PacketId;

public class PacketIlluminate extends BaseBlockPacket {

	public PacketIlluminate(BlockPos _pos, Direction _faceHit) {
		super(_pos, _faceHit);
	}

	public PacketIlluminate(PacketBuffer buf) {
		super(buf);
	}

	@Override
	public PacketId getPacketId() { return PacketId.Illuminate; }

	public static void encode(PacketIlluminate pkt, PacketBuffer buf) {
		buf.writeBlockPos(pkt.pos);
		buf.writeEnumValue(pkt.faceHit);
		buf.writeInt(pkt.stateID);
	}

	public static PacketIlluminate decode(PacketBuffer buf) { return new PacketIlluminate(buf); }

	public static void handle(final PacketIlluminate pkt, Supplier<Context> ctx) {
		ctx.get().enqueueWork(() -> {
			// Work that needs to be threadsafe (most work)
			final ServerPlayerEntity player = ctx.get().getSender(); // the client that sent this packet
			// do stuff
			final World world = player.getEntityWorld();

			IlluminationHelper.getTorchSlot(player);

			if (IlluminationHelper.torchIndx >= 0) {
				IlluminationHelper.lastTorchLocation = new BlockPos(pkt.pos);

				if (pkt.faceHit == Direction.UP) {
					world.setBlockState(pkt.pos, Blocks.TORCH.getDefaultState());
				} else {
					world.setBlockState(pkt.pos, Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.HORIZONTAL_FACING, pkt.faceHit));
				}
				Functions.playSound(world, pkt.pos, SoundEvents.BLOCK_WOOD_HIT, SoundCategory.BLOCKS, 1.0F, world.rand.nextFloat() + 0.5F);

				ItemStack torchStack = player.inventory.decrStackSize(IlluminationHelper.torchIndx, 1);

				if (torchStack.getCount() <= 0) {
					IlluminationHelper.lastTorchLocation = null;
					IlluminationHelper.torchStackCount--;
				}

				if (IlluminationHelper.torchStackCount == 0)
					Functions.NotifyClient(player, TextFormatting.GOLD + "Illumination: " + TextFormatting.WHITE + Functions.localize("minersadvantage.illumination.no_torches"));
			}

		});
	}
}
