package uk.co.duelmonster.minersadvantage.common.services.utility;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * SupremeVantageService keeps this part of MinersAdvantage running without
 * turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less
 * mysterious at 2 AM.
 */
public final class SupremeVantageService {
  // Parity note: key code recognition, cadence, and staged
  // reward sequencing are ported; item-grant implementation detail parity is
  // tracked in checklist. (future-you will thank present-you).

  public static final String CODE_N = "2780872";
  public static final String CODE_D = "3780873";
  private static final char[] DIGIT_KEYS = { '0', '2', '3', '7', '8' };

  /**
   * ClientState keeps this part of MinersAdvantage running without turning server
   * ticks into confetti.
   * It's here to make the behavior obvious, reliable, and slightly less
   * mysterious at 2 AM.
   */
  public record ClientState(String enteredCode, boolean worthy, int idleTicks, int remainingRewards) {
    public ClientState {
      enteredCode = enteredCode == null ? "" : enteredCode;
      remainingRewards = Math.max(0, remainingRewards);
    }

    public ClientState(String enteredCode, boolean worthy, int idleTicks) {
      this(enteredCode, worthy, idleTicks, worthy ? REWARDS.size() : 0);
    }

    /**
     * defaults exists so this code path does one job clearly instead of spreading
     * chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public static ClientState defaults() {
      return new ClientState("", false, 0, 0);
    }
  }

  /**
   * ClientUpdate keeps this part of MinersAdvantage running without turning
   * server ticks into confetti.
   * It's here to make the behavior obvious, reliable, and slightly less
   * mysterious at 2 AM.
   */
  public record ClientUpdate(ClientState state, boolean notifyWorthy, boolean shouldSendRewardPacket,
      String packetCode) {
  }

  /**
   * RewardGrant keeps this part of MinersAdvantage running without turning server
   * ticks into confetti.
   * It's here to make the behavior obvious, reliable, and slightly less
   * mysterious at 2 AM.
   */
  public record RewardGrant(int sequence, String rewardId, String displayName, String itemId, String code) {
  }

  /**
   * EnchantmentGrant is the teammate that keeps this part of the mod
   * understandable and stable.
   * It exists so behavior stays explicit instead of becoming mystery spaghetti at
   * 2 AM.
   */
  public record EnchantmentGrant(String enchantmentId, int level) {
  }

  /**
   * ItemGrantSpec is the teammate that keeps this part of the mod understandable
   * and stable.
   * It exists so behavior stays explicit instead of becoming mystery spaghetti at
   * 2 AM.
   */
  public record ItemGrantSpec(
      RewardGrant reward,
      int count,
      boolean unbreakable,
      List<EnchantmentGrant> enchantments) {
  }

  private final Map<Long, Integer> rewardProgress = new ConcurrentHashMap<>();

  private static final List<EnchantmentGrant> ENCHANTS_UNBREAKABLE = List.of(
      new EnchantmentGrant("minecraft:unbreaking", 3),
      new EnchantmentGrant("minecraft:mending", 1));

  private static final List<EnchantmentGrant> ENCHANTS_EFFICIENCY_FORTUNE = Stream.concat(
      Stream.of(
          new EnchantmentGrant("minecraft:efficiency", 5),
          new EnchantmentGrant("minecraft:fortune", 4)),
      ENCHANTS_UNBREAKABLE.stream()).toList();

  private static final List<EnchantmentGrant> ENCHANTS_EFFICIENCY_SILK_TOUCH = Stream.concat(
      Stream.of(
          new EnchantmentGrant("minecraft:efficiency", 5),
          new EnchantmentGrant("minecraft:silk_touch", 1)),
      ENCHANTS_UNBREAKABLE.stream()).toList();

  private static final List<EnchantmentGrant> ENCHANTS_FIRE_PROTECTION = Stream.concat(
      Stream.of(
          new EnchantmentGrant("minecraft:protection", 4),
          new EnchantmentGrant("minecraft:fire_protection", 4)),
      ENCHANTS_UNBREAKABLE.stream()).toList();

