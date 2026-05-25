package uk.co.duelmonster.minersadvantage.common.config.storage;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import uk.co.duelmonster.minersadvantage.common.config.CaptivationConfig;
import uk.co.duelmonster.minersadvantage.common.config.ClientConfig;
import uk.co.duelmonster.minersadvantage.common.config.CommonConfig;
import uk.co.duelmonster.minersadvantage.common.config.CropinationConfig;
import uk.co.duelmonster.minersadvantage.common.config.CultivationConfig;
import uk.co.duelmonster.minersadvantage.common.config.ExcavationConfig;
import uk.co.duelmonster.minersadvantage.common.config.IlluminationConfig;
import uk.co.duelmonster.minersadvantage.common.config.LumbinationConfig;
import uk.co.duelmonster.minersadvantage.common.config.PathanationConfig;
import uk.co.duelmonster.minersadvantage.common.config.ShaftanationConfig;
import uk.co.duelmonster.minersadvantage.common.config.SubstitutionConfig;
import uk.co.duelmonster.minersadvantage.common.config.SyncedClientConfig;
import uk.co.duelmonster.minersadvantage.common.config.VeinationConfig;
import uk.co.duelmonster.minersadvantage.common.config.VentilationConfig;
import uk.co.duelmonster.minersadvantage.common.services.utility.TorchPlacement;

/**
 * Stores MinersAdvantage config in split TOML files.
 */
public final class MATomlConfigStore {
    private static final String CLIENT_FILE_NAME = "client-config.toml";
    private static final String SERVER_FILE_NAME = "server-config.toml";

    private MATomlConfigStore() {}

