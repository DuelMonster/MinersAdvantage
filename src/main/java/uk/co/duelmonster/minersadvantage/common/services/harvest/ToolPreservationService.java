package uk.co.duelmonster.minersadvantage.common.services.harvest;

import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import uk.co.duelmonster.minersadvantage.common.Functions;
import uk.co.duelmonster.minersadvantage.common.config.CommonConfig;

/**
 * ToolPreservationService keeps MinersAdvantage from spending a player's last few tool uses.
 */
public final class ToolPreservationService {
  private static final int WARNING_COOLDOWN_TICKS = 40;
  private static final Map<ServerPlayer, Long> LAST_WARNING_TICKS = new WeakHashMap<>();

  private ToolPreservationService() {
  }

  /**
   * Check whether an enabled reserve protects this damageable stack.
   */
  public static boolean isProtected(ItemStack stack, CommonConfig config) {
    if (stack == null || stack.isEmpty() || config == null || !config.toolPreservationEnabled()
        || !stack.isDamageableItem() || stack.getMaxDamage() <= 0) {
      return false;
    }

    int remainingDurability = stack.getMaxDamage() - stack.getDamageValue();
    return ToolPreservationPolicy.isAtOrBelowReserve(remainingDurability, config);
  }

  /**
   * Show one concise warning per cooldown window; holding the trigger should not become a chat-based drum solo.
   */
  public static void warn(ServerPlayer player) {
    if (player == null) {
      return;
    }

    long gameTime = player.level().getGameTime();
    synchronized (LAST_WARNING_TICKS) {
      Long lastWarningTick = LAST_WARNING_TICKS.get(player);
      if (lastWarningTick != null && gameTime >= lastWarningTick
          && gameTime - lastWarningTick < WARNING_COOLDOWN_TICKS) {
        return;
      }
      LAST_WARNING_TICKS.put(player, gameTime);
    }

    Functions.NotifyClient(player, Component.translatable("minersadvantage.tool_preservation.unusable"));
  }
}
