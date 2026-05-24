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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.state.BlockState;
import uk.co.duelmonster.minersadvantage.common.config.SubstitutionConfig;
import uk.co.duelmonster.minersadvantage.common.config.SubstitutionConfig.SelectionRule;
import uk.co.duelmonster.minersadvantage.common.config.SubstitutionConfig.SubstitutionAction;
import uk.co.duelmonster.minersadvantage.common.config.SubstitutionConfig.TargetKind;
import uk.co.duelmonster.minersadvantage.common.log.LogUtils;

/**
 * SubstitutionAgent: swaps the player's held tool for the best available one in inventory.
 */
public class SubstitutionAgent extends Agent {
    private static final int HOTBAR_TOOL_SLOTS = 9;
    private static final int RESTORE_IDLE_TICKS = 3;
    private static final double TARGET_RANGE_SQ = 36.0;
    private static final int QUEUE_DEDUPE_TICKS = 1;
    private static final ConcurrentMap<RestoreKey, RestoreState> RESTORE_STATES = new ConcurrentHashMap<>();
    private static final ConcurrentMap<QueueKey, QueueState> QUEUE_STATES = new ConcurrentHashMap<>();

    private static final Comparator<Candidate> CANDIDATE_COMPARATOR =
        Comparator.comparingInt(Candidate::targetPriority)
            .thenComparing(Candidate::targetMatch)
            .thenComparingInt(Candidate::toolPriority)
            .thenComparing(Candidate::toolMatch)
            .thenComparing(Candidate::isSelected, Boolean::compare)
            .thenComparing(Candidate::slot, Comparator.reverseOrder());

    private static boolean hasActiveBreakTarget(ServerPlayer player, QueueState queueState) {
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

        return queueState != null && isTargetInRangeAndSolid(player, queueState.lastTargetPos());
    }

    private static Object resolvePlayerGameMode(ServerPlayer player) {
        try {
            Field gameModeField = player.getClass().getField("gameMode");
            return gameModeField.get(player);
        } catch (ReflectiveOperationException ignored) {
            // Why this exists: mappings can hide this member; try declared field then methods.
        }

        try {
            Field gameModeField = player.getClass().getDeclaredField("gameMode");
            gameModeField.setAccessible(true);
            return gameModeField.get(player);
        } catch (ReflectiveOperationException ignored) {
            // Why this exists: mappings can expose accessors instead of fields.
        }

        for (Method method : player.getClass().getMethods()) {
            if (method.getParameterCount() != 0) {
                continue;
            }
            String lowered = method.getName().toLowerCase(Locale.ROOT);
            if (!lowered.contains("gamemode")) {
                continue;
            }
            try {
                Object value = method.invoke(player);
                if (value != null) {
                    return value;
                }
            } catch (ReflectiveOperationException ignored) {
                // Why this exists: keep searching alternative accessors.
            }
        }
        return null;
    }

    private static boolean readBooleanMember(Object owner, String... hints) {
        for (Method method : owner.getClass().getMethods()) {
            if (method.getParameterCount() != 0) {
                continue;
            }
            if (!boolean.class.equals(method.getReturnType()) && !Boolean.class.equals(method.getReturnType())) {
                continue;
            }
            String lowered = method.getName().toLowerCase(Locale.ROOT);
            for (String hint : hints) {
                if (lowered.contains(hint.toLowerCase(Locale.ROOT))) {
                    try {
                        Object value = method.invoke(owner);
                        if (value instanceof Boolean flag && flag) {
                            return true;
                        }
                    } catch (ReflectiveOperationException ignored) {
                        // Why this exists: continue trying fallback members.
                    }
                }
            }
        }

        for (Field field : owner.getClass().getDeclaredFields()) {
            if (!boolean.class.equals(field.getType()) && !Boolean.class.equals(field.getType())) {
                continue;
            }
            String lowered = field.getName().toLowerCase(Locale.ROOT);
            for (String hint : hints) {
                if (lowered.contains(hint.toLowerCase(Locale.ROOT))) {
                    try {
                        field.setAccessible(true);
                        if (field.getBoolean(owner)) {
                            return true;
                        }
                    } catch (ReflectiveOperationException ignored) {
                        // Why this exists: continue trying fallback members.
                    }
                }
            }
        }
        return false;
    }