    public static SyncedClientConfig load(Path configDir, SyncedClientConfig defaults) {
        SyncedClientConfig fallback = defaults == null ? SyncedClientConfig.defaults() : defaults;
        Path clientFile = configDir.resolve(CLIENT_FILE_NAME);
        Path serverFile = configDir.resolve(SERVER_FILE_NAME);

        Map<String, String> clientValues = readToml(clientFile);
        Map<String, String> serverValues = readToml(serverFile);

        return new SyncedClientConfig(
            new ClientConfig(
                boolValue(clientValues, "disable_particle_effects", fallback.client().disableParticleEffects())
            ),
            new CommonConfig(
                boolValue(serverValues, "common.tps_guard", fallback.common().tpsGuard()),
                boolValue(serverValues, "common.gather_drops", fallback.common().gatherDrops()),
                boolValue(serverValues, "common.auto_illuminate", fallback.common().autoIlluminate()),
                boolValue(serverValues, "common.mine_veins", fallback.common().mineVeins()),
                intValue(serverValues, "common.blocks_per_tick", fallback.common().blocksPerTick(), 1, 1024),
                boolValue(serverValues, "common.enable_tick_delay", fallback.common().enableTickDelay()),
                intValue(serverValues, "common.tick_delay", fallback.common().tickDelay(), 0, 200),
                intValue(serverValues, "common.block_radius", fallback.common().blockRadius(), 1, 128),
                intValue(serverValues, "common.block_limit", fallback.common().blockLimit(), 1, 8192)
            ),
            new CaptivationConfig(
                boolValue(serverValues, "captivation.enabled", fallback.captivation().enabled()),
                boolValue(serverValues, "captivation.allow_in_gui", fallback.captivation().allowInGUI()),
                intValue(serverValues, "captivation.radius_horizontal", fallback.captivation().radiusHorizontal(), 1, 128),
                intValue(serverValues, "captivation.radius_vertical", fallback.captivation().radiusVertical(), 1, 128),
                boolValue(serverValues, "captivation.is_whitelist", fallback.captivation().isWhitelist()),
                boolValue(serverValues, "captivation.unconditional_blacklist", fallback.captivation().unconditionalBlacklist()),
                stringListValue(serverValues, "captivation.blacklist", fallback.captivation().blacklist())
            ),
            new CropinationConfig(
                boolValue(serverValues, "cropination.enabled", fallback.cropination().enabled()),
                boolValue(serverValues, "cropination.harvest_seeds", fallback.cropination().harvestSeeds())
            ),
            new CultivationConfig(
                boolValue(serverValues, "cultivation.enabled", fallback.cultivation().enabled()),
                intValue(serverValues, "cultivation.hydration_distance", fallback.cultivation().hydrationDistance(), 1, 16)
            ),
            new ExcavationConfig(
                boolValue(serverValues, "excavation.enabled", fallback.excavation().enabled()),
                intValue(serverValues, "excavation.radius_horizontal", fallback.excavation().radiusHorizontal(), 1, 64),
                intValue(serverValues, "excavation.radius_vertical", fallback.excavation().radiusVertical(), 1, 64),
                intValue(serverValues, "excavation.processes_per_tick", fallback.excavation().processesPerTick(), 1, 512),
                boolValue(serverValues, "excavation.toggle_mode", fallback.excavation().toggleMode()),
                boolValue(serverValues, "excavation.ignore_block_variants", fallback.excavation().ignoreBlockVariants()),
                boolValue(serverValues, "excavation.is_block_whitelist", fallback.excavation().isBlockWhitelist()),
                stringListValue(serverValues, "excavation.block_blacklist", fallback.excavation().blockBlacklist())
            ),
            new PathanationConfig(
                boolValue(serverValues, "pathanation.enabled", fallback.pathanation().enabled()),
                intValue(serverValues, "pathanation.path_length", fallback.pathanation().pathLength(), 1, 64),
                intValue(serverValues, "pathanation.path_width", fallback.pathanation().pathWidth(), 1, 9)
            ),
            new IlluminationConfig(
                boolValue(serverValues, "illumination.enabled", fallback.illumination().enabled()),
                intValue(serverValues, "illumination.radius_horizontal", fallback.illumination().radiusHorizontal(), 1, 64),
                intValue(serverValues, "illumination.radius_vertical", fallback.illumination().radiusVertical(), 1, 64),
                intValue(serverValues, "illumination.lowest_light_level", fallback.illumination().lowestLightLevel(), 0, 15),
                boolValue(serverValues, "illumination.use_block_light", fallback.illumination().useBlockLight())
            ),
            new LumbinationConfig(
                boolValue(serverValues, "lumbination.enabled", fallback.lumbination().enabled()),
                intValue(serverValues, "lumbination.max_trunk_range", fallback.lumbination().maxTrunkRange(), 1, 128),
                intValue(serverValues, "lumbination.max_leaf_range", fallback.lumbination().maxLeafRange(), 1, 32),
                intValue(serverValues, "lumbination.processes_per_tick", fallback.lumbination().processesPerTick(), 1, 512),
                boolValue(serverValues, "lumbination.chop_tree_below", fallback.lumbination().chopTreeBelow()),
                boolValue(serverValues, "lumbination.destroy_leaves", fallback.lumbination().destroyLeaves()),
                boolValue(serverValues, "lumbination.leaves_affect_durability", fallback.lumbination().leavesAffectDurability()),
                boolValue(serverValues, "lumbination.replant_saplings", fallback.lumbination().replantSaplings()),
                boolValue(serverValues, "lumbination.use_shears_on_leaves", fallback.lumbination().useShearsOnLeaves()),
                boolValue(serverValues, "lumbination.ignore_player_placed_leaves", fallback.lumbination().ignorePlayerPlacedLeaves()),
                stringListValue(serverValues, "lumbination.logs", fallback.lumbination().logs()),
                stringListValue(serverValues, "lumbination.leaves", fallback.lumbination().leaves()),
                stringListValue(serverValues, "lumbination.axes", fallback.lumbination().axes())
            ),
            new ShaftanationConfig(
                boolValue(serverValues, "shaftanation.enabled", fallback.shaftanation().enabled()),
                intValue(serverValues, "shaftanation.max_depth", fallback.shaftanation().maxDepth(), 1, 256),
                intValue(serverValues, "shaftanation.processes_per_tick", fallback.shaftanation().processesPerTick(), 1, 512),
                intValue(serverValues, "shaftanation.shaft_width", fallback.shaftanation().shaftWidth(), 1, 7),
                intValue(serverValues, "shaftanation.shaft_height", fallback.shaftanation().shaftHeight(), 1, 5),
                torchPlacementValue(serverValues, "shaftanation.torch_placement", fallback.shaftanation().torchPlacement())
            ),
            new SubstitutionConfig(
                boolValue(serverValues, "substitution.enabled", fallback.substitution().enabled()),
                boolValue(serverValues, "substitution.allow_mending", fallback.substitution().allowMending()),
                boolValue(serverValues, "substitution.prioritize_silk_touch", fallback.substitution().prioritizeSilkTouch()),
                boolValue(serverValues, "substitution.switch_back", fallback.substitution().switchBack()),
                boolValue(serverValues, "substitution.favour_fortune", fallback.substitution().favourFortune()),
                boolValue(serverValues, "substitution.ignore_if_valid_tool", fallback.substitution().ignoreIfValidTool()),
                boolValue(serverValues, "substitution.ignore_passive_mobs", fallback.substitution().ignorePassiveMobs()),
                stringListValue(serverValues, "substitution.blacklist", fallback.substitution().blacklist()),
                fallback.substitution().selectionRules()
            ),
            new VeinationConfig(
                boolValue(serverValues, "veination.enabled", fallback.veination().enabled()),
                intValue(serverValues, "veination.max_vein_distance", fallback.veination().maxVeinDistance(), 1, 64),
                stringListValue(serverValues, "veination.ores", fallback.veination().ores()),
                boolValue(serverValues, "veination.ore_harvest_without_sneak", fallback.veination().oreHarvestWithoutSneak()),
                boolValue(serverValues, "veination.drop_ores_at_first_block", fallback.veination().dropOresAtFirstBrokenBlock()),
                boolValue(serverValues, "veination.increase_harvest_time_per_ore", fallback.veination().increaseHarvestingTimePerOre()),
                doubleValue(serverValues, "veination.harvest_time_modifier", fallback.veination().increasedHarvestingTimePerOreModifier(), 0.01D, 10.0D),
                stringListValue(serverValues, "veination.pickaxe_blacklist", fallback.veination().pickaxeBlacklist())
            ),
            new VentilationConfig(
                boolValue(serverValues, "ventilation.enabled", fallback.ventilation().enabled()),
                intValue(serverValues, "ventilation.radius_horizontal", fallback.ventilation().radiusHorizontal(), 1, 32),
                intValue(serverValues, "ventilation.radius_vertical", fallback.ventilation().radiusVertical(), 1, 64),
                intValue(serverValues, "ventilation.processes_per_tick", fallback.ventilation().processesPerTick(), 1, 512),
                boolValue(serverValues, "ventilation.place_ladders", fallback.ventilation().placeLadders())
            )
        );
    }

