package uk.co.duelmonster.minersadvantage.common.config;

import java.util.List;

/**
 * SubstitutionConfig keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public record SubstitutionConfig(
    boolean enabled,
    boolean allowMending,
    boolean prioritizeSilkTouch,
    boolean switchBack,
    boolean favourFortune,
    boolean ignoreIfValidTool,
    boolean ignorePassiveMobs,
    List<String> blacklist,
    List<SelectionRule> selectionRules
) {
    /**
     * s ub st it ut io nc on fi g exists so this path stays predictable and easier to debug when things get weird.
     */
    public SubstitutionConfig() {
        this(true, false, false, true, true, true, true, List.of(), defaultSelectionRules());
    }

    /**
     * SubstitutionConfig exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public SubstitutionConfig(boolean enabled, boolean allowMending, boolean prioritizeSilkTouch) {
        this(enabled, allowMending, prioritizeSilkTouch, true, true, true, true, List.of(), defaultSelectionRules());
    }

    public SubstitutionConfig(
        boolean enabled,
        boolean allowMending,
        boolean prioritizeSilkTouch,
        boolean switchBack,
        boolean favourFortune,
        boolean ignoreIfValidTool,
        boolean ignorePassiveMobs,
        List<String> blacklist
    ) {
        this(enabled, allowMending, prioritizeSilkTouch, switchBack, favourFortune, ignoreIfValidTool, ignorePassiveMobs, blacklist, defaultSelectionRules());
    }

    public SubstitutionConfig {
        blacklist = blacklist == null ? List.of() : List.copyOf(blacklist);
        selectionRules = selectionRules == null || selectionRules.isEmpty() ? defaultSelectionRules() : List.copyOf(selectionRules);
    }

    /**
     * favourSilkTouch exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public boolean favourSilkTouch() {
        return prioritizeSilkTouch;
    }

    /**
     * d ef au lt se le ct io nr ul es exists so this path stays predictable and easier to debug when things get weird.
     */
    public static List<SelectionRule> defaultSelectionRules() {
        return List.of(
            new SelectionRule(SubstitutionAction.BREAK, TargetKind.BLOCK_TAG, "minecraft:mineable/pickaxe", "pickaxe", 100, 10, false, true, "", "tool_kind:pickaxe AND correct_tool", 0, 0, false, false),
            new SelectionRule(SubstitutionAction.BREAK, TargetKind.BLOCK_TAG, "minecraft:mineable/axe", "axe", 100, 10, false, false, "", "tool_kind:axe AND correct_tool", 0, 0, false, false),
            new SelectionRule(SubstitutionAction.BREAK, TargetKind.BLOCK_TAG, "minecraft:mineable/shovel", "shovel", 100, 10, false, false, "", "tool_kind:shovel AND correct_tool", 0, 0, false, false),
            new SelectionRule(SubstitutionAction.BREAK, TargetKind.BLOCK_TAG, "minecraft:mineable/hoe", "hoe", 100, 10, false, false, "", "tool_kind:hoe AND correct_tool", 0, 0, false, false),
            new SelectionRule(SubstitutionAction.INTERACT, TargetKind.BLOCK_TAG, "minecraft:mineable/pickaxe", "pickaxe", 90, 8, false, true, "", "tool_kind:pickaxe", 0, 0, false, false),
            new SelectionRule(SubstitutionAction.INTERACT, TargetKind.BLOCK_TAG, "minecraft:mineable/axe", "axe", 90, 8, false, false, "", "tool_kind:axe", 0, 0, false, false),
            new SelectionRule(SubstitutionAction.INTERACT, TargetKind.BLOCK_TAG, "minecraft:mineable/shovel", "shovel", 90, 8, false, false, "", "tool_kind:shovel", 0, 0, false, false),
            new SelectionRule(SubstitutionAction.INTERACT, TargetKind.BLOCK_TAG, "minecraft:mineable/hoe", "hoe", 90, 8, false, false, "", "tool_kind:hoe", 0, 0, false, false)
        );
    }

    /**
     * SubstitutionAction captures the player intent so rule evaluation can stay context-aware.
     */
    public enum SubstitutionAction {
        BREAK,
        INTERACT,
        ATTACK,
        STAT_CHANGE,
        ANY
    }

    /**
     * TargetKind tells the resolver whether a rule is matching tags, entities, or everything.
     */
    public enum TargetKind {
        BLOCK_TAG,
        ENTITY_TYPE,
        ANY
    }

    /**
     * SelectionRule is the one-stop bundle for substitution matching and ranking preferences.
     */
    public record SelectionRule(
        SubstitutionAction action,
        TargetKind targetKind,
        String targetId,
        String requiredToolKind,
        int targetPriority,
        int toolPriority,
        boolean preferSilkTouch,
        boolean preferFortune,
        String targetExpression,
        String toolExpression,
        int minSilkTouch,
        int minFortune,
        boolean requireMending,
        boolean denyMending
    ) {
        public SelectionRule(
            SubstitutionAction action,
            TargetKind targetKind,
            String targetId,
            String requiredToolKind,
            int targetPriority,
            int toolPriority,
            boolean preferSilkTouch,
            boolean preferFortune
        ) {
            this(
                action,
                targetKind,
                targetId,
                requiredToolKind,
                targetPriority,
                toolPriority,
                preferSilkTouch,
                preferFortune,
                "",
                "",
                0,
                0,
                false,
                false
            );
        }

        public SelectionRule {
            action = action == null ? SubstitutionAction.ANY : action;
            targetKind = targetKind == null ? TargetKind.ANY : targetKind;
            targetId = targetId == null ? "" : targetId;
            requiredToolKind = requiredToolKind == null ? "" : requiredToolKind;
            targetExpression = targetExpression == null ? "" : targetExpression;
            toolExpression = toolExpression == null ? "" : toolExpression;
            minSilkTouch = Math.max(0, minSilkTouch);
            minFortune = Math.max(0, minFortune);
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (requireMending && denyMending) {
                denyMending = false;
            }
        }
    }
}
