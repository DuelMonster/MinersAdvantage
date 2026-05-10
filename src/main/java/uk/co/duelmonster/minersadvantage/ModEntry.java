package uk.co.duelmonster.minersadvantage;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import uk.co.duelmonster.minersadvantage.common.MinersAdvantageCore;
import uk.co.duelmonster.minersadvantage.common.event.CommonEventHandlerImpl;
import uk.co.duelmonster.minersadvantage.common.event.ToolEventHandler;

//? if fabric {
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
//?} else {
/*import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;*/ //?}

//? if fabric {
public final class ModEntry implements ModInitializer {
    private final MinersAdvantageCore core = new MinersAdvantageCore();
    private final ToolEventHandler toolEvents = new CommonEventHandlerImpl(core);

    @Override
    public void onInitialize() {
        core.bootstrap();

        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            if (level.isClientSide() || hand != InteractionHand.MAIN_HAND) {
                return InteractionResult.PASS;
            }
            ItemStack stack = player.getMainHandItem();
            BlockPos pos = hitResult.getBlockPos();
            BlockState state = level.getBlockState(pos);
            routeToolUse(stack, pos, state);
            return InteractionResult.PASS;
        });

        PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
            if (level.isClientSide()) {
                return;
            }
            routeToolUse(player.getMainHandItem(), pos, state);
            String blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
            core.workerRuntimeService().interceptLiveDropForPlayer(
                player.getUUID().getLeastSignificantBits(),
                "item:" + blockId,
                1,
                true
            );
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> toolEvents.onServerTick());
    }

    private void routeToolUse(ItemStack stack, BlockPos pos, BlockState state) {
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        String blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();

        if (itemId.contains("pickaxe")) {
            toolEvents.onPickaxeUse(pos.getX(), pos.getY(), pos.getZ(), blockId);
            return;
        }
        if (itemId.contains("shovel")) {
            toolEvents.onShovelUse(pos.getX(), pos.getY(), pos.getZ(), blockId);
            return;
        }
        if (itemId.contains("hoe")) {
            toolEvents.onHoeUse(pos.getX(), pos.getY(), pos.getZ(), blockId);
            return;
        }
        if (itemId.contains("axe")) {
            toolEvents.onAxeUse(pos.getX(), pos.getY(), pos.getZ(), blockId);
        }
    }
}
//?} else {
/*@Mod("minersadvantage")
public final class ModEntry {
    private final MinersAdvantageCore core = new MinersAdvantageCore();
    private final ToolEventHandler toolEvents = new CommonEventHandlerImpl(core);

    public ModEntry(IEventBus modEventBus) {
        core.bootstrap();
        NeoForge.EVENT_BUS.addListener(this::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(this::onServerTick);
    }

    private void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity() == null || event.getLevel().isClientSide()) {
            return;
        }

        routeToolUse(event.getItemStack(), event.getPos(), event.getLevel().getBlockState(event.getPos()));
    }

    private void onServerTick(ServerTickEvent.Post event) {
        toolEvents.onServerTick();
    }

    private void routeToolUse(ItemStack stack, BlockPos pos, BlockState state) {
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        String blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();

        if (itemId.contains("pickaxe")) {
            toolEvents.onPickaxeUse(pos.getX(), pos.getY(), pos.getZ(), blockId);
            return;
        }
        if (itemId.contains("shovel")) {
            toolEvents.onShovelUse(pos.getX(), pos.getY(), pos.getZ(), blockId);
            return;
        }
        if (itemId.contains("hoe")) {
            toolEvents.onHoeUse(pos.getX(), pos.getY(), pos.getZ(), blockId);
            return;
        }
        if (itemId.contains("axe")) {
            toolEvents.onAxeUse(pos.getX(), pos.getY(), pos.getZ(), blockId);
        }
    }
}*/ //?}
