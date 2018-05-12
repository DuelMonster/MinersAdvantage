package co.uk.duelmonster.minersadvantage.events;

import co.uk.duelmonster.minersadvantage.MinersAdvantage;
import co.uk.duelmonster.minersadvantage.client.ClientFunctions;
import co.uk.duelmonster.minersadvantage.client.KeyBindings;
import co.uk.duelmonster.minersadvantage.common.Constants;
import co.uk.duelmonster.minersadvantage.common.PacketID;
import co.uk.duelmonster.minersadvantage.common.Variables;
import co.uk.duelmonster.minersadvantage.handlers.GodItems;
import co.uk.duelmonster.minersadvantage.handlers.SubstitutionHandler;
import co.uk.duelmonster.minersadvantage.packets.NetworkPacket;
import co.uk.duelmonster.minersadvantage.settings.ConfigHandler;
import co.uk.duelmonster.minersadvantage.settings.Settings;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ClientEvents {
	private static int iTickCount = 10;
	
	private enum AttackStage {
		IDLE, SWITCHED
	}
	
	private AttackStage			currentAttackStage	= AttackStage.IDLE;
	private EntityLivingBase	currentTarget		= null;
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
		if (event.getModID().equals(Constants.MOD_ID))
			ConfigHandler.save();
	}
	
	@SubscribeEvent
	public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		ClientFunctions.doJoinWorldEventStuff();
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onItemPickup(EntityItemPickupEvent event) {
		Settings settings = Settings.get();
		
		event.setCanceled(settings.bCaptivationEnabled()
				&& settings.bIsWhitelist()
				&& settings.captivationBlacklist() != null
				&& settings.captivationBlacklist().size() > 0
				&& settings.captivationBlacklist().has(event.getItem().getItem().getItem().getRegistryName().toString().trim()));
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onClientTick(TickEvent.ClientTickEvent event) {
		if (!TickEvent.Phase.START.equals(event.phase))
			return;
		
		Minecraft mc = ClientFunctions.getMC();
		EntityPlayerSP player = ClientFunctions.getPlayer();
		if (player == null || mc.playerController.isInCreativeMode())
			return;
		
		// If an entity was attacked last tick and weapon was switched, we attack now.
		if (currentAttackStage == AttackStage.SWITCHED) {
			player.swingArm(EnumHand.MAIN_HAND);;
			mc.playerController.attackEntity(player, currentTarget);
			currentTarget = null;
			currentAttackStage = AttackStage.IDLE;
			return;
		}
		
		iTickCount = (iTickCount + 1) % 10;
		
		if (iTickCount <= 0)
			return;
		
		Settings settings = Settings.get();
		Variables variables = Variables.get();
		if (!settings.bToggleMode())
			variables.IsExcavationToggled = KeyBindings.excavation_toggle.isKeyDown();
		variables.IsShaftanationToggled = KeyBindings.shaftanation_toggle.isKeyDown();
		variables.IsPlayerAttacking = ClientFunctions.isAttacking();
		
		ClientFunctions.syncSettings();
		
		GodItems.isWorthy(variables.IsExcavationToggled);
		
		// Fire Illumination if enabled and the player is attacking
		if (settings.bIlluminationEnabled() && variables.IsPlayerAttacking) {
			NBTTagCompound tags = new NBTTagCompound();
			tags.setInteger("ID", PacketID.Illuminate.value());
			
			MinersAdvantage.instance.network.sendToServer(new NetworkPacket(tags));
		}
		
		// Cancel the running Excavation agents when player lifts the key binding
		if (settings.bExcavationEnabled() && !variables.IsExcavationToggled)
			GodItems.isWorthy(false);
		
		// Fire Captivation if enabled
		if (settings.bCaptivationEnabled() && !mc.isGamePaused() && mc.inGameHasFocus) {
			NBTTagCompound tags = new NBTTagCompound();
			tags.setInteger("ID", PacketID.Captivate.value());
			
			MinersAdvantage.instance.network.sendToServer(new NetworkPacket(tags));
		}
		
		if (variables.IsPlayerAttacking && mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK) {
			// Record the block face being attacked
			variables.sideHit = mc.objectMouseOver.sideHit;
			
			if (Settings.get().bSubstitutionEnabled() && !SubstitutionHandler.instance.bCurrentlySwitched) {
				World world = player.getEntityWorld();
				BlockPos oPos = mc.objectMouseOver.getBlockPos();
				IBlockState state = world.getBlockState(oPos);
				Block block = state.getBlock();
				
				if (block == null || Blocks.AIR == block || Blocks.BEDROCK == block)
					return;
				
				SubstitutionHandler.instance.processToolSubtitution(world, player, oPos);
			}
		}
		
		// Switch back to previously held item if Substitution is enabled
		if (settings.bSubstitutionEnabled() && !variables.IsPlayerAttacking
				&& SubstitutionHandler.instance.bShouldSwitchBack && SubstitutionHandler.instance.bCurrentlySwitched
				&& SubstitutionHandler.instance.iPrevSlot >= 0 && player.inventory.currentItem != SubstitutionHandler.instance.iPrevSlot) {
			
			System.out.println("Switching slot to (" + SubstitutionHandler.instance.iPrevSlot + ")");
			
			player.inventory.currentItem = SubstitutionHandler.instance.iPrevSlot;
			ClientFunctions.syncCurrentPlayItem(player.inventory.currentItem);
			SubstitutionHandler.instance.reset();
		}
	}
	
	// @SubscribeEvent
	// @SideOnly(Side.CLIENT)
	// public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
	// World world = event.getWorld();
	// if (!world.isRemote || !(event.getEntityPlayer() instanceof EntityPlayerSP) || (event.getEntityPlayer()
	// instanceof FakePlayer))
	// return;
	//
	// EntityPlayerSP player = (EntityPlayerSP) event.getEntityPlayer();
	//
	// }
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onAttackEntity(AttackEntityEvent event) {
		Settings settings = Settings.get();
		if (!settings.bSubstitutionEnabled() || !(event.getEntityPlayer() instanceof EntityPlayerSP) || (event.getEntityPlayer() instanceof FakePlayer))
			return;
		
		EntityPlayerSP player = (EntityPlayerSP) event.getEntityPlayer();
		
		if (settings.bIgnorePassiveMobs() && !(event.getTarget() instanceof EntityMob))
			return;
		
		if (currentAttackStage == AttackStage.IDLE && SubstitutionHandler.instance.processWeaponSubtitution(player, event.getTarget())) {
			// Because we are intercepting an attack & switching weapons, we need to cancel the attack & wait a tick to
			// execute it.
			// This allows the weapon switch to cause the correct damage to the target.
			currentTarget = (EntityLivingBase) event.getTarget();
			currentAttackStage = AttackStage.SWITCHED;
			event.setCanceled(true);
			
		} else if (currentAttackStage != AttackStage.SWITCHED) {
			// Reset the current Attack Stage
			currentTarget = null;
			currentAttackStage = AttackStage.IDLE;
		}
	}
}
