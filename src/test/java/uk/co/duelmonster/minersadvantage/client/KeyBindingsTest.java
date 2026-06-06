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
     * Verify feature-toggle specs stay unbound by default while preserving config-comment translations.
     */
    void exposesFeatureToggleSpecsWithoutDefaultBindings() {
        var featureToggleSpecs = KeyBindings.all().stream()
            .filter(spec -> spec.defaultKey() == null)
            .toList();

        assertEquals(13, KeyBindings.all().size());
        assertEquals(8, featureToggleSpecs.size());
        assertTrue(featureToggleSpecs.stream().anyMatch(spec -> spec.action() == ClientAction.CAPTIVATION_TOGGLE));
        assertTrue(featureToggleSpecs.stream().allMatch(spec -> spec.translationKey().endsWith(".enabled.comment")));
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