    public static void save(Path configDir, SyncedClientConfig config) {
        SyncedClientConfig value = config == null ? SyncedClientConfig.defaults() : config;
        Path clientFile = configDir.resolve(CLIENT_FILE_NAME);
        Path serverFile = configDir.resolve(SERVER_FILE_NAME);

        Map<String, String> clientValues = new LinkedHashMap<>();
        clientValues.put("disable_particle_effects", formatTomlValue(value.client().disableParticleEffects()));

        Map<String, String> serverValues = new LinkedHashMap<>();
        serverValues.put("common.tps_guard", formatTomlValue(value.common().tpsGuard()));
        serverValues.put("common.gather_drops", formatTomlValue(value.common().gatherDrops()));
        serverValues.put("common.auto_illuminate", formatTomlValue(value.common().autoIlluminate()));
        serverValues.put("common.mine_veins", formatTomlValue(value.common().mineVeins()));
        serverValues.put("common.blocks_per_tick", formatTomlValue(value.common().blocksPerTick()));
        serverValues.put("common.enable_tick_delay", formatTomlValue(value.common().enableTickDelay()));
        serverValues.put("common.tick_delay", formatTomlValue(value.common().tickDelay()));
        serverValues.put("common.block_radius", formatTomlValue(value.common().blockRadius()));
        serverValues.put("common.block_limit", formatTomlValue(value.common().blockLimit()));

        serverValues.put("captivation.enabled", formatTomlValue(value.captivation().enabled()));
        serverValues.put("captivation.allow_in_gui", formatTomlValue(value.captivation().allowInGUI()));
        serverValues.put("captivation.radius_horizontal", formatTomlValue(value.captivation().radiusHorizontal()));
        serverValues.put("captivation.radius_vertical", formatTomlValue(value.captivation().radiusVertical()));
        serverValues.put("captivation.is_whitelist", formatTomlValue(value.captivation().isWhitelist()));
        serverValues.put("captivation.unconditional_blacklist", formatTomlValue(value.captivation().unconditionalBlacklist()));
        serverValues.put("captivation.blacklist", formatTomlValue(value.captivation().blacklist()));

        serverValues.put("cropination.enabled", formatTomlValue(value.cropination().enabled()));
        serverValues.put("cropination.harvest_seeds", formatTomlValue(value.cropination().harvestSeeds()));

        serverValues.put("cultivation.enabled", formatTomlValue(value.cultivation().enabled()));
        serverValues.put("cultivation.hydration_distance", formatTomlValue(value.cultivation().hydrationDistance()));

        serverValues.put("excavation.enabled", formatTomlValue(value.excavation().enabled()));
        serverValues.put("excavation.radius_horizontal", formatTomlValue(value.excavation().radiusHorizontal()));
        serverValues.put("excavation.radius_vertical", formatTomlValue(value.excavation().radiusVertical()));
        serverValues.put("excavation.processes_per_tick", formatTomlValue(value.excavation().processesPerTick()));
        serverValues.put("excavation.toggle_mode", formatTomlValue(value.excavation().toggleMode()));
        serverValues.put("excavation.ignore_block_variants", formatTomlValue(value.excavation().ignoreBlockVariants()));
        serverValues.put("excavation.is_block_whitelist", formatTomlValue(value.excavation().isBlockWhitelist()));
        serverValues.put("excavation.block_blacklist", formatTomlValue(value.excavation().blockBlacklist()));

        serverValues.put("pathanation.enabled", formatTomlValue(value.pathanation().enabled()));
        serverValues.put("pathanation.path_length", formatTomlValue(value.pathanation().pathLength()));
        serverValues.put("pathanation.path_width", formatTomlValue(value.pathanation().pathWidth()));

        serverValues.put("illumination.enabled", formatTomlValue(value.illumination().enabled()));
        serverValues.put("illumination.radius_horizontal", formatTomlValue(value.illumination().radiusHorizontal()));
        serverValues.put("illumination.radius_vertical", formatTomlValue(value.illumination().radiusVertical()));
        serverValues.put("illumination.lowest_light_level", formatTomlValue(value.illumination().lowestLightLevel()));
        serverValues.put("illumination.use_block_light", formatTomlValue(value.illumination().useBlockLight()));

        serverValues.put("lumbination.enabled", formatTomlValue(value.lumbination().enabled()));
        serverValues.put("lumbination.max_trunk_range", formatTomlValue(value.lumbination().maxTrunkRange()));
        serverValues.put("lumbination.max_leaf_range", formatTomlValue(value.lumbination().maxLeafRange()));
        serverValues.put("lumbination.processes_per_tick", formatTomlValue(value.lumbination().processesPerTick()));
        serverValues.put("lumbination.chop_tree_below", formatTomlValue(value.lumbination().chopTreeBelow()));
        serverValues.put("lumbination.destroy_leaves", formatTomlValue(value.lumbination().destroyLeaves()));
        serverValues.put("lumbination.leaves_affect_durability", formatTomlValue(value.lumbination().leavesAffectDurability()));
        serverValues.put("lumbination.replant_saplings", formatTomlValue(value.lumbination().replantSaplings()));
        serverValues.put("lumbination.use_shears_on_leaves", formatTomlValue(value.lumbination().useShearsOnLeaves()));
        serverValues.put("lumbination.ignore_player_placed_leaves", formatTomlValue(value.lumbination().ignorePlayerPlacedLeaves()));
        serverValues.put("lumbination.logs", formatTomlValue(value.lumbination().logs()));
        serverValues.put("lumbination.leaves", formatTomlValue(value.lumbination().leaves()));
        serverValues.put("lumbination.axes", formatTomlValue(value.lumbination().axes()));

        serverValues.put("shaftanation.enabled", formatTomlValue(value.shaftanation().enabled()));
        serverValues.put("shaftanation.max_depth", formatTomlValue(value.shaftanation().maxDepth()));
        serverValues.put("shaftanation.processes_per_tick", formatTomlValue(value.shaftanation().processesPerTick()));
        serverValues.put("shaftanation.shaft_width", formatTomlValue(value.shaftanation().shaftWidth()));
        serverValues.put("shaftanation.shaft_height", formatTomlValue(value.shaftanation().shaftHeight()));
        serverValues.put("shaftanation.torch_placement", formatTomlValue(value.shaftanation().torchPlacement().name()));

        serverValues.put("substitution.enabled", formatTomlValue(value.substitution().enabled()));
        serverValues.put("substitution.allow_mending", formatTomlValue(value.substitution().allowMending()));
        serverValues.put("substitution.prioritize_silk_touch", formatTomlValue(value.substitution().prioritizeSilkTouch()));
        serverValues.put("substitution.switch_back", formatTomlValue(value.substitution().switchBack()));
        serverValues.put("substitution.favour_fortune", formatTomlValue(value.substitution().favourFortune()));
        serverValues.put("substitution.ignore_if_valid_tool", formatTomlValue(value.substitution().ignoreIfValidTool()));
        serverValues.put("substitution.ignore_passive_mobs", formatTomlValue(value.substitution().ignorePassiveMobs()));
        serverValues.put("substitution.blacklist", formatTomlValue(value.substitution().blacklist()));

        serverValues.put("veination.enabled", formatTomlValue(value.veination().enabled()));
        serverValues.put("veination.max_vein_distance", formatTomlValue(value.veination().maxVeinDistance()));
        serverValues.put("veination.ores", formatTomlValue(value.veination().ores()));
        serverValues.put("veination.ore_harvest_without_sneak", formatTomlValue(value.veination().oreHarvestWithoutSneak()));
        serverValues.put("veination.drop_ores_at_first_block", formatTomlValue(value.veination().dropOresAtFirstBrokenBlock()));
        serverValues.put("veination.increase_harvest_time_per_ore", formatTomlValue(value.veination().increaseHarvestingTimePerOre()));
        serverValues.put("veination.harvest_time_modifier", formatTomlValue(value.veination().increasedHarvestingTimePerOreModifier()));
        serverValues.put("veination.pickaxe_blacklist", formatTomlValue(value.veination().pickaxeBlacklist()));

        serverValues.put("ventilation.enabled", formatTomlValue(value.ventilation().enabled()));
        serverValues.put("ventilation.radius_horizontal", formatTomlValue(value.ventilation().radiusHorizontal()));
        serverValues.put("ventilation.radius_vertical", formatTomlValue(value.ventilation().radiusVertical()));
        serverValues.put("ventilation.processes_per_tick", formatTomlValue(value.ventilation().processesPerTick()));
        serverValues.put("ventilation.place_ladders", formatTomlValue(value.ventilation().placeLadders()));

        writeToml(clientFile, clientValues, "MinersAdvantage client configuration");
        writeToml(serverFile, serverValues, "MinersAdvantage server configuration");
    }

