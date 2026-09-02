package uk.co.duelmonster.minersadvantage.common.config;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * SubstitutionConfig keeps this part of MinersAdvantage running without turning server ticks into confetti.
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
    List<String> blockBlacklist,
    List<SelectionRule> selectionRules,
    int maxActiveAgents,
    boolean dedupeAgent) {
  /**
   * s ub st it ut io nc on fi g exists so this path stays predictable and easier to debug when things get weird.
   */
  public SubstitutionConfig() {
    this(true, false, false, true, true, true, true, List.of(), List.of(), defaultSelectionRules(), 4, true);
  }

  /**
   * SubstitutionConfig exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public SubstitutionConfig(boolean enabled, boolean allowMending, boolean prioritizeSilkTouch) {
    this(enabled, allowMending, prioritizeSilkTouch, true, true, true, true, List.of(), List.of(),
        defaultSelectionRules(), 4, true);
  }

  public SubstitutionConfig(
      boolean enabled,
      boolean allowMending,
      boolean prioritizeSilkTouch,
      boolean switchBack,
      boolean favourFortune,
      boolean ignoreIfValidTool,
      boolean ignorePassiveMobs,
      List<String> blacklist) {
    this(enabled, allowMending, prioritizeSilkTouch, switchBack, favourFortune, ignoreIfValidTool, ignorePassiveMobs,
        blacklist, List.of(), defaultSelectionRules(), 4, true);
  }

  public SubstitutionConfig(
      boolean enabled,
      boolean allowMending,
      boolean prioritizeSilkTouch,
      boolean switchBack,
      boolean favourFortune,
      boolean ignoreIfValidTool,
      boolean ignorePassiveMobs,
      List<String> blacklist,
      List<String> blockBlacklist) {
    this(enabled, allowMending, prioritizeSilkTouch, switchBack, favourFortune, ignoreIfValidTool, ignorePassiveMobs,
        blacklist, blockBlacklist, defaultSelectionRules(), 4, true);
  }

  public SubstitutionConfig(
      boolean enabled,
      boolean allowMending,
      boolean prioritizeSilkTouch,
      boolean switchBack,
      boolean favourFortune,
      boolean ignoreIfValidTool,
      boolean ignorePassiveMobs,
      List<String> blacklist,
      List<String> blockBlacklist,
      List<SelectionRule> selectionRules) {
    this(enabled, allowMending, prioritizeSilkTouch, switchBack, favourFortune, ignoreIfValidTool, ignorePassiveMobs,
        blacklist, blockBlacklist, selectionRules, 4, true);
  }

  public SubstitutionConfig {
    blacklist = blacklist == null ? List.of() : List.copyOf(blacklist);
    blockBlacklist = blockBlacklist == null ? List.of() : List.copyOf(blockBlacklist);
    selectionRules = selectionRules == null || selectionRules.isEmpty() ? defaultSelectionRules()
        : List.copyOf(selectionRules);
    maxActiveAgents = Math.max(1, maxActiveAgents);
  }

  /**
   * favourSilkTouch exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public boolean favourSilkTouch() {
    return prioritizeSilkTouch;
  }

  /**
   * isBlockBlacklisted exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public boolean isBlockBlacklisted(String blockId) {
    return blockId != null && blockBlacklist.contains(blockId);
  }

  /**
   * d ef au lt se le ct io nr ul es exists so this path stays predictable and easier to debug when things get weird.
   */
  public static List<SelectionRule> defaultSelectionRules() {
    return List.of(
        new SelectionRule(SubstitutionAction.BREAK, TargetKind.BLOCK_TAG, "minecraft:mineable/pickaxe", "pickaxe", 100,
            10, false, true, "", "tool_kind:pickaxe AND correct_tool", 0, 0, false, false),
        new SelectionRule(SubstitutionAction.BREAK, TargetKind.BLOCK_TAG, "minecraft:mineable/axe", "axe", 100, 10,
            false, false, "", "tool_kind:axe AND correct_tool", 0, 0, false, false),
        new SelectionRule(SubstitutionAction.BREAK, TargetKind.BLOCK_TAG, "minecraft:mineable/shovel", "shovel", 100,
            10, false, false, "", "tool_kind:shovel AND correct_tool", 0, 0, false, false),
        new SelectionRule(SubstitutionAction.BREAK, TargetKind.BLOCK_TAG, "minecraft:mineable/hoe", "hoe", 100, 10,
            false, false, "", "tool_kind:hoe AND correct_tool", 0, 0, false, false));
  }

  /**
   * Encode selection rules for TOML persistence.
   */
  public static List<String> encodeSelectionRules(List<SelectionRule> selectionRules) {
    if (selectionRules == null || selectionRules.isEmpty()) {
      return List.of();
    }

    List<String> encodedRules = new ArrayList<>(selectionRules.size());
    for (SelectionRule rule : selectionRules) {
      if (rule != null) {
        encodedRules.add(encodeSelectionRule(rule));
      }
    }
    return List.copyOf(encodedRules);
  }

  /**
   * Decode selection rules from TOML persistence entries.
   */
  public static List<SelectionRule> decodeSelectionRules(List<String> encodedRules) {
    if (encodedRules == null || encodedRules.isEmpty()) {
      return List.of();
    }

    List<SelectionRule> decodedRules = new ArrayList<>(encodedRules.size());
    for (String encodedRule : encodedRules) {
      SelectionRule rule = decodeSelectionRule(encodedRule);
      if (rule != null) {
        decodedRules.add(rule);
      }
    }
    return List.copyOf(decodedRules);
  }

  private static String encodeSelectionRule(SelectionRule rule) {
    return String.join("|",
        "v1",
        encodeField(rule.action().name()),
        encodeField(rule.targetKind().name()),
        encodeField(rule.targetId()),
        encodeField(rule.requiredToolKind()),
        encodeField(Integer.toString(rule.targetPriority())),
        encodeField(Integer.toString(rule.toolPriority())),
        encodeField(Boolean.toString(rule.preferSilkTouch())),
        encodeField(Boolean.toString(rule.preferFortune())),
        encodeField(rule.targetExpression()),
        encodeField(rule.toolExpression()),
        encodeField(Integer.toString(rule.minSilkTouch())),
        encodeField(Integer.toString(rule.minFortune())),
        encodeField(Boolean.toString(rule.requireMending())),
        encodeField(Boolean.toString(rule.denyMending())));
  }

  private static SelectionRule decodeSelectionRule(String encodedRule) {
    if (encodedRule == null || encodedRule.isBlank()) {
      return null;
    }

    String[] parts = encodedRule.split("\\|", -1);
    if (parts.length != 15 || !"v1".equals(parts[0])) {
      return null;
    }

    try {
      return new SelectionRule(
          SubstitutionAction.valueOf(decodeField(parts[1])),
          TargetKind.valueOf(decodeField(parts[2])),
          decodeField(parts[3]),
          decodeField(parts[4]),
          Integer.parseInt(decodeField(parts[5])),
          Integer.parseInt(decodeField(parts[6])),
          Boolean.parseBoolean(decodeField(parts[7])),
          Boolean.parseBoolean(decodeField(parts[8])),
          decodeField(parts[9]),
          decodeField(parts[10]),
          Integer.parseInt(decodeField(parts[11])),
          Integer.parseInt(decodeField(parts[12])),
          Boolean.parseBoolean(decodeField(parts[13])),
          Boolean.parseBoolean(decodeField(parts[14])));
    } catch (RuntimeException exception) {
      return null;
    }
  }

  private static String encodeField(String value) {
    return value == null || value.isEmpty()
        ? ""
        : Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
  }

  private static String decodeField(String value) {
    if (value == null || value.isEmpty()) {
      return "";
    }
    return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
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
      boolean denyMending) {
    public SelectionRule(
        SubstitutionAction action,
        TargetKind targetKind,
        String targetId,
        String requiredToolKind,
        int targetPriority,
        int toolPriority,
        boolean preferSilkTouch,
        boolean preferFortune) {
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
          false);
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
      if (requireMending && denyMending) {
        denyMending = false;
      }
    }
  }
}