  private static final List<EnchantmentGrant> ENCHANTS_SWORD_GRANTS = Stream.concat(
      Stream.of(
          new EnchantmentGrant("minecraft:sharpness", 5),
          new EnchantmentGrant("minecraft:looting", 4)),
      ENCHANTS_UNBREAKABLE.stream()).toList();

  private static final List<EnchantmentGrant> ENCHANTS_SWORD_FIRE = List.of(
      new EnchantmentGrant("minecraft:sharpness", 5),
      new EnchantmentGrant("minecraft:looting", 4),
      new EnchantmentGrant("minecraft:fire_aspect", 2),
      new EnchantmentGrant("minecraft:unbreaking", 3),
      new EnchantmentGrant("minecraft:mending", 1));

  private static final List<EnchantmentGrant> ENCHANTS_FIRE_REGEN = List.of(
      new EnchantmentGrant("minecraft:protection", 4),
      new EnchantmentGrant("minecraft:fire_protection", 4),
      new EnchantmentGrant("minecraft:respiration", 3),
      new EnchantmentGrant("minecraft:aqua_affinity", 1));

  private static final List<EnchantmentGrant> ENCHANTS_FIRE_SWIFT = List.of(
      new EnchantmentGrant("minecraft:protection", 4),
      new EnchantmentGrant("minecraft:fire_protection", 4),
      new EnchantmentGrant("minecraft:swift_sneak", 3));

  private static final List<EnchantmentGrant> ENCHANTS_FIRE_WALK = List.of(
      new EnchantmentGrant("minecraft:protection", 4),
      new EnchantmentGrant("minecraft:fire_protection", 4),
      new EnchantmentGrant("minecraft:feather_falling", 4),
      new EnchantmentGrant("minecraft:depth_strider", 3),
      new EnchantmentGrant("minecraft:soul_speed", 3));

  private static final List<EnchantmentGrant> ENCHANTS_BOW = List.of(
      new EnchantmentGrant("minecraft:power", 10),
      new EnchantmentGrant("minecraft:flame", 1),
      new EnchantmentGrant("minecraft:infinity", 1),
      new EnchantmentGrant("minecraft:unbreaking", 3),
      new EnchantmentGrant("minecraft:mending", 1));

  private static final List<EnchantmentGrant> ENCHANTS_CROSSBOW = List.of(
      new EnchantmentGrant("minecraft:multishot", 1),
      new EnchantmentGrant("minecraft:piercing", 5),
      new EnchantmentGrant("minecraft:quick_charge", 5),
      new EnchantmentGrant("minecraft:unbreaking", 3),
      new EnchantmentGrant("minecraft:mending", 1));

  private static final List<EnchantmentGrant> ENCHANTS_TRIDENT_RIPTIDE = List.of(
      new EnchantmentGrant("minecraft:riptide", 10),
      new EnchantmentGrant("minecraft:impaling", 10),
      new EnchantmentGrant("minecraft:unbreaking", 3),
      new EnchantmentGrant("minecraft:mending", 1));

  private static final List<EnchantmentGrant> ENCHANTS_TRIDENT_LOYALTY = List.of(
      new EnchantmentGrant("minecraft:loyalty", 3),
      new EnchantmentGrant("minecraft:impaling", 5),
      new EnchantmentGrant("minecraft:channeling", 1),
      new EnchantmentGrant("minecraft:unbreaking", 3),
      new EnchantmentGrant("minecraft:mending", 1));

  private static final List<EnchantmentGrant> ENCHANTS_FISHING_ROD = List.of(
      new EnchantmentGrant("minecraft:lure", 4),
      new EnchantmentGrant("minecraft:luck_of_the_sea", 5),
      new EnchantmentGrant("minecraft:unbreaking", 3),
      new EnchantmentGrant("minecraft:mending", 1));

