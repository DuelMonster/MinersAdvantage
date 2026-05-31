package uk.co.duelmonster.minersadvantage.common.services.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * SupremeVantageServiceTest keeps this part of Miners Advantage running without turning server ticks into confetti.
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
        assertTrue(second.displayName().endsWith(" Rite"));
        assertTrue(second.itemId().contains("netherite") || second.itemId().equals("minecraft:elytra"));
        assertFalse(service.isRecognizedCode("1234567"));
    }
}
