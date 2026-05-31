package uk.co.duelmonster.minersadvantage.common.services.captivation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * CaptivationCoreServiceTest keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
class CaptivationCoreServiceTest {
    @Test
    /**
     * r es pe ct sw hi te li st an db la ck li st lo gi c exists so this path stays predictable and easier to debug when things get weird.
     */
    void respectsWhitelistAndBlacklistLogic() {
        Set<String> itemSet = Set.of("apple", "diamond");

        CaptivationCoreService whitelistMode = new CaptivationCoreService(itemSet, true, false);
        assertTrue(whitelistMode.canCaptureItem("apple", false));
        assertFalse(whitelistMode.canCaptureItem("gold_ingot", false));

        CaptivationCoreService blacklistMode = new CaptivationCoreService(itemSet, false, false);
        assertFalse(blacklistMode.canCaptureItem("apple", false));
        assertTrue(blacklistMode.canCaptureItem("gold_ingot", false));
    }

    @Test
    /**
     * r es pe ct su nc on di ti on al bl ac kl is to nd ir ec tp ic ku p exists so this path stays predictable and easier to debug when things get weird.
     */
    void respectsUnconditionalBlacklistOnDirectPickup() {
        Set<String> itemSet = Set.of("creeper_head");
        CaptivationCoreService service = new CaptivationCoreService(itemSet, true, true);

        assertFalse(service.canCaptureItem("creeper_head", true));
        assertTrue(service.canCaptureItem("apple", true));
    }

    @Test
    /**
     * c om pu te sa ab bw it hi nr ad ii exists so this path stays predictable and easier to debug when things get weird.
     */
    void computesAabbWithinRadii() {
        CaptivationCoreService service = new CaptivationCoreService(Set.of(), false, false);

        assertTrue(service.isWithinRadius(0, 0, 0, 2, 1, 2, 3, 2));
        assertFalse(service.isWithinRadius(0, 0, 0, 5, 1, 5, 3, 2));
    }

    @Test
    /**
     * e va lu at es ca pt ur ed ec is io nw it hg ui ga te exists so this path stays predictable and easier to debug when things get weird.
     */
    void evaluatesCaptureDecisionWithGuiGate() {
        CaptivationCoreService service = new CaptivationCoreService(Set.of(), false, false);

        CaptivationCoreService.CaptureDecision blocked =
            service.evaluateCapture("apple", false, true, false, true);
        assertFalse(blocked.canCapture());
        assertTrue(blocked.blockedByGui());

        CaptivationCoreService.CaptureDecision allowed =
            service.evaluateCapture("apple", false, false, false, true);
        assertTrue(allowed.canCapture());
        assertFalse(allowed.blockedByGui());
    }
}
