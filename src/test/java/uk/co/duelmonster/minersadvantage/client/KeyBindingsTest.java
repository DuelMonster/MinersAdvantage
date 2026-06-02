package uk.co.duelmonster.minersadvantage.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import uk.co.duelmonster.minersadvantage.client.KeyBindings.ClientAction;

/**
 * KeyBindingsTest keeps this part of MinersAdvantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
class KeyBindingsTest {
    @Test
    /**
     * e xp os es le ga cy pa ri ty de fa ul tb in di ng s exists so this path stays predictable and easier to debug when things get weird.
     */
    void exposesLegacyParityDefaultBindings() {
        assertEquals(13, KeyBindings.all().size());
        assertTrue(KeyBindings.all().stream().anyMatch(spec -> spec.action() == ClientAction.CAPTIVATION_TOGGLE && spec.defaultKey().equals("KP_1")));
        assertTrue(KeyBindings.all().stream().anyMatch(spec -> spec.action() == ClientAction.ABORT_WORKERS && spec.defaultKey().equals("DELETE")));
    }

    @Test
    void exposesTranslationsForEveryRegisteredKeybinding() throws IOException {
        try (InputStream inputStream = KeyBindingsTest.class.getResourceAsStream("/assets/minersadvantage/lang/en_us.json")) {
            assertTrue(inputStream != null, "Missing en_us language resource");
            String translations = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            for (KeyBindings.KeyBindingSpec spec : KeyBindings.all()) {
                String translationKey = "\"key.minersadvantage." + spec.action().name().toLowerCase() + "\"";
                assertTrue(translations.contains(translationKey), () -> "Missing translation entry for " + spec.action());
            }

            assertTrue(translations.contains("\"key.category.minersadvantage.keybinds\""));
        }
    }
}