  private static final List<RewardGrant> REWARDS = List.of(
      new RewardGrant(1, "soulblade", "Soulblade", "minecraft:diamond_sword", CODE_D),
      new RewardGrant(2, "peacekeeper", "Peacekeeper", "minecraft:diamond_sword", CODE_D),
      new RewardGrant(3, "minora", "Minora", "minecraft:diamond_pickaxe", CODE_D),
      new RewardGrant(4, "silkar", "Silkar", "minecraft:diamond_pickaxe", CODE_D),
      // new RewardGrant(5, "diggle", "Diggle", "minecraft:diamond_shovel", CODE_D),
      new RewardGrant(5, "scoop", "Scoop, there it is...", "minecraft:diamond_shovel", CODE_D),
      new RewardGrant(6, "whirlwind", "Whirlwind", "minecraft:diamond_axe", CODE_D),
      new RewardGrant(7, "scuba_helm", "Tadpols Scuba Helm", "minecraft:diamond_helmet", CODE_D),
      new RewardGrant(8, "black_bones", "Breastplate of Black Bones", "minecraft:diamond_chestplate", CODE_D),
      new RewardGrant(9, "pegasus_wings", "Pegasus' Wings", "minecraft:elytra", CODE_D),
      new RewardGrant(10, "legplates", "Wizadora's Legplates", "minecraft:diamond_leggings", CODE_D),
      new RewardGrant(11, "victims_souls", "Victims Souls", "minecraft:diamond_boots", CODE_D),
      new RewardGrant(12, "firestarter", "Firestarter", "minecraft:bow", CODE_D),
      new RewardGrant(13, "firestarter_ammo", "Firestarter Ammo", "minecraft:arrow", CODE_D),
      new RewardGrant(17, "ten_dolla", "Ten Dolla", "minecraft:diamond_hoe", CODE_D),
      new RewardGrant(18, "shawn", "Shawn", "minecraft:shears", CODE_D),
      new RewardGrant(19, "pyro", "Pyro", "minecraft:flint_and_steel", CODE_D),
      new RewardGrant(20, "poseidons_stash", "Raider of Poseidons Stash", "minecraft:fishing_rod", CODE_D)
  // new RewardGrant(14, "penetrator", "Penertrator", "minecraft:crossbow", CODE_D),
  // new RewardGrant(15, "rocket", "Poseidons Rocket", "minecraft:trident", CODE_D),
  // new RewardGrant(16, "fork", "Poseidons Fork", "minecraft:trident", CODE_D),
  // new RewardGrant(21, "rodney", "Rodney", "minecraft:fishing_rod", CODE_D)
  );