    private static BlockPos readBlockPosMember(Object owner, String... hints) {
        for (Method method : owner.getClass().getMethods()) {
            if (method.getParameterCount() != 0) {
                continue;
            }
            if (!BlockPos.class.isAssignableFrom(method.getReturnType())) {
                continue;
            }
            String lowered = method.getName().toLowerCase(Locale.ROOT);
            for (String hint : hints) {
                if (lowered.contains(hint.toLowerCase(Locale.ROOT))) {
                    try {
                        Object value = method.invoke(owner);
                        if (value instanceof BlockPos pos) {
                            return pos;
                        }
                    } catch (ReflectiveOperationException ignored) {
                        // Why this exists: continue trying fallback members.
                    }
                }
            }
        }

        for (Field field : owner.getClass().getDeclaredFields()) {
            if (!BlockPos.class.isAssignableFrom(field.getType())) {
                continue;
            }
            String lowered = field.getName().toLowerCase(Locale.ROOT);
            for (String hint : hints) {
                if (lowered.contains(hint.toLowerCase(Locale.ROOT))) {
                    try {
                        field.setAccessible(true);
                        Object value = field.get(owner);
                        if (value instanceof BlockPos pos) {
                            return pos;
                        }
                    } catch (ReflectiveOperationException ignored) {
                        // Why this exists: continue trying fallback members.
                    }
                }
            }
        }
        return null;
    }

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

    public SubstitutionAgent(ServerPlayer player) {
        this(player, player.level().getBlockState(player.blockPosition()), SubstitutionAction.BREAK, InteractionHand.MAIN_HAND, new SubstitutionConfig());
    }

    public SubstitutionAgent(ServerPlayer player, BlockState targetState) {
        this(player, targetState, SubstitutionAction.BREAK, InteractionHand.MAIN_HAND, new SubstitutionConfig());
    }

    public SubstitutionAgent(ServerPlayer player, BlockState targetState, InteractionHand hand, SubstitutionConfig config) {
        this(player, targetState, SubstitutionAction.BREAK, hand, config);
    }

    public SubstitutionAgent(
        ServerPlayer player,
        BlockState targetState,
        SubstitutionAction action,
        InteractionHand hand,
        SubstitutionConfig config
    ) {
        super(player);
        this.targetState = targetState;
        this.action = action == null ? SubstitutionAction.BREAK : action;
        this.hand = hand;
        this.config = config == null ? new SubstitutionConfig() : config;
        this.blacklist = Set.copyOf(this.config.blacklist());
    }

    @Override
    public boolean tick() {
        if (targetState == null || targetState.isAir()) {
            return finish("no substitution target state");
        }

        if (!config.enabled()) {
            return finish("substitution disabled");
        }

        ItemStack held = player.getItemInHand(hand);
        RuleResolution rule = resolveRule(held);
        if (rule.allowAnyTool()) {
            return finish("selection rule allows current tool");
        }

        ToolKind requiredKind = rule.requiredKind() == null ? inferRequiredToolKind(held, targetState) : rule.requiredKind();
        if (config.ignoreIfValidTool() && matchesToolKind(held, requiredKind) && held.isCorrectToolForDrops(targetState)) {
            return finish("held tool already valid");
        }

        List<Candidate> candidates = buildCandidates(requiredKind, held, rule);
        if (candidates.isEmpty()) {
            return finish("no candidate tools");
        }

        Candidate best = candidates.stream().max(CANDIDATE_COMPARATOR).orElse(null);
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
            requiredKind
        );

