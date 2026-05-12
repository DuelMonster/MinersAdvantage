package uk.co.duelmonster.minersadvantage.client;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import uk.co.duelmonster.minersadvantage.common.Functions;

/**
 * Legacy compatibility facade for client utility calls.
 */
public final class ClientFunctions {
    public static Minecraft mc = Minecraft.getInstance();

    private ClientFunctions() {}

    public static LocalPlayer getPlayer() {
        return mc != null ? mc.player : null;
    }

    public static boolean isAttacking() {
        return mc != null && mc.options != null && mc.options.keyAttack.isDown();
    }

    public static boolean isUsingItem() {
        return mc != null && mc.options != null && mc.options.keyUse.isDown();
    }

    public static void DebugNotifyClient(String message) {
        LocalPlayer player = getPlayer();
        if (player != null) {
            Functions.DebugNotifyClient(player, message);
        }
    }

    public static void DebugNotifyClient(boolean enabled, String featureName) {
        LocalPlayer player = getPlayer();
        if (player != null) {
            Functions.DebugNotifyClient(player, enabled, featureName);
        }
    }

    public static void NotifyClient(String message) {
        LocalPlayer player = getPlayer();
        if (player != null) {
            Functions.NotifyClient(player, message);
        }
    }

    public static void NotifyClient(boolean enabled, String featureName) {
        LocalPlayer player = getPlayer();
        if (player != null) {
            Functions.NotifyClient(player, enabled, featureName);
        }
    }

    public static void playSound(Level world, SoundEvent sound, BlockPos pos) {
        LocalPlayer player = getPlayer();
        world.playSound(player, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, sound, SoundSource.BLOCKS, 2.0F, 1.0F);
    }

    public static void doJoinWorldEventStuff() {
        // Update notifier is platform-specific in the new architecture.
    }

    public static void syncCurrentPlayItem(int slotIndex) {
        LocalPlayer player = getPlayer();
        if (player == null) {
            return;
        }
        try {
            Method setter = player.getInventory().getClass().getMethod("setSelectedSlot", int.class);
            setter.invoke(player.getInventory(), slotIndex);
        } catch (Exception noSetter) {
            try {
                Field selected = player.getInventory().getClass().getDeclaredField("selected");
                selected.setAccessible(true);
                selected.setInt(player.getInventory(), slotIndex);
            } catch (Exception ignored) {
                return;
            }
        }
        if (mc.gameMode != null) {
            mc.gameMode.tick();
        }
        Functions.sleep(100);
    }
}
