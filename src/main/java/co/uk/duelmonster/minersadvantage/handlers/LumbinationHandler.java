package co.uk.duelmonster.minersadvantage.handlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.google.gson.JsonObject;

import co.uk.duelmonster.minersadvantage.client.ClientFunctions;
import co.uk.duelmonster.minersadvantage.common.Constants;
import co.uk.duelmonster.minersadvantage.common.Functions;
import co.uk.duelmonster.minersadvantage.common.Variables;
import co.uk.duelmonster.minersadvantage.packets.NetworkPacket;
import co.uk.duelmonster.minersadvantage.settings.ConfigHandler;
import co.uk.duelmonster.minersadvantage.settings.Settings;
import co.uk.duelmonster.minersadvantage.workers.AgentProcessor;
import co.uk.duelmonster.minersadvantage.workers.LumbinationAgent;
import net.minecraft.block.Block;
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
	
	private World			world;
	private EntityPlayer	player;
	private Settings		settings;
	
	public int				iTreeWidthPlusX	= 0, iTreeWidthMinusX = 0;
	public int				iTreeWidthPlusZ	= 0, iTreeWidthMinusZ = 0;
	public int				iTrunkWidth		= 0, iTrunkTopY = 0;
	public int				iTreeRootY		= 0;
	public AxisAlignedBB	trunkArea		= null;
	private static boolean	bLogsGot		= false;
	private static boolean	bLeavesGot		= false;
	private static boolean	bAxesGot		= false;
	
	@Override
	public void processClientMessage(NetworkPacket message, MessageContext context) {
		player = ClientFunctions.getPlayer();
		world = player.world;
		settings = Settings.get();
		
		Variables.get().IsLumbinating = true;
	}
	
	@Override
	public void processServerMessage(NetworkPacket message, MessageContext context) {
		player = context.getServerHandler().player;
		if (player == null)
			return;
		
		world = player.world;
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
		
		ConfigHandler.save();
	}
	
	private static void getLogList() {
		if (!bLogsGot) {
			// Check the Forge OreDictionary for any extra Logs and/or Leaves not included in the config file.
			Collection<String> saOreNames = Arrays.asList(OreDictionary.getOreNames());
			
			// Get Logs
			JsonObject lumbinationLogs = Settings.get().lumbinationLogs();
			
			saOreNames.stream()
					.filter(log -> log.toLowerCase().startsWith("log"))
					.forEach(log -> {
						OreDictionary.getOres(log).stream()
								.filter(item -> item.getItem() instanceof ItemBlock).forEach(item -> {
									String sID = item.getItem().getRegistryName().toString().trim();
									if (!lumbinationLogs.has(sID))
										lumbinationLogs.addProperty(sID, "");
								});
					});
			
			ConfigHandler.setValue(Constants.LUMBINATION_ID, "logs", lumbinationLogs.toString());
			
			bLogsGot = true;
		}
	}
	
	private static void getLeafList() {
		if (!bLeavesGot) {
			// Check the Forge OreDictionary for any extra Logs and/or Leaves not included in the config file.
			Collection<String> saOreNames = Arrays.asList(OreDictionary.getOreNames());
			
			// Get Leaves
			JsonObject lumbinationLeaves = Settings.get().lumbinationLeaves();
			
			saOreNames.stream()
					.filter(leaves -> leaves.toLowerCase().endsWith("leaves"))
					.forEach(leaves -> {
						OreDictionary.getOres(leaves).stream()
								.filter(item -> item.getItem() instanceof ItemBlock).forEach(item -> {
									String sID = item.getItem().getRegistryName().toString().trim();
									if (!lumbinationLeaves.has(sID))
										lumbinationLeaves.addProperty(sID, "");
								});
					});
			
			ConfigHandler.setValue(Constants.LUMBINATION_ID, "leaves", lumbinationLeaves.toString());
			
			bLeavesGot = true;
		}
	}
	
	private static void getAxeList() {
		if (!bAxesGot) {
			// Get Axes
			JsonObject lumbinationAxes = Settings.get().lumbinationAxes();
			
			Item.REGISTRY.forEach(item -> {
				if (item instanceof ItemAxe)
					lumbinationAxes.addProperty(item.getRegistryName().toString().trim(), "");
			});
			
			ConfigHandler.setValue(Constants.LUMBINATION_ID, "axes", lumbinationAxes.toString());
			
			bAxesGot = true;
		}
	}
	
	public AxisAlignedBB identifyTree(BlockPos oPos, Block block) {
		AxisAlignedBB rtrnBB = new AxisAlignedBB(oPos, oPos);
		
		if (block.isWood(world, oPos)) {
			BlockPos leafPos = getLeafPos(oPos);
			if (leafPos != null) { // Leaves were found so treat this as a valid tree.
				iTreeRootY = getTreeRoot(oPos, block);
				trunkArea = getTrunkSize(oPos, block);
				
				rtrnBB = trunkArea
						.expand(settings.iLeafRange() - 1, settings.iLeafRange(), settings.iLeafRange() - 1)
						.expand(-(settings.iLeafRange() - 1), 0, -(settings.iLeafRange() - 1));
				
			}
		}
		return rtrnBB;
	}
	
	public BlockPos getLeafPos(BlockPos oPos) {
		for (int yLeaf = oPos.getY(); yLeaf < player.world.getHeight(); yLeaf++)
			for (int xLeaf = -1; xLeaf < 1; xLeaf++)
				for (int zLeaf = -1; zLeaf < 1; zLeaf++) {
					BlockPos leafPos = new BlockPos(oPos.getX() + xLeaf, yLeaf, oPos.getZ() + zLeaf);
					IBlockState leafState = player.world.getBlockState(leafPos);
					Block leafBlock = leafState.getBlock();
					
					if (leafBlock.isLeaves(leafState, world, leafPos) && settings.lumbinationLeaves().has(Functions.getBlockName(leafBlock)))
						return leafPos;
				}
		return null;
	}
	
	private int getTreeRoot(BlockPos oPos, Block block) {
		int iRootLevel = oPos.getY();
		for (int yLevel = oPos.getY() - 1; yLevel > 0; yLevel--) {
			BlockPos checkPos = new BlockPos(oPos.getX(), yLevel, oPos.getZ());
			Block checkBlock = player.world.getBlockState(checkPos).getBlock();
			if (checkBlock.getClass().isInstance(block) || checkBlock.isWood(world, checkPos) || settings.lumbinationLogs().has(Functions.getBlockName(checkBlock)))
				iRootLevel = yLevel;
			
			if (yLevel < iRootLevel)
				break;
		}
		return iRootLevel;
	}
	
	private AxisAlignedBB getTrunkSize(BlockPos oPos, Block block) {
		List<BlockPos> trunkBlocks = new ArrayList<BlockPos>();
		
		for (int iPass = 0; iPass < 2; iPass++)
			for (int yLevel = iTreeRootY; yLevel < world.getHeight(); yLevel++) {
				int iAirCount = 0;
				int iLayerPosCount = trunkBlocks.size();
				BlockPos checkPos = new BlockPos(oPos.getX(), yLevel, oPos.getZ());
				IBlockState checkState = player.world.getBlockState(checkPos);
				Block checkBlock = checkState.getBlock();
				
				if (checkPos.equals(oPos) || checkBlock.getClass().isInstance(block) || checkBlock.isWood(world, checkPos) || settings.lumbinationLogs().has(Functions.getBlockName(checkBlock)))
					if (!trunkBlocks.contains(checkPos)) // && Functions.isPosConnected(trunkBlocks, checkPos))
						trunkBlocks.add(checkPos);
					
				for (int iLoop = 1; iLoop <= 8; iLoop++) {
					int iLoopAirLimit = (((iLoop + (iLoop - 1)) * 4) + 4);
					
					for (int xOffset = -iLoop; xOffset <= iLoop; xOffset++) {
						if (xOffset == -iLoop || xOffset == iLoop) {
							for (int zOffset = -iLoop; zOffset <= iLoop; zOffset++) {
								checkPos = new BlockPos(oPos.getX() + xOffset, yLevel, oPos.getZ() + zOffset);
								checkState = player.world.getBlockState(checkPos);
								checkBlock = checkState.getBlock();
								
								if (Functions.isPosConnected(trunkBlocks, checkPos)
										&& (checkBlock.getClass().isInstance(block)
												|| checkBlock.isWood(world, checkPos)
												|| settings.lumbinationLogs().has(Functions.getBlockName(checkBlock)))) {
									
									if (!trunkBlocks.contains(checkPos))
										trunkBlocks.add(checkPos);
									
									if (iLoop > iTrunkWidth)
										iTrunkWidth = iLoop;
									
								} else //if (checkBlock.isLeaves(checkState, world, checkPos) && settings.lumbinationLeaves().has(Functions.getBlockName(checkBlock)))
									iAirCount++;
							}
						} else {
							for (int zOffset = -iLoop; zOffset <= iLoop; zOffset++) {
								if (zOffset == -iLoop || zOffset == iLoop) {
									checkPos = new BlockPos(oPos.getX() + xOffset, yLevel, oPos.getZ() + zOffset);
									checkState = player.world.getBlockState(checkPos);
									checkBlock = checkState.getBlock();
									
									if (Functions.isPosConnected(trunkBlocks, checkPos)
											&& (checkBlock.getClass().isInstance(block)
													|| checkBlock.isWood(world, checkPos)
													|| settings.lumbinationLogs().has(Functions.getBlockName(checkBlock)))) {
										
										if (!trunkBlocks.contains(checkPos))
											trunkBlocks.add(checkPos);
										
										if (iLoop > iTrunkWidth)
											iTrunkWidth = iLoop;
										
									} else //if (checkBlock.isLeaves(checkState, world, checkPos) || settings.lumbinationLeaves().has(Functions.getBlockName(checkBlock)))
										iAirCount++;
								}
							}
						}
					}
					
					if (!trunkBlocks.isEmpty() && yLevel > iTrunkTopY)
						iTrunkTopY = yLevel;
					
					if (iAirCount >= iLoopAirLimit)
						break;
					
				}
				
				if (iLayerPosCount == trunkBlocks.size())
					break;
			}
		
		return new AxisAlignedBB(
				oPos.getX() + iTrunkWidth, iTreeRootY, oPos.getZ() + iTrunkWidth,
				oPos.getX() - iTrunkWidth, iTrunkTopY, oPos.getZ() - iTrunkWidth);
		
	}
}
