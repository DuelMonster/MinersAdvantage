package uk.co.duelmonster.minersadvantage.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import uk.co.duelmonster.minersadvantage.client.KeyBindings.ClientAction;

/**
 * KeyBindingsTest keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
class KeyBindingsTest {
    @Test
    void exposesLegacyParityDefaultBindings() {
        assertEquals(17, KeyBindings.all().size());
        assertTrue(KeyBindings.all().stream().anyMatch(spec -> spec.action() == ClientAction.CAPTIVATION_TOGGLE && spec.defaultKey().equals("KP_1")));
        assertTrue(KeyBindings.all().stream().anyMatch(spec -> spec.action() == ClientAction.EXCAVATION_SHAPE_NEXT && spec.defaultKey().equals("KP_9")));
        assertTrue(KeyBindings.all().stream().anyMatch(spec -> spec.action() == ClientAction.EXCAVATION_SHAPE_PREV && spec.defaultKey().equals("KP_0")));
        assertTrue(KeyBindings.all().stream().anyMatch(spec -> spec.action() == ClientAction.SHAFTANATION_SHAPE_NEXT && spec.defaultKey().equals("TAB")));
        assertTrue(KeyBindings.all().stream().anyMatch(spec -> spec.action() == ClientAction.SHAFTANATION_SHAPE_PREV && spec.defaultKey().equals("F11")));
        assertTrue(KeyBindings.all().stream().anyMatch(spec -> spec.action() == ClientAction.ABORT_WORKERS && spec.defaultKey().equals("DELETE")));
    }
}
