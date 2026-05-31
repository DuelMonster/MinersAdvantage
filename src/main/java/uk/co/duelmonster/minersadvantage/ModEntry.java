package uk.co.duelmonster.minersadvantage;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import uk.co.duelmonster.minersadvantage.common.config.MAClientRootConfig;
import uk.co.duelmonster.minersadvantage.common.config.MAServerRootConfig;
import uk.co.duelmonster.minersadvantage.common.config.MAConfig_Base;
import uk.co.duelmonster.minersadvantage.common.config.CaptivationConfig;
import uk.co.duelmonster.minersadvantage.common.config.CommonConfig;
import uk.co.duelmonster.minersadvantage.common.config.CropinationConfig;
import uk.co.duelmonster.minersadvantage.common.config.CultivationConfig;
import uk.co.duelmonster.minersadvantage.common.config.ExcavationConfig;
import uk.co.duelmonster.minersadvantage.common.config.IlluminationConfig;
import uk.co.duelmonster.minersadvantage.common.config.LumbinationConfig;
import uk.co.duelmonster.minersadvantage.common.config.PathanationConfig;
import uk.co.duelmonster.minersadvantage.common.config.ShaftanationConfig;
import uk.co.duelmonster.minersadvantage.common.config.SubstitutionConfig;
import uk.co.duelmonster.minersadvantage.common.config.SubstitutionConfig.SubstitutionAction;
import uk.co.duelmonster.minersadvantage.common.config.SyncedClientConfig;
import uk.co.duelmonster.minersadvantage.common.config.VeinationConfig;
import uk.co.duelmonster.minersadvantage.common.config.VentilationConfig;
import uk.co.duelmonster.minersadvantage.common.event.CommonEventHandlerImpl;
import uk.co.duelmonster.minersadvantage.common.event.ToolEventHandler;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;
import uk.co.duelmonster.minersadvantage.common.feature.captivation.CaptivationComponent;
import uk.co.duelmonster.minersadvantage.common.feature.farming.CropinationComponent;
import uk.co.duelmonster.minersadvantage.common.feature.farming.CultivationComponent;
import uk.co.duelmonster.minersadvantage.common.feature.harvest.LumbinationComponent;
import uk.co.duelmonster.minersadvantage.common.feature.mining.ExcavationComponent;
import uk.co.duelmonster.minersadvantage.common.feature.mining.ShaftanationComponent;
import uk.co.duelmonster.minersadvantage.common.feature.mining.VentilationComponent;
import uk.co.duelmonster.minersadvantage.common.feature.utility.IlluminationComponent;
import uk.co.duelmonster.minersadvantage.common.feature.utility.PathanationComponent;
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
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import uk.co.duelmonster.minersadvantage.client.FabricNetworkEvents;

