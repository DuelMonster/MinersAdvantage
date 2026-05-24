package uk.co.duelmonster.minersadvantage;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.block.state.BlockState;
import uk.co.duelmonster.minersadvantage.agent.AgentManager;
import uk.co.duelmonster.minersadvantage.agent.CaptivationAgent;
import uk.co.duelmonster.minersadvantage.agent.CropinationAgent;
import uk.co.duelmonster.minersadvantage.agent.CultivationAgent;
import uk.co.duelmonster.minersadvantage.agent.ExcavationAgent;
import uk.co.duelmonster.minersadvantage.agent.IlluminationAgent;
import uk.co.duelmonster.minersadvantage.agent.LumbinationAgent;
import uk.co.duelmonster.minersadvantage.agent.PathanationAgent;
import uk.co.duelmonster.minersadvantage.agent.ShaftanationAgent;
import uk.co.duelmonster.minersadvantage.agent.SubstitutionAgent;
import uk.co.duelmonster.minersadvantage.agent.VeinationAgent;
import uk.co.duelmonster.minersadvantage.agent.VentilationAgent;
import uk.co.duelmonster.minersadvantage.common.MinersAdvantageCore;
import uk.co.duelmonster.minersadvantage.common.config.ServerOverridesConfig;
import uk.co.duelmonster.minersadvantage.common.config.MAConfig_Base;
import uk.co.duelmonster.minersadvantage.common.config.SubstitutionConfig;
import uk.co.duelmonster.minersadvantage.common.config.SubstitutionConfig.SubstitutionAction;
import uk.co.duelmonster.minersadvantage.common.config.SyncedClientConfig;
import uk.co.duelmonster.minersadvantage.common.config.VeinationConfig;
import uk.co.duelmonster.minersadvantage.common.event.CommonEventHandlerImpl;
import uk.co.duelmonster.minersadvantage.common.event.ToolEventHandler;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;
import uk.co.duelmonster.minersadvantage.common.feature.utility.SubstitutionComponent;
import uk.co.duelmonster.minersadvantage.common.log.LogUtils;
import uk.co.duelmonster.minersadvantage.common.network.PlayerStateSyncPacket;
import uk.co.duelmonster.minersadvantage.common.registry.RegistryPredicates;
import uk.co.duelmonster.minersadvantage.common.services.utility.VeinationRuntimeService;

//? if fabric {
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import uk.co.duelmonster.minersadvantage.client.FabricNetworkEvents;

