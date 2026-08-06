package uk.co.duelmonster.minersadvantage.agent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import uk.co.duelmonster.minersadvantage.common.config.SubstitutionConfig;
import uk.co.duelmonster.minersadvantage.common.config.SubstitutionConfig.SelectionRule;
import uk.co.duelmonster.minersadvantage.common.config.SubstitutionConfig.SubstitutionAction;
import uk.co.duelmonster.minersadvantage.common.config.SubstitutionConfig.TargetKind;
import uk.co.duelmonster.minersadvantage.common.log.LogUtils;

/**
 * SubstitutionAgent: swaps the player's held tool for the best available one in inventory.
 */
public class SubstitutionAgent extends Agent {
  /**
   * ToolKind gives substitution logic a shared language for tool families instead of string chaos.
   */
  private static final int HOTBAR_TOOL_SLOTS = 9;
  private static final int RESTORE_IDLE_TICKS = 3;
  private static final int ATTACK_RESTORE_IDLE_TICKS = 12;
  private static final int BREAK_FALLBACK_ACTIVE_TICKS = 10;
  private static final double TARGET_RANGE_SQ = 36.0;
  private static final int QUEUE_DEDUPE_TICKS = 1;
  private static final ConcurrentMap<RestoreKey, RestoreState> RESTORE_STATES = new ConcurrentHashMap<>();
  private static final ConcurrentMap<QueueKey, QueueState> QUEUE_STATES = new ConcurrentHashMap<>();
  private static final ClassValue<PlayerReflectionSnapshot> PLAYER_REFLECTIONS = new ClassValue<>() {
    @Override
    protected PlayerReflectionSnapshot computeValue(Class<?> type) {
      return new PlayerReflectionSnapshot(
          resolveGameModeCarrierAccessors(type),
          resolveGameModeCarrierFields(type),
          resolveBooleanAccessors(type),
          resolveBooleanFields(type),
          resolveBlockPosAccessors(type),
          resolveBlockPosFields(type),
          resolveSwingBooleanAccessors(type),
          resolveSwingIntegerAccessors(type));
    }
  };

  private static final Comparator<Candidate> CANDIDATE_COMPARATOR = Comparator.comparingInt(Candidate::targetPriority)
      .thenComparing(Candidate::targetMatch)
      .thenComparingInt(Candidate::toolPriority)
      .thenComparing(Candidate::toolMatch)
      .thenComparing(Candidate::isSelected, Boolean::compare)
      .thenComparing(Candidate::slot, Comparator.reverseOrder());

  /**
   * Determine whether player is actively breaking a valid target block.
   */
  private static boolean hasActiveBreakTarget(ServerPlayer player, QueueState queueState, long now) {
    Object gameMode = resolvePlayerGameMode(player);
    if (gameMode != null) {
      if (readBooleanMember(gameMode, "isDestroyingBlock", "destroying", "isDestroying")) {
        return true;
      }

      BlockPos destroyPos = readBlockPosMember(gameMode, "destroyPos", "destroyPosCurrent", "delayedDestroyPos");
      if (destroyPos != null && isTargetInRangeAndSolid(player, destroyPos)) {
        return true;
      }
    }

    if (queueState == null || !isTargetInRangeAndSolid(player, queueState.lastTargetPos())) {
      return false;
    }

    long queueAge = now - queueState.lastSeenTick();
    if (queueAge > BREAK_FALLBACK_ACTIVE_TICKS) {
      if (queueAge == BREAK_FALLBACK_ACTIVE_TICKS + 1L) {
        LogUtils.logDebug(
            "Substitution restore active-break fallback expired player={} pos={} queueAge={} fallbackTicks={}",
            player.getScoreboardName(),
            queueState.lastTargetPos(),
            queueAge,
            BREAK_FALLBACK_ACTIVE_TICKS);
      }
      return false;
    }

    return true;
  }

  /**
   * Resolve game mode object across mappings by trying fields then accessors.
   */
  private static Object resolvePlayerGameMode(ServerPlayer player) {
    PlayerReflectionSnapshot reflection = PLAYER_REFLECTIONS.get(player.getClass());
    for (Method method : reflection.gameModeMethods()) {
      try {
        Object value = method.invoke(player);
        if (value != null && isLikelyGameModeCarrier(value.getClass())) {
          return value;
        }
      } catch (ReflectiveOperationException ignored) {
        // keep searching alternative accessors.
      }
    }

    for (Field field : reflection.gameModeFields()) {
      Object value = readField(player, field);
      if (value != null && isLikelyGameModeCarrier(value.getClass())) {
        return value;
      }
    }
    return null;
  }

  private static Object readField(Object owner, Field field) {
    try {
      return field.get(owner);
    } catch (ReflectiveOperationException ignored) {
      return null;
    }
  }

