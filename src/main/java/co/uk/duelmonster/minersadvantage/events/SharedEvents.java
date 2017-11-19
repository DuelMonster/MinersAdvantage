package co.uk.duelmonster.minersadvantage.events;

import co.uk.duelmonster.minersadvantage.MinersAdvantage;
import co.uk.duelmonster.minersadvantage.common.Functions;
import co.uk.duelmonster.minersadvantage.common.PacketID;
import co.uk.duelmonster.minersadvantage.common.Variables;
import co.uk.duelmonster.minersadvantage.packets.NetworkPacket;
import co.uk.duelmonster.minersadvantage.settings.Settings;
import co.uk.duelmonster.minersadvantage.workers.AgentProcessor;
import co.uk.duelmonster.minersadvantage.workers.DropsSpawner;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.UseHoeEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SharedEvents {
	
	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload event) {
		World world = event.getWorld();
		if (world.isRemote || world.getMinecraftServer().isServerRunning())
			return;
		
		AgentProcessor.instance.resetAgentList();
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onEntitySpawn(EntityJoinWorldEvent event) {
		World world = event.getWorld();
		Entity entity = event.getEntity();
		if (!(entity instanceof EntityLiving)) {
			if (world.isRemote || event.getEntity().isDead || event.isCanceled()
					|| (!(entity instanceof EntityItem) && !(entity instanceof EntityXPOrb))) {
				return;
			}
			
			if (!AgentProcessor.instance.currentAgents.isEmpty()) {
				if (event.getEntity() instanceof EntityItem) {
					EntityItem eItem = (EntityItem) event.getEntity();
					
					DropsSpawner.recordDrop(eItem);
					
					event.setCanceled(true);
				} else if (event.getEntity() instanceof EntityXPOrb) {
					EntityXPOrb orb = (EntityXPOrb) event.getEntity();
					
					DropsSpawner.addXP(orb.getXpValue());
					
					event.setCanceled(true);
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onBlockBreak(BlockEvent.BreakEvent event) {
		
		if (!(event.getPlayer() instanceof EntityPlayerMP) || (event.getPlayer() instanceof FakePlayer))
			return;
		
		World world = event.getWorld();
		EntityPlayerMP player = (EntityPlayerMP) event.getPlayer();
		
		// Ensure that the currently active agent wasn't responsible for this Break Event
		if (!AgentProcessor.instance.currentAgents.isEmpty() &&
				AgentProcessor.instance.getCurrentAgent(player.getUniqueID()) != null &&
				!AgentProcessor.instance.getCurrentAgent(player.getUniqueID()).shouldProcess(event.getPos()))
			return;
		
		ItemStack heldItem = player.getHeldItem(EnumHand.MAIN_HAND);
		if (heldItem.isEmpty())
			return;
		
		if (Functions.isItemBlacklisted(heldItem) ||
				Functions.isBlockBlacklisted(event.getState().getBlock()) ||
				event.getState().getBlock().isAir(event.getState(), world, event.getPos()))
			return;
		
		BlockPos oPos = event.getPos();
		IBlockState state = event.getState();
		
		Settings settings = Settings.get(player.getUniqueID());
		Variables playerVars = Variables.get(player.getUniqueID());
		if (player.canHarvestBlock(state) && playerVars.IsPlayerAttacking) {
			NBTTagCompound tags = new NBTTagCompound();
			
			if (playerVars.IsShaftanationToggled)
				tags.setInteger("ID", PacketID.Shaftanate.value());
			else if (state.getBlock().isWood(world, oPos)
					&& ((heldItem.getItem() instanceof ItemAxe)
							|| settings.lumbinationAxes().has(Functions.getItemName(heldItem))))
				tags.setInteger("ID", PacketID.Lumbinate.value());
			else
				tags.setInteger("ID", PacketID.Excavate.value());
			
			tags.setInteger("x", oPos.getX());
			tags.setInteger("y", oPos.getY());
			tags.setInteger("z", oPos.getZ());
			tags.setInteger("sideHit", Variables.get(player.getUniqueID()).sideHit.getIndex());
			tags.setInteger("stateID", Block.getStateId(state));
			
			MinersAdvantage.instance.network.sendTo(new NetworkPacket(tags), player);
		}
	}
	
	@SubscribeEvent
	public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		World world = event.getWorld();
		if (world.isRemote || !(event.getEntityPlayer() instanceof EntityPlayerMP) || (event.getEntityPlayer() instanceof FakePlayer))
			return;
		
		EntityPlayerMP player = (EntityPlayerMP) event.getEntityPlayer();
		
		if (event.getItemStack().getItem() instanceof ItemSpade) {
			BlockPos oPos = event.getPos();
			Block block = world.getBlockState(oPos).getBlock();
			
			if (block == Blocks.DIRT || block == Blocks.GRASS) {
				NBTTagCompound tags = new NBTTagCompound();
				tags.setInteger("ID", PacketID.LayPath.value());
				tags.setInteger("x", oPos.getX());
				tags.setInteger("y", oPos.getY());
				tags.setInteger("z", oPos.getZ());
				
				MinersAdvantage.instance.network.sendTo(new NetworkPacket(tags), player);
			}
		}
	}
	
	@SubscribeEvent
	public void onUseHoeEvent(UseHoeEvent event) {
		World world = event.getWorld();
		if (world.isRemote || !(event.getEntityPlayer() instanceof EntityPlayerMP) || (event.getEntityPlayer() instanceof FakePlayer))
			return;
		
		EntityPlayerMP player = (EntityPlayerMP) event.getEntityPlayer();
		
		BlockPos oPos = event.getPos();
		Block block = world.getBlockState(oPos).getBlock();
		NBTTagCompound tags = new NBTTagCompound();
		tags.setInteger("x", oPos.getX());
		tags.setInteger("y", oPos.getY());
		tags.setInteger("z", oPos.getZ());
		
		if (block == Blocks.DIRT || block == Blocks.GRASS) {
			tags.setInteger("ID", PacketID.TileFarmland.value());
			
			MinersAdvantage.instance.network.sendTo(new NetworkPacket(tags), player);
		} else if (block instanceof IPlantable) {
			tags.setInteger("ID", PacketID.HarvestCrops.value());
			
			MinersAdvantage.instance.network.sendTo(new NetworkPacket(tags), player);
		}
	}
}
