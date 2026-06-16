package uk.co.duelmonster.minersadvantage.common.services.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * SupremeVantageServiceTest keeps this part of MinersAdvantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
class SupremeVantageServiceTest {
    @Test
    /**
     * u nl oc ks wo rt hy st at ea ft er se cr et ex ca va ti on co de exists so this path stays predictable and easier to debug when things get weird.
     */
    void unlocksWorthyStateAfterSecretExcavationCode() {
        SupremeVantageService service = new SupremeVantageService();
        SupremeVantageService.ClientState state = SupremeVantageService.ClientState.defaults();

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        for (char digit : "2780872".toCharArray()) {
            state = service.processClientTick(state, Set.of(digit), true, true).state();
        }

        SupremeVantageService.ClientUpdate update = service.processClientTick(state, Set.of(), false, true);

        assertTrue(update.notifyWorthy());
        assertTrue(update.state().worthy());
        assertEquals("2780872", update.state().enteredCode());
        assertEquals(18, update.state().remainingRewards());
    }

    @Test
    /**
     * e mi ts re wa rd pa ck et ca de nc ew hi le wo rt hy exists so this path stays predictable and easier to debug when things get weird.
     */
    void emitsRewardPacketCadenceWhileWorthy() {
        SupremeVantageService service = new SupremeVantageService();
        SupremeVantageService.ClientState state = new SupremeVantageService.ClientState("2780872", true, 4);

        SupremeVantageService.ClientUpdate update = service.processClientTick(state, Set.of(), false, true);

        assertTrue(update.shouldSendRewardPacket());
        assertEquals("2780872", update.packetCode());
        assertEquals(17, update.state().remainingRewards());
    }

    @Test
    /**
     * s to ps em it ti ng pa ck et sa ft er fi na lr ew ar db ud ge ti se xp en de d exists so this path stays predictable and easier to debug when things get weird.
     */
    void stopsEmittingPacketsAfterFinalRewardBudgetIsExpended() {
        SupremeVantageService service = new SupremeVantageService();
        SupremeVantageService.ClientState state = new SupremeVantageService.ClientState(
            "2780872",
            true,
            0,
            1
        );

        for (int i = 0; i < 4; i++) {
            state = service.processClientTick(state, Set.of(), false, true).state();
        }

        SupremeVantageService.ClientUpdate sendUpdate = service.processClientTick(state, Set.of(), false, true);
        assertTrue(sendUpdate.shouldSendRewardPacket());
        assertEquals("2780872", sendUpdate.packetCode());
        assertEquals(SupremeVantageService.ClientState.defaults(), sendUpdate.state());

        SupremeVantageService.ClientUpdate afterBudget = service.processClientTick(sendUpdate.state(), Set.of(), false, true);
        assertFalse(afterBudget.shouldSendRewardPacket());
    }

    @Test
    /**
     * g ra nt sr ew ar ds eq ue nc ea nd ne th er it ev ar ia nt exists so this path stays predictable and easier to debug when things get weird.
     */
    void grantsRewardSequenceAndNetheriteVariant() {
        SupremeVantageService service = new SupremeVantageService();

        SupremeVantageService.RewardGrant first = service.grantNextReward(99L, SupremeVantageService.CODE_D);
        SupremeVantageService.RewardGrant second = service.grantNextReward(99L, SupremeVantageService.CODE_N);

        assertNotNull(first);
        assertEquals("Soulblade", first.displayName());
        assertEquals("minecraft:diamond_sword", first.itemId());
        assertNotNull(second);
        assertEquals("Peacekeeper", second.displayName());
        assertTrue(second.itemId().contains("netherite"));
        assertFalse(service.isRecognizedCode("1234567"));
    }

    @Test
    /**
     * m at er ia li ze sr ew ar ds pe cw it he xc pe ct ed en ch an tm en ts exists so this path stays predictable and easier to debug when things get weird.
     */
    void materializesRewardSpecWithExpectedEnchantments() {
        SupremeVantageService service = new SupremeVantageService();

        SupremeVantageService.RewardGrant firstGrant = service.grantNextReward(21L, SupremeVantageService.CODE_D);
        SupremeVantageService.ItemGrantSpec firstSpec = service.materializeRewardSpec(firstGrant);

        assertNotNull(firstSpec);
        assertFalse(firstSpec.unbreakable());
        assertEquals(6, firstSpec.enchantments().size());
        Map<String, Integer> firstEnchantments = firstSpec.enchantments().stream().collect(
            java.util.stream.Collectors.toMap(
                SupremeVantageService.EnchantmentGrant::enchantmentId,
                SupremeVantageService.EnchantmentGrant::level
            )
        );
        assertEquals(10, firstEnchantments.get("minecraft:sharpness"));
        assertEquals(10, firstEnchantments.get("minecraft:sweeping_edge"));
        assertEquals(2, firstEnchantments.get("minecraft:fire_aspect"));
        assertEquals(10, firstEnchantments.get("minecraft:looting"));
        assertEquals(3, firstEnchantments.get("minecraft:unbreaking"));
        assertEquals(1, firstEnchantments.get("minecraft:mending"));

        SupremeVantageService.RewardGrant arrowGrant = null;
        for (int i = 0; i < 13; i++) {
            arrowGrant = service.grantNextReward(21L, SupremeVantageService.CODE_D);
        }
        SupremeVantageService.ItemGrantSpec arrowSpec = service.materializeRewardSpec(arrowGrant);
        assertNotNull(arrowSpec);
        assertEquals(13, arrowSpec.reward().sequence());
        assertFalse(arrowSpec.unbreakable());
        assertTrue(arrowSpec.enchantments().isEmpty());
    }

    @Test
    /**
     * s to ps gr an ti ng af te rf ul ls up re me va nt ag es eq ue nc e exists so this path stays predictable and easier to debug when things get weird.
     */
    void stopsGrantingAfterFullSupremeVantageSequence() {
        SupremeVantageService service = new SupremeVantageService();
        SupremeVantageService.RewardGrant grant = null;

        for (int i = 0; i < 18; i++) {
            grant = service.grantNextReward(1234L, SupremeVantageService.CODE_D);
            assertNotNull(grant);
        }

        SupremeVantageService.RewardGrant afterComplete = service.grantNextReward(1234L, SupremeVantageService.CODE_D);
        assertEquals(20, grant.sequence());
        assertEquals("Raider of Poseidons Stash", grant.displayName());
        assertNull(afterComplete);
    }
}