  private static boolean isLikelyGameModeCarrier(Class<?> candidateType) {
    for (Method method : candidateType.getMethods()) {
      if (method.getReturnType() != InteractionResult.class) {
        continue;
      }

      Class<?>[] parameterTypes = method.getParameterTypes();
      if (parameterTypes.length == 4
          && parameterTypes[0].isAssignableFrom(ServerPlayer.class)
          && parameterTypes[1].isAssignableFrom(Level.class)
          && parameterTypes[2].isAssignableFrom(InteractionHand.class)
          && parameterTypes[3].isAssignableFrom(BlockHitResult.class)) {
        return true;
      }

      if (parameterTypes.length == 5
          && parameterTypes[0].isAssignableFrom(ServerPlayer.class)
          && parameterTypes[1].isAssignableFrom(Level.class)
          && parameterTypes[2].isAssignableFrom(ItemStack.class)
          && parameterTypes[3].isAssignableFrom(InteractionHand.class)
          && parameterTypes[4].isAssignableFrom(BlockHitResult.class)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Read a boolean member by hint names from methods and fields.
   */
  private static boolean readBooleanMember(Object owner, String... hints) {
    PlayerReflectionSnapshot reflection = PLAYER_REFLECTIONS.get(owner.getClass());
    for (Method method : reflection.booleanMethods()) {
      String lowered = method.getName().toLowerCase(Locale.ROOT);
      for (String hint : hints) {
        if (lowered.contains(hint.toLowerCase(Locale.ROOT))) {
          try {
            Object value = method.invoke(owner);
            if (value instanceof Boolean flag && flag) {
              return true;
            }
          } catch (ReflectiveOperationException ignored) {
            // continue trying fallback members.
          }
        }
      }
    }

    for (Field field : reflection.booleanFields()) {
      String lowered = field.getName().toLowerCase(Locale.ROOT);
      for (String hint : hints) {
        if (lowered.contains(hint.toLowerCase(Locale.ROOT))) {
          try {
            if (field.getBoolean(owner)) {
              return true;
            }
          } catch (ReflectiveOperationException ignored) {
            // continue trying fallback members.
          }
        }
      }
    }
    return false;
  }

  /**
   * Read a BlockPos member by hint names from methods and fields.
   */
  private static BlockPos readBlockPosMember(Object owner, String... hints) {
    PlayerReflectionSnapshot reflection = PLAYER_REFLECTIONS.get(owner.getClass());
    for (Method method : reflection.blockPosMethods()) {
      String lowered = method.getName().toLowerCase(Locale.ROOT);
      for (String hint : hints) {
        if (lowered.contains(hint.toLowerCase(Locale.ROOT))) {
          try {
            Object value = method.invoke(owner);
            if (value instanceof BlockPos pos) {
              return pos;
            }
          } catch (ReflectiveOperationException ignored) {
            // continue trying fallback members.
          }
        }
      }
    }

    for (Field field : reflection.blockPosFields()) {
      String lowered = field.getName().toLowerCase(Locale.ROOT);
      for (String hint : hints) {
        if (lowered.contains(hint.toLowerCase(Locale.ROOT))) {
          try {
            Object value = field.get(owner);
            if (value instanceof BlockPos pos) {
              return pos;
            }
          } catch (ReflectiveOperationException ignored) {
            // continue trying fallback members.
          }
        }
      }
    }
    return null;
  }

  /**
   * Check target proximity and solidity before considering active break state.
   */
  private static boolean isTargetInRangeAndSolid(ServerPlayer player, BlockPos pos) {
    if (pos == null) {
      return false;
    }
    if (player.level().getBlockState(pos).isAir()) {
      return false;
    }

    double x = player.getX() - (pos.getX() + 0.5D);
    double y = player.getY() - (pos.getY() + 0.5D);
    double z = player.getZ() - (pos.getZ() + 0.5D);
    return x * x + y * y + z * z <= TARGET_RANGE_SQ;
  }

  /**
   * ToolKind gives substitution logic a shared language for tool families instead of string chaos.
   */
  private enum ToolKind {
    PICKAXE,
    AXE,
    SHOVEL,
    HOE
  }

  private final BlockState targetState;
  private final SubstitutionAction action;
  private final InteractionHand hand;
  private final SubstitutionConfig config;
  private final Set<String> blacklist;
  private final String targetEntityTypeId;

  /**
   * Convenience constructor using current player block as target.
   */
  public SubstitutionAgent(ServerPlayer player) {
    this(player, player.level().getBlockState(player.blockPosition()), SubstitutionAction.BREAK,
        InteractionHand.MAIN_HAND, new SubstitutionConfig());
  }

  /**
   * Convenience constructor with explicit target state.
   */
  public SubstitutionAgent(ServerPlayer player, BlockState targetState) {
    this(player, targetState, SubstitutionAction.BREAK, InteractionHand.MAIN_HAND, new SubstitutionConfig());
  }

  /**
   * Convenience constructor for explicit hand/config.
   */
  public SubstitutionAgent(ServerPlayer player, BlockState targetState, InteractionHand hand,
      SubstitutionConfig config) {
    this(player, targetState, SubstitutionAction.BREAK, hand, config);
  }

  /**
   * Constructor with explicit action and config.
   */
  public SubstitutionAgent(
      ServerPlayer player,
      BlockState targetState,
      SubstitutionAction action,
      InteractionHand hand,
      SubstitutionConfig config) {
    this(player, targetState, action, hand, config, null);
  }

  /**
   * Full constructor supporting block and entity substitution contexts.
   */
  public SubstitutionAgent(
      ServerPlayer player,
      BlockState targetState,
      SubstitutionAction action,
      InteractionHand hand,
      SubstitutionConfig config,
      String targetEntityTypeId) {
    super(player);
    this.targetState = targetState;
    this.action = action == null ? SubstitutionAction.BREAK : action;
    this.hand = hand;
    this.config = config == null ? new SubstitutionConfig() : config;
    this.blacklist = Set.copyOf(this.config.blacklist());
    this.targetEntityTypeId = targetEntityTypeId == null ? "" : targetEntityTypeId.toLowerCase(Locale.ROOT);
  }

  /**
   * Evaluate rules and apply best substitution candidate when needed.
   */
  /**
   * Evaluate one substitution cycle and apply best slot switch when needed.
   */
  @Override
  /**
   * t ic k exists so this path stays predictable and easier to debug when things get weird.
   */
  public boolean tick() {
    ItemStack held = player.getItemInHand(hand);
    LogUtils.logDebug(
        "Substitution tick start player={} action={} hand={} held={} targetBlock={} targetEntity={} config[enabled={},ignoreIfValidTool={},allowMending={},switchBack={}]",
        player.getScoreboardName(),
        action,
        hand,
        itemId(held),
        targetState == null || targetState.isAir() ? "air" : BuiltInRegistries.BLOCK.getKey(targetState.getBlock()),
        targetEntityTypeId,
        config.enabled(),
        config.ignoreIfValidTool(),
        config.allowMending(),
        config.switchBack());

    if (targetState == null || targetState.isAir()) {
      return finish("no substitution target state");
    }

    if (!config.enabled()) {
      return finish("substitution disabled");
    }

    RuleResolution rule = resolveRule(held);
    LogUtils.logDebug(
        "Substitution rule resolved player={} action={} source={} requiredKind={} allowAnyTool={} targetPriority={} toolPriority={} preferSilkTouch={} preferFortune={} minSilkTouch={} minFortune={} requireMending={} denyMending={} toolExpression='{}'",
        player.getScoreboardName(),
        action,
        rule.source(),
        rule.requiredKind(),
        rule.allowAnyTool(),
        rule.targetPriority(),
        rule.toolPriority(),
        rule.preferSilkTouch(),
        rule.preferFortune(),
        rule.minSilkTouch(),
        rule.minFortune(),
        rule.requireMending(),
        rule.denyMending(),
        rule.toolExpression());
    if (rule.allowAnyTool()) {
      return finish("selection rule allows current tool");
    }

    ToolKind requiredKind = rule.requiredKind() == null ? inferRequiredToolKind(held, targetState)
        : rule.requiredKind();
    if (config.ignoreIfValidTool() && matchesToolKind(held, requiredKind) && held.isCorrectToolForDrops(targetState)) {
      return finish("held tool already valid");
    }

    List<Candidate> candidates = buildCandidates(requiredKind, held, rule);
    if (candidates.isEmpty()) {
      return finish("no candidate tools");
    }

    Candidate best = null;
    for (Candidate candidate : candidates) {
      if (best == null || CANDIDATE_COMPARATOR.compare(candidate, best) > 0) {
        best = candidate;
      }
    }
    if (best == null) {
      return finish("no best substitution candidate");
    }

    ItemStack replacement = best.stack();
    if (replacement.isEmpty() || ItemStack.isSameItemSameComponents(held, replacement)) {
      touchRestoreState();
      return finish("best candidate equals held");
    }

    if (hand != InteractionHand.MAIN_HAND) {
      return finish("substitution currently supports main hand only");
    }

    int previousSelectedSlot = hand == InteractionHand.MAIN_HAND ? selectedHotbarSlot() : -1;
    if (!setSelectedHotbarSlot(player, best.slot())) {
      return finish("unable to set selected hotbar slot");
    }
    rememberRestoreState(previousSelectedSlot, best.slot());

    LogUtils.logDebug(
        "Substitution selected slot player={} hand={} slot={} held={} replacement={} target={} requiredKind={}",
        player.getScoreboardName(),
        hand,
        best.slot(),
        itemId(held),
        itemId(replacement),
        BuiltInRegistries.BLOCK.getKey(targetState.getBlock()),
        requiredKind);

    return finish("tool substitution evaluated");
  }

  /**
   * Build scored candidate list from hotbar items.
   */
  private List<Candidate> buildCandidates(ToolKind requiredKind, ItemStack held, RuleResolution rule) {
    List<Candidate> candidates = new ArrayList<>();
    MatchRating targetMatch = targetMatch(requiredKind, held);
    if (!targetMatch.matches()) {
      LogUtils.logDebug(
          "Substitution candidate scan aborted player={} action={} hand={} reason=target-no-match requiredKind={} held={} target={}",
          player.getScoreboardName(),
          action,
          hand,
          requiredKind,
          itemId(held),
          BuiltInRegistries.BLOCK.getKey(targetState.getBlock()));
      return candidates;
    }

    int scanned = 0;
    int emptySlots = 0;
    int blacklisted = 0;
    int toolMismatches = 0;
    int selectedHotbarSlot = selectedHotbarSlot();
    for (int slot = 0; slot < Math.min(HOTBAR_TOOL_SLOTS, player.getInventory().getContainerSize()); slot++) {
      scanned++;
      ItemStack stack = player.getInventory().getItem(slot);
      if (stack.isEmpty()) {
        emptySlots++;
        continue;
      }

      String itemId = itemId(stack);
      if (blacklist.contains(itemId)) {
        blacklisted++;
        continue;
      }

      MatchRating toolMatch = toolMatch(stack, requiredKind, rule);
      if (!toolMatch.matches()) {
        toolMismatches++;
        continue;
      }

      int toolPriority = toolPriority(stack, requiredKind, rule);
      boolean selected = hand == InteractionHand.MAIN_HAND && slot == selectedHotbarSlot;
      Candidate candidate = new Candidate(rule.targetPriority(), targetMatch, toolPriority, toolMatch, slot, selected,
          stack);
      candidates.add(candidate);

      if (LogUtils.isDebugLoggingEnabled()) {
        LogUtils.logDebug(
            "Substitution candidate player={} action={} hand={} slot={} item={} target={} rule={} targetMatch={} toolPriority={} toolMatch={} selected={}",
            player.getScoreboardName(),
            action,
            hand,
            slot,
            itemId,
            BuiltInRegistries.BLOCK.getKey(targetState.getBlock()),
            rule.source(),
            targetMatch,
            toolPriority,
            toolMatch,
            selected);
      }
    }

    LogUtils.logDebug(
        "Substitution candidate scan result player={} action={} hand={} requiredKind={} scannedSlots={} emptySlots={} blacklisted={} rejectedByMatch={} candidates={} selectedSlot={} blacklistSize={} target={} ruleSource={}",
        player.getScoreboardName(),
        action,
        hand,
        requiredKind,
        scanned,
        emptySlots,
        blacklisted,
        toolMismatches,
        candidates.size(),
        selectedHotbarSlot,
        blacklist.size(),
        BuiltInRegistries.BLOCK.getKey(targetState.getBlock()),
        rule.source());

    return candidates;
  }

  /**
   * Compute target suitability score for required tool kind.
   */
  private MatchRating targetMatch(ToolKind requiredKind, ItemStack held) {
    boolean tagMatch = switch (requiredKind) {
      case PICKAXE -> targetState.is(BlockTags.MINEABLE_WITH_PICKAXE);
      case AXE -> targetState.is(BlockTags.MINEABLE_WITH_AXE);
      case SHOVEL -> targetState.is(BlockTags.MINEABLE_WITH_SHOVEL);
      case HOE -> targetState.is(BlockTags.MINEABLE_WITH_HOE);
    };

    boolean inferredFromHeld = !tagMatch && matchesToolKind(held, requiredKind);
    if (!tagMatch && !inferredFromHeld) {
      LogUtils.logDebug(
          "Substitution target mismatch player={} action={} requiredKind={} held={} target={} requiresCorrectTool={}",
          player.getScoreboardName(),
          action,
          requiredKind,
          itemId(held),
          BuiltInRegistries.BLOCK.getKey(targetState.getBlock()),
          targetState.requiresCorrectToolForDrops());
      return MatchRating.noMatch();
    }

    double[] levels = new double[] {
        tagMatch ? 1.0 : 0.0,
        targetState.requiresCorrectToolForDrops() ? 1.0 : 0.0,
        inferredFromHeld ? 1.0 : 0.0
    };
    return MatchRating.match(levels);
  }

  /**
   * Compute stack suitability score against target and rule constraints.
   */
  private MatchRating toolMatch(ItemStack stack, ToolKind requiredKind, RuleResolution rule) {
    if (!matchesToolKind(stack, requiredKind)) {
      return MatchRating.noMatch();
    }

    boolean canDropCorrectly = !targetState.requiresCorrectToolForDrops() || stack.isCorrectToolForDrops(targetState);
    if (!canDropCorrectly) {
      return MatchRating.noMatch();
    }

    if (!matchesToolExpression(rule, stack, requiredKind, canDropCorrectly)) {
      return MatchRating.noMatch();
    }

    int silkLevel = enchantmentLevel(stack, Enchantments.SILK_TOUCH);
    int fortuneLevel = enchantmentLevel(stack, Enchantments.FORTUNE);
    int mendingLevel = enchantmentLevel(stack, Enchantments.MENDING);
    if (silkLevel < rule.minSilkTouch() || fortuneLevel < rule.minFortune()) {
      return MatchRating.noMatch();
    }
    if (rule.requireMending() && mendingLevel <= 0) {
      return MatchRating.noMatch();
    }
    if (rule.denyMending() && mendingLevel > 0) {
      return MatchRating.noMatch();
    }

    double destroySpeed = stack.getDestroySpeed(targetState);
    double normalizedSpeed = Math.max(0.0, destroySpeed - 1.0);
    double enchantRating = enchantPreferenceRating(silkLevel, fortuneLevel, rule);
    double durabilityRating = durabilityRating(stack);

    return MatchRating.match(new double[] {
        canDropCorrectly ? 1.0 : 0.0,
        normalizedSpeed,
        enchantRating,
        durabilityRating
    });
  }

  /**
   * Compute candidate tool priority with small context bonuses.
   */
  private int toolPriority(ItemStack stack, ToolKind requiredKind, RuleResolution rule) {
    int priority = rule.toolPriority() + (matchesToolKind(stack, requiredKind) ? 10 : 0);
    if (stack.is(ItemTags.MINING_ENCHANTABLE)) {
      priority += 1;
    }
    if (isPickaxeTool(stack) && requiredKind == ToolKind.PICKAXE) {
      priority += 2;
    }
    return priority;
  }

  /**
   * Score enchantment preference according to context and rule flags.
   */
  private double enchantPreferenceRating(int silkLevel, int fortuneLevel, RuleResolution rule) {
    boolean oreContext = targetState.is(BlockTags.MINEABLE_WITH_PICKAXE);

    if (!oreContext) {
      return silkLevel + fortuneLevel;
    }
    if (rule.preferSilkTouch()) {
      return silkLevel * 10.0 + fortuneLevel;
    }
    if (rule.preferFortune()) {
      return fortuneLevel * 10.0 + silkLevel;
    }
    return silkLevel + fortuneLevel;
  }

  /**
   * Resolve most specific matching rule or fallback implicit defaults.
   */
  private RuleResolution resolveRule(ItemStack held) {
    Optional<SelectionRule> bestRule = config.selectionRules().stream()
        .filter(this::actionMatches)
        .filter(this::targetMatches)
        .max(Comparator.comparingInt(SelectionRule::targetPriority)
            .thenComparingInt(this::targetSpecificity)
            .thenComparing(SelectionRule::targetId));

    if (bestRule.isEmpty()) {
      LogUtils.logDebug(
          "Substitution rule fallback player={} action={} targetBlock={} targetEntity={} reason=no-specific-rule",
          player.getScoreboardName(),
          action,
          BuiltInRegistries.BLOCK.getKey(targetState.getBlock()),
          targetEntityTypeId);
      return new RuleResolution(
          null,
          100,
          10,
          config.prioritizeSilkTouch(),
          config.favourFortune(),
          false,
          "implicit",
          "",
          0,
          0,
          false,
          false);
    }

    SelectionRule rule = bestRule.get();
    Optional<ToolKind> parsedRequiredKind = parseRequiredToolKind(rule.requiredToolKind());
    if (parsedRequiredKind.isEmpty() && rule.requiredToolKind() != null && !rule.requiredToolKind().isBlank()) {
      LogUtils.logWarn(
          "Invalid substitution requiredToolKind '{}' for player={} action={} rule={} - falling back to inferred tool kind",
          rule.requiredToolKind(),
          player.getScoreboardName(),
          action,
          rule.targetKind() + ":" + rule.targetId());
    }
    ToolKind requiredKind = parsedRequiredKind.orElseGet(() -> inferRequiredToolKind(held, targetState));
    return new RuleResolution(
        requiredKind,
        rule.targetPriority(),
        rule.toolPriority(),
        rule.preferSilkTouch() || config.prioritizeSilkTouch(),
        rule.preferFortune() || config.favourFortune(),
        parsedRequiredKind.isEmpty(),
        rule.targetKind() + ":" + rule.targetId(),
        rule.toolExpression(),
        rule.minSilkTouch(),
        rule.minFortune(),
        rule.requireMending(),
        rule.denyMending());
  }

  /**
   * Check whether a selection rule applies to current substitution action.
   */
  private boolean actionMatches(SelectionRule rule) {
    return rule.action() == SubstitutionAction.ANY || rule.action() == action;
  }

  /**
   * Check structural target kind and optional target expression.
   */
  private boolean targetMatches(SelectionRule rule) {
    boolean structuralMatch = switch (rule.targetKind()) {
      case ANY -> true;
      case ENTITY_TYPE -> !targetEntityTypeId.isBlank() && targetEntityTypeId.equalsIgnoreCase(rule.targetId());
      case BLOCK_TAG -> matchesKnownBlockTag(rule.targetId());
    };
    if (!structuralMatch) {
      return false;
    }

    return evaluateExpression(rule.targetExpression(), this::matchesTargetSelector);
  }

  /**
   * Resolve known block-tag identifiers used by substitution rules.
   */
  private boolean matchesKnownBlockTag(String tagId) {
    return switch (tagId) {
      case "minecraft:mineable/pickaxe" -> targetState.is(BlockTags.MINEABLE_WITH_PICKAXE);
      case "minecraft:mineable/axe" -> targetState.is(BlockTags.MINEABLE_WITH_AXE);
      case "minecraft:mineable/shovel" -> targetState.is(BlockTags.MINEABLE_WITH_SHOVEL);
      case "minecraft:mineable/hoe" -> targetState.is(BlockTags.MINEABLE_WITH_HOE);
      default -> false;
    };
  }

  /**
   * Return target-kind specificity rank for tie-breaking rule selection.
   */
  private int targetSpecificity(SelectionRule rule) {
    return switch (rule.targetKind()) {
      case ANY -> 0;
      case ENTITY_TYPE -> 1;
      case BLOCK_TAG -> 2;
    };
  }

  /**
   * Evaluate boolean tool expression against a concrete tool stack.
   */
  private boolean matchesToolExpression(RuleResolution rule, ItemStack stack, ToolKind requiredKind,
      boolean canDropCorrectly) {
    return evaluateExpression(rule.toolExpression(),
        selector -> matchesToolSelector(selector, stack, requiredKind, canDropCorrectly));
  }

  /**
   * Evaluate one target selector atom.
   */
  private boolean matchesTargetSelector(String selector) {
    String normalized = selector.trim().toLowerCase(Locale.ROOT);
    if (normalized.isBlank()) {
      return true;
    }

    if (normalized.startsWith("block_tag:")) {
      return matchesKnownBlockTag(normalized.substring("block_tag:".length()));
    }
    if (normalized.startsWith("action:")) {
      String actionToken = normalized.substring("action:".length()).trim();
      return action.name().equalsIgnoreCase(actionToken);
    }
    if (normalized.startsWith("target_block:")) {
      String blockId = BuiltInRegistries.BLOCK.getKey(targetState.getBlock()).toString();
      return blockId.equalsIgnoreCase(normalized.substring("target_block:".length()).trim());
    }
    if (normalized.startsWith("entity_type:")) {
      return !targetEntityTypeId.isBlank()
          && targetEntityTypeId.equalsIgnoreCase(normalized.substring("entity_type:".length()).trim());
    }
    return switch (normalized) {
      case "requires_correct_tool" -> targetState.requiresCorrectToolForDrops();
      case "mineable_pickaxe" -> targetState.is(BlockTags.MINEABLE_WITH_PICKAXE);
      case "mineable_axe" -> targetState.is(BlockTags.MINEABLE_WITH_AXE);
      case "mineable_shovel" -> targetState.is(BlockTags.MINEABLE_WITH_SHOVEL);
      case "mineable_hoe" -> targetState.is(BlockTags.MINEABLE_WITH_HOE);
      default -> false;
    };
  }

  /**
   * Evaluate one tool selector atom.
   */
  private boolean matchesToolSelector(String selector, ItemStack stack, ToolKind requiredKind,
      boolean canDropCorrectly) {
    String normalized = selector.trim().toLowerCase(Locale.ROOT);
    if (normalized.isBlank()) {
      return true;
    }

    if (normalized.startsWith("tool_kind:")) {
      Optional<ToolKind> parsed = parseRequiredToolKind(normalized.substring("tool_kind:".length()));
      return parsed.isPresent() && matchesToolKind(stack, parsed.get());
    }
    if (normalized.startsWith("item:")) {
      return itemId(stack).equalsIgnoreCase(normalized.substring("item:".length()).trim());
    }
    if (normalized.startsWith("action:")) {
      String actionToken = normalized.substring("action:".length()).trim();
      return action.name().equalsIgnoreCase(actionToken);
    }

    return switch (normalized) {
      case "correct_tool" -> canDropCorrectly;
      case "mining_enchantable" -> stack.is(ItemTags.MINING_ENCHANTABLE);
      case "required_kind" -> matchesToolKind(stack, requiredKind);
      default -> false;
    };
  }

  /**
   * Parse and evaluate lightweight boolean expression using AND/OR/NOT.
   */
  private boolean evaluateExpression(String expression, Predicate<String> atomEvaluator) {
    if (expression == null || expression.isBlank()) {
      return true;
    }

    try {
      return new BooleanExpressionParser(expression).evaluate(atomEvaluator);
    } catch (IllegalArgumentException ex) {
      LogUtils.logWarn("Invalid substitution expression '{}' for player={} action={}: {}", expression,
          player.getScoreboardName(), action, ex.getMessage());
      return false;
    }
  }

  /**
   * Parse configured required tool-kind token.
   */
  private Optional<ToolKind> parseRequiredToolKind(String value) {
    if (value == null || value.isBlank()) {
      return Optional.empty();
    }

    return switch (value.trim().toLowerCase(Locale.ROOT)) {
      case "pickaxe" -> Optional.of(ToolKind.PICKAXE);
      case "axe" -> Optional.of(ToolKind.AXE);
      case "shovel" -> Optional.of(ToolKind.SHOVEL);
      case "hoe" -> Optional.of(ToolKind.HOE);
      default -> Optional.empty();
    };
  }

  /**
   * Compute normalized durability score for tool ranking.
   */
  private double durabilityRating(ItemStack stack) {
    if (!stack.isDamageableItem() || stack.getMaxDamage() <= 0) {
      return stack.getCount();
    }

    if (!config.allowMending() && enchantmentLevel(stack, Enchantments.MENDING) > 0) {
      return 0.0;
    }
    return (double) (stack.getMaxDamage() - stack.getDamageValue()) / stack.getMaxDamage();
  }

  /**
   * Resolve current hotbar slot for this player.
   */
  private int selectedHotbarSlot() {
    return selectedHotbarSlot(player);
  }

  /**
   * Store restore state so slot can be switched back after inactivity.
   */
  private void rememberRestoreState(int previousSelectedSlot, int switchedToSlot) {
    if (!config.switchBack()) {
      clearRestoreState(player, hand);
      return;
    }

    RestoreKey key = new RestoreKey(player.getUUID(), hand);
    RESTORE_STATES.put(
        key,
        new RestoreState(
            previousSelectedSlot,
            switchedToSlot,
            player.level().getGameTime(),
            action));
  }

  /**
   * Refresh restore-state activity timestamp.
   */
  private void touchRestoreState() {
    touchRestoreState(player, hand);
  }

  /**
   * Return effective break-hand stack while substitution switch is active.
   */
  public static ItemStack effectiveBreakHandStack(ServerPlayer player, InteractionHand hand) {
    if (player == null || hand == null) {
      return ItemStack.EMPTY;
    }

    ItemStack current = player.getItemInHand(hand);
    RestoreState state = RESTORE_STATES.get(new RestoreKey(player.getUUID(), hand));
    if (state == null || state.action() != SubstitutionAction.BREAK) {
      return current;
    }

    long now = player.level().getGameTime();
    if (now - state.lastActivityTick() > (RESTORE_IDLE_TICKS + 1L)) {
      return current;
    }

    int slot = state.switchedToSlot();
    int maxSlot = Math.min(HOTBAR_TOOL_SLOTS, player.getInventory().getContainerSize());
    if (slot < 0 || slot >= maxSlot) {
      return current;
    }

    ItemStack switched = player.getInventory().getItem(slot);
    return switched.isEmpty() ? current : switched;
  }

  /**
   * Deduplicate start-queue requests for same target in adjacent ticks.
   */
  public static boolean shouldQueueStartSubstitution(
      ServerPlayer player,
      InteractionHand hand,
      SubstitutionAction action,
      BlockPos targetPos) {
    if (player == null || hand == null || action == null || targetPos == null) {
      return true;
    }

    long now = player.level().getGameTime();
    QueueKey key = new QueueKey(player.getUUID(), hand, action);
    QueueState previous = QUEUE_STATES.get(key);
    if (previous != null) {
      boolean sameTarget = previous.lastTargetPos().equals(targetPos);
      if (sameTarget && now - previous.lastQueuedTick() <= QUEUE_DEDUPE_TICKS) {
        QUEUE_STATES.put(key, previous.withLastSeenTick(now));
        touchRestoreState(player, hand);
        LogUtils.logDebug(
            "Substitution queue deduped player={} hand={} action={} pos={} queuedTick={} now={} dedupeTicks={}",
            player.getScoreboardName(),
            hand,
            action,
            targetPos,
            previous.lastQueuedTick(),
            now,
            QUEUE_DEDUPE_TICKS);
        return false;
      }
    }

    QUEUE_STATES.put(key, new QueueState(targetPos.immutable(), now, now));
    touchRestoreState(player, hand);
    LogUtils.logDebug(
        "Substitution queue accepted player={} hand={} action={} pos={} tick={}",
        player.getScoreboardName(),
        hand,
        action,
        targetPos,
        now);
    return true;
  }

  /**
   * Update queue/restore activity when substitution work is observed.
   */
  public static void markSubstitutionActivity(ServerPlayer player, InteractionHand hand, SubstitutionAction action,
      BlockPos targetPos) {
    if (player == null || hand == null || action == null || targetPos == null) {
      return;
    }

    long now = player.level().getGameTime();
    QueueKey key = new QueueKey(player.getUUID(), hand, action);
    QueueState previous = QUEUE_STATES.get(key);
    if (previous == null) {
      QUEUE_STATES.put(key, new QueueState(targetPos.immutable(), now, now));
    } else {
      QUEUE_STATES.put(key, previous.withTargetAndSeen(targetPos.immutable(), now));
    }
    touchRestoreState(player, hand);
    LogUtils.logDebug(
        "Substitution activity marked player={} hand={} action={} pos={} tick={} hadPreviousQueueState={}",
        player.getScoreboardName(),
        hand,
        action,
        targetPos,
        now,
        previous != null);
  }

  /**
   * Touch restore state for one player+hand.
   */
  private static void touchRestoreState(ServerPlayer player, InteractionHand hand) {
    RestoreKey key = new RestoreKey(player.getUUID(), hand);
    RestoreState state = RESTORE_STATES.get(key);
    if (state == null) {
      return;
    }

    RESTORE_STATES.put(key, state.withLastActivityTick(player.level().getGameTime()));
  }

  /**
   * Process pending switch-back logic for both hands.
   */
  public static void processSwitchBack(ServerPlayer player) {
    if (player == null) {
      return;
    }

    processSwitchBack(player, InteractionHand.MAIN_HAND);
    processSwitchBack(player, InteractionHand.OFF_HAND);
  }

  /**
   * Clear restore/queue state for this player.
   */
  public static void clearRestoreState(ServerPlayer player) {
    if (player == null) {
      return;
    }

    clearRestoreState(player, InteractionHand.MAIN_HAND);
    clearRestoreState(player, InteractionHand.OFF_HAND);
    QUEUE_STATES.keySet().removeIf(key -> key.playerId().equals(player.getUUID()));
  }

  /**
   * Evaluate idle timeout and restore previous selected slot when safe.
   */
  private static void processSwitchBack(ServerPlayer player, InteractionHand hand) {
    RestoreKey key = new RestoreKey(player.getUUID(), hand);
    RestoreState state = RESTORE_STATES.get(key);
    if (state == null) {
      return;
    }

    if (AgentManager.get().hasBlockingAutomationAgent(player)) {
      long now = player.level().getGameTime();
      RESTORE_STATES.put(key, state.withLastActivityTick(now));
      LogUtils.logDebug(
          "Substitution restore deferred player={} hand={} reason=automation-agent-active action={}",
          player.getScoreboardName(),
          hand,
          state.action());
      return;
    }

    long now = player.level().getGameTime();
    int restoreIdleTicks = restoreIdleTicksForAction(state.action());
    if (now - state.lastActivityTick() <= restoreIdleTicks) {
      LogUtils.logDebug(
          "Substitution restore deferred player={} hand={} reason=idle-window action={} lastActivity={} now={}",
          player.getScoreboardName(),
          hand,
          state.action(),
          state.lastActivityTick(),
          now);
      return;
    }

    QueueState queueState = QUEUE_STATES.get(new QueueKey(player.getUUID(), hand, state.action()));
    if (state.action() == SubstitutionAction.BREAK && hasActiveBreakTarget(player, queueState, now)) {
      RESTORE_STATES.put(key, state.withLastActivityTick(now));
      LogUtils.logDebug(
          "Substitution restore deferred player={} hand={} reason=active-break-target action={} pos={}",
          player.getScoreboardName(),
          hand,
          state.action(),
          queueState == null ? null : queueState.lastTargetPos());
      return;
    }

    if (queueState != null && now - queueState.lastSeenTick() <= restoreIdleTicks) {
      RESTORE_STATES.put(key, state.withLastActivityTick(now));
      LogUtils.logDebug(
          "Substitution restore deferred player={} hand={} reason=recent-queue-activity action={} lastSeen={} now={} pos={}",
          player.getScoreboardName(),
          hand,
          state.action(),
          queueState.lastSeenTick(),
          now,
          queueState.lastTargetPos());
      return;
    }

    if (isStillUsingTool(player)) {
      RESTORE_STATES.put(key, state.withLastActivityTick(now));
      LogUtils.logDebug(
          "Substitution restore deferred player={} hand={} reason=still-using-tool action={}",
          player.getScoreboardName(),
          hand,
          state.action());
      return;
    }

    if (hand == InteractionHand.MAIN_HAND) {
      int selectedSlot = selectedHotbarSlot(player);
      if (selectedSlot != state.switchedToSlot()) {
        RESTORE_STATES.remove(key);
        LogUtils.logDebug(
            "Substitution restore skipped player={} hand={} reason=selected-slot-changed expected={} current={}",
            player.getScoreboardName(),
            hand,
            state.switchedToSlot(),
            selectedSlot);
        return;
      }
    }

    if (state.previousSelectedSlot() < 0
        || state.previousSelectedSlot() >= Math.min(HOTBAR_TOOL_SLOTS, player.getInventory().getContainerSize())) {
      RESTORE_STATES.remove(key);
      LogUtils.logDebug(
          "Substitution restore aborted player={} hand={} reason=invalid-previous-slot previousSlot={} switchedTo={} action={}",
          player.getScoreboardName(),
          hand,
          state.previousSelectedSlot(),
          state.switchedToSlot(),
          state.action());
      return;
    }

    if (!setSelectedHotbarSlot(player, state.previousSelectedSlot())) {
      RESTORE_STATES.remove(key);
      LogUtils.logDebug(
          "Substitution restore aborted player={} hand={} reason=set-selected-slot-failed previousSlot={} action={}",
          player.getScoreboardName(),
          hand,
          state.previousSelectedSlot(),
          state.action());
      return;
    }
    RESTORE_STATES.remove(key);

    LogUtils.logDebug(
        "Substitution restored player={} hand={} slot={} action={}",
        player.getScoreboardName(),
        hand,
        state.previousSelectedSlot(),
        state.action());
  }

  static int restoreIdleTicksForAction(SubstitutionAction action) {
    return action == SubstitutionAction.ATTACK ? ATTACK_RESTORE_IDLE_TICKS : RESTORE_IDLE_TICKS;
  }

  /**
   * Remove restore-state entry for one hand.
   */
  private static void clearRestoreState(ServerPlayer player, InteractionHand hand) {
    RESTORE_STATES.remove(new RestoreKey(player.getUUID(), hand));
  }

  /**
   * Heuristic check for active swing/use state across mapping differences.
   */
  private static boolean isStillUsingTool(ServerPlayer player) {
    if (player.isUsingItem()) {
      return true;
    }

    PlayerReflectionSnapshot reflection = PLAYER_REFLECTIONS.get(player.getClass());
    for (Method method : reflection.swingBooleanMethods()) {

      try {
        Object value = method.invoke(player);
        if (value instanceof Boolean flag && flag) {
          return true;
        }
      } catch (ReflectiveOperationException ignored) {
        // mixed mappings differ across targets and should not break restore logic.
      }
    }

    for (Method method : reflection.swingIntegerMethods()) {

      try {
        Object value = method.invoke(player);
        if (value instanceof Integer counter && counter > 0) {
          return true;
        }
      } catch (ReflectiveOperationException ignored) {
        // mixed mappings differ across targets and should not break restore logic.
      }
    }
    return false;
  }

  /**
   * Resolve selected hotbar slot via method/field fallbacks.
   */
  private static int selectedHotbarSlot(ServerPlayer player) {
    return player.getInventory().getSelectedSlot();
  }

  /**
   * Set selected slot and sync packet to client when possible.
   */
  private static boolean setSelectedHotbarSlot(ServerPlayer player, int slot) {
    if (slot < 0 || slot >= HOTBAR_TOOL_SLOTS) {
      LogUtils.logDebug(
          "Substitution slot set rejected player={} reason=invalid-slot slot={}",
          player.getScoreboardName(),
          slot);
      return false;
    }

    try {
      // Mirror Autoswitch's production-safe approach: direct inventory API call.
      player.getInventory().setSelectedSlot(slot);
      syncSelectedSlotToClient(player, slot);
      return true;
    } catch (RuntimeException exception) {
      LogUtils.logDebug(
          "Substitution slot set failed player={} slot={} reason=inventory-setSelectedSlot-threw:{}",
          player.getScoreboardName(),
          slot,
          exception.getMessage());
      return false;
    }
  }

  /**
   * Send selected-slot update packet through resolved connection.
   */
  private static void syncSelectedSlotToClient(ServerPlayer player, int slot) {
    if (player.connection == null) {
      LogUtils.logDebug(
          "Substitution slot sync skipped player={} slot={} reason=no-connection-handle",
          player.getScoreboardName(),
          slot);
      return;
    }

    player.connection.send(new ClientboundSetHeldSlotPacket(slot));
  }

  /**
   * Read enchantment level by invoking mapping-dependent getLevel signature.
   */
  private int enchantmentLevel(ItemStack stack, Object enchantmentKey) {
    Object enchantments = stack.getEnchantments();
    if (enchantments == null) {
      return 0;
    }

    for (Method method : enchantments.getClass().getMethods()) {
      if (!"getLevel".equals(method.getName()) || method.getParameterCount() != 1) {
        continue;
      }

      Class<?> parameter = method.getParameterTypes()[0];
      try {
        if (parameter.isInstance(enchantmentKey)) {
          Object level = method.invoke(enchantments, enchantmentKey);
          if (level instanceof Integer intLevel) {
            return intLevel;
          }
        }
      } catch (ReflectiveOperationException ignored) {
        // mixed mapping signatures are expected across targets.
      }
    }
    return 0;
  }

  /**
   * Infer needed tool kind from block tags, then fallback to held-tool family.
   */
  private ToolKind inferRequiredToolKind(ItemStack main, BlockState state) {
    if (state.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
      return ToolKind.PICKAXE;
    }
    if (state.is(BlockTags.MINEABLE_WITH_AXE)) {
      return ToolKind.AXE;
    }
    if (state.is(BlockTags.MINEABLE_WITH_SHOVEL)) {
      return ToolKind.SHOVEL;
    }
    if (state.is(BlockTags.MINEABLE_WITH_HOE)) {
      return ToolKind.HOE;
    }

    if (isPickaxeTool(main)) {
      return ToolKind.PICKAXE;
    }
    if (main.getItem() instanceof AxeItem) {
      return ToolKind.AXE;
    }
    if (main.getItem() instanceof HoeItem) {
      return ToolKind.HOE;
    }
    return ToolKind.SHOVEL;
  }

  /**
   * Check whether stack matches required tool family.
   */
  private boolean matchesToolKind(ItemStack stack, ToolKind requiredKind) {
    if (stack.isEmpty()) {
      return false;
    }
    return switch (requiredKind) {
      case PICKAXE -> isPickaxeTool(stack);
      case AXE -> stack.getItem() instanceof AxeItem;
      case SHOVEL -> stack.getItem() instanceof ShovelItem;
      case HOE -> stack.getItem() instanceof HoeItem;
    };
  }

  /**
   * Identify pickaxe tools by item id suffix.
   */
  private boolean isPickaxeTool(ItemStack stack) {
    String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
    return path.endsWith("_pickaxe");
  }

  /**
   * Return canonical item id used by logs and blacklist filtering.
   */
  private String itemId(ItemStack stack) {
    return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
  }

  /**
   * Candidate packages one hotbar option with its scoring metadata for stable ranking.
   */
  private record Candidate(
      int targetPriority,
      MatchRating targetMatch,
      int toolPriority,
      MatchRating toolMatch,
      int slot,
      boolean isSelected,
      ItemStack stack) {
  }

  /**
   * RuleResolution is the final rule snapshot after defaults and overrides are merged.
   */
  private record RuleResolution(
      ToolKind requiredKind,
      int targetPriority,
      int toolPriority,
      boolean preferSilkTouch,
      boolean preferFortune,
      boolean allowAnyTool,
      String source,
      String toolExpression,
      int minSilkTouch,
      int minFortune,
      boolean requireMending,
      boolean denyMending) {
  }

  /**
   * Restore-state map key scoped by player and hand.
   */
  private record RestoreKey(UUID playerId, InteractionHand hand) {
  }

  /**
   * RestoreState tracks where we came from so we can switch back gracefully after substitution.
   */
  private record RestoreState(
      int previousSelectedSlot,
      int switchedToSlot,
      long lastActivityTick,
      SubstitutionAction action) {
    /**
     * Copy restore state while refreshing last-activity tick.
     */
    private RestoreState withLastActivityTick(long tick) {
      return new RestoreState(previousSelectedSlot, switchedToSlot, tick, action);
    }
  }

  /**
   * Queue-state map key scoped by player, hand, and action type.
   */
  private record QueueKey(UUID playerId, InteractionHand hand, SubstitutionAction action) {
  }

  /**
   * Queue dedupe and liveness metadata for recent substitution targets.
   */
  private record QueueState(BlockPos lastTargetPos, long lastQueuedTick, long lastSeenTick) {
    /**
     * Copy queue state while updating last-seen tick.
     */
    private QueueState withLastSeenTick(long tick) {
      return new QueueState(lastTargetPos, lastQueuedTick, tick);
    }

    /**
     * Copy queue state while replacing tracked target and seen tick.
     */
    private QueueState withTargetAndSeen(BlockPos targetPos, long tick) {
      return new QueueState(targetPos, lastQueuedTick, tick);
    }
  }

  private static final class BooleanExpressionParser {
    private final List<String> tokens;
    private int index;

    /**
     * Tokenize expression once for deterministic parser traversal.
     */
    private BooleanExpressionParser(String expression) {
      this.tokens = tokenize(expression);
      this.index = 0;
    }

    /**
     * Evaluate expression and fail on trailing unexpected tokens.
     */
    private boolean evaluate(Predicate<String> atomEvaluator) {
      boolean value = parseOr(atomEvaluator);
      if (index < tokens.size()) {
        throw new IllegalArgumentException("Unexpected token '" + tokens.get(index) + "'");
      }
      return value;
    }

    /**
     * Parse OR-precedence branch.
     */
    private boolean parseOr(Predicate<String> atomEvaluator) {
      boolean value = parseAnd(atomEvaluator);
      while (matchKeyword("OR")) {
        boolean rhs = parseAnd(atomEvaluator);
        value = value || rhs;
      }
      return value;
    }

    /**
     * Parse AND-precedence branch.
     */
    private boolean parseAnd(Predicate<String> atomEvaluator) {
      boolean value = parseNot(atomEvaluator);
      while (matchKeyword("AND")) {
        boolean rhs = parseNot(atomEvaluator);
        value = value && rhs;
      }
      return value;
    }

    /**
     * Parse unary NOT chain.
     */
    private boolean parseNot(Predicate<String> atomEvaluator) {
      if (matchKeyword("NOT")) {
        return !parseNot(atomEvaluator);
      }
      return parsePrimary(atomEvaluator);
    }

    /**
     * Parse grouped expression or leaf atom.
     */
    private boolean parsePrimary(Predicate<String> atomEvaluator) {
      if (match("(")) {
        boolean value = parseOr(atomEvaluator);
        if (!match(")")) {
          throw new IllegalArgumentException("Missing closing ')'");
        }
        return value;
      }

      if (index >= tokens.size()) {
        throw new IllegalArgumentException("Expression ended unexpectedly");
      }
      return atomEvaluator.test(tokens.get(index++));
    }

    /**
     * Match case-insensitive keyword token.
     */
    private boolean matchKeyword(String keyword) {
      if (index >= tokens.size()) {
        return false;
      }
      if (tokens.get(index).equalsIgnoreCase(keyword)) {
        index++;
        return true;
      }
      return false;
    }

    /**
     * Match exact token.
     */
    private boolean match(String token) {
      if (index >= tokens.size()) {
        return false;
      }
      if (tokens.get(index).equals(token)) {
        index++;
        return true;
      }
      return false;
    }

    /**
     * Split expression into tokens while preserving parentheses.
     */
    private static List<String> tokenize(String expression) {
      String normalized = expression.replace("(", " ( ").replace(")", " ) ").trim();
      if (normalized.isEmpty()) {
        return List.of();
      }
      return Arrays.stream(normalized.split("\\s+"))
          .filter(token -> !token.isBlank())
          .toList();
    }
  }

  private record PlayerReflectionSnapshot(
      List<Method> gameModeMethods,
      List<Field> gameModeFields,
      List<Method> booleanMethods,
      List<Field> booleanFields,
      List<Method> blockPosMethods,
      List<Field> blockPosFields,
      List<Method> swingBooleanMethods,
      List<Method> swingIntegerMethods) {
  }

  private static List<Method> resolveGameModeCarrierAccessors(Class<?> ownerType) {
    List<Method> methods = new ArrayList<>();
    for (Method method : ownerType.getMethods()) {
      if (method.getParameterCount() == 0) {
        methods.add(method);
      }
    }
    return List.copyOf(methods);
  }

  private static List<Field> resolveGameModeCarrierFields(Class<?> ownerType) {
    List<Field> fields = new ArrayList<>();
    for (Field field : ownerType.getFields()) {
      fields.add(field);
    }
    for (Field field : ownerType.getDeclaredFields()) {
      field.setAccessible(true);
      fields.add(field);
    }
    return List.copyOf(fields);
  }

  private static List<Method> resolveBooleanAccessors(Class<?> ownerType) {
    List<Method> methods = new ArrayList<>();
    for (Method method : ownerType.getMethods()) {
      if (method.getParameterCount() == 0
          && (boolean.class.equals(method.getReturnType()) || Boolean.class.equals(method.getReturnType()))) {
        methods.add(method);
      }
    }
    return List.copyOf(methods);
  }

  private static List<Field> resolveBooleanFields(Class<?> ownerType) {
    List<Field> fields = new ArrayList<>();
    for (Field field : ownerType.getDeclaredFields()) {
      if (boolean.class.equals(field.getType()) || Boolean.class.equals(field.getType())) {
        field.setAccessible(true);
        fields.add(field);
      }
    }
    return List.copyOf(fields);
  }

  private static List<Method> resolveBlockPosAccessors(Class<?> ownerType) {
    List<Method> methods = new ArrayList<>();
    for (Method method : ownerType.getMethods()) {
      if (method.getParameterCount() == 0 && BlockPos.class.isAssignableFrom(method.getReturnType())) {
        methods.add(method);
      }
    }
    return List.copyOf(methods);
  }

  private static List<Field> resolveBlockPosFields(Class<?> ownerType) {
    List<Field> fields = new ArrayList<>();
    for (Field field : ownerType.getDeclaredFields()) {
      if (BlockPos.class.isAssignableFrom(field.getType())) {
        field.setAccessible(true);
        fields.add(field);
      }
    }
    return List.copyOf(fields);
  }

  private static List<Method> resolveSwingBooleanAccessors(Class<?> ownerType) {
    List<Method> methods = new ArrayList<>();
    for (Method method : ownerType.getMethods()) {
      if (method.getParameterCount() != 0) {
        continue;
      }
      if (!boolean.class.equals(method.getReturnType()) && !Boolean.class.equals(method.getReturnType())) {
        continue;
      }
      if (method.getName().toLowerCase(Locale.ROOT).contains("swing")) {
        methods.add(method);
      }
    }
    return List.copyOf(methods);
  }

  private static List<Method> resolveSwingIntegerAccessors(Class<?> ownerType) {
    List<Method> methods = new ArrayList<>();
    for (Method method : ownerType.getMethods()) {
      if (method.getParameterCount() != 0) {
        continue;
      }
      if (!int.class.equals(method.getReturnType()) && !Integer.class.equals(method.getReturnType())) {
        continue;
      }
      if (method.getName().toLowerCase(Locale.ROOT).contains("swing")) {
        methods.add(method);
      }
    }
    return List.copyOf(methods);
  }

  /**
   * MatchRating stores multi-level match scores and compares them lexicographically for deterministic picks.
   */
  private record MatchRating(boolean matches, double[] levels) implements Comparable<MatchRating> {
    /**
     * Construct non-match marker value.
     */
    private static MatchRating noMatch() {
      return new MatchRating(false, new double[0]);
    }

    /**
     * Construct match marker with score levels.
     */
    private static MatchRating match(double[] levels) {
      return new MatchRating(true, levels == null ? new double[0] : levels);
    }

    /**
     * Compare match ratings lexicographically by score levels.
     */
    /**
     * Compare rating vectors lexicographically.
     */
    @Override
    /**
     * c om pa re to exists so this path stays predictable and easier to debug when things get weird.
     */
    public int compareTo(MatchRating other) {
      if (matches != other.matches) {
        return matches ? 1 : -1;
      }

      int max = Math.max(levels.length, other.levels.length);
      for (int i = 0; i < max; i++) {
        double left = i < levels.length ? levels[i] : 0.0;
        double right = i < other.levels.length ? other.levels[i] : 0.0;
        int diff = Double.compare(left, right);
        if (diff != 0) {
          return diff;
        }
      }
      return 0;
    }

    /**
     * Render compact debug string for logging candidate scores.
     */
    /**
     * Render concise match/debug representation.
     */
    @Override
    /**
     * t os tr in g exists so this path stays predictable and easier to debug when things get weird.
     */
    public String toString() {
      StringBuilder out = new StringBuilder(matches ? "match[" : "no-match[");
      for (int i = 0; i < levels.length; i++) {
        if (i > 0) {
          out.append(',');
        }
        out.append(String.format(Locale.ROOT, "%.3f", levels[i]));
      }
      out.append(']');
      return out.toString();
    }
  }
}
