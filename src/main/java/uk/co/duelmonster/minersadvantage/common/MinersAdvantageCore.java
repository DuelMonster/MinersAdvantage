package uk.co.duelmonster.minersadvantage.common;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import uk.co.duelmonster.minersadvantage.agent.AgentManager;
import uk.co.duelmonster.minersadvantage.agent.IlluminationAgent;
import uk.co.duelmonster.minersadvantage.agent.IlluminationPlaceAgent;
import uk.co.duelmonster.minersadvantage.common.component.ComponentDescriptor;
import uk.co.duelmonster.minersadvantage.common.component.ComponentLifecycle;
import uk.co.duelmonster.minersadvantage.common.component.ComponentRegistry;
import uk.co.duelmonster.minersadvantage.common.config.CaptivationConfig;
import uk.co.duelmonster.minersadvantage.common.config.CommonConfig;
import uk.co.duelmonster.minersadvantage.common.config.CropinationConfig;
import uk.co.duelmonster.minersadvantage.common.config.CultivationConfig;
import uk.co.duelmonster.minersadvantage.common.config.ExcavationConfig;
import uk.co.duelmonster.minersadvantage.common.config.IlluminationConfig;
import uk.co.duelmonster.minersadvantage.common.config.LumbinationConfig;
import uk.co.duelmonster.minersadvantage.common.config.MAConfig_Base;
import uk.co.duelmonster.minersadvantage.common.config.MAServerRootConfig;
import uk.co.duelmonster.minersadvantage.common.config.PathanationConfig;
import uk.co.duelmonster.minersadvantage.common.config.ShaftanationConfig;
import uk.co.duelmonster.minersadvantage.common.config.SubstitutionConfig;
import uk.co.duelmonster.minersadvantage.common.config.SyncedClientConfig;
import uk.co.duelmonster.minersadvantage.common.config.VeinationConfig;
import uk.co.duelmonster.minersadvantage.common.config.VentilationConfig;
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
import uk.co.duelmonster.minersadvantage.common.feature.utility.VeinationComponent;
import uk.co.duelmonster.minersadvantage.common.log.LogUtils;
import uk.co.duelmonster.minersadvantage.common.network.AbortWorkersPacket;
import uk.co.duelmonster.minersadvantage.common.network.ComponentTogglePacket;
import uk.co.duelmonster.minersadvantage.common.network.FeatureDispatchPacket;
import uk.co.duelmonster.minersadvantage.common.network.IlluminationActionPacket;
import uk.co.duelmonster.minersadvantage.common.network.PlayerStateSyncPacket;
import uk.co.duelmonster.minersadvantage.common.network.SupremeVantagePacket;
import uk.co.duelmonster.minersadvantage.common.event.FeatureEventHandler;
import uk.co.duelmonster.minersadvantage.common.services.core.PlayerStateService;
import uk.co.duelmonster.minersadvantage.common.services.core.ServerTickOrchestrator;
import uk.co.duelmonster.minersadvantage.common.services.policy.PolicyCoreService;
import uk.co.duelmonster.minersadvantage.common.services.processing.WorkerRuntimeService;
import uk.co.duelmonster.minersadvantage.common.services.sync.SyncCoreService;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeBootstrap;
import uk.co.duelmonster.minersadvantage.common.services.utility.SupremeVantageService;