        return finish("tool substitution evaluated");
    }

    private List<Candidate> buildCandidates(ToolKind requiredKind, ItemStack held, RuleResolution rule) {
        List<Candidate> candidates = new ArrayList<>();
        MatchRating targetMatch = targetMatch(requiredKind, held);
        if (!targetMatch.matches()) {
            return candidates;
        }

        int selectedHotbarSlot = selectedHotbarSlot();
        for (int slot = 0; slot < Math.min(HOTBAR_TOOL_SLOTS, player.getInventory().getContainerSize()); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }

            String itemId = itemId(stack);
            if (blacklist.contains(itemId)) {
                continue;
            }

            MatchRating toolMatch = toolMatch(stack, requiredKind, rule);
            if (!toolMatch.matches()) {
                continue;
            }

            int toolPriority = toolPriority(stack, requiredKind, rule);
            boolean selected = hand == InteractionHand.MAIN_HAND && slot == selectedHotbarSlot;
            Candidate candidate = new Candidate(rule.targetPriority(), targetMatch, toolPriority, toolMatch, slot, selected, stack);
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
                    selected
                );
            }
        }

        return candidates;
    }

    private MatchRating targetMatch(ToolKind requiredKind, ItemStack held) {
        boolean tagMatch = switch (requiredKind) {
            case PICKAXE -> targetState.is(BlockTags.MINEABLE_WITH_PICKAXE);
            case AXE -> targetState.is(BlockTags.MINEABLE_WITH_AXE);
            case SHOVEL -> targetState.is(BlockTags.MINEABLE_WITH_SHOVEL);
            case HOE -> targetState.is(BlockTags.MINEABLE_WITH_HOE);
        };

        boolean inferredFromHeld = !tagMatch && matchesToolKind(held, requiredKind);
        if (!tagMatch && !inferredFromHeld) {
            return MatchRating.noMatch();
        }

        double[] levels = new double[] {
            tagMatch ? 1.0 : 0.0,
            targetState.requiresCorrectToolForDrops() ? 1.0 : 0.0,
            inferredFromHeld ? 1.0 : 0.0
        };
        return MatchRating.match(levels);
    }

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

    private RuleResolution resolveRule(ItemStack held) {
        Optional<SelectionRule> bestRule = config.selectionRules().stream()
            .filter(this::actionMatches)
            .filter(this::targetMatches)
            .max(Comparator.comparingInt(SelectionRule::targetPriority)
                .thenComparingInt(this::targetSpecificity)
                .thenComparing(SelectionRule::targetId));

        if (bestRule.isEmpty()) {
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
                false
            );
        }

        SelectionRule rule = bestRule.get();
        Optional<ToolKind> parsedRequiredKind = parseRequiredToolKind(rule.requiredToolKind());
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
            rule.denyMending()
        );
    }

    private boolean actionMatches(SelectionRule rule) {
        return rule.action() == SubstitutionAction.ANY || rule.action() == action;
    }

    private boolean targetMatches(SelectionRule rule) {
        boolean structuralMatch = switch (rule.targetKind()) {
            case ANY -> true;
            case ENTITY_TYPE -> false;
            case BLOCK_TAG -> matchesKnownBlockTag(rule.targetId());
        };
        if (!structuralMatch) {
            return false;
        }

        return evaluateExpression(rule.targetExpression(), this::matchesTargetSelector);
    }

    private boolean matchesKnownBlockTag(String tagId) {
        return switch (tagId) {
            case "minecraft:mineable/pickaxe" -> targetState.is(BlockTags.MINEABLE_WITH_PICKAXE);
            case "minecraft:mineable/axe" -> targetState.is(BlockTags.MINEABLE_WITH_AXE);
            case "minecraft:mineable/shovel" -> targetState.is(BlockTags.MINEABLE_WITH_SHOVEL);
            case "minecraft:mineable/hoe" -> targetState.is(BlockTags.MINEABLE_WITH_HOE);
            default -> false;
        };
    }

    private int targetSpecificity(SelectionRule rule) {
        return switch (rule.targetKind()) {
            case ANY -> 0;
            case ENTITY_TYPE -> 1;
            case BLOCK_TAG -> 2;
        };
    }

    private boolean matchesToolExpression(RuleResolution rule, ItemStack stack, ToolKind requiredKind, boolean canDropCorrectly) {
        return evaluateExpression(rule.toolExpression(), selector -> matchesToolSelector(selector, stack, requiredKind, canDropCorrectly));
    }

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
        return switch (normalized) {
            case "requires_correct_tool" -> targetState.requiresCorrectToolForDrops();
            case "mineable_pickaxe" -> targetState.is(BlockTags.MINEABLE_WITH_PICKAXE);
            case "mineable_axe" -> targetState.is(BlockTags.MINEABLE_WITH_AXE);
            case "mineable_shovel" -> targetState.is(BlockTags.MINEABLE_WITH_SHOVEL);
            case "mineable_hoe" -> targetState.is(BlockTags.MINEABLE_WITH_HOE);
            default -> false;
        };
    }

    private boolean matchesToolSelector(String selector, ItemStack stack, ToolKind requiredKind, boolean canDropCorrectly) {
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

    private boolean evaluateExpression(String expression, Predicate<String> atomEvaluator) {
        if (expression == null || expression.isBlank()) {
            return true;
        }

        try {
            return new BooleanExpressionParser(expression).evaluate(atomEvaluator);
        } catch (IllegalArgumentException ex) {
            LogUtils.logWarn("Invalid substitution expression '{}' for player={} action={}: {}", expression, player.getScoreboardName(), action, ex.getMessage());
            return false;
        }
    }

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

    private double durabilityRating(ItemStack stack) {
        if (!stack.isDamageableItem() || stack.getMaxDamage() <= 0) {
            return stack.getCount();
        }

        if (!config.allowMending() && enchantmentLevel(stack, Enchantments.MENDING) > 0) {
            return 0.0;
        }
        return (double) (stack.getMaxDamage() - stack.getDamageValue()) / stack.getMaxDamage();
    }

    private int selectedHotbarSlot() {
        return selectedHotbarSlot(player);
    }

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
                action
            )
        );
    }

    private void touchRestoreState() {
        touchRestoreState(player, hand);
    }

    public static boolean shouldQueueStartSubstitution(
        ServerPlayer player,
        InteractionHand hand,
        SubstitutionAction action,
        BlockPos targetPos
    ) {
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
                return false;
            }
        }

        QUEUE_STATES.put(key, new QueueState(targetPos.immutable(), now, now));
        touchRestoreState(player, hand);
        return true;
    }

    public static void markSubstitutionActivity(ServerPlayer player, InteractionHand hand, SubstitutionAction action, BlockPos targetPos) {
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
    }

    private static void touchRestoreState(ServerPlayer player, InteractionHand hand) {
        RestoreKey key = new RestoreKey(player.getUUID(), hand);
        RestoreState state = RESTORE_STATES.get(key);
        if (state == null) {
            return;
        }

        RESTORE_STATES.put(key, state.withLastActivityTick(player.level().getGameTime()));
    }

    public static void processSwitchBack(ServerPlayer player) {
        if (player == null) {
            return;
        }

        processSwitchBack(player, InteractionHand.MAIN_HAND);
        processSwitchBack(player, InteractionHand.OFF_HAND);
    }

    public static void clearRestoreState(ServerPlayer player) {
        if (player == null) {
            return;
        }

        clearRestoreState(player, InteractionHand.MAIN_HAND);
        clearRestoreState(player, InteractionHand.OFF_HAND);
        QUEUE_STATES.keySet().removeIf(key -> key.playerId().equals(player.getUUID()));
    }

    private static void processSwitchBack(ServerPlayer player, InteractionHand hand) {
        RestoreKey key = new RestoreKey(player.getUUID(), hand);
        RestoreState state = RESTORE_STATES.get(key);
        if (state == null) {
            return;
        }

        long now = player.level().getGameTime();
        if (now - state.lastActivityTick() <= RESTORE_IDLE_TICKS) {
            return;
        }

        QueueState queueState = QUEUE_STATES.get(new QueueKey(player.getUUID(), hand, state.action()));
        if (state.action() == SubstitutionAction.BREAK && hasActiveBreakTarget(player, queueState)) {
            RESTORE_STATES.put(key, state.withLastActivityTick(now));
            return;
        }

        if (queueState != null && now - queueState.lastSeenTick() <= RESTORE_IDLE_TICKS) {
            RESTORE_STATES.put(key, state.withLastActivityTick(now));
            return;
        }

        if (isStillUsingTool(player)) {
            RESTORE_STATES.put(key, state.withLastActivityTick(now));
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
                    selectedSlot
                );
                return;
            }
        }

        if (state.previousSelectedSlot() < 0 || state.previousSelectedSlot() >= Math.min(HOTBAR_TOOL_SLOTS, player.getInventory().getContainerSize())) {
            RESTORE_STATES.remove(key);
            return;
        }

        if (!setSelectedHotbarSlot(player, state.previousSelectedSlot())) {
            RESTORE_STATES.remove(key);
            return;
        }
        RESTORE_STATES.remove(key);

        LogUtils.logDebug(
            "Substitution restored player={} hand={} slot={} action={}",
            player.getScoreboardName(),
            hand,
            state.previousSelectedSlot(),
            state.action()
        );
    }

    private static void clearRestoreState(ServerPlayer player, InteractionHand hand) {
        RESTORE_STATES.remove(new RestoreKey(player.getUUID(), hand));
    }

    private static boolean isStillUsingTool(ServerPlayer player) {
        if (player.isUsingItem()) {
            return true;
        }

        for (Method method : player.getClass().getMethods()) {
            if (method.getParameterCount() != 0) {
                continue;
            }
            if (!boolean.class.equals(method.getReturnType()) && !Boolean.class.equals(method.getReturnType())) {
                continue;
            }

            String name = method.getName();
            String lowered = name.toLowerCase(Locale.ROOT);
            if (!lowered.contains("swing")) {
                continue;
            }

            try {
                Object value = method.invoke(player);
                if (value instanceof Boolean flag && flag) {
                    return true;
                }
            } catch (ReflectiveOperationException ignored) {
                // Why this exists: mixed mappings differ across targets and should not break restore logic.
            }
        }

        for (Method method : player.getClass().getMethods()) {
            if (method.getParameterCount() != 0) {
                continue;
            }
            if (!int.class.equals(method.getReturnType()) && !Integer.class.equals(method.getReturnType())) {
                continue;
            }

            String lowered = method.getName().toLowerCase(Locale.ROOT);
            if (!lowered.contains("swing")) {
                continue;
            }

            try {
                Object value = method.invoke(player);
                if (value instanceof Integer counter && counter > 0) {
                    return true;
                }
            } catch (ReflectiveOperationException ignored) {
                // Why this exists: mixed mappings differ across targets and should not break restore logic.
            }
        }
        return false;
    }

    private static int selectedHotbarSlot(ServerPlayer player) {
        try {
            Method getSelectedSlot = player.getInventory().getClass().getMethod("getSelectedSlot");
            Object value = getSelectedSlot.invoke(player.getInventory());
            if (value instanceof Integer slot) {
                return slot;
            }
        } catch (ReflectiveOperationException ignored) {
            // Why this exists: fall through to a safe default for mixed mappings.
        }

        try {
            Field selectedField = player.getInventory().getClass().getDeclaredField("selected");
            selectedField.setAccessible(true);
            return selectedField.getInt(player.getInventory());
        } catch (ReflectiveOperationException ignored) {
            // Why this exists: mixed mappings may expose selected slot as a field.
        }
        return 0;
    }

    private static boolean setSelectedHotbarSlot(ServerPlayer player, int slot) {
        if (slot < 0 || slot >= HOTBAR_TOOL_SLOTS) {
            return false;
        }

        Object inventory = player.getInventory();
        boolean selected = false;
        try {
            Method method = inventory.getClass().getMethod("setSelectedSlot", int.class);
            method.invoke(inventory, slot);
            selected = true;
        } catch (ReflectiveOperationException ignored) {
            // Why this exists: try alternate names/field for mixed mappings.
        }

        if (!selected) {
            try {
                Method method = inventory.getClass().getMethod("setSelected", int.class);
                method.invoke(inventory, slot);
                selected = true;
            } catch (ReflectiveOperationException ignored) {
                // Why this exists: try alternate names/field for mixed mappings.
            }
        }

        if (!selected) {
            try {
                Field selectedField = inventory.getClass().getDeclaredField("selected");
                selectedField.setAccessible(true);
                selectedField.setInt(inventory, slot);
                selected = true;
            } catch (ReflectiveOperationException ignored) {
                // Why this exists: mixed mappings may expose selected slot as a field.
            }
        }

        if (selected) {
            syncSelectedSlotToClient(player, slot);
        }
        return selected;
    }

    private static void syncSelectedSlotToClient(ServerPlayer player, int slot) {
        Object connection = null;
        try {
            Field connectionField = player.getClass().getField("connection");
            connection = connectionField.get(player);
        } catch (ReflectiveOperationException ignored) {
            // Why this exists: mappings differ; try accessor method next.
        }

        if (connection == null) {
            for (Method method : player.getClass().getMethods()) {
                if (method.getParameterCount() == 0 && method.getName().toLowerCase(Locale.ROOT).contains("connection")) {
                    try {
                        connection = method.invoke(player);
                        break;
                    } catch (ReflectiveOperationException ignored) {
                        // Why this exists: continue probing compatible accessors.
                    }
                }
            }
        }

        if (connection == null) {
            return;
        }

        Object packet = tryCreateHeldSlotPacket(slot);
        if (packet == null) {
            return;
        }

        for (Method method : connection.getClass().getMethods()) {
            if (!"send".equals(method.getName()) || method.getParameterCount() != 1) {
                continue;
            }
            try {
                method.invoke(connection, packet);
                return;
            } catch (ReflectiveOperationException ignored) {
                // Why this exists: method signatures differ by target; try next overload.
            }
        }
    }

    private static Object tryCreateHeldSlotPacket(int slot) {
        String[] packetTypes = new String[] {
            "net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket",
            "net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket"
        };
        for (String packetType : packetTypes) {
            try {
                Class<?> type = Class.forName(packetType);
                return type.getConstructor(int.class).newInstance(slot);
            } catch (ReflectiveOperationException ignored) {
                // Why this exists: class names differ across versions/mappings.
            }
        }
        return null;
    }

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
                // Why this exists: mixed mapping signatures are expected across targets.
            }
        }
        return 0;
    }

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

    private boolean isPickaxeTool(ItemStack stack) {
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        return path.endsWith("_pickaxe");
    }

    private String itemId(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }

    private record Candidate(
        int targetPriority,
        MatchRating targetMatch,
        int toolPriority,
        MatchRating toolMatch,
        int slot,
        boolean isSelected,
        ItemStack stack
    ) {
    }

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
        boolean denyMending
    ) {
    }

    private record RestoreKey(UUID playerId, InteractionHand hand) {
    }

    private record RestoreState(
        int previousSelectedSlot,
        int switchedToSlot,
        long lastActivityTick,
        SubstitutionAction action
    ) {
        private RestoreState withLastActivityTick(long tick) {
            return new RestoreState(previousSelectedSlot, switchedToSlot, tick, action);
        }
    }

    private record QueueKey(UUID playerId, InteractionHand hand, SubstitutionAction action) {
    }

    private record QueueState(BlockPos lastTargetPos, long lastQueuedTick, long lastSeenTick) {
        private QueueState withLastSeenTick(long tick) {
            return new QueueState(lastTargetPos, lastQueuedTick, tick);
        }

        private QueueState withTargetAndSeen(BlockPos targetPos, long tick) {
            return new QueueState(targetPos, lastQueuedTick, tick);
        }
    }

    private static final class BooleanExpressionParser {
        private final List<String> tokens;
        private int index;

        private BooleanExpressionParser(String expression) {
            this.tokens = tokenize(expression);
            this.index = 0;
        }

        private boolean evaluate(Predicate<String> atomEvaluator) {
            boolean value = parseOr(atomEvaluator);
            if (index < tokens.size()) {
                throw new IllegalArgumentException("Unexpected token '" + tokens.get(index) + "'");
            }
            return value;
        }

        private boolean parseOr(Predicate<String> atomEvaluator) {
            boolean value = parseAnd(atomEvaluator);
            while (matchKeyword("OR")) {
                boolean rhs = parseAnd(atomEvaluator);
                value = value || rhs;
            }
            return value;
        }

        private boolean parseAnd(Predicate<String> atomEvaluator) {
            boolean value = parseNot(atomEvaluator);
            while (matchKeyword("AND")) {
                boolean rhs = parseNot(atomEvaluator);
                value = value && rhs;
            }
            return value;
        }

        private boolean parseNot(Predicate<String> atomEvaluator) {
            if (matchKeyword("NOT")) {
                return !parseNot(atomEvaluator);
            }
            return parsePrimary(atomEvaluator);
        }

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

    private record MatchRating(boolean matches, double[] levels) implements Comparable<MatchRating> {
        private static MatchRating noMatch() {
            return new MatchRating(false, new double[0]);
        }

        private static MatchRating match(double[] levels) {
            return new MatchRating(true, levels == null ? new double[0] : levels);
        }

        @Override
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

        @Override
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
