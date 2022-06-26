package uk.co.duelmonster.minersadvantage.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.VersionChecker;
import net.minecraftforge.fml.VersionChecker.CheckResult;
import net.minecraftforge.fml.VersionChecker.Status;
import net.minecraftforge.forgespi.language.IModInfo;
import uk.co.duelmonster.minersadvantage.common.Constants;
import uk.co.duelmonster.minersadvantage.common.Functions;

@OnlyIn(Dist.CLIENT)
public class ClientFunctions {

  public static Minecraft mc             = null;
  private static boolean  updateNotified = false;

  public static LocalPlayer getPlayer() {
    return mc != null ? mc.player : null;
  }

  public static boolean isAttacking() {
    return mc.options.keyAttack.isDown();
  }

  public static boolean isUsingItem() {
    return mc.options.keyUse.isDown();
  }

  // public static int getKeyIndexEx(String name) {
  // int index = Mouse.getButtonIndex(name);
  // if (index == -1) {
  // return Keyboard.getKeyIndex(name);
  // } else {
  // return index - 100;
  // }
  // }
  //
  // public static boolean isDownEx(String name) {
  // return isDownEx(getKeyIndexEx(name));
  // }
  //
  // public static boolean isDownEx(int index) {
  // if (index < 0) {
  // return Mouse.isButtonDown(index + 100);
  // } else {
  // return Keyboard.isDown(index);
  // }
  // }

  public static void DebugNotifyClient(String sMsg) {
    Functions.DebugNotifyClient(getPlayer(), sMsg);
  }

  public static void DebugNotifyClient(boolean bIsOn, String sFeatureName) {
    Functions.DebugNotifyClient(getPlayer(), bIsOn, sFeatureName);
  }

  public static void NotifyClient(String sMsg) {
    Functions.NotifyClient(getPlayer(), sMsg);
  }

  public static void NotifyClient(boolean bIsOn, String sFeatureName) {
    Functions.NotifyClient(getPlayer(), bIsOn, sFeatureName);
  }

  public static void playSound(Level world, SoundEvent sound, BlockPos pos) {
    world.playSound(getPlayer(), pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, sound, SoundSource.BLOCKS, 2.0F, 1.0F);
  }

  public static void doJoinWorldEventStuff() {
    if (!updateNotified) {
      updateNotified = true;

      IModInfo    info     = ModList.get().getModFileById(Constants.MOD_ID).getMods().get(0);
      CheckResult verCheck = VersionChecker.getResult(info);
      if (verCheck.status() == Status.OUTDATED)
        Functions.NotifyClient(getPlayer(), ChatFormatting.GRAY + Functions.localize("minersadvantage.version.update"));
    }
  }

  /**
   * Sync's the players currently held item to the server
   */
  public static void syncCurrentPlayItem(int iSlotIndx) {
    getPlayer().getInventory().selected = iSlotIndx;

    // MA.NETWORK.sendToServer(new CHeldItemChangePacket(iSlotIndx));
    mc.gameMode.tick();

    // Pause the current Thread to allow the server to catch up and realize that we
    // have changed the slot.
    // Without this sleep, we can end up with phantom blocks.
    Functions.sleep(100);
  }
}