/**
 * MinersAdvantageCore keeps this part of MinersAdvantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class MinersAdvantageCore {
  private static final long SUPREME_VANTAGE_GRANT_COOLDOWN_MS = 200L;
  private final ComponentRegistry componentRegistry = new ComponentRegistry();
  private final Map<FeatureId, ComponentLifecycle> components = new EnumMap<>(FeatureId.class);
  private final PlayerStateService playerStateService;
  private final ServerTickOrchestrator tickOrchestrator;
  private final PolicyCoreService policyCoreService;
  private final SyncCoreService syncCoreService;
  private final SupremeVantageService supremeVantageService;
  private final SyncedClientConfig defaultConfig;
  private final Map<Long, Long> supremeVantageLastGrantAtMs = new HashMap<>();

  /**
   * MinersAdvantageCore exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public MinersAdvantageCore() {
    this.playerStateService = new PlayerStateService();
    this.tickOrchestrator = new ServerTickOrchestrator(playerStateService);
    this.policyCoreService = new PolicyCoreService();
    this.syncCoreService = new SyncCoreService();
    this.supremeVantageService = new SupremeVantageService();
    this.defaultConfig = MAConfig_Base.getGlobalConfig();
  }

  /**
   * bootstrap exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public void bootstrap() {
    LogUtils.logInfo("Bootstrapping core components");
    MAShapeBootstrap.ensureInitialized();
    tickOrchestrator.setTpsGuardActive(defaultConfig.common().tpsGuard());
    tickOrchestrator.setProcessingDelay(defaultConfig.common().enableTickDelay(), defaultConfig.common().tickDelay());
    registerFeaturesFromConfig(defaultConfig);
    componentRegistry.enableAll();
    LogUtils.logInfo("Bootstrapped {} feature components", components.size());
  }

  private void registerFeaturesFromConfig(SyncedClientConfig config) {
    registerFeature(FeatureId.CAPTIVATION, "captivation", config::captivation, CaptivationComponent::new);
    registerFeature(FeatureId.CROPINATION, "cropination", config::cropination, CropinationComponent::new);
    registerFeature(FeatureId.CULTIVATION, "cultivation", config::cultivation, CultivationComponent::new);
    registerFeature(FeatureId.EXCAVATION, "excavation", config::excavation, ExcavationComponent::new);
    registerFeature(FeatureId.ILLUMINATION, "illumination", config::illumination, IlluminationComponent::new);
    registerFeature(FeatureId.LUMBINATION, "lumbination", config::lumbination, LumbinationComponent::new);
    registerFeature(FeatureId.PATHANATION, "pathanation", config::pathanation, PathanationComponent::new);
    registerFeature(FeatureId.SHAFTANATION, "shaftanation", config::shaftanation, ShaftanationComponent::new);
    registerFeature(FeatureId.SUBSTITUTION, "substitution", config::substitution, SubstitutionComponent::new);
    registerFeature(FeatureId.VEINATION, "veination", config::veination, VeinationComponent::new);
    registerFeature(FeatureId.VENTILATION, "ventilation", config::ventilation, VentilationComponent::new);
  }

  private void reloadComponentsFromConfig(SyncedClientConfig config) {
    EnumMap<FeatureId, Boolean> previousEnabledStates = new EnumMap<>(FeatureId.class);
    for (Map.Entry<FeatureId, ComponentLifecycle> entry : components.entrySet()) {
      previousEnabledStates.put(entry.getKey(), isComponentManuallyEnabled(entry.getValue()));
    }

    componentRegistry.disableAll();
    componentRegistry.cleanupAll();
    components.clear();
    registerFeaturesFromConfig(config);

    for (Map.Entry<FeatureId, ComponentLifecycle> entry : components.entrySet()) {
      if (previousEnabledStates.getOrDefault(entry.getKey(), true)) {
        entry.getValue().enable();
      } else {
        entry.getValue().disable();
      }
    }
  }

  private boolean isComponentManuallyEnabled(ComponentLifecycle component) {
    try {
      Field enabledField = component.getClass().getDeclaredField("enabled");
      enabledField.setAccessible(true);
      Object value = enabledField.get(component);
      if (value instanceof Boolean bool) {
        return bool;
      }
    } catch (ReflectiveOperationException ignored) {
      // Fallback to public lifecycle status when component internals differ.
    }
    return component.isEnabled();
  }

  /**
   * r eg is te rf ea tu re exists so this path stays predictable and easier to debug when things get weird.
   */
  private <C> void registerFeature(FeatureId id, String key, Supplier<C> configGetter,
      Function<C, ? extends ComponentLifecycle> componentFactory) {
    ComponentLifecycle component = componentFactory.apply(configGetter.get());
    components.put(id, component);
    componentRegistry
        .register(new ComponentDescriptor(key, key.substring(0, 1).toUpperCase() + key.substring(1), component));
    LogUtils.logDebug("Registered component feature={} type={}", id, component.getClass().getSimpleName());
  }

  /**
   * shutdown exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public void shutdown() {
    componentRegistry.disableAll();
    componentRegistry.cleanupAll();
  }

  /**
   * components exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public Map<FeatureId, ComponentLifecycle> components() {
    return Map.copyOf(components);
  }

  /**
   * serverTick exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public void serverTick() {
    tickOrchestrator.onServerTick();
    componentRegistry.tickAll();
  }

  /**
   * playerStateService exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public PlayerStateService playerStateService() {
    return playerStateService;
  }

  /**
   * tickOrchestrator exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public ServerTickOrchestrator tickOrchestrator() {
    return tickOrchestrator;
  }

  /**
   * workerRuntimeService exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public WorkerRuntimeService workerRuntimeService() {
    return tickOrchestrator.workerRuntimeService();
  }

  /**
   * handleAbortPacket exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public WorkerRuntimeService.AbortResult handleAbortPacket(AbortWorkersPacket packet) {
    LogUtils.logDebug("Handling abort packet playerId={}", packet.playerId());
    AgentManager.get().clearAgents(packet.playerId());
    return workerRuntimeService().abortAllForPlayerWithStats(packet.playerId());
  }

  /**
   * handleComponentTogglePacket exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public boolean handleComponentTogglePacket(ComponentTogglePacket packet) {
    LogUtils.logDebug("Handling component toggle feature={} enabled={}", packet.feature(), packet.enabled());
    ComponentLifecycle component = components.get(packet.feature());
    if (component == null) {
      LogUtils.logWarn("Received toggle for unknown feature={}", packet.feature());
      return false;
    }

    if (packet.enabled()) {
      component.enable();
    } else {
      component.disable();
    }
    return component.isEnabled();
  }

  /**
   * handlePlayerStateSyncPacket exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public SyncCoreService.PlayerSyncState handlePlayerStateSyncPacket(PlayerStateSyncPacket packet) {
    MAServerRootConfig authoritativeServerConfig = MAConfig_Base.getServerRootConfig();
    PlayerStateService.PlayerState current = playerStateService.getPlayerState(packet.playerId());
    SyncCoreService.PlayerSyncState previousSyncState = syncCoreService.getPlayerState(packet.playerId());
    boolean unchangedState = current.excavationToggled() == packet.excavationToggled()
        && current.shaftVentToggled() == packet.shaftVentToggled()
        && current.selectedExcavationShapeIndex() == packet.selectedExcavationShapeIndex()
        && current.selectedShaftanationShapeIndex() == packet.selectedShaftanationShapeIndex();
    boolean unchangedClientConfig = previousSyncState.clientConfig().equals(packet.clientConfig());
    boolean unchangedServerConfig = previousSyncState.serverConfig().equals(authoritativeServerConfig);
    if (unchangedState && unchangedClientConfig && unchangedServerConfig) {
      return previousSyncState;
    }

    LogUtils.logDebug("Synchronizing player state playerId={} commonConfig={}", packet.playerId(),
        authoritativeServerConfig.common());
    playerStateService.updatePlayerState(
        packet.playerId(),
        new PlayerStateService.PlayerState(
            packet.playerId(),
            current.hungerGuardActive(),
            current.lastHarvestTick(),
            current.recentHarvests(),
            packet.excavationToggled(),
            packet.shaftVentToggled(),
            packet.selectedExcavationShapeIndex(),
            packet.selectedShaftanationShapeIndex()));
    SyncCoreService.PlayerSyncState state = syncCoreService.synchronize(
        packet.playerId(),
        packet.clientConfig(),
        authoritativeServerConfig,
        policyCoreService);
    boolean configChanged = !previousSyncState.clientConfig().equals(state.clientConfig())
        || !previousSyncState.serverConfig().equals(state.serverConfig());
    MAConfig_Base.setClientRootConfig(state.clientConfig());
    MAConfig_Base.setServerRootConfig(state.serverConfig());
    if (configChanged) {
      reloadComponentsFromConfig(MAConfig_Base.getGlobalConfig());
    }
    tickOrchestrator.setTpsGuardActive(state.serverConfig().common().tpsGuard());
    tickOrchestrator.setProcessingDelay(state.serverConfig().common().enableTickDelay(),
        state.serverConfig().common().tickDelay());
    return state;
  }

  /**
   * handleFeatureDispatchPacket exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public void handleFeatureDispatchPacket(FeatureDispatchPacket packet) {
    LogUtils.logDebug(
        "Handling feature dispatch feature={} tool={} block={} pos=({}, {}, {})",
        packet.feature(),
        packet.toolId(),
        packet.blockId(),
        packet.blockX(),
        packet.blockY(),
        packet.blockZ());
    FeatureEventHandler.onToolUse(
        packet.feature(),
        packet.blockX(),
        packet.blockY(),
        packet.blockZ(),
        packet.blockId(),
        packet.toolId());
  }

  /**
   * handleIlluminationActionPacket routes explicit client illumination requests into the live server agent path.
   */
  public boolean handleIlluminationActionPacket(ServerPlayer player, IlluminationActionPacket packet) {
    if (player == null || packet == null) {
      return false;
    }

    ComponentLifecycle component = components.get(FeatureId.ILLUMINATION);
    if (component == null || !component.isEnabled()) {
      return false;
    }

    SyncedClientConfig effectiveConfig = syncCoreService.getPlayerState(player.getUUID().getLeastSignificantBits())
        .effectiveConfig();
    CommonConfig commonConfig = effectiveConfig == null ? defaultConfig.common() : effectiveConfig.common();
    IlluminationConfig illuminationConfig = effectiveConfig == null ? defaultConfig.illumination()
        : effectiveConfig.illumination();
    if (illuminationConfig == null || !illuminationConfig.enabled()) {
      return false;
    }

    BlockPos targetPos = new BlockPos(packet.blockX(), packet.blockY(), packet.blockZ());
    LogUtils.logDebug(
        "Handling illumination action player={} area={} target={}",
        player.getScoreboardName(),
        packet.area(),
        targetPos);

    if (!packet.area()) {
      AgentManager.get().addAgent(player,
          new IlluminationPlaceAgent(player, targetPos, packet.faceDirection(), illuminationConfig, commonConfig));
    } else {
      // Area mode intentionally centers on the player so targeting misses do not offset coverage.
      BlockPos origin = player.blockPosition().above();
      AgentManager.get().addAgent(player, new IlluminationAgent(player, origin, illuminationConfig, commonConfig));
    }
    return true;
  }

  /**
   * handleSupremeVantagePacket exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public SupremeVantageService.RewardGrant handleSupremeVantagePacket(SupremeVantagePacket packet) {
    return supremeVantageService.grantNextReward(packet.playerId(), packet.code());
  }

  /**
   * handleSupremeVantagePacket exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public SupremeVantageService.ItemGrantSpec handleSupremeVantagePacket(ServerPlayer player,
      SupremeVantagePacket packet) {
    if (player == null || packet == null) {
      return null;
    }

    long authoritativePlayerId = player.getUUID().getLeastSignificantBits();
    long nowMs = System.currentTimeMillis();
    Long previousGrantAtMs = supremeVantageLastGrantAtMs.get(authoritativePlayerId);
    if (previousGrantAtMs != null && (nowMs - previousGrantAtMs) < SUPREME_VANTAGE_GRANT_COOLDOWN_MS) {
      LogUtils.logDebug(
          "SupremeVantage grant throttled player={} deltaMs={} cooldownMs={}",
          player.getScoreboardName(),
          nowMs - previousGrantAtMs,
          SUPREME_VANTAGE_GRANT_COOLDOWN_MS);
      return null;
    }

    SupremeVantageService.RewardGrant grant = supremeVantageService.grantNextReward(authoritativePlayerId,
        packet.code());
    if (grant == null) {
      LogUtils.logDebug(
          "SupremeVantage grant skipped player={} code={} reason=sequence-complete-or-invalid",
          player.getScoreboardName(),
          packet.code());
      return null;
    }

    supremeVantageLastGrantAtMs.put(authoritativePlayerId, nowMs);
    SupremeVantageService.ItemGrantSpec spec = supremeVantageService.materializeRewardSpec(grant);
    LogUtils.logDebug(
        "SupremeVantage grant player={} sequence={} rewardId={} itemId={} enchantCount={}",
        player.getScoreboardName(),
        grant.sequence(),
        grant.rewardId(),
        grant.itemId(),
        spec == null ? 0 : spec.enchantments().size());
    grantSupremeVantageReward(player, spec);
    return spec;
  }

  /**
   * handleSupremeVantagePacketGrantSpec exists to keep this step focused, predictable, and debuggable.
   * In short: one clear job here beats ten confusing side-effects elsewhere.
   */
  public SupremeVantageService.ItemGrantSpec handleSupremeVantagePacketGrantSpec(SupremeVantagePacket packet) {
    SupremeVantageService.RewardGrant grant = handleSupremeVantagePacket(packet);
    return supremeVantageService.materializeRewardSpec(grant);
  }

  /**
   * grantSupremeVantageReward exists to keep this step focused, predictable, and debuggable.
   * In short: one clear job here beats ten confusing side-effects elsewhere.
   */
  private void grantSupremeVantageReward(ServerPlayer player, SupremeVantageService.ItemGrantSpec spec) {
    if (player == null || spec == null || spec.reward() == null) {
      return;
    }

    ItemStack rewardStack = materializeSupremeVantageItemStack(player, spec);
    if (rewardStack == null || rewardStack.isEmpty()) {
      return;
    }

    boolean addedToInventory = player.addItem(rewardStack);
    if (addedToInventory) {
      return;
    }

    BlockPos pos = player.blockPosition();
    player.level().addFreshEntity(new ItemEntity(player.level(), pos.getX(), pos.getY(), pos.getZ(), rewardStack));
  }

  /**
   * materializeSupremeVantageItemStack exists to keep this step focused, predictable, and debuggable.
   * In short: one clear job here beats ten confusing side-effects elsewhere.
   */
  private ItemStack materializeSupremeVantageItemStack(ServerPlayer player, SupremeVantageService.ItemGrantSpec spec) {
    Item item = resolveItem(spec.reward().itemId());
    if (item == null) {
      LogUtils.logWarn("SupremeVantage reward skipped: unknown item id={}", spec.reward().itemId());
      return ItemStack.EMPTY;
    }

    int count = Math.max(1, spec.count());
    ItemStack stack = new ItemStack(item, count);
    applyDisplayName(stack, spec.reward().displayName());
    if (spec.unbreakable()) {
      applyUnbreakable(stack);
    }

    applySupremeVantageEnchantments(player, stack, spec.enchantments());
    return stack;
  }

  /**
   * applySupremeVantageEnchantments exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  private void applySupremeVantageEnchantments(ServerPlayer player, ItemStack stack,
      List<SupremeVantageService.EnchantmentGrant> enchantments) {
    if (stack == null || stack.isEmpty() || enchantments == null || enchantments.isEmpty()) {
      return;
    }

    Object enchantmentRegistry = resolveSupremeVantageEnchantmentRegistry(player);
    if (enchantmentRegistry == null) {
      LogUtils.logWarn("SupremeVantage enchantments skipped: enchantment registry unavailable");
      return;
    }

    for (SupremeVantageService.EnchantmentGrant enchantmentGrant : enchantments) {
      if (enchantmentGrant.enchantmentId() == null || enchantmentGrant.enchantmentId().isBlank()) {
        LogUtils.logWarn("SupremeVantage enchantment skipped: invalid id={}", enchantmentGrant.enchantmentId());
        continue;
      }

      Object enchantmentValue = resolveRegistryValue(enchantmentRegistry, enchantmentGrant.enchantmentId());
      if (enchantmentValue == null) {
        LogUtils.logWarn("SupremeVantage enchantment skipped: unknown id={}", enchantmentGrant.enchantmentId());
        continue;
      }

      boolean applied = tryApplyEnchantment(stack, enchantmentValue, enchantmentRegistry, enchantmentGrant.level());
      if (!applied) {
        LogUtils.logWarn(
            "SupremeVantage enchantment application failed: id={} level={} item={}",
            enchantmentGrant.enchantmentId(),
            enchantmentGrant.level(),
            specItemId(stack));
      }
    }
  }

  /**
   * resolveItem exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  private Item resolveItem(String itemId) {
    Object itemRegistry = BuiltInRegistries.ITEM;
    if (itemId == null || itemId.isBlank()) {
      return null;
    }

    Object value = resolveRegistryValue(itemRegistry, itemId);
    return value instanceof Item item ? item : null;
  }

  /**
   * resolveRegistryByFieldName exists to keep this step focused, predictable, and debuggable.
   * In short: one clear job here beats ten confusing side-effects elsewhere.
   */
  private Object resolveRegistryByFieldName(String fieldName) {
    return resolveStaticField("net.minecraft.core.registries.BuiltInRegistries", fieldName);
  }

  /**
   * resolveSupremeVantageEnchantmentRegistry exists to keep this step focused, predictable, and debuggable.
   * In short: one clear job here beats ten confusing side-effects elsewhere.
   */
  private Object resolveSupremeVantageEnchantmentRegistry(ServerPlayer player) {
    Object builtInRegistry = resolveRegistryByFieldName("ENCHANTMENT");
    if (builtInRegistry != null) {
      return builtInRegistry;
    }

    Object enchantmentRegistryKey = resolveStaticField("net.minecraft.core.registries.Registries", "ENCHANTMENT");
    if (enchantmentRegistryKey == null || player == null) {
      return null;
    }

    Object registryAccess = invokeNoArgMethod(player, "registryAccess");
    if (registryAccess == null && player.level() != null) {
      registryAccess = invokeNoArgMethod(player.level(), "registryAccess");
    }
    if (registryAccess == null) {
      return null;
    }

    Object registry = invokeSingleArgMethod(registryAccess, "registryOrThrow", enchantmentRegistryKey);
    if (registry != null) {
      return registry;
    }

    return invokeSingleArgMethod(registryAccess, "lookupOrThrow", enchantmentRegistryKey);
  }

  /**
   * resolveRegistryValue exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  private Object resolveRegistryValue(Object registry, String id) {
    if (registry == null || id == null) {
      return null;
    }

    if (registry instanceof Iterable<?> iterable) {
      for (Object candidate : iterable) {
        String candidateId = registryKeyAsString(registry, candidate);
        if (id.equals(candidateId)) {
          return candidate;
        }
      }
    }

    Object resourceLocation = parseResourceLocation(id);
    for (Method method : registry.getClass().getMethods()) {
      if (method.getParameterCount() != 1) {
        continue;
      }

      String name = method.getName();
      if (!"get".equals(name) && !"getValue".equals(name) && !"getOptional".equals(name) && !"byName".equals(name)) {
        continue;
      }

      Class<?> parameterType = method.getParameterTypes()[0];
      Object argument;
      if (parameterType == String.class) {
        argument = id;
      } else if (resourceLocation != null && parameterType.isInstance(resourceLocation)) {
        argument = resourceLocation;
      } else {
        continue;
      }

      try {
        Object result = method.invoke(registry, argument);
        Object unwrapped = unwrapOptional(result);
        Object value = unwrapHolderValue(unwrapped);
        if (value != null) {
          return value;
        }
      } catch (ReflectiveOperationException ignored) {
        // mixed mapping signatures are expected across targets.
      }
    }
    return null;
  }

  /**
   * registryKeyAsString exists to keep this step focused, predictable, and debuggable.
   * In short: one clear job here beats ten confusing side-effects elsewhere.
   */
  private String registryKeyAsString(Object registry, Object candidate) {
    if (registry == null || candidate == null) {
      return null;
    }

    for (Method method : registry.getClass().getMethods()) {
      if (!"getKey".equals(method.getName()) || method.getParameterCount() != 1) {
        continue;
      }

      Class<?> parameter = method.getParameterTypes()[0];
      if (!parameter.isInstance(candidate)) {
        continue;
      }

      try {
        Object key = method.invoke(registry, candidate);
        return key == null ? null : key.toString();
      } catch (ReflectiveOperationException ignored) {
        // mixed mapping signatures are expected across targets.
      }
    }
    return null;
  }

  /**
   * tryApplyEnchantment exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  private boolean tryApplyEnchantment(ItemStack stack, Object enchantmentValue, Object enchantmentRegistry, int level) {
    Object holder = null;
    if (enchantmentRegistry != null && enchantmentValue != null) {
      for (Method method : enchantmentRegistry.getClass().getMethods()) {
        if (!"wrapAsHolder".equals(method.getName()) || method.getParameterCount() != 1) {
          continue;
        }

        Class<?> parameter = method.getParameterTypes()[0];
        if (!parameter.isInstance(enchantmentValue)) {
          continue;
        }

        try {
          holder = method.invoke(enchantmentRegistry, enchantmentValue);
          break;
        } catch (ReflectiveOperationException ignored) {
          // mixed mapping signatures are expected across targets.
        }
      }
    }

    for (Method method : ItemStack.class.getMethods()) {
      if (!"enchant".equals(method.getName()) || method.getParameterCount() != 2) {
        continue;
      }

      Class<?>[] parameterTypes = method.getParameterTypes();
      if (parameterTypes[1] != int.class && parameterTypes[1] != Integer.class) {
        continue;
      }

      try {
        if (parameterTypes[0].isInstance(enchantmentValue)) {
          method.invoke(stack, enchantmentValue, level);
          return true;
        }
        if (holder != null && parameterTypes[0].isInstance(holder)) {
          method.invoke(stack, holder, level);
          return true;
        }
      } catch (ReflectiveOperationException ignored) {
        // mixed mapping signatures are expected across targets.
      }
    }
    return false;
  }

  /**
   * resolveStaticField exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  private Object resolveStaticField(String className, String fieldName) {
    try {
      Class<?> owner = Class.forName(className);
      return owner.getField(fieldName).get(null);
    } catch (ReflectiveOperationException exception) {
      return null;
    }
  }

  /**
   * invokeNoArgMethod exists to keep this step focused, predictable, and debuggable.
   * In short: one clear job here beats ten confusing side-effects elsewhere.
   */
  private Object invokeNoArgMethod(Object target, String methodName) {
    if (target == null) {
      return null;
    }

    for (Method method : target.getClass().getMethods()) {
      if (!methodName.equals(method.getName()) || method.getParameterCount() != 0) {
        continue;
      }

      try {
        return method.invoke(target);
      } catch (ReflectiveOperationException ignored) {
        // mixed mapping signatures are expected across targets.
      }
    }

    return null;
  }

  /**
   * invokeSingleArgMethod exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  private Object invokeSingleArgMethod(Object target, String methodName, Object argument) {
    if (target == null || argument == null) {
      return null;
    }

    for (Method method : target.getClass().getMethods()) {
      if (!methodName.equals(method.getName()) || method.getParameterCount() != 1) {
        continue;
      }

      Class<?> parameterType = method.getParameterTypes()[0];
      if (!parameterType.isInstance(argument)) {
        continue;
      }

      try {
        return method.invoke(target, argument);
      } catch (ReflectiveOperationException ignored) {
        // mixed mapping signatures are expected across targets.
      }
    }

    return null;
  }

  /**
   * parseResourceLocation exists to keep this step focused, predictable, and debuggable.
   * In short: one clear job here beats ten confusing side-effects elsewhere.
   */
  private Object parseResourceLocation(String id) {
    if (id == null || id.isBlank()) {
      return null;
    }

    try {
      Class<?> resourceLocationClass = Class.forName("net.minecraft.resources.ResourceLocation");
      try {
        Method parseMethod = resourceLocationClass.getMethod("parse", String.class);
        return parseMethod.invoke(null, id);
      } catch (NoSuchMethodException ignored) {
        Method tryParseMethod = resourceLocationClass.getMethod("tryParse", String.class);
        return tryParseMethod.invoke(null, id);
      }
    } catch (ReflectiveOperationException exception) {
      return null;
    }
  }

  /**
   * applyDisplayName exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  private void applyDisplayName(ItemStack stack, String displayName) {
    if (stack == null || displayName == null || displayName.isBlank()) {
      return;
    }

    Component name = Component.literal(displayName);
    for (Method method : ItemStack.class.getMethods()) {
      if (method.getParameterCount() != 1) {
        continue;
      }
      String methodName = method.getName();
      if (!"setHoverName".equals(methodName) && !"setCustomName".equals(methodName)) {
        continue;
      }

      Class<?> parameter = method.getParameterTypes()[0];
      if (!parameter.isInstance(name)) {
        continue;
      }

      try {
        method.invoke(stack, name);
        return;
      } catch (ReflectiveOperationException ignored) {
        // mixed mapping signatures are expected across targets.
      }
    }
  }

  /**
   * applyUnbreakable exists to keep this step focused, predictable, and debuggable.
   * In short: one clear job here beats ten confusing side-effects elsewhere.
   */
  private void applyUnbreakable(ItemStack stack) {
    if (stack == null || stack.isEmpty()) {
      return;
    }

    // Legacy tag path (older targets)
    try {
      Method tagMethod = ItemStack.class.getMethod("getOrCreateTag");
      Object tag = tagMethod.invoke(stack);
      if (tag != null) {
        Method putBoolean = tag.getClass().getMethod("putBoolean", String.class, boolean.class);
        putBoolean.invoke(tag, "Unbreakable", true);
        return;
      }
    } catch (ReflectiveOperationException ignored) {
      // mixed mapping signatures are expected across targets.
    }

    // Component path (newer targets)
    try {
      Class<?> dataComponentsClass = Class.forName("net.minecraft.core.component.DataComponents");
      Object unbreakableType = dataComponentsClass.getField("UNBREAKABLE").get(null);

      Object unbreakableValue = null;
      try {
        Class<?> unbreakableClass = Class.forName("net.minecraft.world.item.component.Unbreakable");
        try {
          unbreakableValue = unbreakableClass.getConstructor(boolean.class).newInstance(false);
        } catch (NoSuchMethodException noBooleanCtor) {
          unbreakableValue = unbreakableClass.getDeclaredConstructor().newInstance();
        }
      } catch (ClassNotFoundException ignored) {
        // some mappings encode this component value differently.
      }

      if (unbreakableValue == null) {
        return;
      }

      for (Method method : ItemStack.class.getMethods()) {
        if (!"set".equals(method.getName()) || method.getParameterCount() != 2) {
          continue;
        }

        Class<?>[] parameterTypes = method.getParameterTypes();
        if (!parameterTypes[0].isInstance(unbreakableType) || !parameterTypes[1].isInstance(unbreakableValue)) {
          continue;
        }

        method.invoke(stack, unbreakableType, unbreakableValue);
        return;
      }
    } catch (ReflectiveOperationException ignored) {
      // mixed mapping signatures are expected across targets.
    }
  }

  /**
   * unwrapOptional exists to keep this step focused, predictable, and debuggable.
   * In short: one clear job here beats ten confusing side-effects elsewhere.
   */
  private Object unwrapOptional(Object value) {
    if (value instanceof Optional<?> optional) {
      return optional.orElse(null);
    }
    return value;
  }

  /**
   * unwrapHolderValue exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  private Object unwrapHolderValue(Object value) {
    if (value == null) {
      return null;
    }

    try {
      Method valueMethod = value.getClass().getMethod("value");
      return valueMethod.invoke(value);
    } catch (ReflectiveOperationException ignored) {
      return value;
    }
  }

  /**
   * specItemId exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  private String specItemId(ItemStack stack) {
    return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
  }

  /**
   * commonConfig exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public CommonConfig commonConfig() {
    return defaultConfig.common();
  }

  /**
   * policyCoreService exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public PolicyCoreService policyCoreService() {
    return policyCoreService;
  }

  /**
   * syncCoreService exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public SyncCoreService syncCoreService() {
    return syncCoreService;
  }

  /**
   * defaultConfig exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public SyncedClientConfig defaultConfig() {
    return defaultConfig;
  }

  /**
   * supremeVantageService exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public SupremeVantageService supremeVantageService() {
    return supremeVantageService;
  }

}
