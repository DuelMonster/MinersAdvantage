package uk.co.duelmonster.minersadvantage.helpers;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.RotatedPillarBlock;
import net.minecraft.block.SaplingBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShearsItem;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import uk.co.duelmonster.minersadvantage.common.Functions;
import uk.co.duelmonster.minersadvantage.config.MAConfig_Base;
import uk.co.duelmonster.minersadvantage.config.MAConfig_Client;
import uk.co.duelmonster.minersadvantage.config.SyncedClientConfig;
import uk.co.duelmonster.minersadvantage.workers.DropsSpawner;

public class LumbinationHelper {
	
	private World			world;
	private PlayerEntity	player;
	
	public int				iTreeWidthPlusX	= 0, iTreeWidthMinusX = 0;
	public int				iTreeWidthPlusZ	= 0, iTreeWidthMinusZ = 0;
	public int				iTrunkWidth		= 0, iTrunkTopY = 0;
	public int				iTreeRootY		= 0;
	public AxisAlignedBB	trunkArea		= null;
	public List<BlockPos>	trunkPositions	= new ArrayList<BlockPos>();
	private AxisAlignedBB	plantationArea	= null;
	private ItemStack		oShears			= null;
	
	public void setPlayer(PlayerEntity player) {
		this.player = player;
		this.world = player.world;
	}
	
	public boolean isWood(BlockState state) {
		return state.isIn(BlockTags.LOGS)
				|| state.isIn(BlockTags.PLANKS)
				|| state.isIn(BlockTags.WOODEN_BUTTONS)
				|| state.isIn(BlockTags.WOODEN_DOORS)
				|| state.isIn(BlockTags.WOODEN_FENCES)
				|| state.isIn(BlockTags.WOODEN_PRESSURE_PLATES)
				|| state.isIn(BlockTags.WOODEN_SLABS)
				|| state.isIn(BlockTags.WOODEN_STAIRS)
				|| state.isIn(BlockTags.WOODEN_TRAPDOORS)
				|| state.getMaterial() == Material.WOOD;
	}
	
	public boolean isValidLog(BlockState state) {
		SyncedClientConfig clientConfig = MAConfig_Base.getPlayerConfig(player.getUniqueID());
		
		return state.isIn(BlockTags.LOGS)
				|| (state.getBlock() instanceof RotatedPillarBlock
						&& state.getMaterial() == Material.WOOD)
				|| (clientConfig.lumbination.logs != null
						&& clientConfig.lumbination.logs.isEmpty() == false
						&& clientConfig.lumbination.logs.contains(Functions.getName(state.getBlock().asItem())));
	}
	
	public boolean isValidLeaves(BlockState state) {
		SyncedClientConfig clientConfig = MAConfig_Base.getPlayerConfig(player.getUniqueID());
		
		return state.isIn(BlockTags.LEAVES)
				|| (state.getBlock() instanceof LeavesBlock
						&& state.getMaterial() == Material.LEAVES)
				|| (clientConfig.lumbination.leaves != null
						&& clientConfig.lumbination.leaves.isEmpty() == false
						&& clientConfig.lumbination.leaves.contains(Functions.getName(state.getBlock().asItem())));
	}
	
	public boolean isValidAxe(Item heldItem) {
		SyncedClientConfig clientConfig = MAConfig_Base.getPlayerConfig(player.getUniqueID());
		
		return (heldItem instanceof AxeItem)
				|| (clientConfig.lumbination.axes != null
						&& clientConfig.lumbination.axes.isEmpty() == false
						&& clientConfig.lumbination.axes.contains(Functions.getName(heldItem)));
	}
	
	public AxisAlignedBB identifyTree(BlockPos oPos, BlockState state) {
		AxisAlignedBB rtrnBB = new AxisAlignedBB(oPos, oPos);
		Block block = state.getBlock();
		
		if (isValidLog(state)) {
			BlockPos leafPos = getLeafPos(oPos);
			if (leafPos != null) { // Leaves were found so treat this as a valid tree.
				iTreeRootY = getTreeRoot(oPos, block);
				trunkArea = getTrunkSize(oPos, block);
				
				SyncedClientConfig clientConfig = MAConfig_Client.getPlayerConfig(player.getUniqueID());
				
				int iLeafRangeIncrease = clientConfig.lumbination.leafRange / 2;
				
				rtrnBB = trunkArea
						.expand(iLeafRangeIncrease, clientConfig.lumbination.leafRange, iLeafRangeIncrease)
						.expand(-iLeafRangeIncrease, 0, -iLeafRangeIncrease);
				
			}
		}
		return rtrnBB;
	}
	
