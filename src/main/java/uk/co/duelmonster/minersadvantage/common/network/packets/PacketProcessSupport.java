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

final class PacketProcessSupport {
    private static final String FALLBACK_BLOCK_ID = "minecraft:air";
    private static final String FALLBACK_TOOL_ID = "minecraft:air";

    private PacketProcessSupport() {}

    static void dispatchFeature(Object player, FeatureId feature) {
        dispatchFeature(player, feature, BlockPos.ZERO, 0, null);
    }

    static void dispatchFeature(Object player, FeatureId feature, BaseBlockPacket packet) {
        if (packet == null) {
            return;
        }
        dispatchFeature(player, feature, packet.pos, packet.stateID, null);
    }

    static void dispatchSubstitution(Object player, PacketSubstituteTool packet) {
        if (packet == null) {
            return;
        }

        if (packet.slot >= 0) {
            Variables vars = resolveVariables(player);
            vars.optimalSlot = packet.slot;
            vars.currentlySwitched = true;
        }

        String hint = packet.slot >= 0 ? "slot:" + packet.slot : null;
        dispatchFeature(player, FeatureId.SUBSTITUTION, packet.pos, packet.stateID, hint);
    }

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

    private static String resolveBlockId(int stateId) {
        if (stateId > 0) {
            try {
                return BuiltInRegistries.BLOCK.getKey(Block.stateById(stateId).getBlock()).toString();
            } catch (RuntimeException ignored) {
                // Keep compatibility dispatch resilient for legacy state ids.
            }
        }
        return FALLBACK_BLOCK_ID;
    }

    private static String resolveToolId(Object player) {
        if (player instanceof Player minecraftPlayer) {
            return toolIdFromStack(minecraftPlayer.getMainHandItem());
        }

        try {
            Method method = player == null ? null : player.getClass().getMethod("getMainHandItem");
            Object result = method == null ? null : method.invoke(player);
            if (result instanceof ItemStack stack) {
                return toolIdFromStack(stack);
            }
        } catch (ReflectiveOperationException | SecurityException ignored) {
            // Fallback to default tool id for compatibility invocation paths.
        }

        return FALLBACK_TOOL_ID;
    }

    private static String toolIdFromStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return FALLBACK_TOOL_ID;
        }
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }

    private static Variables resolveVariables(Object player) {
        UUID uuid = resolveUuid(player);
        if (uuid == null) {
            return Variables.get();
        }
        return Variables.get(uuid);
    }

    private static UUID resolveUuid(Object player) {
        if (player instanceof Player minecraftPlayer) {
            return minecraftPlayer.getUUID();
        }

        try {
            Method method = player == null ? null : player.getClass().getMethod("getUUID");
            Object result = method == null ? null : method.invoke(player);
            if (result instanceof UUID uuid) {
                return uuid;
            }
        } catch (ReflectiveOperationException | SecurityException ignored) {
            // Fallback to singleton Variables state for compatibility callers.
        }

        return null;
    }
}
