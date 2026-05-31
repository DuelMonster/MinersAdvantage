package uk.co.duelmonster.minersadvantage.common.config.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import uk.co.duelmonster.minersadvantage.common.config.SyncedClientConfig;
import uk.co.duelmonster.minersadvantage.common.services.utility.TorchPlacement;

/**
 * Tests for TOML config persistence, clamping, and fallback behavior.
 * In short: prove config I/O is boring and predictable, which is exactly what we want.
 */
class MATomlConfigStoreTest {
    @TempDir
    Path tempDir;

    /**
     * Round-trip test: save config, load config, and ensure key values survive the journey unchanged.
     */
    @Test
    /**
     * s av et he nl oa dr ou nd tr ip sc or ev al ue s exists so this path stays predictable and easier to debug when things get weird.
     */
    void saveThenLoadRoundTripsCoreValues() {
        SyncedClientConfig saved = new SyncedClientConfig(
            SyncedClientConfig.defaults().client(),
            SyncedClientConfig.defaults().common(),
            SyncedClientConfig.defaults().captivation(),
            SyncedClientConfig.defaults().cropination(),
            SyncedClientConfig.defaults().cultivation(),
            SyncedClientConfig.defaults().excavation(),
            SyncedClientConfig.defaults().pathanation(),
            SyncedClientConfig.defaults().illumination(),
            SyncedClientConfig.defaults().lumbination(),
            new uk.co.duelmonster.minersadvantage.common.config.ShaftanationConfig(true, 24, 6, 2, 3, TorchPlacement.BOTH_WALLS),
            SyncedClientConfig.defaults().substitution(),
            new uk.co.duelmonster.minersadvantage.common.config.VeinationConfig(true, 7, java.util.List.of("minecraft:diamond_ore"), true, true, true, 0.6D, java.util.List.of("minecraft:wooden_pickaxe")),
            SyncedClientConfig.defaults().ventilation()
        );

        MATomlConfigStore.save(tempDir, saved);
        SyncedClientConfig loaded = MATomlConfigStore.load(tempDir, SyncedClientConfig.defaults());

        assertEquals(24, loaded.shaftanation().depth());
        assertEquals(6, loaded.shaftanation().processesPerTick());
        assertEquals(2, loaded.shaftanation().width());
        assertEquals(3, loaded.shaftanation().height());
        assertEquals(TorchPlacement.BOTH_WALLS, loaded.shaftanation().torchPlacement());
        assertEquals(7, loaded.veination().maxVeinDistance());
        assertEquals(0.6D, loaded.veination().increasedHarvestingTimePerOreModifier());
        assertEquals(java.util.List.of("minecraft:diamond_ore"), loaded.veination().ores());
        assertEquals(java.util.List.of("minecraft:wooden_pickaxe"), loaded.veination().pickaxeBlacklist());
    }

    /**
     * Invalid values should clamp or fall back safely, then persist back out in sane normalized form.
     */
    @Test
    void loadClampsOrFallsBackForInvalidValues() throws Exception {
        Files.writeString(
            tempDir.resolve("server-config.toml"),
            """
            veination.max_vein_distance = -100
            veination.harvest_time_modifier = 999
            shaftanation.torch_placement = \"invalid_mode\"
            """,
            StandardCharsets.UTF_8
        );
        Files.writeString(
            tempDir.resolve("client-config.toml"),
            "disable_particle_effects = maybe",
            StandardCharsets.UTF_8
        );

        SyncedClientConfig loaded = MATomlConfigStore.load(tempDir, SyncedClientConfig.defaults());

        assertEquals(1, loaded.veination().maxVeinDistance());
        assertEquals(10.0D, loaded.veination().increasedHarvestingTimePerOreModifier());
        assertEquals(TorchPlacement.FLOOR, loaded.shaftanation().torchPlacement());
        assertFalse(loaded.client().disableParticleEffects());

        MATomlConfigStore.save(tempDir, loaded);
        String writtenServer = Files.readString(tempDir.resolve("server-config.toml"), StandardCharsets.UTF_8);
        String writtenClient = Files.readString(tempDir.resolve("client-config.toml"), StandardCharsets.UTF_8);
        // After clamping, we expect persisted values to reflect the corrected state, not the original nonsense input.
        assertTrue(writtenServer.contains("veination.max_vein_distance = 1"));
        assertTrue(writtenServer.contains("veination.harvest_time_modifier = 10.0"));
        assertTrue(writtenClient.contains("disable_particle_effects = false"));
    }
}
