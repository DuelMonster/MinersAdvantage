package uk.co.duelmonster.minersadvantage;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import java.lang.reflect.Method;
import uk.co.duelmonster.minersadvantage.common.MinersAdvantageCore;
import uk.co.duelmonster.minersadvantage.common.config.ServerOverridesConfig;
import uk.co.duelmonster.minersadvantage.common.config.SyncedClientConfig;
import uk.co.duelmonster.minersadvantage.common.event.CommonEventHandlerImpl;
import uk.co.duelmonster.minersadvantage.common.event.ToolEventHandler;
import uk.co.duelmonster.minersadvantage.common.network.PlayerStateSyncPacket;

//? if fabric {
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import uk.co.duelmonster.minersadvantage.client.FabricNetworkEvents;

/**
 * ModEntry keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class ModEntry implements ModInitializer {
    private final MinersAdvantageCore core = new MinersAdvantageCore();
    private final ToolEventHandler toolEvents = new CommonEventHandlerImpl(core);

    @Override
    /**
     * onInitialize exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public void onInitialize() {
        core.bootstrap();
        FabricNetworkEvents.initialize(core);
        FabricNetworkEvents.registerPayloadTypes();
        FabricNetworkEvents.registerServerHandlers();
        registerFabricLevelUnloadEvent();

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> onPlayerLogin(handler.getPlayer()));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> onPlayerLogout(handler.getPlayer()));
        ServerEntityEvents.ENTITY_LOAD.register(this::onFabricEntityLoad);

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

    /**
     * registerFabricLevelUnloadEvent exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private void registerFabricLevelUnloadEvent() {
        try {
            // 1.21.11 branch
            Class<?> worldEventsClass = Class.forName("net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents");
            Object unloadEvent = worldEventsClass.getField("UNLOAD").get(null);
            Method registerMethod = unloadEvent.getClass().getMethod("register", Class.forName("net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents$Unload"));
            Object callback = java.lang.reflect.Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{Class.forName("net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents$Unload")},
                (proxy, method, args) -> {
                    onServerLevelUnload((ServerLevel) args[1]);
                    return null;
                }
            );
            registerMethod.invoke(unloadEvent, callback);
        } catch (ReflectiveOperationException missingWorldEvents) {
            try {
                // 26.1.2 branch
                Class<?> levelEventsClass = Class.forName("net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents");
                Object unloadEvent = levelEventsClass.getField("UNLOAD").get(null);
                Method registerMethod = unloadEvent.getClass().getMethod("register", Class.forName("net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents$Unload"));
                Object callback = java.lang.reflect.Proxy.newProxyInstance(
                    getClass().getClassLoader(),
                    new Class[]{Class.forName("net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents$Unload")},
                    (proxy, method, args) -> {
                        onServerLevelUnload((ServerLevel) args[1]);
                        return null;
                    }
                );
                registerMethod.invoke(unloadEvent, callback);
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Unable to register Fabric level unload event", exception);
            }
        }
    }

    /**
     * onPlayerLogin exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private void onPlayerLogin(ServerPlayer player) {
        long playerId = player.getUUID().getLeastSignificantBits();
        SyncedClientConfig defaults = SyncedClientConfig.defaults();
        core.handlePlayerStateSyncPacket(new PlayerStateSyncPacket(playerId, defaults, defaults, new ServerOverridesConfig()));
    }

    /**
     * onPlayerLogout exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private void onPlayerLogout(ServerPlayer player) {
        long playerId = player.getUUID().getLeastSignificantBits();
        core.workerRuntimeService().abortAllForPlayerWithStats(playerId);
        core.playerStateService().clearPlayerState(playerId);
    }

    /**
     * onServerLevelUnload exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private void onServerLevelUnload(ServerLevel level) {
        for (ServerPlayer player : level.players()) {
            onPlayerLogout(player);
        }
    }

    /**
     * onFabricEntityLoad exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private void onFabricEntityLoad(Entity entity, ServerLevel level) {
        if (entity instanceof ItemEntity itemEntity) {
            Player nearest = level.getNearestPlayer(entity, 8.0);
            if (nearest instanceof ServerPlayer serverPlayer) {
                String itemId = BuiltInRegistries.ITEM.getKey(itemEntity.getItem().getItem()).toString();
                toolEvents.onItemPickup(itemId, false);
                core.workerRuntimeService().interceptLiveDropForPlayer(
                    serverPlayer.getUUID().getLeastSignificantBits(),
                    "item:" + itemId,
                    itemEntity.getItem().getCount(),
                    true
                );
            }
        } else if (entity instanceof ExperienceOrb orb) {
            Player nearest = level.getNearestPlayer(entity, 8.0);
            if (nearest instanceof ServerPlayer serverPlayer) {
                core.workerRuntimeService().interceptLiveDropForPlayer(
                    serverPlayer.getUUID().getLeastSignificantBits(),
                    "xp_orb",
                    orb.getValue(),
                    true
                );
            }
        }
    }

    /**
     * routeToolUse exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
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
/*
import net.neoforged.bus.api.IEventBus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import uk.co.duelmonster.minersadvantage.client.NeoForgeNetworkEvents;

@Mod("minersadvantage")
/**
 * ModEntry keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class ModEntry {
    private final MinersAdvantageCore core = new MinersAdvantageCore();
    private final ToolEventHandler toolEvents = new CommonEventHandlerImpl(core);

    /**
     * ModEntry exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public ModEntry(IEventBus modEventBus, ModContainer modContainer, Dist dist) {
        core.bootstrap();
        NeoForgeNetworkEvents.initialize(core);
        if (dist == Dist.CLIENT) {
            modContainer.registerExtensionPoint(
                IConfigScreenFactory.class,
                (IConfigScreenFactory) (container, modListScreen) -> uk.co.duelmonster.minersadvantage.client.MinersAdvantageConfigScreen.create(modListScreen)
            );
        }
        NeoForge.EVENT_BUS.addListener(this::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(this::onLeftClickBlock);
        NeoForge.EVENT_BUS.addListener(this::onServerTick);
        NeoForge.EVENT_BUS.addListener(this::onPlayerLogin);
        NeoForge.EVENT_BUS.addListener(this::onPlayerLogout);
        NeoForge.EVENT_BUS.addListener(this::onLevelUnload);
        NeoForge.EVENT_BUS.addListener(this::onEntityJoinLevel);
        NeoForge.EVENT_BUS.addListener(this::onToolModification);
    }

    /**
     * onPlayerLogin exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        long playerId = player.getUUID().getLeastSignificantBits();
        SyncedClientConfig defaults = SyncedClientConfig.defaults();
        core.handlePlayerStateSyncPacket(new PlayerStateSyncPacket(playerId, defaults, defaults, new ServerOverridesConfig()));
    }

    /**
     * onPlayerLogout exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        long playerId = player.getUUID().getLeastSignificantBits();
        core.workerRuntimeService().abortAllForPlayerWithStats(playerId);
        core.playerStateService().clearPlayerState(playerId);
    }

    /**
     * onLevelUnload exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private void onLevelUnload(LevelEvent.Unload event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        for (ServerPlayer player : level.players()) {
            core.workerRuntimeService().abortAllForPlayerWithStats(player.getUUID().getLeastSignificantBits());
            core.playerStateService().clearPlayerState(player.getUUID().getLeastSignificantBits());
        }
    }

    /**
     * onEntityJoinLevel exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || event.getLevel().isClientSide()) {
            return;
        }

        Entity entity = event.getEntity();
        if (entity instanceof ItemEntity itemEntity) {
            Player nearest = level.getNearestPlayer(entity, 8.0);
            if (nearest instanceof ServerPlayer serverPlayer) {
                String itemId = BuiltInRegistries.ITEM.getKey(itemEntity.getItem().getItem()).toString();
                toolEvents.onItemPickup(itemId, false);
                core.workerRuntimeService().interceptLiveDropForPlayer(
                    serverPlayer.getUUID().getLeastSignificantBits(),
                    "item:" + itemId,
                    itemEntity.getItem().getCount(),
                    true
                );
            }
        } else if (entity instanceof ExperienceOrb orb) {
            Player nearest = level.getNearestPlayer(entity, 8.0);
            if (nearest instanceof ServerPlayer serverPlayer) {
                core.workerRuntimeService().interceptLiveDropForPlayer(
                    serverPlayer.getUUID().getLeastSignificantBits(),
                    "xp_orb",
                    orb.getValue(),
                    true
                );
            }
        }
    }

    /**
     * onToolModification exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private void onToolModification(BlockEvent.BlockToolModificationEvent event) {
        if (event.getPlayer() == null || event.getLevel().isClientSide()) {
            return;
        }
        BlockState state = event.getFinalState() != null ? event.getFinalState() : event.getState();
        routeToolUse(event.getHeldItemStack(), event.getPos(), state);
    }

    /**
     * onRightClickBlock exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity() == null || event.getLevel().isClientSide()) {
            return;
        }

        routeToolUse(event.getItemStack(), event.getPos(), event.getLevel().getBlockState(event.getPos()));
    }

    /**
     * onLeftClickBlock exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getEntity() == null || event.getLevel().isClientSide()) {
            return;
        }

        String blockId = BuiltInRegistries.BLOCK.getKey(event.getLevel().getBlockState(event.getPos()).getBlock()).toString();
        core.workerRuntimeService().interceptLiveDropForPlayer(
            event.getEntity().getUUID().getLeastSignificantBits(),
            "item:" + blockId,
            1,
            true
        );
    }

    /**
     * onServerTick exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private void onServerTick(ServerTickEvent.Post event) {
        toolEvents.onServerTick();
    }

    /**
     * routeToolUse exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
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
*/ //?}