  /**
   * processClientTick exists so this code path does one job clearly instead of
   * spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public ClientUpdate processClientTick(ClientState state, Set<Character> pressedDigits, boolean excavationToggled,
      boolean allFeaturesEnabled) {
    if (!allFeaturesEnabled) {
      return new ClientUpdate(ClientState.defaults(), false, false, "");
    }

    String code = state.enteredCode();
    if (excavationToggled) {
      for (char digit : DIGIT_KEYS) {
        if (pressedDigits.contains(digit) && (code.isEmpty() || code.charAt(code.length() - 1) != digit)) {
          code += digit;
        }
      }
      return new ClientUpdate(new ClientState(code, state.worthy(), 0, state.remainingRewards()), false, false,
          "");
    }

    if (!state.worthy() && isRecognizedCode(code)) {
      return new ClientUpdate(new ClientState(code, true, 0, REWARDS.size()), true, false, "");
    }

    if (state.worthy()) {
      int idleTicks = state.idleTicks() + 1;
      boolean shouldSend = idleTicks >= 5 && state.remainingRewards() > 0;
      ClientState nextState;
      if (shouldSend) {
        int remaining = state.remainingRewards() - 1;
        nextState = remaining > 0 ? new ClientState(code, true, 0, remaining) : ClientState.defaults();
      } else {
        nextState = new ClientState(code, true, idleTicks, state.remainingRewards());
      }
      return new ClientUpdate(nextState, false, shouldSend, shouldSend ? code : "");
    }

    int idleTicks = state.idleTicks() + 1;
    if ((!code.isEmpty() && idleTicks > 5) || idleTicks >= 1000) {
      return new ClientUpdate(ClientState.defaults(), false, false, "");
    }
    return new ClientUpdate(new ClientState(code, false, idleTicks), false, false, "");
  }

  /**
   * grantNextReward exists so this code path does one job clearly instead of
   * spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public RewardGrant grantNextReward(long playerId, String code) {
    if (!isRecognizedCode(code)) {
      return null;
    }

    int index = rewardProgress.getOrDefault(playerId, 0);
    if (index >= REWARDS.size()) {
      // Treat a new valid code run after completion as a full sequence restart.
      index = 0;
    }

    RewardGrant base = REWARDS.get(index);
    rewardProgress.put(playerId, index + 1);
    return applyRiteVariant(base, code);
  }

  /**
   * applyRiteVariant exists to keep this step focused, predictable, and
   * debuggable.
   * In short: one clear job here beats ten confusing side-effects elsewhere.
   */
  private RewardGrant applyRiteVariant(RewardGrant base, String code) {
    if (!CODE_N.equals(code)) {
      return new RewardGrant(base.sequence(), base.rewardId(), base.displayName(), base.itemId(), code);
    }

    String itemId = switch (base.sequence()) {
      case 1, 2 -> "minecraft:netherite_sword";
      case 3, 4 -> "minecraft:netherite_pickaxe";
      case 5 -> "minecraft:netherite_shovel";
      case 6 -> "minecraft:netherite_axe";
      case 7 -> "minecraft:netherite_helmet";
      case 8 -> "minecraft:netherite_chestplate";
      case 10 -> "minecraft:netherite_leggings";
      case 11 -> "minecraft:netherite_boots";
      case 17 -> "minecraft:netherite_hoe";
      default -> base.itemId();
    };

    return new RewardGrant(base.sequence(), base.rewardId(), base.displayName(), itemId, code);
  }

  /**
   * materializeRewardSpec exists to keep this step focused, predictable, and
   * debuggable.
   * In short: one clear job here beats ten confusing side-effects elsewhere.
   */
  public ItemGrantSpec materializeRewardSpec(RewardGrant reward) {
    if (reward == null) {
      return null;
    }

    List<EnchantmentGrant> enchantments = switch (reward.sequence()) {
      case 1 -> ENCHANTS_SWORD_FIRE;
      case 2 -> ENCHANTS_SWORD_GRANTS;
      case 3 -> ENCHANTS_EFFICIENCY_FORTUNE;
      case 4 -> ENCHANTS_EFFICIENCY_SILK_TOUCH;
      case 5, 6 -> ENCHANTS_EFFICIENCY_FORTUNE;
      case 7 -> ENCHANTS_FIRE_REGEN;
      case 8 -> ENCHANTS_FIRE_PROTECTION;
      case 9 -> ENCHANTS_UNBREAKABLE;
      case 10 -> ENCHANTS_FIRE_SWIFT;
      case 11 -> ENCHANTS_FIRE_WALK;
      case 12 -> ENCHANTS_BOW;
      case 14 -> ENCHANTS_CROSSBOW;
      case 15 -> ENCHANTS_TRIDENT_RIPTIDE;
      case 16 -> ENCHANTS_TRIDENT_LOYALTY;
      case 17, 18, 19 -> ENCHANTS_UNBREAKABLE;
      case 20 -> ENCHANTS_FISHING_ROD;
      default -> List.of();
    };

    int count = 1;
    boolean unbreakable = false;
    // boolean unbreakable = switch (reward.sequence()) {
    // case 13 -> false;
    // default -> true;
    // };

    return new ItemGrantSpec(reward, count, unbreakable, enchantments);
  }

  /**
   * isRecognizedCode exists so this code path does one job clearly instead of
   * spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public boolean isRecognizedCode(String code) {
    return CODE_N.equals(code) || CODE_D.equals(code);
  }
}
