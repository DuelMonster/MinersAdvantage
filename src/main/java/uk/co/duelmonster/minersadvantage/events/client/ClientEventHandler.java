package uk.co.duelmonster.minersadvantage.events.client;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.item.ShovelItem;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import uk.co.duelmonster.minersadvantage.client.ClientFunctions;
import uk.co.duelmonster.minersadvantage.client.KeyBindings;
import uk.co.duelmonster.minersadvantage.client.MAParticleManager;
import uk.co.duelmonster.minersadvantage.common.Constants;
import uk.co.duelmonster.minersadvantage.common.Functions;
import uk.co.duelmonster.minersadvantage.common.Variables;
import uk.co.duelmonster.minersadvantage.config.MAConfig;
import uk.co.duelmonster.minersadvantage.events.server.ServerEventHandler;
import uk.co.duelmonster.minersadvantage.helpers.SubstitutionHelper;
import uk.co.duelmonster.minersadvantage.helpers.SupremeVantage;
import uk.co.duelmonster.minersadvantage.network.NetworkHandler;
import uk.co.duelmonster.minersadvantage.network.packets.PacketCaptivate;
import uk.co.duelmonster.minersadvantage.network.packets.PacketPathanate;
import uk.co.duelmonster.minersadvantage.network.packets.PacketSubstituteTool;

@EventBusSubscriber(modid = Constants.MOD_ID)
public class ClientEventHandler extends ServerEventHandler {
	
	private enum AttackStage {
		IDLE, SWITCHED
	}
	
	public ClientEventHandler() {
		Minecraft mc = Minecraft.getInstance();
		ClientFunctions.mc = mc;
		
		if (MAParticleManager.getOriginal() == null)
			MAParticleManager.setOriginal(mc.particles);
		
		if (MAConfig.CLIENT.disableParticleEffects.get()) {
			mc.particles = MAParticleManager.set(new MAParticleManager(mc.world, mc.getTextureManager()));
			((IReloadableResourceManager) mc.getResourceManager()).addReloadListener(mc.particles);
		}
		
		// KeyBindings
		KeyBindings.registerKeys();
	}
	
	private static AttackStage			currentAttackStage	= AttackStage.IDLE;
	private static SubstitutionHelper	substitutionHelper	= new SubstitutionHelper();
	
	@SubscribeEvent
	// Client Tick event
	public static void onClientTick(TickEvent.ClientTickEvent event) {
		Variables variables = Variables.get();
		if (event.phase != Phase.END || ClientFunctions.mc == null || !variables.HasPlayerSpawned)
			return;
		
		Minecraft mc = ClientFunctions.mc;
		
		if (MAConfig.CLIENT.captivation.enabled() && !mc.isGamePaused()
				&& (mc.isGameFocused() || MAConfig.CLIENT.captivation.allowInGUI())) {
			NetworkHandler.sendToServer(new PacketCaptivate());
		}
		
		if (mc.playerController.isInCreativeMode())
			return;
		
		BlockRayTraceResult blockResult = null;
		if (mc.objectMouseOver instanceof BlockRayTraceResult) {
			blockResult = (BlockRayTraceResult) mc.objectMouseOver;
			
			// Record the block face being attacked
			variables.faceHit = blockResult.getFace();
		}
		
		variables.IsPlayerAttacking = ClientFunctions.isAttacking();
		
		// Sync Variables with the Server
		Variables.syncToServer();
		
		SupremeVantage.isWorthy(variables.IsExcavationToggled);
		
		ClientPlayerEntity player = ClientFunctions.getPlayer();
		if (player == null)
			return;
		
		if (variables.IsPlayerAttacking && mc.objectMouseOver != null && mc.objectMouseOver instanceof BlockRayTraceResult) {
			
			if (MAConfig.CLIENT.substitution.enabled() && !variables.currentlySwitched) {
				World		world	= player.getEntityWorld();
				BlockPos	pos		= blockResult.getPos();
				BlockState	state	= world.getBlockState(pos);
				Block		block	= state.getBlock();
				
				if (block == null || Blocks.AIR == block || Blocks.BEDROCK == block)
					return;
				
				NetworkHandler.sendToServer(new PacketSubstituteTool(pos));
			}
		}
		
		// Switch back to previously held item if Substitution is enabled
		if (MAConfig.CLIENT.substitution.enabled() && (!variables.IsPlayerAttacking || variables.IsVeinating)
				&& variables.shouldSwitchBack && variables.currentlySwitched
				&& variables.prevSlot >= 0 && player.inventory.currentItem != variables.prevSlot) {
			
			ClientFunctions.syncCurrentPlayItem(variables.prevSlot);
			
			variables.resetSubstitution();
		}
		
	}
	
	@SubscribeEvent
	public void onAttackEntity(AttackEntityEvent event) {
		if (!MAConfig.CLIENT.substitution.enabled() || !(event.getPlayer() instanceof ClientPlayerEntity) || (event.getPlayer() instanceof FakePlayer))
			return;
		
		ClientPlayerEntity player = (ClientPlayerEntity) event.getPlayer();
		
		if (MAConfig.CLIENT.substitution.ignorePassiveMobs() && !(event.getTarget() instanceof MobEntity))
			return;
		
		if (currentAttackStage == AttackStage.IDLE && substitutionHelper.processWeaponSubtitution(player, event.getTarget())) {
			// Because we are intercepting an attack & switching weapons, we need to cancel
			// the attack & wait a tick to
			// execute it. This allows the weapon switch to cause the correct damage to the
			// target.
			// currentTarget = (LivingEntity) event.getTarget();
			currentAttackStage = AttackStage.SWITCHED;
			event.setCanceled(true);
			
		} else if (currentAttackStage != AttackStage.SWITCHED) {
			// Reset the current Attack Stage
			// currentTarget = null;
			currentAttackStage = AttackStage.IDLE;
		}
	}
	
	@SubscribeEvent
	// Item Pickup event
	public static void onItemPickup(EntityItemPickupEvent event) {
		if (event.isCancelable()) {
			// Cancel the item pickup if said item is blacklisted
			event.setCanceled(
					MAConfig.CLIENT.captivation.enabled()
							&& MAConfig.CLIENT.captivation.isWhitelist() == false
							&& MAConfig.CLIENT.captivation.unconditionalBlacklist()
							&& MAConfig.CLIENT.captivation.blacklist() != null
							&& MAConfig.CLIENT.captivation.blacklist().isEmpty() == false
							&& MAConfig.CLIENT.captivation.blacklist().contains(Functions.getName(event.getItem())));
		}
	}
	
	@SubscribeEvent
	public static void onItemRightClick(PlayerInteractEvent.RightClickItem event) {
		World world = event.getWorld();
		if (world.isRemote
				&& event.getItemStack() != null
				&& event.getItemStack().getItem() instanceof ShovelItem
				&& ClientFunctions.mc.objectMouseOver instanceof BlockRayTraceResult) {
			
			BlockRayTraceResult	blockResult	= (BlockRayTraceResult) ClientFunctions.mc.objectMouseOver;
			BlockPos			pos			= blockResult.getPos();
			BlockState			state		= world.getBlockState(pos);
			
			if (state.isIn(BlockTags.DIRT_LIKE) || state.getBlock().equals(Blocks.GRASS_PATH)) {
				NetworkHandler.sendToServer(new PacketPathanate(pos));
			}
		}
	}
	
}