	public BlockPos getLeafPos(BlockPos oPos) {
		for (int yLeaf = oPos.getY(); yLeaf < player.world.getHeight(); yLeaf++)
			for (int xLeaf = -1; xLeaf < 1; xLeaf++)
				for (int zLeaf = -1; zLeaf < 1; zLeaf++) {
					BlockPos leafPos = new BlockPos(oPos.getX() + xLeaf, yLeaf, oPos.getZ() + zLeaf);
					BlockState leafState = player.world.getBlockState(leafPos);
					
					if (isValidLeaves(leafState))
						return leafPos;
				}
		return null;
	}
	
	private int getTreeRoot(BlockPos oPos, Block wood) {
		int iRootLevel = oPos.getY();
		// Get lowest point of the tree trunk
		for (int yLevel = oPos.getY() - 1; yLevel > 0; yLevel--) {
			BlockPos checkPos = new BlockPos(oPos.getX(), yLevel, oPos.getZ());
			BlockState checkState = player.world.getBlockState(checkPos);
			
			if (isValidLog(checkState))
				iRootLevel = yLevel;
			
			if (yLevel < iRootLevel)
				break;
		}
		
		// Get the plantation area for sapling re-planting if required
		SyncedClientConfig clientConfig = MAConfig_Client.getPlayerConfig(player.getUniqueID());
		
		if (clientConfig.lumbination.replantSaplings)
			plantationArea = getPlantationArea(new BlockPos(oPos.getX(), iRootLevel, oPos.getZ()), wood);
		
		return iRootLevel;
	}
	
	private AxisAlignedBB getPlantationArea(BlockPos oPos, Block wood) {
		// Retrieve tree root positions
		List<BlockPos> areaPositions = getPlantationAreaPositions(new ArrayList<BlockPos>(), oPos, wood);
		
		// Identify Start and End positions
		int iStartX = 0, iEndX = 0, iStartZ = 0, iEndZ = 0;
		for (BlockPos oCheckPos : areaPositions) {
			if (iStartX == 0 || oCheckPos.getX() < iStartX)
				iStartX = oCheckPos.getX();
			if (iStartZ == 0 || oCheckPos.getZ() < iStartZ)
				iStartZ = oCheckPos.getZ();
			if (iEndX == 0 || oCheckPos.getX() > iEndX)
				iEndX = oCheckPos.getX();
			if (iEndZ == 0 || oCheckPos.getZ() > iEndZ)
				iEndZ = oCheckPos.getZ();
		}
		
		return new AxisAlignedBB(new BlockPos(iStartX, oPos.getY(), iStartZ), new BlockPos(iEndX, oPos.getY(), iEndZ));
	}
	
	private List<BlockPos> getPlantationAreaPositions(List<BlockPos> pAreaPositions, BlockPos oPos, Block wood) {
		
		if (!pAreaPositions.contains(oPos))
			pAreaPositions.add(oPos);
		
		for (int xOffset = -1; xOffset <= 1; xOffset++)
			for (int zOffset = -1; zOffset <= 1; zOffset++) {
				BlockPos checkPos = new BlockPos(oPos.getX() + xOffset, oPos.getY(), oPos.getZ() + zOffset);
				BlockState checkState = player.world.getBlockState(checkPos);
				if (!pAreaPositions.contains(checkPos) &&
						isValidLog(checkState)) {
					pAreaPositions.add(checkPos);
					getPlantationAreaPositions(pAreaPositions, checkPos, wood);
				}
			}
		
		return pAreaPositions;
	}
	
