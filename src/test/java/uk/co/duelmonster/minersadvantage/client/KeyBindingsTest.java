package uk.co.duelmonster.minersadvantage.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import uk.co.duelmonster.minersadvantage.client.KeyBindings.ClientAction;

class KeyBindingsTest {
    @Test
    void exposesLegacyParityDefaultBindings() {
        assertEquals(14, KeyBindings.all().size());
        assertTrue(KeyBindings.all().stream().anyMatch(spec -> spec.action() == ClientAction.CAPTIVATION_TOGGLE && spec.defaultKey().equals("KP_1")));
        assertTrue(KeyBindings.all().stream().anyMatch(spec -> spec.action() == ClientAction.ABORT_WORKERS && spec.defaultKey().equals("DELETE")));
    }
}