/**
 * ModEntry keeps this part of MinersAdvantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class ModEntry implements ModInitializer {
    private final MinersAdvantageCore core = new MinersAdvantageCore();
    private final ToolEventHandler toolEvents = new CommonEventHandlerImpl(core);
    private final VeinationRuntimeService veinationRuntime = new VeinationRuntimeService();
    private final Map<Long, BreakFaceState> lastBreakFaces = new ConcurrentHashMap<>();

    /**
     * Bootstrap core services and register all Fabric-side gameplay/network/event hooks.
     */
    @Override
    /**
     * o ni ni ti al iz e exists so this path stays predictable and easier to debug when things get weird.
     */
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
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (world.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
                return InteractionResult.PASS;
            }

            rememberBreakFace(serverPlayer, pos, direction);

            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (!isFeatureEnabled(FeatureId.SUBSTITUTION)) {
                return InteractionResult.PASS;
            }

            BlockState state = world.getBlockState(pos);
            ItemStack stack = player.getItemInHand(hand);

            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (!SubstitutionAgent.shouldQueueStartSubstitution(serverPlayer, hand, SubstitutionAction.BREAK, pos)) {
                return InteractionResult.PASS;
            }

            LogUtils.logDebug("Attack block trigger feature=Substitution player={} hand={} item={} pos={}", serverPlayer.getScoreboardName(), hand, itemId(stack), pos);
            SubstitutionAgent agent = new SubstitutionAgent(serverPlayer, state, SubstitutionAction.BREAK, hand, substitutionConfig(serverPlayer));
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (!agent.tick()) {
                AgentManager.get().addAgent(serverPlayer, agent);
            }
            return InteractionResult.PASS;
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (world.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
                return InteractionResult.PASS;
            }

            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (!isFeatureEnabled(FeatureId.SUBSTITUTION)) {
                return InteractionResult.PASS;
            }

            SubstitutionConfig config = substitutionConfig(serverPlayer);
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (config.ignorePassiveMobs() && entity.getType().getCategory().isFriendly()) {
                return InteractionResult.PASS;
            }

            BlockPos targetPos = entity.blockPosition();
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (!SubstitutionAgent.shouldQueueStartSubstitution(serverPlayer, hand, SubstitutionAction.ATTACK, targetPos)) {
                SubstitutionAgent.markSubstitutionActivity(serverPlayer, hand, SubstitutionAction.ATTACK, targetPos);
                return InteractionResult.PASS;
            }

            BlockState contextState = world.getBlockState(targetPos);
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (contextState.isAir()) {
                contextState = world.getBlockState(targetPos.below());
            }
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (contextState.isAir()) {
                contextState = world.getBlockState(serverPlayer.blockPosition());
            }
            LogUtils.logDebug(
                "Attack entity trigger feature=Substitution player={} hand={} item={} targetEntity={} pos={}",
                serverPlayer.getScoreboardName(),
                hand,
                itemId(player.getItemInHand(hand)),
                BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()),
                targetPos
            );
            String targetEntityTypeId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
            SubstitutionAgent agent = new SubstitutionAgent(serverPlayer, contextState, SubstitutionAction.ATTACK, hand, config, targetEntityTypeId);
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (!agent.tick()) {
                AgentManager.get().addAgent(serverPlayer, agent);
            }
            return InteractionResult.PASS;
        });

        PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
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
            CommonConfig playerCommonConfig = commonConfig(serverPlayer);
            VeinationConfig veinationConfig = veinationConfig(serverPlayer);
            boolean veinationGesture = veinationConfig.oreHarvestWithoutSneak() ? !player.isShiftKeyDown() : player.isShiftKeyDown();
            boolean allowedPickaxe = veinationRuntime.isPickaxeAllowed(level, veinationConfig, stack);

            boolean allowedOre = veinationRuntime.isOreAllowed(veinationConfig, state);

            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (isFeatureEnabled(FeatureId.VEINATION) && playerCommonConfig.mineVeins() && veinationGesture && isPickaxeTool(stack) && allowedPickaxe && allowedOre) {
                LogUtils.logDebug("Block break trigger feature=Veination player={} item={} block={} pos={}", serverPlayer.getScoreboardName(), itemId, brokenBlockId, pos);
                veinationRuntime.registerDropAnchor(serverPlayer, pos, veinationConfig);
                AgentManager agentManager = AgentManager.get();
                // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                if (!agentManager.hasAgentType(serverPlayer, VeinationAgent.class)) {
                    agentManager.addAgent(serverPlayer, new VeinationAgent(serverPlayer, pos, state, playerCommonConfig, veinationRuntime, veinationConfig));
                }
            } else if (shaftModeActive) {
                Direction breakFace = consumeBreakFace(serverPlayer, pos);
                boolean verticalFace = breakFace == Direction.UP || breakFace == Direction.DOWN;
                AgentManager agentManager = AgentManager.get();
                // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                if (verticalFace) {
                    LogUtils.logDebug("Block break trigger feature=Ventilation player={} item={} block={} pos={} face={}", serverPlayer.getScoreboardName(), itemId, brokenBlockId, pos, breakFace);
                    agentManager.addAgent(serverPlayer, new VentilationAgent(serverPlayer, pos, breakFace != null ? breakFace.getOpposite() : Direction.DOWN, ventilationConfig(serverPlayer), commonConfig(serverPlayer), veinationRuntime, veinationConfig, stack));
                } else {
                    LogUtils.logDebug("Block break trigger feature=Shaftanation player={} item={} block={} pos={} face={}", serverPlayer.getScoreboardName(), itemId, brokenBlockId, pos, breakFace == null ? "unknown" : breakFace);
                    // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                    if (!agentManager.hasAgentType(serverPlayer, ShaftanationAgent.class)) {
                        agentManager.addAgent(serverPlayer, new ShaftanationAgent(serverPlayer, pos, serverPlayer.getDirection(), shaftanationConfig(serverPlayer), commonConfig(serverPlayer), illuminationConfig(serverPlayer).lowestLightLevel(), veinationRuntime, veinationConfig, stack, playerState.selectedShaftanationShapeIndex(), breakFace));
                    }
                }
            } else if (!shaftModeActive && excavationActive) {
                LogUtils.logDebug("Block break trigger feature=Excavation player={} item={} block={} pos={}", serverPlayer.getScoreboardName(), itemId, brokenBlockId, pos);
                ExcavationConfig excavationConfig = excavationConfig(serverPlayer);
                CommonConfig commonConfig = commonConfig(serverPlayer);
                IlluminationConfig excavationIlluminationConfig = isFeatureEnabled(FeatureId.ILLUMINATION) ? illuminationConfig(serverPlayer) : null;
                AgentManager.get().addAgent(
                    serverPlayer,
                    new ExcavationAgent(
                        serverPlayer,
                        pos,
                        state,
                        excavationConfig,
                        commonConfig,
                        excavationConfig.width(),
                        excavationConfig.height(),
                        excavationConfig.depth(),
                        veinationRuntime,
                        veinationConfig,
                        stack,
                        excavationIlluminationConfig,
                        playerState.selectedExcavationShapeIndex(),
                        consumeBreakFace(serverPlayer, pos)
                    )
                );
            } else if (isFeatureEnabled(FeatureId.LUMBINATION)) {
                LumbinationConfig lumbinationConfig = lumbinationConfig(serverPlayer);
                // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                if (isConfiguredAxe(stack, lumbinationConfig) && isConfiguredLog(state, lumbinationConfig)) {
                LogUtils.logDebug("Block break trigger feature=Lumbination player={} item={} block={} pos={}", serverPlayer.getScoreboardName(), itemId, brokenBlockId, pos);
                    AgentManager.get().addAgent(serverPlayer, new LumbinationAgent(serverPlayer, pos, state, lumbinationConfig, commonConfig(serverPlayer)));
                }
            }
        });

        registerCollectiveDigSpeedCallback();

        UseItemCallback.EVENT.register((player, world, hand) -> {
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (world.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
                return InteractionResult.PASS;
            }
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (player.isShiftKeyDown()) {
                LogUtils.logDebug("Use item trigger feature=Captivation player={} hand={} item={}", serverPlayer.getScoreboardName(), hand, itemId(player.getMainHandItem()));
                AgentManager.get().addAgent(serverPlayer, new CaptivationAgent(serverPlayer, captivationConfig(serverPlayer)));
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (world.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
                return InteractionResult.PASS;
            }

            BlockPos pos = hitResult.getBlockPos();
            BlockState state = world.getBlockState(pos);
            ItemStack stack = player.getItemInHand(hand);
            String itemId = itemId(stack);
            String targetBlockId = blockId(state);

            routeToolUse(stack, pos, state);

            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (isHoeTool(stack)) {
                // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                if (RegistryPredicates.isCropBlock(state)) {
                    LogUtils.logDebug("Use block trigger feature=Cropination player={} item={} block={} pos={}", serverPlayer.getScoreboardName(), itemId, targetBlockId, pos);
                    CommonConfig commonConfig = commonConfig(serverPlayer);
                    AgentManager.get().addAgent(serverPlayer, new CropinationAgent(serverPlayer, pos, commonConfig.blockRadius(), cropinationConfig(serverPlayer), commonConfig));
                    return InteractionResult.SUCCESS;
                }
                // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                if (RegistryPredicates.isDirtLike(state)) {
                    LogUtils.logDebug("Use block trigger feature=Cultivation player={} item={} block={} pos={}", serverPlayer.getScoreboardName(), itemId, targetBlockId, pos);
                    CommonConfig commonConfig = commonConfig(serverPlayer);
                    AgentManager.get().addAgent(serverPlayer, new CultivationAgent(serverPlayer, pos, commonConfig.blockRadius(), cultivationConfig(serverPlayer), commonConfig));
                    return InteractionResult.SUCCESS;
                }
            }

            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (isShovelTool(stack) && state.is(BlockTags.DIRT)) {
                LogUtils.logDebug("Use block trigger feature=Pathanation player={} item={} block={} pos={}", serverPlayer.getScoreboardName(), itemId, targetBlockId, pos);
                AgentManager.get().addAgent(serverPlayer, new PathanationAgent(serverPlayer, pos, serverPlayer.getDirection(), pathanationConfig(serverPlayer), commonConfig(serverPlayer)));
                return InteractionResult.SUCCESS;
            }

            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (isPickaxeTool(stack) && player.isShiftKeyDown()) {
                // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                if (RegistryPredicates.isOreLike(state)) {
                    return InteractionResult.PASS;
                }
            }

            var playerState = core.playerStateService().getPlayerState(playerId(serverPlayer));
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (playerState.shaftVentToggled() && !player.isShiftKeyDown()) {
                Direction face = hitResult.getDirection();
                boolean verticalFace = face == Direction.UP || face == Direction.DOWN;
                // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                if (verticalFace) {
                    LogUtils.logDebug("Use block trigger feature=Ventilation player={} item={} block={} pos={} face={}", serverPlayer.getScoreboardName(), itemId, targetBlockId, pos, face);
                    VeinationConfig veinationConfig = veinationConfig(serverPlayer);
                    AgentManager.get().addAgent(serverPlayer, new VentilationAgent(serverPlayer, pos, face.getOpposite(), ventilationConfig(serverPlayer), commonConfig(serverPlayer), veinationRuntime, veinationConfig, stack));
                } else {
                    LogUtils.logDebug("Use block trigger feature=Shaftanation player={} item={} block={} pos={} face={}", serverPlayer.getScoreboardName(), itemId, targetBlockId, pos, face);
                    AgentManager agentManager = AgentManager.get();
                    // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                    if (!agentManager.hasAgentType(serverPlayer, ShaftanationAgent.class)) {
                        VeinationConfig veinationConfig = veinationConfig(serverPlayer);
                        agentManager.addAgent(serverPlayer, new ShaftanationAgent(serverPlayer, pos, serverPlayer.getDirection(), shaftanationConfig(serverPlayer), commonConfig(serverPlayer), illuminationConfig(serverPlayer).lowestLightLevel(), veinationRuntime, veinationConfig, stack, playerState.selectedShaftanationShapeIndex(), face));
                    }
                }
                return InteractionResult.SUCCESS;
            }

            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (isFeatureEnabled(FeatureId.SUBSTITUTION) && player.isShiftKeyDown()) {
                // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                if (!SubstitutionAgent.shouldQueueStartSubstitution(serverPlayer, hand, SubstitutionAction.INTERACT, pos)) {
                    SubstitutionAgent.markSubstitutionActivity(serverPlayer, hand, SubstitutionAction.INTERACT, pos);
                    return InteractionResult.SUCCESS;
                }
                LogUtils.logDebug("Use block trigger feature=Substitution player={} item={} pos={}", serverPlayer.getScoreboardName(), itemId, pos);
                SubstitutionAgent agent = new SubstitutionAgent(serverPlayer, state, SubstitutionAction.INTERACT, hand, substitutionConfig(serverPlayer));
                // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                if (!agent.tick()) {
                    AgentManager.get().addAgent(serverPlayer, agent);
                }
                return InteractionResult.SUCCESS;
            }

            return InteractionResult.PASS;
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            int tickCount = server.getTickCount();
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            for (ServerLevel level : server.getAllLevels()) {
                AgentManager.get().tick(level);
                // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                if (tickCount % 20 == 0) {
                    // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                    for (ServerPlayer serverPlayer : level.players()) {
                        SubstitutionAgent.processSwitchBack(serverPlayer);
                        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                        if (!AgentManager.get().hasAgentType(serverPlayer, CaptivationAgent.class)) {
                            LogUtils.logDebug("Server tick trigger feature=Captivation player={} intervalTicks={}", serverPlayer.getScoreboardName(), tickCount);
                            AgentManager.get().addAgent(serverPlayer, new CaptivationAgent(serverPlayer, captivationConfig(serverPlayer)));
                        }
                    }
                } else {
                    // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                    for (ServerPlayer serverPlayer : level.players()) {
                        SubstitutionAgent.processSwitchBack(serverPlayer);
                    }
                }
            }
            toolEvents.onServerTick();
        });
    }

    /**
     * Register world/level unload callback using whichever Fabric lifecycle API is present.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    /**
     * r eg is te rf ab ri cl ev el un lo ad ev en t exists so this path stays predictable and easier to debug when things get weird.
     */
    private void registerFabricLevelUnloadEvent() {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        try {
            Class<?> worldEventsClass = Class.forName("net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents");
            Event<?> unloadEvent = (Event<?>) worldEventsClass.getField("UNLOAD").get(null);
            Object callback = createFabricUnloadCallback("net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents$Unload");
            ((Event) unloadEvent).register(callback);
        } catch (ClassNotFoundException missingWorldEvents) {
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
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

    /**
     * Build dynamic unload listener proxy compatible across mapping variants.
     */
    private Object createFabricUnloadCallback(String listenerClassName) throws ReflectiveOperationException {
        Class<?> listenerClass = Class.forName(listenerClassName);
        InvocationHandler handler = (proxy, method, args) -> {
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (method.getDeclaringClass() == Object.class) {
                return switch (method.getName()) {
                    case "toString" -> "MinersAdvantageFabricUnloadCallback";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> args != null && args.length > 0 && proxy == args[0];
                    default -> null;
                };
            }

            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (args != null && args.length > 1 && args[1] instanceof ServerLevel level) {
                onServerLevelUnload(level);
            }
            return null;
        };
        return Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{listenerClass}, handler);
    }

    /**
     * Register optional Collective dig-speed callback when the library is available.
     */
    private void registerCollectiveDigSpeedCallback() {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        try {
            Class<?> collectivePlayerEventsClass = Class.forName("com.natamus.collective.fabric.callbacks.CollectivePlayerEvents");
            Object digSpeedEvent = collectivePlayerEventsClass.getField("ON_PLAYER_DIG_SPEED_CALC").get(null);
            Method registerMethod = findSingleArgumentMethod(digSpeedEvent.getClass(), "register");
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
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

    /**
     * Build dynamic dig-speed callback proxy forwarding through veination runtime.
     */
    private Object createCollectiveDigSpeedCallback(Class<?> listenerClass) {
        InvocationHandler handler = (proxy, method, args) -> {
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (method.getDeclaringClass() == Object.class) {
                return switch (method.getName()) {
                    case "toString" -> "MinersAdvantageCollectiveDigSpeedCallback";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> args != null && args.length > 0 && proxy == args[0];
                    default -> null;
                };
            }

            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
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

    /**
     * Find first public method by name that accepts exactly one argument.
     */
    private Method findSingleArgumentMethod(Class<?> type, String name) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        for (Method method : type.getMethods()) {
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (method.getName().equals(name) && method.getParameterCount() == 1) {
                return method;
            }
        }
        return null;
    }

    /**
     * Initialize synced player state on login.
     */
    private void onPlayerLogin(ServerPlayer player) {
        long playerId = playerId(player);
        LogUtils.logInfo("Player login player={} id={}", player.getScoreboardName(), playerId);
        MAClientRootConfig clientConfig = MAConfig_Base.getClientRootConfig();
        MAServerRootConfig serverConfig = MAConfig_Base.getServerRootConfig();
        core.handlePlayerStateSyncPacket(new PlayerStateSyncPacket(playerId, clientConfig, serverConfig));
    }

    /**
     * Flush player-specific runtime/sync/worker state on logout.
     */
    private void onPlayerLogout(ServerPlayer player) {
        long playerId = playerId(player);
        LogUtils.logInfo("Player logout player={} id={}", player.getScoreboardName(), playerId);
        SubstitutionAgent.clearRestoreState(player);
        core.workerRuntimeService().abortAllForPlayerWithStats(playerId);
        core.playerStateService().clearPlayerState(playerId);
    }

    /**
     * Treat level unload as implicit logout for all players in that level.
     */
    private void onServerLevelUnload(ServerLevel level) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        for (ServerPlayer player : level.players()) {
            onPlayerLogout(player);
        }
    }

    /**
     * Observe item/xp entity loads and feed runtime drop-tracking services.
     */
    private void onFabricEntityLoad(Entity entity, ServerLevel level) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (entity instanceof ItemEntity itemEntity) {
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (level.getNearestPlayer(entity, 8.0) instanceof ServerPlayer serverPlayer) {
                String dropItemId = itemId(itemEntity.getItem());
                LogUtils.logDebug("Observed item entity load player={} item={} count={}", serverPlayer.getScoreboardName(), dropItemId, itemEntity.getItem().getCount());
                boolean gatherDrops = commonConfig(serverPlayer).gatherDrops();
                veinationRuntime.handleItemEntityJoin(level, entity, serverPlayer, veinationConfig(serverPlayer), gatherDrops);
                toolEvents.onItemPickup(dropItemId, false);
                core.workerRuntimeService().interceptLiveDropForPlayer(
                    playerId(serverPlayer),
                    "item:" + dropItemId,
                    itemEntity.getItem().getCount(),
                    gatherDrops
                );
            }
        } else if (entity instanceof ExperienceOrb orb) {
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (level.getNearestPlayer(entity, 8.0) instanceof ServerPlayer serverPlayer) {
                LogUtils.logDebug("Observed xp orb load player={} value={}", serverPlayer.getScoreboardName(), orb.getValue());
                boolean gatherDrops = commonConfig(serverPlayer).gatherDrops();
                core.workerRuntimeService().interceptLiveDropForPlayer(
                    playerId(serverPlayer),
                    "xp_orb",
                    orb.getValue(),
                    gatherDrops
                );
            }
        }
    }

    /**
     * Cache last attacked block face for later shaft/vent orientation decisions.
     */
    private void rememberBreakFace(ServerPlayer serverPlayer, BlockPos pos, Direction face) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (serverPlayer == null || pos == null || face == null) {
            return;
        }
        lastBreakFaces.put(playerId(serverPlayer), new BreakFaceState(pos.immutable(), face));
    }

    /**
     * Consume cached break face if it still matches the target block position.
     */
    private Direction consumeBreakFace(ServerPlayer serverPlayer, BlockPos pos) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (serverPlayer == null || pos == null) {
            return null;
        }
        BreakFaceState state = lastBreakFaces.remove(playerId(serverPlayer));
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (state == null || !state.pos().equals(pos)) {
            return null;
        }
        return state.face();
    }

    /**
     * b re ak fa ce st at e exists so this path stays predictable and easier to debug when things get weird.
     */
    private record BreakFaceState(BlockPos pos, Direction face) {
    }

    /**
     * Route generic tool-use telemetry to the matching tool event sink.
     */
    private void routeToolUse(ItemStack stack, BlockPos pos, BlockState state) {
        String blockId = blockId(state);

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (isPickaxeTool(stack)) {
            toolEvents.onPickaxeUse(pos.getX(), pos.getY(), pos.getZ(), blockId);
            return;
        }
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (isShovelTool(stack)) {
            toolEvents.onShovelUse(pos.getX(), pos.getY(), pos.getZ(), blockId);
            return;
        }
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (isHoeTool(stack)) {
            toolEvents.onHoeUse(pos.getX(), pos.getY(), pos.getZ(), blockId);
            return;
        }
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (isAxeTool(stack)) {
            toolEvents.onAxeUse(pos.getX(), pos.getY(), pos.getZ(), blockId);
        }
    }

    /**
     * Resolve canonical item id string.
     */
    private static String itemId(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }

    /**
     * Placeholder excavation tool gate (currently permissive).
     */
    private static boolean isExcavationTool(ItemStack stack) {
        return true;
    }

    /**
     * Placeholder shaft tool gate (currently permissive).
     */
    private static boolean isShaftTool(ItemStack stack) {
        return true;
    }

    /**
     * Check whether tool is eligible for substitution workflows.
     */
    private static boolean isSubstitutionTool(ItemStack stack) {
        return isPickaxeTool(stack)
            || stack.getItem() instanceof AxeItem
            || stack.getItem() instanceof ShovelItem
            || stack.getItem() instanceof HoeItem;
    }

    /**
     * Resolve substitution config from component or fallback defaults.
     */
    private SubstitutionConfig substitutionConfig() {
        var component = core.components().get(FeatureId.SUBSTITUTION);
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (component instanceof SubstitutionComponent substitutionComponent) {
            return substitutionComponent.config();
        }
        return SyncedClientConfig.defaults().substitution();
    }

    /**
     * Resolve per-player effective substitution config with sync fallback.
     */
    private SubstitutionConfig substitutionConfig(ServerPlayer player) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (player == null) {
            return substitutionConfig();
        }

        var synced = core.syncCoreService().getPlayerState(playerId(player));
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (synced != null && synced.effectiveConfig() != null && synced.effectiveConfig().substitution() != null) {
            return synced.effectiveConfig().substitution();
        }
        return substitutionConfig();
    }

    /**
     * Resolve captivation config from component or global fallback.
     */
    private CaptivationConfig captivationConfig() {
        var component = core.components().get(FeatureId.CAPTIVATION);
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (component instanceof CaptivationComponent captivationComponent) {
            return captivationComponent.config();
        }
        return MAConfig_Base.getGlobalConfig().captivation();
    }

    /**
     * Resolve per-player effective captivation config with sync fallback.
     */
    private CaptivationConfig captivationConfig(ServerPlayer player) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (player == null) {
            return captivationConfig();
        }

        var synced = core.syncCoreService().getPlayerState(playerId(player));
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (synced != null && synced.effectiveConfig() != null && synced.effectiveConfig().captivation() != null) {
            return synced.effectiveConfig().captivation();
        }
        return captivationConfig();
    }

    /**
     * Resolve cropination config from component or global fallback.
     */
    private CropinationConfig cropinationConfig() {
        var component = core.components().get(FeatureId.CROPINATION);
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (component instanceof CropinationComponent cropinationComponent) {
            return cropinationComponent.config();
        }
        return MAConfig_Base.getGlobalConfig().cropination();
    }

    /**
     * Resolve per-player effective cropination config with sync fallback.
     */
    private CropinationConfig cropinationConfig(ServerPlayer player) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (player == null) {
            return cropinationConfig();
        }

        var synced = core.syncCoreService().getPlayerState(playerId(player));
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (synced != null && synced.effectiveConfig() != null && synced.effectiveConfig().cropination() != null) {
            return synced.effectiveConfig().cropination();
        }
        return cropinationConfig();
    }

    /**
     * Resolve cultivation config from component or global fallback.
     */
    private CultivationConfig cultivationConfig() {
        var component = core.components().get(FeatureId.CULTIVATION);
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (component instanceof CultivationComponent cultivationComponent) {
            return cultivationComponent.config();
        }
        return MAConfig_Base.getGlobalConfig().cultivation();
    }

    /**
     * Resolve per-player effective cultivation config with sync fallback.
     */
    private CultivationConfig cultivationConfig(ServerPlayer player) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (player == null) {
            return cultivationConfig();
        }

        var synced = core.syncCoreService().getPlayerState(playerId(player));
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (synced != null && synced.effectiveConfig() != null && synced.effectiveConfig().cultivation() != null) {
            return synced.effectiveConfig().cultivation();
        }
        return cultivationConfig();
    }

    /**
     * Resolve excavation config from component or global fallback.
     */
    private ExcavationConfig excavationConfig() {
        var component = core.components().get(FeatureId.EXCAVATION);
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (component instanceof ExcavationComponent excavationComponent) {
            return excavationComponent.config();
        }
        return MAConfig_Base.getGlobalConfig().excavation();
    }

    /**
     * Resolve per-player effective excavation config with sync fallback.
     */
    private ExcavationConfig excavationConfig(ServerPlayer player) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (player == null) {
            return excavationConfig();
        }

        var synced = core.syncCoreService().getPlayerState(playerId(player));
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (synced != null && synced.effectiveConfig() != null && synced.effectiveConfig().excavation() != null) {
            return synced.effectiveConfig().excavation();
        }
        return excavationConfig();
    }

    /**
     * Resolve global common config.
     */
    private CommonConfig commonConfig() {
        return MAConfig_Base.getGlobalConfig().common();
    }

    /**
     * Resolve per-player effective common config with sync fallback.
     */
    private CommonConfig commonConfig(ServerPlayer player) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (player == null) {
            return commonConfig();
        }

        var synced = core.syncCoreService().getPlayerState(playerId(player));
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (synced != null && synced.effectiveConfig() != null && synced.effectiveConfig().common() != null) {
            return synced.effectiveConfig().common();
        }
        return commonConfig();
    }

    /**
     * Resolve lumbination config from component or global fallback.
     */
    private LumbinationConfig lumbinationConfig() {
        var component = core.components().get(FeatureId.LUMBINATION);
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (component instanceof LumbinationComponent lumbinationComponent) {
            return lumbinationComponent.config();
        }
        return MAConfig_Base.getGlobalConfig().lumbination();
    }

    /**
     * Resolve per-player effective lumbination config with sync fallback.
     */
    private LumbinationConfig lumbinationConfig(ServerPlayer player) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (player == null) {
            return lumbinationConfig();
        }

        var synced = core.syncCoreService().getPlayerState(playerId(player));
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (synced != null && synced.effectiveConfig() != null && synced.effectiveConfig().lumbination() != null) {
            return synced.effectiveConfig().lumbination();
        }
        return lumbinationConfig();
    }

    /**
     * Validate held axe against optional lumbination axe allowlist.
     */
    private static boolean isConfiguredAxe(ItemStack stack, LumbinationConfig config) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (!isAxeTool(stack)) {
            return false;
        }
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (config == null || config.axes().isEmpty()) {
            return true;
        }
        String heldItemId = itemId(stack);
        return config.axes().contains(heldItemId);
    }

    /**
     * Validate target block against optional lumbination log allowlist.
     */
    private static boolean isConfiguredLog(BlockState state, LumbinationConfig config) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (state == null || config == null) {
            return false;
        }
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (config.logs().isEmpty()) {
            return state.is(BlockTags.LOGS);
        }
        String targetBlockId = blockId(state);
        return config.logs().contains(targetBlockId);
    }

    /**
     * Resolve pathanation config from component or global fallback.
     */
    private PathanationConfig pathanationConfig() {
        var component = core.components().get(FeatureId.PATHANATION);
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (component instanceof PathanationComponent pathanationComponent) {
            return pathanationComponent.config();
        }
        return MAConfig_Base.getGlobalConfig().pathanation();
    }

    /**
     * Resolve per-player effective pathanation config with sync fallback.
     */
    private PathanationConfig pathanationConfig(ServerPlayer player) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (player == null) {
            return pathanationConfig();
        }

        var synced = core.syncCoreService().getPlayerState(playerId(player));
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (synced != null && synced.effectiveConfig() != null && synced.effectiveConfig().pathanation() != null) {
            return synced.effectiveConfig().pathanation();
        }
        return pathanationConfig();
    }

    /**
     * Resolve illumination config from component or global fallback.
     */
    private IlluminationConfig illuminationConfig() {
        var component = core.components().get(FeatureId.ILLUMINATION);
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (component instanceof IlluminationComponent illuminationComponent) {
            return illuminationComponent.config();
        }
        return MAConfig_Base.getGlobalConfig().illumination();
    }

    /**
     * Resolve per-player effective illumination config with sync fallback.
     */
    private IlluminationConfig illuminationConfig(ServerPlayer player) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (player == null) {
            return illuminationConfig();
        }

        var synced = core.syncCoreService().getPlayerState(playerId(player));
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (synced != null && synced.effectiveConfig() != null && synced.effectiveConfig().illumination() != null) {
            return synced.effectiveConfig().illumination();
        }
        return illuminationConfig();
    }

    /**
     * Resolve shaftanation config from component or global fallback.
     */
    private ShaftanationConfig shaftanationConfig() {
        var component = core.components().get(FeatureId.SHAFTANATION);
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (component instanceof ShaftanationComponent shaftanationComponent) {
            return shaftanationComponent.config();
        }
        return MAConfig_Base.getGlobalConfig().shaftanation();
    }

    /**
     * Resolve per-player effective shaftanation config with sync fallback.
     */
    private ShaftanationConfig shaftanationConfig(ServerPlayer player) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (player == null) {
            return shaftanationConfig();
        }

        var synced = core.syncCoreService().getPlayerState(playerId(player));
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (synced != null && synced.effectiveConfig() != null && synced.effectiveConfig().shaftanation() != null) {
            return synced.effectiveConfig().shaftanation();
        }
        return shaftanationConfig();
    }

    /**
     * Resolve ventilation config from component or global fallback.
     */
    private VentilationConfig ventilationConfig() {
        var component = core.components().get(FeatureId.VENTILATION);
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (component instanceof VentilationComponent ventilationComponent) {
            return ventilationComponent.config();
        }
        return MAConfig_Base.getGlobalConfig().ventilation();
    }

    /**
     * Resolve per-player effective ventilation config with sync fallback.
     */
    private VentilationConfig ventilationConfig(ServerPlayer player) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (player == null) {
            return ventilationConfig();
        }

        var synced = core.syncCoreService().getPlayerState(playerId(player));
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (synced != null && synced.effectiveConfig() != null && synced.effectiveConfig().ventilation() != null) {
            return synced.effectiveConfig().ventilation();
        }
        return ventilationConfig();
    }

    /**
     * Resolve global veination config.
     */
    private VeinationConfig veinationConfig() {
        return MAConfig_Base.getGlobalConfig().veination();
    }

    /**
     * Resolve per-player effective veination config with sync fallback.
     */
    private VeinationConfig veinationConfig(ServerPlayer player) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (player == null) {
            return veinationConfig();
        }

        var synced = core.syncCoreService().getPlayerState(playerId(player));
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (synced != null && synced.effectiveConfig() != null && synced.effectiveConfig().veination() != null) {
            return synced.effectiveConfig().veination();
        }
        return veinationConfig();
    }

    /**
     * Identify pickaxe tool via item id suffix.
     */
    private static boolean isPickaxeTool(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().endsWith("_pickaxe");
    }

    /**
     * Identify shovel tool by runtime type.
     */
    private static boolean isShovelTool(ItemStack stack) {
        return stack.getItem() instanceof ShovelItem;
    }

    /**
     * Identify axe tool by runtime type.
     */
    private static boolean isAxeTool(ItemStack stack) {
        return stack.getItem() instanceof AxeItem;
    }

    /**
     * Identify hoe tool by runtime type.
     */
    private static boolean isHoeTool(ItemStack stack) {
        return stack.getItem() instanceof HoeItem;
    }

    /**
     * Check whether a feature component exists and is enabled.
     */
    private boolean isFeatureEnabled(FeatureId featureId) {
        var component = core.components().get(featureId);
        return component != null && component.isEnabled();
    }

    /**
     * Resolve canonical block id string.
     */
    private static String blockId(BlockState state) {
        return BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
    }

    /**
     * Build compact player id used by runtime services.
     */
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
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
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
    private final Map<Long, BreakFaceState> lastBreakFaces = new ConcurrentHashMap<>();

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
        NeoForge.EVENT_BUS.addListener(this::onRightClickItem);
        NeoForge.EVENT_BUS.addListener(this::onLeftClickBlock);
        NeoForge.EVENT_BUS.addListener(this::onBreakSpeed);
        NeoForge.EVENT_BUS.addListener(this::onServerTick);
        NeoForge.EVENT_BUS.addListener(this::onPlayerLogin);
        NeoForge.EVENT_BUS.addListener(this::onPlayerLogout);
        NeoForge.EVENT_BUS.addListener(this::onLevelUnload);
        NeoForge.EVENT_BUS.addListener(this::onEntityJoinLevel);
        NeoForge.EVENT_BUS.addListener(this::onAttackEntity);
        NeoForge.EVENT_BUS.addListener(this::onToolModification);
    }

    private void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        long playerId = playerId(player);
        LogUtils.logInfo("Player login player={} id={}", player.getScoreboardName(), playerId);
        MAClientRootConfig clientConfig = MAConfig_Base.getClientRootConfig();
        MAServerRootConfig serverConfig = MAConfig_Base.getServerRootConfig();
        core.handlePlayerStateSyncPacket(new PlayerStateSyncPacket(playerId, clientConfig, serverConfig));
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
                boolean gatherDrops = commonConfig(serverPlayer).gatherDrops();
                veinationRuntime.handleItemEntityJoin(level, entity, serverPlayer, veinationConfig(serverPlayer), gatherDrops);
                toolEvents.onItemPickup(itemId, false);
                core.workerRuntimeService().interceptLiveDropForPlayer(
                    playerId(serverPlayer),
                    "item:" + itemId,
                    itemEntity.getItem().getCount(),
                    gatherDrops
                );
            }
        } else if (entity instanceof ExperienceOrb orb) {
            if (level.getNearestPlayer(entity, 8.0) instanceof ServerPlayer serverPlayer) {
                boolean gatherDrops = commonConfig(serverPlayer).gatherDrops();
                core.workerRuntimeService().interceptLiveDropForPlayer(
                    playerId(serverPlayer),
                    "xp_orb",
                    orb.getValue(),
                    gatherDrops
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

        BlockPos pos = event.getPos();
        BlockState state = event.getLevel().getBlockState(pos);
        ItemStack stack = event.getItemStack();
        String itemId = itemId(stack);

        if (isHoeTool(stack)) {
            if (RegistryPredicates.isCropBlock(state)) {
                CommonConfig playerCommonConfig = commonConfig(serverPlayer);
                AgentManager.get().addAgent(serverPlayer, new CropinationAgent(serverPlayer, pos, playerCommonConfig.blockRadius(), cropinationConfig(serverPlayer), playerCommonConfig));
                return;
            }
            if (RegistryPredicates.isDirtLike(state)) {
                CommonConfig playerCommonConfig = commonConfig(serverPlayer);
                AgentManager.get().addAgent(serverPlayer, new CultivationAgent(serverPlayer, pos, playerCommonConfig.blockRadius(), cultivationConfig(serverPlayer), playerCommonConfig));
                return;
            }
        }

        if (isShovelTool(stack) && state.is(BlockTags.DIRT)) {
            AgentManager.get().addAgent(serverPlayer, new PathanationAgent(serverPlayer, pos, serverPlayer.getDirection(), pathanationConfig(serverPlayer), commonConfig(serverPlayer)));
            return;
        }

        var playerState = core.playerStateService().getPlayerState(playerId(serverPlayer));
        if (playerState.shaftVentToggled() && !event.getEntity().isShiftKeyDown()) {
            Direction face = event.getFace();
            boolean verticalFace = face == Direction.UP || face == Direction.DOWN;
            VeinationConfig veinationConfig = veinationConfig(serverPlayer);
            if (verticalFace) {
                AgentManager.get().addAgent(serverPlayer, new VentilationAgent(serverPlayer, pos, face.getOpposite(), ventilationConfig(serverPlayer), commonConfig(serverPlayer), veinationRuntime, veinationConfig, stack));
            } else {
                AgentManager agentManager = AgentManager.get();
                if (!agentManager.hasAgentType(serverPlayer, ShaftanationAgent.class)) {
                    agentManager.addAgent(serverPlayer, new ShaftanationAgent(serverPlayer, pos, serverPlayer.getDirection(), shaftanationConfig(serverPlayer), commonConfig(serverPlayer), illuminationConfig(serverPlayer).lowestLightLevel(), veinationRuntime, veinationConfig, stack, playerState.selectedShaftanationShapeIndex(), face));
                }
            }
            return;
        }

        if (!isFeatureEnabled(FeatureId.SUBSTITUTION)) {
            return;
        }

        if (event.getEntity().isShiftKeyDown()) {
            if (!SubstitutionAgent.shouldQueueStartSubstitution(serverPlayer, event.getHand(), SubstitutionAction.INTERACT, pos)) {
                SubstitutionAgent.markSubstitutionActivity(serverPlayer, event.getHand(), SubstitutionAction.INTERACT, pos);
                return;
            }
            SubstitutionAgent agent = new SubstitutionAgent(serverPlayer, state, SubstitutionAction.INTERACT, event.getHand(), substitutionConfig(serverPlayer));
            if (!agent.tick()) {
                AgentManager.get().addAgent(serverPlayer, agent);
            }
        }
    }

    private void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntity() == null || event.getLevel().isClientSide()) {
            return;
        }

        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        if (!isFeatureEnabled(FeatureId.CAPTIVATION)) {
            return;
        }

        if (event.getEntity().isShiftKeyDown()) {
            AgentManager.get().addAgent(serverPlayer, new CaptivationAgent(serverPlayer, captivationConfig(serverPlayer)));
        }
    }

    private void onAttackEntity(AttackEntityEvent event) {
        if (event.getEntity() == null || event.getEntity().level().isClientSide()) {
            return;
        }

        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        if (!isFeatureEnabled(FeatureId.SUBSTITUTION)) {
            return;
        }

        Entity target = event.getTarget();
        if (target == null) {
            return;
        }

        SubstitutionConfig config = substitutionConfig(serverPlayer);
        if (config.ignorePassiveMobs() && target.getType().getCategory().isFriendly()) {
            return;
        }

        BlockPos targetPos = target.blockPosition();
        if (!SubstitutionAgent.shouldQueueStartSubstitution(serverPlayer, InteractionHand.MAIN_HAND, SubstitutionAction.ATTACK, targetPos)) {
            SubstitutionAgent.markSubstitutionActivity(serverPlayer, InteractionHand.MAIN_HAND, SubstitutionAction.ATTACK, targetPos);
            return;
        }

        BlockState contextState = target.level().getBlockState(targetPos);
        if (contextState.isAir()) {
            contextState = target.level().getBlockState(targetPos.below());
        }
        if (contextState.isAir()) {
            contextState = target.level().getBlockState(serverPlayer.blockPosition());
        }

        String targetEntityTypeId = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType()).toString();
        SubstitutionAgent agent = new SubstitutionAgent(serverPlayer, contextState, SubstitutionAction.ATTACK, InteractionHand.MAIN_HAND, config, targetEntityTypeId);
        if (!agent.tick()) {
            AgentManager.get().addAgent(serverPlayer, agent);
        }
    }

    private void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getEntity() == null || event.getLevel().isClientSide()) {
            return;
        }

        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            ItemStack stack = SubstitutionAgent.effectiveBreakHandStack(serverPlayer, InteractionHand.MAIN_HAND);
            BlockState state = event.getLevel().getBlockState(event.getPos());
            rememberBreakFace(serverPlayer, event.getPos(), event.getFace());
            long id = playerId(serverPlayer);
            var playerState = core.playerStateService().getPlayerState(id);
            boolean shaftModeActive = isFeatureEnabled(FeatureId.SHAFTANATION) && playerState.shaftVentToggled();
            boolean excavationActive = isFeatureEnabled(FeatureId.EXCAVATION) && playerState.isExcavationActive();
            CommonConfig playerCommonConfig = commonConfig(serverPlayer);
            VeinationConfig config = veinationConfig(serverPlayer);
            boolean veinationGesture = config.oreHarvestWithoutSneak() ? !event.getEntity().isShiftKeyDown() : event.getEntity().isShiftKeyDown();
            boolean allowedPickaxe = veinationRuntime.isPickaxeAllowed((Level) event.getLevel(), config, stack);
            boolean allowedOre = veinationRuntime.isOreAllowed(config, state);

            if (isFeatureEnabled(FeatureId.VEINATION) && playerCommonConfig.mineVeins() && veinationGesture && isPickaxeTool(stack) && allowedPickaxe && allowedOre) {
                veinationRuntime.registerDropAnchor(serverPlayer, event.getPos(), config);
                AgentManager agentManager = AgentManager.get();
                if (!agentManager.hasAgentType(serverPlayer, VeinationAgent.class)) {
                    agentManager.addAgent(serverPlayer, new VeinationAgent(serverPlayer, event.getPos(), playerCommonConfig, veinationRuntime, config));
                }
            } else if (shaftModeActive) {
                Direction breakFace = event.getFace();
                boolean verticalFace = breakFace == Direction.UP || breakFace == Direction.DOWN;
                AgentManager agentManager = AgentManager.get();
                if (verticalFace) {
                    agentManager.addAgent(serverPlayer, new VentilationAgent(serverPlayer, event.getPos(), breakFace.getOpposite(), ventilationConfig(serverPlayer), commonConfig(serverPlayer), veinationRuntime, config, stack));
                } else if (!agentManager.hasAgentType(serverPlayer, ShaftanationAgent.class)) {
                    agentManager.addAgent(serverPlayer, new ShaftanationAgent(serverPlayer, event.getPos(), serverPlayer.getDirection(), shaftanationConfig(serverPlayer), commonConfig(serverPlayer), illuminationConfig(serverPlayer).lowestLightLevel(), veinationRuntime, config, stack, playerState.selectedShaftanationShapeIndex(), breakFace));
                }
            } else if (!shaftModeActive && excavationActive) {
                ExcavationConfig excavationConfig = excavationConfig(serverPlayer);
                IlluminationConfig excavationIlluminationConfig = isFeatureEnabled(FeatureId.ILLUMINATION) ? illuminationConfig(serverPlayer) : null;
                AgentManager.get().addAgent(
                    serverPlayer,
                    new ExcavationAgent(
                        serverPlayer,
                        event.getPos(),
                        state,
                        excavationConfig,
                        playerCommonConfig,
                        excavationConfig.width(),
                        excavationConfig.height(),
                        excavationConfig.depth(),
                        veinationRuntime,
                        config,
                        stack,
                        excavationIlluminationConfig,
                        playerState.selectedExcavationShapeIndex(),
                        event.getFace()
                    )
                );
            } else if (isFeatureEnabled(FeatureId.LUMBINATION)) {
                LumbinationConfig lumbinationConfig = lumbinationConfig(serverPlayer);
                if (isConfiguredAxe(stack, lumbinationConfig) && isConfiguredLog(state, lumbinationConfig)) {
                    AgentManager.get().addAgent(serverPlayer, new LumbinationAgent(serverPlayer, event.getPos(), state, lumbinationConfig, commonConfig(serverPlayer)));
                }
            }
        }

        if (event.getEntity() instanceof ServerPlayer serverPlayer && isFeatureEnabled(FeatureId.SUBSTITUTION)) {
            ItemStack stack = event.getItemStack();
            BlockState state = event.getLevel().getBlockState(event.getPos());
            if (!SubstitutionAgent.shouldQueueStartSubstitution(serverPlayer, event.getHand(), SubstitutionAction.BREAK, event.getPos())) {
                SubstitutionAgent.markSubstitutionActivity(serverPlayer, event.getHand(), SubstitutionAction.BREAK, event.getPos());
            } else {
                SubstitutionAgent agent = new SubstitutionAgent(serverPlayer, state, SubstitutionAction.BREAK, event.getHand(), substitutionConfig(serverPlayer));
                if (!agent.tick()) {
                    AgentManager.get().addAgent(serverPlayer, agent);
                }
            }
        }

        String blockId = BuiltInRegistries.BLOCK.getKey(event.getLevel().getBlockState(event.getPos()).getBlock()).toString();
        boolean gatherDrops = event.getEntity() instanceof ServerPlayer serverPlayer ? commonConfig(serverPlayer).gatherDrops() : true;
        core.workerRuntimeService().interceptLiveDropForPlayer(
            event.getEntity().getUUID().getLeastSignificantBits(),
            "item:" + blockId,
            1,
            gatherDrops
        );
    }

    private void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (event.getEntity() == null || event.getEntity().level().isClientSide()) {
            return;
        }

        VeinationConfig activeConfig = event.getEntity() instanceof ServerPlayer serverPlayer ? veinationConfig(serverPlayer) : veinationConfig();

        float adjusted = veinationRuntime.adjustedDigSpeed(
            event.getEntity().level(),
            event.getEntity(),
            event.getOriginalSpeed(),
            event.getState(),
            activeConfig
        );
        if (adjusted != event.getOriginalSpeed()) {
            event.setNewSpeed(adjusted);
        }
    }

    private void onServerTick(ServerTickEvent.Post event) {
        if (event.getServer() != null) {
            int tickCount = event.getServer().getTickCount();
            for (ServerLevel level : event.getServer().getAllLevels()) {
                AgentManager.get().tick(level);
                if (tickCount % 20 == 0) {
                    for (ServerPlayer serverPlayer : level.players()) {
                        SubstitutionAgent.processSwitchBack(serverPlayer);
                        if (isFeatureEnabled(FeatureId.CAPTIVATION) && !AgentManager.get().hasAgentType(serverPlayer, CaptivationAgent.class)) {
                            AgentManager.get().addAgent(serverPlayer, new CaptivationAgent(serverPlayer, captivationConfig(serverPlayer)));
                        }
                    }
                } else {
                    for (ServerPlayer serverPlayer : level.players()) {
                        SubstitutionAgent.processSwitchBack(serverPlayer);
                    }
                }
            }
        }
        toolEvents.onServerTick();
    }

    private void rememberBreakFace(ServerPlayer serverPlayer, BlockPos pos, Direction face) {
        if (serverPlayer == null || pos == null || face == null) {
            return;
        }
        lastBreakFaces.put(playerId(serverPlayer), new BreakFaceState(pos.immutable(), face));
    }

    private Direction consumeBreakFace(ServerPlayer serverPlayer, BlockPos pos) {
        if (serverPlayer == null || pos == null) {
            return null;
        }
        BreakFaceState state = lastBreakFaces.remove(playerId(serverPlayer));
        if (state == null || !state.pos().equals(pos)) {
            return null;
        }
        return state.face();
    }

    private record BreakFaceState(BlockPos pos, Direction face) {
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

    private static boolean isExcavationTool(ItemStack stack) {
        return true;
    }

    private static boolean isShaftTool(ItemStack stack) {
        return true;
    }

    private static boolean isConfiguredAxe(ItemStack stack, LumbinationConfig config) {
        if (!isAxeTool(stack)) {
            return false;
        }
        if (config == null || config.axes().isEmpty()) {
            return true;
        }
        return config.axes().contains(itemId(stack));
    }

    private static boolean isConfiguredLog(BlockState state, LumbinationConfig config) {
        if (state == null || config == null) {
            return false;
        }
        if (config.logs().isEmpty()) {
            return state.is(BlockTags.LOGS);
        }
        return config.logs().contains(blockId(state));
    }

    private ExcavationConfig excavationConfig() {
        var component = core.components().get(FeatureId.EXCAVATION);
        if (component instanceof ExcavationComponent excavationComponent) {
            return excavationComponent.config();
        }
        return MAConfig_Base.getGlobalConfig().excavation();
    }

    private ExcavationConfig excavationConfig(ServerPlayer player) {
        if (player == null) {
            return excavationConfig();
        }

        var synced = core.syncCoreService().getPlayerState(playerId(player));
        if (synced != null && synced.effectiveConfig() != null && synced.effectiveConfig().excavation() != null) {
            return synced.effectiveConfig().excavation();
        }
        return excavationConfig();
    }

    private LumbinationConfig lumbinationConfig() {
        var component = core.components().get(FeatureId.LUMBINATION);
        if (component instanceof LumbinationComponent lumbinationComponent) {
            return lumbinationComponent.config();
        }
        return MAConfig_Base.getGlobalConfig().lumbination();
    }

    private LumbinationConfig lumbinationConfig(ServerPlayer player) {
        if (player == null) {
            return lumbinationConfig();
        }

        var synced = core.syncCoreService().getPlayerState(playerId(player));
        if (synced != null && synced.effectiveConfig() != null && synced.effectiveConfig().lumbination() != null) {
            return synced.effectiveConfig().lumbination();
        }
        return lumbinationConfig();
    }

    private CropinationConfig cropinationConfig() {
        var component = core.components().get(FeatureId.CROPINATION);
        if (component instanceof CropinationComponent cropinationComponent) {
            return cropinationComponent.config();
        }
        return MAConfig_Base.getGlobalConfig().cropination();
    }

    private CropinationConfig cropinationConfig(ServerPlayer player) {
        if (player == null) {
            return cropinationConfig();
        }

        var synced = core.syncCoreService().getPlayerState(playerId(player));
        if (synced != null && synced.effectiveConfig() != null && synced.effectiveConfig().cropination() != null) {
            return synced.effectiveConfig().cropination();
        }
        return cropinationConfig();
    }

    private CultivationConfig cultivationConfig() {
        var component = core.components().get(FeatureId.CULTIVATION);
        if (component instanceof CultivationComponent cultivationComponent) {
            return cultivationComponent.config();
        }
        return MAConfig_Base.getGlobalConfig().cultivation();
    }

    private CultivationConfig cultivationConfig(ServerPlayer player) {
        if (player == null) {
            return cultivationConfig();
        }

        var synced = core.syncCoreService().getPlayerState(playerId(player));
        if (synced != null && synced.effectiveConfig() != null && synced.effectiveConfig().cultivation() != null) {
            return synced.effectiveConfig().cultivation();
        }
        return cultivationConfig();
    }

    private PathanationConfig pathanationConfig() {
        var component = core.components().get(FeatureId.PATHANATION);
        if (component instanceof PathanationComponent pathanationComponent) {
            return pathanationComponent.config();
        }
        return MAConfig_Base.getGlobalConfig().pathanation();
    }

    private PathanationConfig pathanationConfig(ServerPlayer player) {
        if (player == null) {
            return pathanationConfig();
        }

        var synced = core.syncCoreService().getPlayerState(playerId(player));
        if (synced != null && synced.effectiveConfig() != null && synced.effectiveConfig().pathanation() != null) {
            return synced.effectiveConfig().pathanation();
        }
        return pathanationConfig();
    }

    private IlluminationConfig illuminationConfig() {
        var component = core.components().get(FeatureId.ILLUMINATION);
        if (component instanceof IlluminationComponent illuminationComponent) {
            return illuminationComponent.config();
        }
        return MAConfig_Base.getGlobalConfig().illumination();
    }

    private IlluminationConfig illuminationConfig(ServerPlayer player) {
        if (player == null) {
            return illuminationConfig();
        }

        var synced = core.syncCoreService().getPlayerState(playerId(player));
        if (synced != null && synced.effectiveConfig() != null && synced.effectiveConfig().illumination() != null) {
            return synced.effectiveConfig().illumination();
        }
        return illuminationConfig();
    }

    private ShaftanationConfig shaftanationConfig() {
        var component = core.components().get(FeatureId.SHAFTANATION);
        if (component instanceof ShaftanationComponent shaftanationComponent) {
            return shaftanationComponent.config();
        }
        return MAConfig_Base.getGlobalConfig().shaftanation();
    }

    private ShaftanationConfig shaftanationConfig(ServerPlayer player) {
        if (player == null) {
            return shaftanationConfig();
        }

        var synced = core.syncCoreService().getPlayerState(playerId(player));
        if (synced != null && synced.effectiveConfig() != null && synced.effectiveConfig().shaftanation() != null) {
            return synced.effectiveConfig().shaftanation();
        }
        return shaftanationConfig();
    }

    private VentilationConfig ventilationConfig() {
        var component = core.components().get(FeatureId.VENTILATION);
        if (component instanceof VentilationComponent ventilationComponent) {
            return ventilationComponent.config();
        }
        return MAConfig_Base.getGlobalConfig().ventilation();
    }

    private VentilationConfig ventilationConfig(ServerPlayer player) {
        if (player == null) {
            return ventilationConfig();
        }

        var synced = core.syncCoreService().getPlayerState(playerId(player));
        if (synced != null && synced.effectiveConfig() != null && synced.effectiveConfig().ventilation() != null) {
            return synced.effectiveConfig().ventilation();
        }
        return ventilationConfig();
    }

    private SubstitutionConfig substitutionConfig() {
        var component = core.components().get(FeatureId.SUBSTITUTION);
        if (component instanceof SubstitutionComponent substitutionComponent) {
            return substitutionComponent.config();
        }
        return SyncedClientConfig.defaults().substitution();
    }

    private CaptivationConfig captivationConfig() {
        var component = core.components().get(FeatureId.CAPTIVATION);
        if (component instanceof CaptivationComponent captivationComponent) {
            return captivationComponent.config();
        }
        return MAConfig_Base.getGlobalConfig().captivation();
    }

    private CaptivationConfig captivationConfig(ServerPlayer player) {
        if (player == null) {
            return captivationConfig();
        }

        var synced = core.syncCoreService().getPlayerState(playerId(player));
        if (synced != null && synced.effectiveConfig() != null && synced.effectiveConfig().captivation() != null) {
            return synced.effectiveConfig().captivation();
        }
        return captivationConfig();
    }

    private SubstitutionConfig substitutionConfig(ServerPlayer player) {
        if (player == null) {
            return substitutionConfig();
        }

        var synced = core.syncCoreService().getPlayerState(playerId(player));
        if (synced != null && synced.effectiveConfig() != null && synced.effectiveConfig().substitution() != null) {
            return synced.effectiveConfig().substitution();
        }
        return substitutionConfig();
    }

    private CommonConfig commonConfig() {
        return MAConfig_Base.getGlobalConfig().common();
    }

    private CommonConfig commonConfig(ServerPlayer player) {
        if (player == null) {
            return commonConfig();
        }

        var synced = core.syncCoreService().getPlayerState(playerId(player));
        if (synced != null && synced.effectiveConfig() != null && synced.effectiveConfig().common() != null) {
            return synced.effectiveConfig().common();
        }
        return commonConfig();
    }

    private VeinationConfig veinationConfig() {
        var component = core.components().get(FeatureId.VEINATION);
        if (component instanceof uk.co.duelmonster.minersadvantage.common.feature.utility.VeinationComponent veinationComponent) {
            return veinationComponent.config();
        }
        return SyncedClientConfig.defaults().veination();
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