    private static Map<String, String> readToml(Path filePath) {
        Map<String, String> values = new LinkedHashMap<>();
        if (!Files.isRegularFile(filePath)) {
            return values;
        }
        try {
            for (String line : Files.readAllLines(filePath, StandardCharsets.UTF_8)) {
                String clean = stripComment(line).trim();
                if (clean.isEmpty()) {
                    continue;
                }
                int equals = clean.indexOf('=');
                if (equals <= 0) {
                    continue;
                }
                String key = clean.substring(0, equals).trim();
                String value = clean.substring(equals + 1).trim();
                values.put(key, value);
            }
        } catch (Exception exception) {
            warn("Unable to read TOML config " + filePath + ": " + exception.getMessage());
        }
        return values;
    }

    private static void writeToml(Path filePath, Map<String, String> values, String header) {
        StringBuilder builder = new StringBuilder();
        builder.append("# ").append(header).append("\n\n");
        values.forEach((key, value) -> builder.append(key).append(" = ").append(value).append("\n"));
        try {
            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, builder.toString(), StandardCharsets.UTF_8);
        } catch (Exception exception) {
            warn("Unable to write TOML config " + filePath + ": " + exception.getMessage());
        }
    }

    private static String stripComment(String line) {
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char current = line.charAt(i);
            if (current == '"' && (i == 0 || line.charAt(i - 1) != '\\')) {
                inQuotes = !inQuotes;
            } else if (current == '#' && !inQuotes) {
                return line.substring(0, i);
            }
        }
        return line;
    }

    private static boolean boolValue(Map<String, String> values, String key, boolean fallback) {
        String raw = values.get(key);
        if (raw == null) {
            return fallback;
        }
        String normalized = stripQuotes(raw).toLowerCase(Locale.ROOT);
        if ("true".equals(normalized)) {
            return true;
        }
        if ("false".equals(normalized)) {
            return false;
        }
        warn("Invalid boolean for config key " + key + ": " + raw);
        return fallback;
    }

    private static int intValue(Map<String, String> values, String key, int fallback, int min, int max) {
        String raw = values.get(key);
        if (raw == null) {
            return fallback;
        }
        try {
            int value = Integer.parseInt(stripQuotes(raw));
            return Math.max(min, Math.min(max, value));
        } catch (NumberFormatException exception) {
            warn("Invalid integer for config key " + key + ": " + raw);
            return fallback;
        }
    }

    private static double doubleValue(Map<String, String> values, String key, double fallback, double min, double max) {
        String raw = values.get(key);
        if (raw == null) {
            return fallback;
        }
        try {
            double value = Double.parseDouble(stripQuotes(raw));
            return Math.max(min, Math.min(max, value));
        } catch (NumberFormatException exception) {
            warn("Invalid decimal for config key " + key + ": " + raw);
            return fallback;
        }
    }

    private static List<String> stringListValue(Map<String, String> values, String key, List<String> fallback) {
        String raw = values.get(key);
        if (raw == null) {
            return fallback;
        }

        String value = raw.trim();
        if (!value.startsWith("[") || !value.endsWith("]")) {
            warn("Invalid list for config key " + key + ": " + raw);
            return fallback;
        }

        String body = value.substring(1, value.length() - 1).trim();
        if (body.isEmpty()) {
            return List.of();
        }

        List<String> result = new ArrayList<>();
        StringBuilder item = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < body.length(); i++) {
            char current = body.charAt(i);
            if (current == '"' && (i == 0 || body.charAt(i - 1) != '\\')) {
                inQuotes = !inQuotes;
                item.append(current);
                continue;
            }
            if (current == ',' && !inQuotes) {
                addListValue(result, item.toString());
                item.setLength(0);
                continue;
            }
            item.append(current);
        }
        addListValue(result, item.toString());
        return List.copyOf(result);
    }

    private static void addListValue(List<String> target, String candidate) {
        String value = stripQuotes(candidate.trim());
        if (!value.isBlank()) {
            target.add(value);
        }
    }

    private static TorchPlacement torchPlacementValue(Map<String, String> values, String key, TorchPlacement fallback) {
        String raw = values.get(key);
        if (raw == null) {
            return fallback;
        }
        String value = stripQuotes(raw).trim();
        if (value.isEmpty()) {
            return fallback;
        }
        try {
            return TorchPlacement.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            warn("Invalid torch placement for config key " + key + ": " + raw);
            return fallback;
        }
    }

    private static String stripQuotes(String value) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.length() >= 2 && trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            return trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed;
    }

    private static String formatTomlValue(Object value) {
        if (value instanceof Boolean || value instanceof Number) {
            return String.valueOf(value);
        }
        if (value instanceof List<?> list) {
            List<String> values = new ArrayList<>(list.size());
            for (Object element : list) {
                values.add('"' + escapeTomlString(String.valueOf(element)) + '"');
            }
            return '[' + String.join(", ", values) + ']';
        }
        return '"' + escapeTomlString(String.valueOf(value)) + '"';
    }

    private static String escapeTomlString(String value) {
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"");
    }

    private static void warn(String message) {
        System.err.println("[MinersAdvantage] " + message);
    }
}