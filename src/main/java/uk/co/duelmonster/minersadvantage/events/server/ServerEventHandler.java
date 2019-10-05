package uk.co.duelmonster.minersadvantage.events.server;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropsBlock;
import net.minecraft.block.IGrowable;
import net.minecraft.block.NetherWartBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.UseHoeEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import uk.co.duelmonster.minersadvantage.common.Variables;
import uk.co.duelmonster.minersadvantage.config.MAConfig;
import uk.co.duelmonster.minersadvantage.network.NetworkHandler;
import uk.co.duelmonster.minersadvantage.network.packets.IMAPacket;
import uk.co.duelmonster.minersadvantage.network.packets.PacketCropinate;
import uk.co.duelmonster.minersadvantage.network.packets.PacketCultivate;
import uk.co.duelmonster.minersadvantage.network.packets.PacketExcavate;
import uk.co.duelmonster.minersadvantage.network.packets.PacketLumbinate;
import uk.co.duelmonster.minersadvantage.network.packets.PacketShaftanate;
import uk.co.duelmonster.minersadvantage.network.packets.PacketVeinate;
import uk.co.duelmonster.minersadvantage.workers.AgentProcessor;
import uk.co.duelmonster.minersadvantage.workers.DropsSpawner;

@Mod.EventBusSubscriber
public class ServerEventHandler {

//	@SubscribeEvent
//	// Server Starting event
//	public static void onServerStarting(FMLServerStartingEvent event) {}
//
//	@SubscribeEvent
//	// Server Started event
//	public static void onServerStarted(FMLServerStartedEvent event) {}
//
//	@SubscribeEvent
//	// Server Stopping event
//	public static void onServerStopping(FMLServerStoppingEvent event) {}

	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase != TickEvent.Phase.END)
			return;

		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if (null == server)
			return;

		List<ServerPlayerEntity> players = server.getPlayerList().getPlayers();
		if (players != null && !players.isEmpty()) {
			for (ServerPlayerEntity player : players) {
				Variables vars = Variables.get(player.getUniqueID());

				if (vars != null) {
					if (vars.skipNext) {
						vars.skipNext = false;
						return;
					} else {
						AgentProcessor.INSTANCE.fireAgentTicks(player.world);
						AgentProcessor.INSTANCE.setCurrentAgent(player.getUniqueID(), null);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public static void onWorldUnload(WorldEvent.Unload event) {
		World world = (World)event.getWorld();
		if (world.isRemote || world.getServer().isServerRunning())
			return;

		AgentProcessor.INSTANCE.resetAgentList();
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onEntitySpawn(EntityJoinWorldEvent event) {
		World world = event.getWorld();
		Entity entity = event.getEntity();
		if (!(entity instanceof LivingEntity)) {
			if (world.isRemote
					|| !event.getEntity().isAlive()
					|| event.isCanceled()
					|| (!(entity instanceof ItemEntity) && !(entity instanceof ExperienceOrbEntity))) { return; }

			if (!AgentProcessor.INSTANCE.currentAgents.isEmpty()) {
				if (event.getEntity() instanceof ItemEntity) {
					ItemEntity eItem = (ItemEntity)event.getEntity();

					DropsSpawner.recordDrop(eItem);

					event.setCanceled(true);
				} else if (event.getEntity() instanceof ExperienceOrbEntity) {
					ExperienceOrbEntity orb = (ExperienceOrbEntity)event.getEntity();

					DropsSpawner.addXP(orb.getXpValue());

					event.setCanceled(true);
				}
			}
		}
	}

	@SubscribeEvent
	public static void onBlockBreak(BlockEvent.BreakEvent event) {

		if (!(event.getPlayer() instanceof ServerPlayerEntity) || (event.getPlayer() instanceof FakePlayer))
			return;

		World world = (World)event.getWorld();
		ServerPlayerEntity player = (ServerPlayerEntity)event.getPlayer();

		// Ensure that the currently active agent wasn't responsible for this Break
		// Event
		if (!AgentProcessor.INSTANCE.currentAgents.isEmpty() &&
				AgentProcessor.INSTANCE.getCurrentAgent(player.getUniqueID()) != null &&
				!AgentProcessor.INSTANCE.getCurrentAgent(player.getUniqueID()).shouldProcess(event.getPos()))
			return;

		Variables variables = Variables.get(player.getUniqueID());

		BlockPos pos = event.getPos();
		BlockState state = event.getState();
		Direction faceHit = variables.faceHit;

		ItemStack heldItem = player.getHeldItem(Hand.MAIN_HAND);
		if (heldItem.isEmpty())
			return;

		if (MAConfig.CLIENT.excavation.isBlacklisted(heldItem) ||
				MAConfig.CLIENT.excavation.isBlacklisted(event.getState().getBlock()) ||
				event.getState().getBlock().isAir(event.getState(), world, event.getPos()))
			return;

		IMAPacket packet = null;

		if (variables.IsShaftanationToggled) {
			packet = new PacketShaftanate(pos, faceHit, Block.getStateId(state));
		} else if (MAConfig.CLIENT.lumbination.enabled() && heldItem.getItem() instanceof AxeItem) {
			packet = new PacketLumbinate(pos, faceHit, Block.getStateId(state));
		} else if (MAConfig.CLIENT.veination.enabled() && state.isIn(Tags.Blocks.ORES)) {
			packet = new PacketVeinate(pos, faceHit, Block.getStateId(state));
		} else if (variables.IsExcavationToggled || variables.IsSingleLayerToggled) { packet = new PacketExcavate(pos, faceHit, Block.getStateId(state)); }

		if (packet != null) { NetworkHandler.sendToServer(packet); }
	}

	@SubscribeEvent
	public static void onUseHoeEvent(UseHoeEvent event) {
		World world = event.getContext().getWorld();
		if (world.isRemote || !(event.getPlayer() instanceof ServerPlayerEntity) || (event.getPlayer() instanceof FakePlayer))
			return;

		BlockPos pos = event.getContext().getPos();
		Block block = world.getBlockState(pos).getBlock();

		if (world.getBlockState(pos).isIn(BlockTags.DIRT_LIKE)) {

			NetworkHandler.sendToServer(new PacketCultivate(pos));

		} else if (block instanceof IGrowable || block instanceof IPlantable || block instanceof CropsBlock || block instanceof NetherWartBlock) {

			NetworkHandler.sendToServer(new PacketCropinate(pos));

		}
	}
}
