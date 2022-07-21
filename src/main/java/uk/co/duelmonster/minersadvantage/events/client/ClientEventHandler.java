package uk.co.duelmonster.minersadvantage.events.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import uk.co.duelmonster.minersadvantage.MA;
import uk.co.duelmonster.minersadvantage.client.ClientFunctions;
import uk.co.duelmonster.minersadvantage.common.Functions;
import uk.co.duelmonster.minersadvantage.common.Variables;
import uk.co.duelmonster.minersadvantage.config.MAConfig;
import uk.co.duelmonster.minersadvantage.helpers.SubstitutionHelper;
import uk.co.duelmonster.minersadvantage.helpers.SupremeVantage;
import uk.co.duelmonster.minersadvantage.network.packets.PacketCaptivate;
import uk.co.duelmonster.minersadvantage.network.packets.PacketSubstituteTool;

public class ClientEventHandler {

  private enum AttackStage {
    IDLE, SWITCHED
  }

  private static AttackStage        currentAttackStage = AttackStage.IDLE;
  private static SubstitutionHelper substitutionHelper = new SubstitutionHelper();

  // Client Tick event
  @SubscribeEvent
  public void onClientTick(TickEvent.ClientTickEvent event) {
    Variables variables = Variables.get();
    if (event.phase != Phase.END || ClientFunctions.mc == null)
      return;

    Minecraft   mc     = ClientFunctions.mc;
    LocalPlayer player = ClientFunctions.getPlayer();
    if (player == null || mc.gameMode.getPlayerMode() == GameType.SPECTATOR)
      return;

    if (!variables.HasPlayerSpawned) {
      if (player.isAddedToWorld()) {
        variables.HasPlayerSpawned = true;
      } else
        return;
    }

    // Sync Settings with the Server
    MAConfig.CLIENT.syncPlayerConfigToServer();

    if (MAConfig.CLIENT.captivation.enabled() && !mc.isPaused()
        && (mc.isWindowActive() || MAConfig.CLIENT.captivation.allowInGUI())) {
      MA.NETWORK.sendToServer(new PacketCaptivate());
    }

    if (mc.gameMode.getPlayerMode() == GameType.CREATIVE)
      return;

    BlockHitResult blockResult = null;
    if (mc.hitResult instanceof BlockHitResult) {
      blockResult = (BlockHitResult) mc.hitResult;

      // Record the block face being attacked
      variables.faceHit = blockResult.getDirection();
    }

    variables.IsPlayerAttacking = ClientFunctions.isAttacking();

    // Sync Variables with the Server
    Variables.syncToServer();

    SupremeVantage.isWorthy(variables.IsExcavationToggled);

    if (variables.IsPlayerAttacking && mc.hitResult != null && mc.hitResult instanceof BlockHitResult) {

      if (MAConfig.CLIENT.substitution.enabled() && !variables.currentlySwitched) {
        ClientLevel world = player.clientLevel;
        BlockPos    pos   = blockResult.getBlockPos();
        BlockState  state = world.getBlockState(pos);
        Block       block = state.getBlock();

        if (block == null || state.isAir() || Blocks.BEDROCK == block)
          return;

        MA.NETWORK.sendToServer(new PacketSubstituteTool(pos));
      }
    }

    // Switch back to previously held item if Substitution is enabled
    if (MAConfig.CLIENT.substitution.enabled() && (!variables.IsPlayerAttacking || variables.IsVeinating)
        && variables.shouldSwitchBack && variables.currentlySwitched
        && variables.prevSlot >= 0 && player.getInventory().selected != variables.prevSlot) {

      ClientFunctions.syncCurrentPlayItem(variables.prevSlot);

      variables.resetSubstitution();
    }

  }

  @SubscribeEvent
  public void onAttackEntity(AttackEntityEvent event) {
    if (!MAConfig.CLIENT.substitution.enabled() || !(event.getTarget() instanceof LocalPlayer) || (event.getTarget() instanceof FakePlayer))
      return;

    LocalPlayer player = (LocalPlayer) event.getTarget();

    if (MAConfig.CLIENT.substitution.ignorePassiveMobs() && !(event.getTarget() instanceof Mob))
      return;

    if (currentAttackStage == AttackStage.IDLE && substitutionHelper.processWeaponSubtitution(player, event.getTarget())) {
      // Because we are intercepting an attack & switching weapons, we need to cancel
      // the attack & wait a tick to execute it. This allows the weapon switch to cause
      // the correct damage to the target.
      // currentTarget = (LivingEntity) event.getTarget();
      currentAttackStage = AttackStage.SWITCHED;
      event.setCanceled(true);

    } else if (currentAttackStage != AttackStage.SWITCHED) {
      // Reset the current Attack Stage
      // currentTarget = null;
      currentAttackStage = AttackStage.IDLE;
    }
  }

  // Item Pickup event
  @SubscribeEvent
  public void onItemPickup(EntityItemPickupEvent event) {
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

}
