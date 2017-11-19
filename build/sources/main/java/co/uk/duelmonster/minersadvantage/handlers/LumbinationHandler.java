package co.uk.duelmonster.minersadvantage.handlers;

import java.util.Arrays;
import java.util.Collection;

import com.google.gson.JsonObject;

import co.uk.duelmonster.minersadvantage.client.ClientFunctions;
import co.uk.duelmonster.minersadvantage.common.Functions;
import co.uk.duelmonster.minersadvantage.common.Variables;
import co.uk.duelmonster.minersadvantage.packets.PacketBase;
import co.uk.duelmonster.minersadvantage.settings.Settings;
import co.uk.duelmonster.minersadvantage.workers.AgentProcessor;
import co.uk.duelmonster.minersadvantage.workers.LumbinationAgent;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.oredict.OreDictionary;

public class LumbinationHandler implements IPacketHandler {
	
	public static LumbinationHandler instance = new LumbinationHandler();
	
	private EntityPlayer	player;
	private Settings		settings;
	
	public int	iTreeWidthPlusX	= 0, iTreeWidthMinusX = 0;
	public int	iTreeWidthPlusZ	= 0, iTreeWidthMinusZ = 0;
	
	private static boolean	bLogsGot	= false;
	private static boolean	bLeavesGot	= false;
	private static boolean	bAxesGot	= false;
	
	@Override
	public void processClientMessage(PacketBase message, MessageContext context) {
		player = ClientFunctions.getPlayer();
		settings = Settings.get();
		
		Variables.get().IsLumbinating = true;
	}
	
	@Override
	public void processServerMessage(PacketBase message, MessageContext context) {
		player = context.getServerHandler().player;
		if (player == null)
			return;
		
		settings = Settings.get(player.getUniqueID());
		
		if (message.getTags().getBoolean("cancel")) {
			player.getServer().addScheduledTask(new Runnable() {
				@Override
				public void run() {
					AgentProcessor.instance.stopProcessing((EntityPlayerMP) player);
				}
			});
			return;
		}
		
		player.getServer().addScheduledTask(new Runnable() {
			@Override
			public void run() {
				AgentProcessor.instance.startProcessing((EntityPlayerMP) player, new LumbinationAgent((EntityPlayerMP) player, message.getTags()));
			}
		});
	}
	
	public static void getLumbinationLists() {
		getLogList();
		getLeafList();
		getAxeList();
	}
	
	private static void getLogList() {
		if (!bLogsGot) {
			// Check the Forge OreDictionary for any extra Logs and/or Leaves not included in the config file.
			Collection<String> saOreNames = Arrays.asList(OreDictionary.getOreNames());
			
			// Get Logs
			JsonObject lumbinationLogs = Settings.get().lumbinationLogs;
			
			saOreNames.stream()
					.filter(log -> log.toLowerCase().startsWith("log"))
					.forEach(log -> {
						OreDictionary.getOres(log).stream()
								.filter(item -> item.getItem() instanceof ItemBlock).forEach(item -> {
									String sID = item.getItem().getRegistryName().toString().trim();
									if (!lumbinationLogs.has(sID))
										lumbinationLogs.add(sID, null);
								});
					});
		}
	}
	
	private static void getLeafList() {
		if (!bLeavesGot) {
			// Check the Forge OreDictionary for any extra Logs and/or Leaves not included in the config file.
			Collection<String> saOreNames = Arrays.asList(OreDictionary.getOreNames());
			
			// Get Leaves
			JsonObject lumbinationLeaves = Settings.get().lumbinationLeaves;
			
			saOreNames.stream()
					.filter(leaves -> leaves.toLowerCase().endsWith("leaves"))
					.forEach(leaves -> {
						OreDictionary.getOres(leaves).stream()
								.filter(item -> item.getItem() instanceof ItemBlock).forEach(item -> {
									String sID = item.getItem().getRegistryName().toString().trim();
									if (!lumbinationLeaves.has(sID))
										lumbinationLeaves.add(sID, null);
								});
					});
			
			bLeavesGot = true;
		}
	}
	
	private static void getAxeList() {
		if (!bAxesGot) {
			// Get Axes
			JsonObject lumbinationAxes = Settings.get().lumbinationAxes;
			
			Item.REGISTRY.forEach(item -> {
				if (item instanceof ItemAxe)
					lumbinationAxes.add(item.getRegistryName().toString().trim(), null);
			});
			
			bAxesGot = true;
		}
	}
	