	private AxisAlignedBB getTrunkSize(BlockPos oPos, Block block) {
		SyncedClientConfig clientConfig = MAConfig_Client.getPlayerConfig(player.getUniqueID());
		
		for (int iPass = 0; iPass < 2; iPass++)
			for (int yLevel = iTreeRootY; yLevel < world.getHeight(); yLevel++) {
				int iAirCount = 0;
				int iLayerPosCount = trunkPositions.size();
				BlockPos checkPos = new BlockPos(oPos.getX(), yLevel, oPos.getZ());
				BlockState checkState = player.world.getBlockState(checkPos);
				Block checkBlock = checkState.getBlock();
				
				if ((checkPos.equals(oPos) || isValidLog(checkState)) && !trunkPositions.contains(checkPos)) // &&
																										// Functions.isPosConnected(trunkPositions,
																										// checkPos))
					trunkPositions.add(checkPos);
				
				for (int iLoop = 1; iLoop <= clientConfig.lumbination.trunkRange; iLoop++) {
					int iLoopAirLimit = (((iLoop + (iLoop - 1)) * 4) + 4);
					iAirCount = 0;
					
					for (int xOffset = -iLoop; xOffset <= iLoop; xOffset++) {
						if (xOffset == -iLoop || xOffset == iLoop) {
							for (int zOffset = -iLoop; zOffset <= iLoop; zOffset++) {
								checkPos = new BlockPos(oPos.getX() + xOffset, yLevel, oPos.getZ() + zOffset);
								checkState = player.world.getBlockState(checkPos);
								checkBlock = checkState.getBlock();
								
								if (isValidLog(checkState)) {
									
									if (!trunkPositions.contains(checkPos))
										trunkPositions.add(checkPos);
									
									if (iLoop > iTrunkWidth)
										iTrunkWidth = iLoop;
									
								} else // if (isLeaves(checkState) &&
										// clientConfig.lumbinationLeaves.has(Functions.getBlockName(checkBlock)))
									iAirCount++;
							}
						} else {
							for (int zOffset = -iLoop; zOffset <= iLoop; zOffset++) {
								if (zOffset == -iLoop || zOffset == iLoop) {
									checkPos = new BlockPos(oPos.getX() + xOffset, yLevel, oPos.getZ() + zOffset);
									checkState = player.world.getBlockState(checkPos);
									checkBlock = checkState.getBlock();
									
									if (isValidLog(checkState) || clientConfig.lumbination.logs.contains(Functions.getName(checkBlock))) {
										
										if (!trunkPositions.contains(checkPos))
											trunkPositions.add(checkPos);
										
										if (iLoop > iTrunkWidth)
											iTrunkWidth = iLoop;
										
									} else // if (checkBlock.isLeaves(checkState, world, checkPos)
										iAirCount++;
								}
							}
						}
					}
					
					if (!trunkPositions.isEmpty() && yLevel > iTrunkTopY)
						iTrunkTopY = yLevel;
					
					if (iAirCount >= iLoopAirLimit)
						break;
					
				}
				
				if (iLayerPosCount == trunkPositions.size())
					break;
			}
		
		// Identify the top of the Tree trunk
		trunkPositions.forEach(log -> {
			if (log.getY() > iTrunkTopY)
				iTrunkTopY = log.getY();
		});
		
		return new AxisAlignedBB(
				oPos.getX() + iTrunkWidth, iTreeRootY, oPos.getZ() + iTrunkWidth,
				oPos.getX() - iTrunkWidth, iTrunkTopY, oPos.getZ() - iTrunkWidth);
		
	}
	
	public void replantSaplings(List<Entity> dropsHistory, BlockState leafState, BlockPos leafPos) {
		if (plantationArea != null) {
			ItemStack saplingStack = null;
			
			BlockItem saplingItem = DropsSpawner.getDropOfBlockTypeFromList(SaplingBlock.class, dropsHistory);
			
			// Get sapling(s) from players inventory
			List<ItemStack> allSaplingStacks = Functions.getAllStacksOfClassTypeFromInventory(player.inventory, SaplingBlock.class);
			
			// Ensure the sapling stacks returned match the tree type being harvested
			for (ItemStack stack : allSaplingStacks) {
				if (stack.getItem().equals(saplingItem)) {
					saplingStack = stack;
					break;
				}
			}
			
			if (saplingStack != null) {
				Block oPlantable = ((BlockItem) saplingStack.getItem()).getBlock();
				
				int iPlantedCount = 0;
				
				// Navigate the sapling plantation area
				for (double x = plantationArea.minX; x <= plantationArea.maxX; x++)
					for (double z = plantationArea.minZ; z <= plantationArea.maxZ; z++) {
						// Ensure the sapling can be planted on the block below
						BlockPos oPos = new BlockPos(x, iTreeRootY, z);
						if (!world.isAirBlock(oPos.down())
								&& Functions.canSustainPlant(world, oPos.down(), (IPlantable) oPlantable)
								&& (world.isAirBlock(oPos) || world.getBlockState(oPos).getMaterial().isReplaceable())) {
							// Re-plant a sapling
							world.setBlockState(oPos, oPlantable.getDefaultState().with(SaplingBlock.STAGE, Integer.valueOf(0)));
							Functions.playSound(world, oPos, SoundEvents.BLOCK_GRASS_PLACE, SoundCategory.BLOCKS, 1.0F, world.rand.nextFloat() + 0.5F);
							iPlantedCount++;
						}
					}
				
				// Decrease the size of the sapling stack be one
				player.inventory.decrStackSize(Functions.getSlotFromInventory(player, saplingStack), iPlantedCount);
			}
		}
	}
	
	public boolean playerHasShears() {
		return getPlayersShears() != null;
	}
	
	public ItemStack getPlayersShears() {
		if (oShears == null)
			oShears = Functions.getStackOfClassTypeFromHotBar(player.inventory, ShearsItem.class);
		
		return oShears;
	}
	
}
