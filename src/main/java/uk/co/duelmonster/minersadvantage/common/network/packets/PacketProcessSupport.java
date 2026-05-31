package uk.co.duelmonster.minersadvantage.common.network.packets;

import java.lang.reflect.Method;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import uk.co.duelmonster.minersadvantage.common.Variables;
import uk.co.duelmonster.minersadvantage.common.event.FeatureEventHandler;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;

/**
 * PacketProcessSupport is the teammate that keeps this part of the mod understandable and stable.
 * It exists so behavior stays explicit instead of becoming mystery spaghetti at 2 AM.
 */
final class PacketProcessSupport {
    private static final String FALLBACK_ID = "minecraft:air";

    private PacketProcessSupport() {}

    /**
     * dispatchFeature exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    static void dispatchFeature(Object player, FeatureId feature) {
        dispatchFeature(player, feature, BlockPos.ZERO, 0, null);
    }

    /**
     * dispatchFeature exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    static void dispatchFeature(Object player, FeatureId feature, BaseBlockPacket packet) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (packet == null) {
            return;
        }
        dispatchFeature(player, feature, packet.pos, packet.stateID, null);
    }

    /**
     * dispatchSubstitution exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    static void dispatchSubstitution(Object player, PacketSubstituteTool packet) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (packet == null) {
            return;
        }

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (packet.slot >= 0) {
            Variables vars = resolveVariables(player);
            vars.optimalSlot = packet.slot;
            vars.currentlySwitched = true;
        }

        String hint = packet.slot >= 0 ? "slot:" + packet.slot : null;
        dispatchFeature(player, FeatureId.SUBSTITUTION, packet.pos, packet.stateID, hint);
    }

    /**
     * markAgentsStopped exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    static void markAgentsStopped(Object player) {
        Variables vars = resolveVariables(player);
        vars.IsCropinating = false;
        vars.IsExcavating = false;
        vars.IsIlluminating = false;
        vars.IsLumbinating = false;
        vars.IsPathanating = false;
        vars.IsShaftanating = false;
        vars.IsVeinating = false;
        vars.IsVentilating = false;
        vars.resetSubstitution();
    }

    /**
     * dispatchFeature exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    private static void dispatchFeature(Object player, FeatureId feature, BlockPos pos, int stateId, String blockIdHint) {
        BlockPos safePos = pos == null ? BlockPos.ZERO : pos;
        String blockId = blockIdHint == null || blockIdHint.isBlank() ? resolveBlockId(stateId) : blockIdHint;
        String toolId = resolveToolId(player);

        FeatureEventHandler.onToolUse(
            feature,
            safePos.getX(),
            safePos.getY(),
            safePos.getZ(),
            blockId,
            toolId
        );
    }

    /**
     * resolveBlockId exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    private static String resolveBlockId(int stateId) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (stateId > 0) {
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            try {
                return BuiltInRegistries.BLOCK.getKey(Block.stateById(stateId).getBlock()).toString();
            } catch (RuntimeException ignored) {
                // Why this exists: Keep compatibility dispatch resilient for legacy state ids. (future-you will thank present-you).
            }
        }
        return FALLBACK_ID;
    }

    /**
     * resolveToolId exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    private static String resolveToolId(Object player) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (player instanceof Player minecraftPlayer) {
            return toolIdFromStack(minecraftPlayer.getMainHandItem());
        }

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        try {
            Method method = player == null ? null : player.getClass().getMethod("getMainHandItem");
            Object result = method == null ? null : method.invoke(player);
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (result instanceof ItemStack stack) {
                return toolIdFromStack(stack);
            }
        } catch (ReflectiveOperationException | SecurityException ignored) {
            // Why this exists: Fallback to default tool id for compatibility invocation paths. (future-you will thank present-you).
        }

        return FALLBACK_ID;
    }

    /**
     * toolIdFromStack exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    private static String toolIdFromStack(ItemStack stack) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (stack == null || stack.isEmpty()) {
            return FALLBACK_ID;
        }
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }

    /**
     * resolveVariables exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    private static Variables resolveVariables(Object player) {
        UUID uuid = resolveUuid(player);
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (uuid == null) {
            return Variables.get();
        }
        return Variables.get(uuid);
    }

    /**
     * resolveUuid exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    private static UUID resolveUuid(Object player) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (player instanceof Player minecraftPlayer) {
            return minecraftPlayer.getUUID();
        }

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        try {
            Method method = player == null ? null : player.getClass().getMethod("getUUID");
            Object result = method == null ? null : method.invoke(player);
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (result instanceof UUID uuid) {
                return uuid;
            }
        } catch (ReflectiveOperationException | SecurityException ignored) {
            // Why this exists: Fallback to singleton Variables state for compatibility callers. (future-you will thank present-you).
        }

        return null;
    }
}