/**
 * ModEntry keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class ModEntry implements ModInitializer {
    private final MinersAdvantageCore core = new MinersAdvantageCore();
    private final ToolEventHandler toolEvents = new CommonEventHandlerImpl(core);
    private final VeinationRuntimeService veinationRuntime = new VeinationRuntimeService();

    @Override
    public void onInitialize() {
        LogUtils.applyConfiguredLogging();
        LogUtils.logInfo("Initializing {} {} debugLogging={}", ModCommon.MOD_NAME, ModCommon.MOD_VERSION, LogUtils.isDebugLoggingEnabled());
        core.bootstrap();
        FabricNetworkEvents.initialize(core);
        FabricNetworkEvents.registerPayloadTypes();
        FabricNetworkEvents.registerServerHandlers();
        registerFabricLevelUnloadEvent();

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> onPlayerLogin(handler.getPlayer()));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> onPlayerLogout(handler.getPlayer()));
        ServerEntityEvents.ENTITY_LOAD.register(this::onFabricEntityLoad);

        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (world.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
                return InteractionResult.PASS;
            }

            if (!isFeatureEnabled(FeatureId.SUBSTITUTION)) {
                return InteractionResult.PASS;
            }

            BlockState state = world.getBlockState(pos);
            ItemStack stack = player.getItemInHand(hand);

            if (!SubstitutionAgent.shouldQueueStartSubstitution(serverPlayer, hand, SubstitutionAction.BREAK, pos)) {
                return InteractionResult.PASS;
            }

            LogUtils.logDebug("Attack block trigger feature=Substitution player={} hand={} item={} pos={}", serverPlayer.getScoreboardName(), hand, itemId(stack), pos);
            AgentManager.get().addAgent(serverPlayer, new SubstitutionAgent(serverPlayer, state, SubstitutionAction.BREAK, hand, substitutionConfig()));
            return InteractionResult.PASS;
        });

        PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
            if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
                return;
            }
            ItemStack stack = SubstitutionAgent.effectiveBreakHandStack(serverPlayer, InteractionHand.MAIN_HAND);
            String itemId = itemId(stack);
            String brokenBlockId = blockId(state);
            long playerId = playerId(serverPlayer);
            var playerState = core.playerStateService().getPlayerState(playerId);
            boolean shaftModeActive = isFeatureEnabled(FeatureId.SHAFTANATION) && playerState.shaftVentToggled();
            boolean excavationActive = isFeatureEnabled(FeatureId.EXCAVATION) && playerState.isExcavationActive();
            VeinationConfig veinationConfig = veinationConfig(serverPlayer);
            boolean veinationGesture = veinationConfig.oreHarvestWithoutSneak() ? !player.isShiftKeyDown() : player.isShiftKeyDown();
            boolean allowedPickaxe = veinationRuntime.isPickaxeAllowed(level, veinationConfig, stack);

            if (isFeatureEnabled(FeatureId.VEINATION) && veinationGesture && isPickaxeTool(stack) && allowedPickaxe && RegistryPredicates.isOreLike(state)) {
                LogUtils.logDebug("Block break trigger feature=Veination player={} item={} block={} pos={}", serverPlayer.getScoreboardName(), itemId, brokenBlockId, pos);
                veinationRuntime.registerDropAnchor(serverPlayer, pos, veinationConfig);
                AgentManager agentManager = AgentManager.get();
                if (!agentManager.hasAgentType(serverPlayer, VeinationAgent.class)) {
                    agentManager.addAgent(serverPlayer, new VeinationAgent(serverPlayer, pos, state, veinationRuntime, veinationConfig));
                }
            } else if (!shaftModeActive && excavationActive && isExcavationTool(stack) && isExcavationBlock(state, stack)) {
                LogUtils.logDebug("Block break trigger feature=Excavation player={} item={} block={} pos={}", serverPlayer.getScoreboardName(), itemId, brokenBlockId, pos);
                AgentManager.get().addAgent(serverPlayer, new ExcavationAgent(serverPlayer, pos, 3, state.getBlock()));
            } else if (isAxeTool(stack) && state.is(BlockTags.LOGS)) {
                LogUtils.logDebug("Block break trigger feature=Lumbination player={} item={} block={} pos={}", serverPlayer.getScoreboardName(), itemId, brokenBlockId, pos);
                AgentManager.get().addAgent(serverPlayer, new LumbinationAgent(serverPlayer, pos, state.getBlock()));
            }
        });

        registerCollectiveDigSpeedCallback();

        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
                return InteractionResult.PASS;
            }
            if (player.isShiftKeyDown()) {
                LogUtils.logDebug("Use item trigger feature=Captivation player={} hand={} item={}", serverPlayer.getScoreboardName(), hand, itemId(player.getMainHandItem()));
                AgentManager.get().addAgent(serverPlayer, new CaptivationAgent(serverPlayer, 6.0));
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
                return InteractionResult.PASS;
            }

            BlockPos pos = hitResult.getBlockPos();
            BlockState state = world.getBlockState(pos);
            ItemStack stack = player.getItemInHand(hand);
            String itemId = itemId(stack);
            String targetBlockId = blockId(state);

            routeToolUse(stack, pos, state);

            if (isHoeTool(stack)) {
                if (RegistryPredicates.isCropBlock(state)) {
                    LogUtils.logDebug("Use block trigger feature=Cropination player={} item={} block={} pos={}", serverPlayer.getScoreboardName(), itemId, targetBlockId, pos);
                    AgentManager.get().addAgent(serverPlayer, new CropinationAgent(serverPlayer, pos, 3));
                    return InteractionResult.SUCCESS;
                }
                if (RegistryPredicates.isDirtLike(state)) {
                    LogUtils.logDebug("Use block trigger feature=Cultivation player={} item={} block={} pos={}", serverPlayer.getScoreboardName(), itemId, targetBlockId, pos);
                    AgentManager.get().addAgent(serverPlayer, new CultivationAgent(serverPlayer, pos, 3));
                    return InteractionResult.SUCCESS;
                }
            }

            if (itemId.endsWith("_torch") || itemId.endsWith(":torch")) {
                LogUtils.logDebug("Use block trigger feature=Illumination player={} item={} pos={}", serverPlayer.getScoreboardName(), itemId, pos);
                AgentManager.get().addAgent(serverPlayer, new IlluminationAgent(serverPlayer, pos.above(), 8));
                return InteractionResult.SUCCESS;
            }

            if (isShovelTool(stack) && RegistryPredicates.isDirtLike(state)) {
                LogUtils.logDebug("Use block trigger feature=Pathanation player={} item={} block={} pos={}", serverPlayer.getScoreboardName(), itemId, targetBlockId, pos);
                AgentManager.get().addAgent(serverPlayer, new PathanationAgent(serverPlayer, pos, 8));
                return InteractionResult.SUCCESS;
            }

            if (isPickaxeTool(stack) && player.isShiftKeyDown()) {
                if (RegistryPredicates.isOreLike(state)) {
                    return InteractionResult.PASS;
                }
            }

            if (isPickaxeTool(stack) && RegistryPredicates.isStoneLike(state) && !player.isShiftKeyDown()) {
                LogUtils.logDebug("Use block trigger feature=Ventilation player={} item={} block={} pos={}", serverPlayer.getScoreboardName(), itemId, targetBlockId, pos);
                AgentManager.get().addAgent(serverPlayer, new VentilationAgent(serverPlayer, pos, 8));
                return InteractionResult.SUCCESS;
            }

            if (isFeatureEnabled(FeatureId.SUBSTITUTION) && player.isShiftKeyDown()) {
                if (!SubstitutionAgent.shouldQueueStartSubstitution(serverPlayer, hand, SubstitutionAction.INTERACT, pos)) {
                    SubstitutionAgent.markSubstitutionActivity(serverPlayer, hand, SubstitutionAction.INTERACT, pos);
                    return InteractionResult.SUCCESS;
                }
                LogUtils.logDebug("Use block trigger feature=Substitution player={} item={} pos={}", serverPlayer.getScoreboardName(), itemId, pos);
                AgentManager.get().addAgent(serverPlayer, new SubstitutionAgent(serverPlayer, state, SubstitutionAction.INTERACT, hand, substitutionConfig()));
                return InteractionResult.SUCCESS;
            }

            return InteractionResult.PASS;
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            int tickCount = server.getTickCount();
            for (ServerLevel level : server.getAllLevels()) {
                AgentManager.get().tick(level);
                if (tickCount % 20 == 0) {
                    for (ServerPlayer serverPlayer : level.players()) {
                        SubstitutionAgent.processSwitchBack(serverPlayer);
                        if (!AgentManager.get().hasAgentType(serverPlayer, CaptivationAgent.class)) {
                            LogUtils.logDebug("Server tick trigger feature=Captivation player={} intervalTicks={}", serverPlayer.getScoreboardName(), tickCount);
                            AgentManager.get().addAgent(serverPlayer, new CaptivationAgent(serverPlayer, 6.0));
                        }
                    }
                } else {
                    for (ServerPlayer serverPlayer : level.players()) {
                        SubstitutionAgent.processSwitchBack(serverPlayer);
                    }
                }
            }
            toolEvents.onServerTick();
        });
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void registerFabricLevelUnloadEvent() {
        try {
            Class<?> worldEventsClass = Class.forName("net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents");
            Event<?> unloadEvent = (Event<?>) worldEventsClass.getField("UNLOAD").get(null);
            Object callback = createFabricUnloadCallback("net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents$Unload");
            ((Event) unloadEvent).register(callback);
        } catch (ClassNotFoundException missingWorldEvents) {
            try {
                Class<?> levelEventsClass = Class.forName("net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents");
                Event<?> unloadEvent = (Event<?>) levelEventsClass.getField("UNLOAD").get(null);
                Object callback = createFabricUnloadCallback("net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents$Unload");
                ((Event) unloadEvent).register(callback);
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Unable to register Fabric level unload event", exception);
            }
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to register Fabric level unload event", exception);
        }
    }

    private Object createFabricUnloadCallback(String listenerClassName) throws ReflectiveOperationException {
        Class<?> listenerClass = Class.forName(listenerClassName);
        InvocationHandler handler = (proxy, method, args) -> {
            if (method.getDeclaringClass() == Object.class) {
                return switch (method.getName()) {
                    case "toString" -> "MinersAdvantageFabricUnloadCallback";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> args != null && args.length > 0 && proxy == args[0];
                    default -> null;
                };
            }

            if (args != null && args.length > 1 && args[1] instanceof ServerLevel level) {
                onServerLevelUnload(level);
            }
            return null;
        };
        return Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{listenerClass}, handler);
    }

    private void registerCollectiveDigSpeedCallback() {
        try {
            Class<?> collectivePlayerEventsClass = Class.forName("com.natamus.collective.fabric.callbacks.CollectivePlayerEvents");
            Object digSpeedEvent = collectivePlayerEventsClass.getField("ON_PLAYER_DIG_SPEED_CALC").get(null);
            Method registerMethod = findSingleArgumentMethod(digSpeedEvent.getClass(), "register");
            if (registerMethod == null) {
                LogUtils.logDebug("Collective dig speed callback registration skipped: register method not found");
                return;
            }
            Class<?> listenerClass = registerMethod.getParameterTypes()[0];
            Object callback = createCollectiveDigSpeedCallback(listenerClass);
            registerMethod.invoke(digSpeedEvent, callback);
            LogUtils.logDebug("Registered Collective Fabric dig speed callback for Veination");
        } catch (ClassNotFoundException ignored) {
            LogUtils.logDebug("Collective not present on Fabric classpath; dig speed callback not registered");
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to register Collective Fabric dig speed callback", exception);
        }
    }

    private Object createCollectiveDigSpeedCallback(Class<?> listenerClass) {
        InvocationHandler handler = (proxy, method, args) -> {
            if (method.getDeclaringClass() == Object.class) {
                return switch (method.getName()) {
                    case "toString" -> "MinersAdvantageCollectiveDigSpeedCallback";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> args != null && args.length > 0 && proxy == args[0];
                    default -> null;
                };
            }

            if (args == null || args.length < 4 || !(args[0] instanceof net.minecraft.world.level.Level level)
                || !(args[1] instanceof Player player) || !(args[2] instanceof Float digSpeed)
                || !(args[3] instanceof BlockState state)) {
                return args != null && args.length > 2 && args[2] instanceof Float value ? value : 1.0F;
            }

            VeinationConfig activeConfig = player instanceof ServerPlayer serverPlayer ? veinationConfig(serverPlayer) : veinationConfig();
            return veinationRuntime.adjustedDigSpeed(level, player, digSpeed, state, activeConfig);
        };

        return Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{listenerClass}, handler);
    }

    private Method findSingleArgumentMethod(Class<?> type, String name) {
        for (Method method : type.getMethods()) {
            if (method.getName().equals(name) && method.getParameterCount() == 1) {
                return method;
            }
        }
        return null;
    }

    private void onPlayerLogin(ServerPlayer player) {
        long playerId = playerId(player);
        LogUtils.logInfo("Player login player={} id={}", player.getScoreboardName(), playerId);
        SyncedClientConfig global = MAConfig_Base.getGlobalConfig();
        core.handlePlayerStateSyncPacket(new PlayerStateSyncPacket(playerId, global, global, new ServerOverridesConfig()));
    }

    private void onPlayerLogout(ServerPlayer player) {
        long playerId = playerId(player);
        LogUtils.logInfo("Player logout player={} id={}", player.getScoreboardName(), playerId);
        SubstitutionAgent.clearRestoreState(player);
        core.workerRuntimeService().abortAllForPlayerWithStats(playerId);
        core.playerStateService().clearPlayerState(playerId);
    }

    private void onServerLevelUnload(ServerLevel level) {
        for (ServerPlayer player : level.players()) {
            onPlayerLogout(player);
        }
    }

    private void onFabricEntityLoad(Entity entity, ServerLevel level) {
        if (entity instanceof ItemEntity itemEntity) {
            if (level.getNearestPlayer(entity, 8.0) instanceof ServerPlayer serverPlayer) {
                String dropItemId = itemId(itemEntity.getItem());
                LogUtils.logDebug("Observed item entity load player={} item={} count={}", serverPlayer.getScoreboardName(), dropItemId, itemEntity.getItem().getCount());
                veinationRuntime.handleItemEntityJoin(level, entity, serverPlayer, veinationConfig(serverPlayer));
                toolEvents.onItemPickup(dropItemId, false);
                core.workerRuntimeService().interceptLiveDropForPlayer(
                    playerId(serverPlayer),
                    "item:" + dropItemId,
                    itemEntity.getItem().getCount(),
                    true
                );
            }
        } else if (entity instanceof ExperienceOrb orb) {
            if (level.getNearestPlayer(entity, 8.0) instanceof ServerPlayer serverPlayer) {
                LogUtils.logDebug("Observed xp orb load player={} value={}", serverPlayer.getScoreboardName(), orb.getValue());
                core.workerRuntimeService().interceptLiveDropForPlayer(
                    playerId(serverPlayer),
                    "xp_orb",
                    orb.getValue(),
                    true
                );
            }
        }
    }

    private void routeToolUse(ItemStack stack, BlockPos pos, BlockState state) {
        String blockId = blockId(state);

        if (isPickaxeTool(stack)) {
            toolEvents.onPickaxeUse(pos.getX(), pos.getY(), pos.getZ(), blockId);
            return;
        }
        if (isShovelTool(stack)) {
            toolEvents.onShovelUse(pos.getX(), pos.getY(), pos.getZ(), blockId);
            return;
        }
        if (isHoeTool(stack)) {
            toolEvents.onHoeUse(pos.getX(), pos.getY(), pos.getZ(), blockId);
            return;
        }
        if (isAxeTool(stack)) {
            toolEvents.onAxeUse(pos.getX(), pos.getY(), pos.getZ(), blockId);
        }
    }

    private static String itemId(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }

    private static boolean isExcavationTool(ItemStack stack) {
        return isPickaxeTool(stack) || isShovelTool(stack);
    }

    private static boolean isExcavationBlock(BlockState state, ItemStack stack) {
        if (isPickaxeTool(stack)) {
            return state.is(BlockTags.MINEABLE_WITH_PICKAXE) || RegistryPredicates.isStoneLike(state) || RegistryPredicates.isOreLike(state);
        }
        if (isShovelTool(stack)) {
            return state.is(BlockTags.MINEABLE_WITH_SHOVEL) || RegistryPredicates.isDirtLike(state);
        }
        return false;
    }

    private static boolean isSubstitutionTool(ItemStack stack) {
        return isPickaxeTool(stack)
            || stack.getItem() instanceof AxeItem
            || stack.getItem() instanceof ShovelItem
            || stack.getItem() instanceof HoeItem;
    }

    private SubstitutionConfig substitutionConfig() {
        var component = core.components().get(FeatureId.SUBSTITUTION);
        if (component instanceof SubstitutionComponent substitutionComponent) {
            return substitutionComponent.config();
        }
        return SyncedClientConfig.defaults().substitution();
    }

    private VeinationConfig veinationConfig() {
        return MAConfig_Base.getGlobalConfig().veination();
    }

    private VeinationConfig veinationConfig(ServerPlayer player) {
        if (player == null) {
            return veinationConfig();
        }

        var synced = core.syncCoreService().getPlayerState(playerId(player));
        if (synced != null && synced.effectiveConfig() != null && synced.effectiveConfig().veination() != null) {
            return synced.effectiveConfig().veination();
        }
        return veinationConfig();
    }

    private static boolean isPickaxeTool(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().endsWith("_pickaxe");
    }

    private static boolean isShovelTool(ItemStack stack) {
        return stack.getItem() instanceof ShovelItem;
    }

    private static boolean isAxeTool(ItemStack stack) {
        return stack.getItem() instanceof AxeItem;
    }

    private static boolean isHoeTool(ItemStack stack) {
        return stack.getItem() instanceof HoeItem;
    }

    private boolean isFeatureEnabled(FeatureId featureId) {
        var component = core.components().get(featureId);
        return component != null && component.isEnabled();
    }

    private static String blockId(BlockState state) {
        return BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
    }

    private static long playerId(Player player) {
        return player.getUUID().getLeastSignificantBits();
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
import net.minecraft.world.level.Level;
import uk.co.duelmonster.minersadvantage.client.NeoForgeNetworkEvents;

@Mod("minersadvantage")
public final class ModEntry {
    private final MinersAdvantageCore core = new MinersAdvantageCore();
    private final ToolEventHandler toolEvents = new CommonEventHandlerImpl(core);
    private final VeinationRuntimeService veinationRuntime = new VeinationRuntimeService();

    public ModEntry(IEventBus modEventBus, ModContainer modContainer, Dist dist) {
        LogUtils.applyConfiguredLogging();
        LogUtils.logInfo("Initializing {} {} on NeoForge debugLogging={}", ModCommon.MOD_NAME, ModCommon.MOD_VERSION, LogUtils.isDebugLoggingEnabled());
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
        NeoForge.EVENT_BUS.addListener(this::onBreakSpeed);
        NeoForge.EVENT_BUS.addListener(this::onServerTick);
        NeoForge.EVENT_BUS.addListener(this::onPlayerLogin);
        NeoForge.EVENT_BUS.addListener(this::onPlayerLogout);
        NeoForge.EVENT_BUS.addListener(this::onLevelUnload);
        NeoForge.EVENT_BUS.addListener(this::onEntityJoinLevel);
        NeoForge.EVENT_BUS.addListener(this::onToolModification);
    }

    private void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        long playerId = playerId(player);
        LogUtils.logInfo("Player login player={} id={}", player.getScoreboardName(), playerId);
        SyncedClientConfig defaults = SyncedClientConfig.defaults();
        core.handlePlayerStateSyncPacket(new PlayerStateSyncPacket(playerId, defaults, defaults, new ServerOverridesConfig()));
    }

    private void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        long playerId = playerId(player);
        LogUtils.logInfo("Player logout player={} id={}", player.getScoreboardName(), playerId);
        SubstitutionAgent.clearRestoreState(player);
        core.workerRuntimeService().abortAllForPlayerWithStats(playerId);
        core.playerStateService().clearPlayerState(playerId);
    }

    private void onLevelUnload(LevelEvent.Unload event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        for (ServerPlayer player : level.players()) {
            SubstitutionAgent.clearRestoreState(player);
            core.workerRuntimeService().abortAllForPlayerWithStats(player.getUUID().getLeastSignificantBits());
            core.playerStateService().clearPlayerState(player.getUUID().getLeastSignificantBits());
        }
    }

    private void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || event.getLevel().isClientSide()) {
            return;
        }

        Entity entity = event.getEntity();
        if (entity instanceof ItemEntity itemEntity) {
            if (level.getNearestPlayer(entity, 8.0) instanceof ServerPlayer serverPlayer) {
                String itemId = itemId(itemEntity.getItem());
                veinationRuntime.handleItemEntityJoin(level, entity, serverPlayer, veinationConfig());
                toolEvents.onItemPickup(itemId, false);
                core.workerRuntimeService().interceptLiveDropForPlayer(
                    playerId(serverPlayer),
                    "item:" + itemId,
                    itemEntity.getItem().getCount(),
                    true
                );
            }
        } else if (entity instanceof ExperienceOrb orb) {
            if (level.getNearestPlayer(entity, 8.0) instanceof ServerPlayer serverPlayer) {
                core.workerRuntimeService().interceptLiveDropForPlayer(
                    playerId(serverPlayer),
                    "xp_orb",
                    orb.getValue(),
                    true
                );
            }
        }
    }

    private void onToolModification(BlockEvent.BlockToolModificationEvent event) {
        if (event.getPlayer() == null || event.getLevel().isClientSide()) {
            return;
        }
        BlockState state = event.getFinalState() != null ? event.getFinalState() : event.getState();
        LogUtils.logDebug("NeoForge tool modification player={} item={} block={} pos={}", event.getPlayer().getScoreboardName(), itemId(event.getHeldItemStack()), blockId(state), event.getPos());
        routeToolUse(event.getHeldItemStack(), event.getPos(), state);
    }

    private void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity() == null || event.getLevel().isClientSide()) {
            return;
        }

        LogUtils.logDebug("NeoForge right click player={} item={} block={} pos={}", event.getEntity().getScoreboardName(), itemId(event.getItemStack()), blockId(event.getLevel().getBlockState(event.getPos())), event.getPos());
        routeToolUse(event.getItemStack(), event.getPos(), event.getLevel().getBlockState(event.getPos()));

        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (!isFeatureEnabled(FeatureId.SUBSTITUTION)) {
            return;
        }

        ItemStack stack = event.getItemStack();
        if (event.getEntity().isShiftKeyDown()) {
            BlockState state = event.getLevel().getBlockState(event.getPos());
            if (!SubstitutionAgent.shouldQueueStartSubstitution(serverPlayer, event.getHand(), SubstitutionAction.INTERACT, event.getPos())) {
                SubstitutionAgent.markSubstitutionActivity(serverPlayer, event.getHand(), SubstitutionAction.INTERACT, event.getPos());
                return;
            }
            AgentManager.get().addAgent(serverPlayer, new SubstitutionAgent(serverPlayer, state, SubstitutionAction.INTERACT, event.getHand(), substitutionConfig()));
        }
    }

    private void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getEntity() == null || event.getLevel().isClientSide()) {
            return;
        }

        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            ItemStack stack = event.getItemStack();
            BlockState state = event.getLevel().getBlockState(event.getPos());
            VeinationConfig config = veinationConfig();
            boolean veinationGesture = config.oreHarvestWithoutSneak() ? !event.getEntity().isShiftKeyDown() : event.getEntity().isShiftKeyDown();
            boolean allowedPickaxe = veinationRuntime.isPickaxeAllowed((Level) event.getLevel(), config, stack);

            if (isFeatureEnabled(FeatureId.VEINATION) && veinationGesture && isPickaxeTool(stack) && allowedPickaxe && RegistryPredicates.isOreLike(state)) {
                veinationRuntime.registerDropAnchor(serverPlayer, event.getPos(), config);
                AgentManager agentManager = AgentManager.get();
                if (!agentManager.hasAgentType(serverPlayer, VeinationAgent.class)) {
                    agentManager.addAgent(serverPlayer, new VeinationAgent(serverPlayer, event.getPos(), veinationRuntime, config));
                }
            }
        }

        if (event.getEntity() instanceof ServerPlayer serverPlayer && isFeatureEnabled(FeatureId.SUBSTITUTION)) {
            ItemStack stack = event.getItemStack();
            BlockState state = event.getLevel().getBlockState(event.getPos());
            if (!SubstitutionAgent.shouldQueueStartSubstitution(serverPlayer, event.getHand(), SubstitutionAction.BREAK, event.getPos())) {
                SubstitutionAgent.markSubstitutionActivity(serverPlayer, event.getHand(), SubstitutionAction.BREAK, event.getPos());
            } else {
                AgentManager.get().addAgent(serverPlayer, new SubstitutionAgent(serverPlayer, state, SubstitutionAction.BREAK, event.getHand(), substitutionConfig()));
            }
        }

        String blockId = BuiltInRegistries.BLOCK.getKey(event.getLevel().getBlockState(event.getPos()).getBlock()).toString();
        core.workerRuntimeService().interceptLiveDropForPlayer(
            event.getEntity().getUUID().getLeastSignificantBits(),
            "item:" + blockId,
            1,
            true
        );
    }

    private void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (event.getEntity() == null || event.getEntity().level().isClientSide()) {
            return;
        }

        float adjusted = veinationRuntime.adjustedDigSpeed(
            event.getEntity().level(),
            event.getEntity(),
            event.getOriginalSpeed(),
            event.getState(),
            veinationConfig()
        );
        if (adjusted != event.getOriginalSpeed()) {
            event.setNewSpeed(adjusted);
        }
    }

    private void onServerTick(ServerTickEvent.Post event) {
        if (event.getServer() != null) {
            for (ServerLevel level : event.getServer().getAllLevels()) {
                for (ServerPlayer serverPlayer : level.players()) {
                    SubstitutionAgent.processSwitchBack(serverPlayer);
                }
            }
        }
        toolEvents.onServerTick();
    }

    private void routeToolUse(ItemStack stack, BlockPos pos, BlockState state) {
        String blockId = blockId(state);

        if (isPickaxeTool(stack)) {
            toolEvents.onPickaxeUse(pos.getX(), pos.getY(), pos.getZ(), blockId);
            return;
        }
        if (isShovelTool(stack)) {
            toolEvents.onShovelUse(pos.getX(), pos.getY(), pos.getZ(), blockId);
            return;
        }
        if (isHoeTool(stack)) {
            toolEvents.onHoeUse(pos.getX(), pos.getY(), pos.getZ(), blockId);
            return;
        }
        if (isAxeTool(stack)) {
            toolEvents.onAxeUse(pos.getX(), pos.getY(), pos.getZ(), blockId);
        }
    }

    private static String itemId(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }

    private static boolean isSubstitutionTool(ItemStack stack) {
        return isPickaxeTool(stack)
            || stack.getItem() instanceof AxeItem
            || stack.getItem() instanceof ShovelItem
            || stack.getItem() instanceof HoeItem;
    }

    private SubstitutionConfig substitutionConfig() {
        var component = core.components().get(FeatureId.SUBSTITUTION);
        if (component instanceof SubstitutionComponent substitutionComponent) {
            return substitutionComponent.config();
        }
        return SyncedClientConfig.defaults().substitution();
    }

    private VeinationConfig veinationConfig() {
        var component = core.components().get(FeatureId.VEINATION);
        if (component instanceof uk.co.duelmonster.minersadvantage.common.feature.utility.VeinationComponent veinationComponent) {
            return veinationComponent.config();
        }
        return SyncedClientConfig.defaults().veination();
    }

    private boolean isFeatureEnabled(FeatureId featureId) {
        var component = core.components().get(featureId);
        return component != null && component.isEnabled();
    }

    private static boolean isPickaxeTool(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().endsWith("_pickaxe");
    }

    private static boolean isShovelTool(ItemStack stack) {
        return stack.getItem() instanceof ShovelItem;
    }

    private static boolean isAxeTool(ItemStack stack) {
        return stack.getItem() instanceof AxeItem;
    }

    private static boolean isHoeTool(ItemStack stack) {
        return stack.getItem() instanceof HoeItem;
    }

    private static String blockId(BlockState state) {
        return BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
    }

    private static long playerId(Player player) {
        return player.getUUID().getLeastSignificantBits();
    }
}
*/ //?}