	public AxisAlignedBB identifyTree(BlockPos oPos, Block block) {
		World world = player.world;
		int iTreeMinY = getTreeRoot(oPos, block), iTreeMaxY = oPos.getY();
		
		if (block.isWood(world, oPos)) {
			BlockPos leafPos = getLeafPos(oPos);
			Block leafBlock = world.getBlockState(leafPos).getBlock();
			
			int yLevel = leafPos.getY();
			iTreeMaxY = yLevel;
			int yLevelPrev = 0;
			int iLeafRangeX = settings.iLeafRange;
			int iLeafRangeZ = settings.iLeafRange;
			
			for (int yLeaf = yLevel; yLeaf < world.getHeight(); yLeaf++) {
				
				int iWidthPlusX = 0, iWidthMinusX = 0;
				int iWidthPlusZ = 0, iWidthMinusZ = 0;
				int iAirCount = 0;
				
				for (int xOffset = -iLeafRangeX; xOffset <= iLeafRangeX; xOffset++) {
					BlockPos checkPos = new BlockPos(oPos.getX() + xOffset, yLevel, oPos.getZ());
					Block checkBlock = world.getBlockState(checkPos).getBlock();
					
					if (checkBlock.getClass().isInstance(leafBlock)) {
						if (xOffset < 0)
							iWidthMinusX++;
						else if (xOffset > 0)
							iWidthPlusX++;
						
						if (yLeaf != yLevelPrev) {
							yLevelPrev = yLeaf;
							iTreeMaxY++;
						}
					} else if (xOffset != 0 && checkBlock.isWood(world, checkPos)) {
						if (xOffset < 0)
							iWidthMinusX++;
						else if (xOffset > 0)
							iWidthPlusX++;
						
						iLeafRangeX++;
					} else
						iAirCount++;
				}
				
				for (int zOffset = -iLeafRangeZ; zOffset <= iLeafRangeZ; zOffset++) {
					BlockPos checkPos = new BlockPos(oPos.getX(), yLevel, oPos.getZ() + zOffset);
					Block checkBlock = world.getBlockState(checkPos).getBlock();
					
					if (checkBlock.getClass().isInstance(leafBlock)) {
						if (zOffset < 0)
							iWidthMinusZ++;
						else if (zOffset > 0)
							iWidthPlusZ++;
						
						if (yLeaf != yLevelPrev) {
							yLevelPrev = yLeaf;
							iTreeMaxY++;
						}
					} else if (zOffset != 0 && checkBlock.isWood(world, checkPos)) {
						if (zOffset < 0)
							iWidthMinusZ++;
						else if (zOffset > 0)
							iWidthPlusZ++;
						
						iLeafRangeZ++;
					} else
						iAirCount++;
				}
				
				if (iWidthPlusX > iTreeWidthPlusX)
					iTreeWidthPlusX = iWidthPlusX;
				if (iWidthMinusX > iTreeWidthMinusX)
					iTreeWidthMinusX = iWidthMinusX;
				if (iWidthPlusZ > iTreeWidthPlusZ)
					iTreeWidthPlusZ = iWidthPlusZ;
				if (iWidthMinusZ > iTreeWidthMinusZ)
					iTreeWidthMinusZ = iWidthMinusZ;
				
				yLevelPrev = yLeaf;
				
				if (yLeaf > yLevel && getTotalLayerCount() > iLeafRangeX * iLeafRangeZ && iAirCount >= getTotalLayerCount())
					break;
			}
		}
		
		return new AxisAlignedBB(
				oPos.getX() + iTreeWidthPlusX, iTreeMinY, oPos.getZ() + iTreeWidthPlusZ,
				oPos.getX() - iTreeWidthMinusX, iTreeMaxY, oPos.getZ() - iTreeWidthMinusZ);
	}
	
	public BlockPos getLeafPos(BlockPos oPos) {
		for (int yLeaf = oPos.getY(); yLeaf < player.world.getHeight(); yLeaf++)
			for (int xLeaf = -1; xLeaf < 1; xLeaf++)
				for (int zLeaf = -1; zLeaf < 1; zLeaf++) {
					BlockPos leafPos = new BlockPos(oPos.getX() + xLeaf, yLeaf, oPos.getZ() + zLeaf);
					IBlockState leafState = player.world.getBlockState(leafPos);
					Block leafBlock = leafState.getBlock();
					
					if (leafState.getMaterial() == Material.LEAVES && settings.lumbinationLeaves.has(Functions.getBlockName(leafBlock)))
						return leafPos;
				}
		return null;
	}
	
	private int getTotalLayerCount() {
		int iTotalCountX = iTreeWidthPlusX + iTreeWidthMinusX;
		int iTotalCountZ = iTreeWidthPlusZ + iTreeWidthMinusZ;
		
		return iTotalCountX * iTotalCountZ;
	}
	
	private int getTreeRoot(BlockPos oPos, Block block) {
		for (int y = oPos.getY() - 1; y > 0; y--) {
			Block checkBlock = player.world.getBlockState(new BlockPos(oPos.getX(), y, oPos.getZ())).getBlock();
			if (!checkBlockgetClass().isInstance(block))
				return y + 1;
		}
		return oPos.getY();
	}
	
}
