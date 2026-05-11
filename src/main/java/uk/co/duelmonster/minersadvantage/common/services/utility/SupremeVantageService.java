package uk.co.duelmonster.minersadvantage.common.services.utility;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * SupremeVantageService keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class SupremeVantageService {
    public static final String CODE_N = "2780872";
    public static final String CODE_D = "3780873";

    /**
     * ClientState keeps this part of Miners Advantage running without turning server ticks into confetti.
     * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
     */
    public record ClientState(String enteredCode, boolean worthy, int idleTicks) {
        public ClientState {
            enteredCode = enteredCode == null ? "" : enteredCode;
        }

        /**
         * defaults exists so this code path does one job clearly instead of spreading chaos across callers.
         * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
         */
        public static ClientState defaults() {
            return new ClientState("", false, 0);
        }
    }

    /**
     * ClientUpdate keeps this part of Miners Advantage running without turning server ticks into confetti.
     * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
     */
    public record ClientUpdate(ClientState state, boolean notifyWorthy, boolean shouldSendRewardPacket, String packetCode) {}

    /**
     * RewardGrant keeps this part of Miners Advantage running without turning server ticks into confetti.
     * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
     */
    public record RewardGrant(int sequence, String rewardId, String displayName, String itemId, String code) {}

    private final Map<Long, Integer> rewardProgress = new HashMap<>();
    private static final List<RewardGrant> REWARDS = List.of(
        new RewardGrant(1, "soulblade", "Soulblade", "minecraft:diamond_sword", CODE_D),
        new RewardGrant(2, "peacekeeper", "Peacekeeper", "minecraft:diamond_sword", CODE_D),
        new RewardGrant(3, "minora", "Minora", "minecraft:diamond_pickaxe", CODE_D),
        new RewardGrant(4, "silkar", "Silkar", "minecraft:diamond_pickaxe", CODE_D),
        new RewardGrant(5, "diggle", "Diggle", "minecraft:diamond_shovel", CODE_D),
        new RewardGrant(6, "whirlwind", "Whirlwind", "minecraft:diamond_axe", CODE_D),
        new RewardGrant(7, "scuba_helm", "Tadpols Scuba Helm", "minecraft:diamond_helmet", CODE_D),
        new RewardGrant(8, "black_bones", "Breastplate of Black Bones", "minecraft:diamond_chestplate", CODE_D),
        new RewardGrant(9, "pegasus_wings", "Pegasus' Wings", "minecraft:elytra", CODE_D),
        new RewardGrant(10, "legplates", "Wizadora's Legplates", "minecraft:diamond_leggings", CODE_D),
        new RewardGrant(11, "victims_souls", "Victims Souls", "minecraft:diamond_boots", CODE_D),
        new RewardGrant(12, "firestarter", "Firestarter", "minecraft:bow", CODE_D),
        new RewardGrant(13, "firestarter_ammo", "Firestarter Ammo", "minecraft:arrow", CODE_D),
        new RewardGrant(14, "penetrator", "Penertrator", "minecraft:crossbow", CODE_D),
        new RewardGrant(15, "rocket", "Poseidons Rocket", "minecraft:trident", CODE_D),
        new RewardGrant(16, "fork", "Poseidons Fork", "minecraft:trident", CODE_D),
        new RewardGrant(17, "ten_dolla", "Ten Dolla", "minecraft:diamond_hoe", CODE_D),
        new RewardGrant(18, "shawn", "Shawn", "minecraft:shears", CODE_D),
        new RewardGrant(19, "pyro", "Pyro", "minecraft:flint_and_steel", CODE_D),
        new RewardGrant(20, "poseidons_stash", "Raider of Poseidons Stash", "minecraft:fishing_rod", CODE_D),
        new RewardGrant(21, "rodney", "Rodney", "minecraft:fishing_rod", CODE_D)
    );

    /**
     * processClientTick exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public ClientUpdate processClientTick(ClientState state, Set<Character> pressedDigits, boolean excavationToggled, boolean allFeaturesEnabled) {
        if (!allFeaturesEnabled) {
            return new ClientUpdate(ClientState.defaults(), false, false, "");
        }

        String code = state.enteredCode();
        if (excavationToggled) {
            for (char digit : List.of('0', '2', '7', '8')) {
                if (pressedDigits.contains(digit) && (code.isEmpty() || code.charAt(code.length() - 1) != digit)) {
                    code += digit;
                }
            }
            return new ClientUpdate(new ClientState(code, state.worthy(), 0), false, false, "");
        }

        if (!state.worthy() && isRecognizedCode(code)) {
            return new ClientUpdate(new ClientState(code, true, 0), true, false, "");
        }

        if (state.worthy()) {
            int idleTicks = state.idleTicks() + 1;
            boolean shouldSend = idleTicks >= 5;
            return new ClientUpdate(new ClientState(code, true, shouldSend ? 0 : idleTicks), false, shouldSend, shouldSend ? code : "");
        }

        int idleTicks = state.idleTicks() + 1;
        if ((!code.isEmpty() && idleTicks > 5) || idleTicks >= 1000) {
            return new ClientUpdate(ClientState.defaults(), false, false, "");
        }
        return new ClientUpdate(new ClientState(code, false, idleTicks), false, false, "");
    }

    /**
     * grantNextReward exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public RewardGrant grantNextReward(long playerId, String code) {
        if (!isRecognizedCode(code)) {
            return null;
        }

        int index = rewardProgress.getOrDefault(playerId, 0);
        RewardGrant base = REWARDS.get(index);
        rewardProgress.put(playerId, (index + 1) % REWARDS.size());
        if (CODE_N.equals(code)) {
            return new RewardGrant(base.sequence(), base.rewardId(), base.displayName() + " Rite", base.itemId().replace("diamond", "netherite"), code);
        }
        return base;
    }

    /**
     * isRecognizedCode exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public boolean isRecognizedCode(String code) {
        return CODE_N.equals(code) || CODE_D.equals(code);
    }
}

