package uk.co.duelmonster.minersadvantage.agent;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.tags.BlockTags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.ItemTags;
import net.minecraft.server.level.ServerPlayer;
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
    private static final int INVENTORY_TOOL_SLOTS = 36;

    private static final Comparator<Candidate> CANDIDATE_COMPARATOR =
        Comparator.comparingInt(Candidate::targetPriority)
            .thenComparing(Candidate::targetMatch)
            .thenComparingInt(Candidate::toolPriority)
            .thenComparing(Candidate::toolMatch)
            .thenComparing(Candidate::isSelected, Boolean::compare)
            .thenComparing(Candidate::slot, Comparator.reverseOrder());

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
            return finish("best candidate equals held");
        }

        ItemStack previous = held.copy();
        player.setItemInHand(hand, replacement.copy());
        player.getInventory().setItem(best.slot(), previous);

        LogUtils.logDebug(
            "Substitution switched player={} hand={} slot={} held={} replacement={} target={} requiredKind={}",
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
        for (int slot = 0; slot < Math.min(INVENTORY_TOOL_SLOTS, player.getInventory().getContainerSize()); slot++) {
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
        try {
            Method getSelectedSlot = player.getInventory().getClass().getMethod("getSelectedSlot");
            Object value = getSelectedSlot.invoke(player.getInventory());
            if (value instanceof Integer slot) {
                return slot;
            }
        } catch (ReflectiveOperationException ignored) {
            // Why this exists: fall through to a safe default for mixed mappings.
        }
        return 0;
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