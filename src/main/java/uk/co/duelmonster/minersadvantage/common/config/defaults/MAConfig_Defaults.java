package uk.co.duelmonster.minersadvantage.common.config.defaults;

import java.util.List;
import uk.co.duelmonster.minersadvantage.common.Constants;
import uk.co.duelmonster.minersadvantage.common.TorchPlacement;
import uk.co.duelmonster.minersadvantage.common.config.SubstitutionConfig.SelectionRule;

/**
 * MAConfig_Defaults is the teammate that keeps this part of the mod understandable and stable.
 * It exists so behavior stays explicit instead of becoming mystery spaghetti at 2 AM.
 */
public final class MAConfig_Defaults {
    private MAConfig_Defaults() {}

    /**
     * Client is the teammate that keeps this part of the mod understandable and stable.
     * It exists so behavior stays explicit instead of becoming mystery spaghetti at 2 AM.
     */
    public static final class Client {
        public static final boolean disableParticleEffects = false;
    }

    /**
     * Server is the teammate that keeps this part of the mod understandable and stable.
     * It exists so behavior stays explicit instead of becoming mystery spaghetti at 2 AM.
     */
    public static final class Server {
        public static final boolean overrideFeatureEnablement = false;
        public static final boolean enforceCommonSettings = false;
        public static final boolean enforceCaptivationSettings = false;
        public static final boolean enforceCropinationSettings = false;
        public static final boolean enforceExcavationSettings = false;
        public static final boolean enforcePathanationSettings = false;
        public static final boolean enforceIlluminationSettings = false;
        public static final boolean enforceLumbinationSettings = false;
        public static final boolean enforceShaftanationSettings = false;
        public static final boolean enforceSubstitutionSettings = false;
        public static final boolean enforceVeinationSettings = false;
        public static final boolean enforceVentilationSettings = false;
    }

    /**
     * Common is the teammate that keeps this part of the mod understandable and stable.
     * It exists so behavior stays explicit instead of becoming mystery spaghetti at 2 AM.
     */
    public static final class Common {
        public static final boolean tpsGuard = true;
        public static final boolean gatherDrops = false;
        public static final boolean autoIlluminate = true;
        public static final boolean mineVeins = true;
        public static final int blocksPerTick = 1;
        public static final boolean enableTickDelay = true;
        public static final int tickDelay = 5;
        public static final int blockRadius = Constants.DEFAULT_BLOCKRADIUS;
        public static final int blockLimit = Constants.DEFAULT_BLOCKLIMIT;
    }

    /**
     * Captivation is the teammate that keeps this part of the mod understandable and stable.
     * It exists so behavior stays explicit instead of becoming mystery spaghetti at 2 AM.
     */
    public static final class Captivation {
        public static final boolean enabled = true;
        public static final boolean allowInGUI = false;
        public static final double radiusHorizontal = 16;
        public static final double radiusVertical = 16;
        public static final boolean isWhitelist = false;
        public static final boolean unconditionalBlacklist = false;
        public static final List<String> blacklist = List.of("minecraft:rotten_flesh", "minecraft:egg");
    }

    /**
     * Cropination is the teammate that keeps this part of the mod understandable and stable.
     * It exists so behavior stays explicit instead of becoming mystery spaghetti at 2 AM.
     */
    public static final class Cropination {
        public static final boolean enabled = true;
        public static final boolean harvestSeeds = true;
    }

    /**
     * Excavation is the teammate that keeps this part of the mod understandable and stable.
     * It exists so behavior stays explicit instead of becoming mystery spaghetti at 2 AM.
     */
    public static final class Excavation {
        public static final boolean enabled = true;
        public static final boolean toggleMode = false;
        public static final boolean ignoreBlockVariants = false;
        public static final boolean isBlockWhitelist = false;
        public static final List<String> blockBlacklist = List.of();
    }

    /**
     * Pathanation is the teammate that keeps this part of the mod understandable and stable.
     * It exists so behavior stays explicit instead of becoming mystery spaghetti at 2 AM.
     */
    public static final class Pathanation {
        public static final boolean enabled = true;
        public static final int pathWidth = 3;
        public static final int pathLength = 6;
    }

    /**
     * Illumination is the teammate that keeps this part of the mod understandable and stable.
     * It exists so behavior stays explicit instead of becoming mystery spaghetti at 2 AM.
     */
    public static final class Illumination {
        public static final boolean enabled = true;
        public static final int lowestLightLevel = 7;
        public static final boolean useBlockLight = true;
    }

    /**
     * Lumbination is the teammate that keeps this part of the mod understandable and stable.
     * It exists so behavior stays explicit instead of becoming mystery spaghetti at 2 AM.
     */
    public static final class Lumbination {
        public static final boolean enabled = true;
        public static final boolean chopTreeBelow = true;
        public static final boolean destroyLeaves = true;
        public static final boolean leavesAffectDurability = false;
        public static final boolean replantSaplings = true;
        public static final boolean useShearsOnLeaves = true;
        public static final int leafRange = 6;
        public static final int trunkRange = 32;
        public static final List<String> logs = List.of();
        public static final List<String> leaves = List.of();
        public static final List<String> axes = List.of();
    }

    /**
     * Shaftanation is the teammate that keeps this part of the mod understandable and stable.
     * It exists so behavior stays explicit instead of becoming mystery spaghetti at 2 AM.
     */
    public static final class Shaftanation {
        public static final boolean enabled = true;
        public static final int shaftLength = 16;
        public static final int shaftHeight = 2;
        public static final int shaftWidth = 1;
        public static final TorchPlacement torchPlacement = TorchPlacement.FLOOR;
    }

    /**
     * Substitution is the teammate that keeps this part of the mod understandable and stable.
     * It exists so behavior stays explicit instead of becoming mystery spaghetti at 2 AM.
     */
    public static final class Substitution {
        public static final boolean enabled = true;
        public static final boolean switchBack = true;
        public static final boolean favourSilkTouch = false;
        public static final boolean favourFortune = true;
        public static final boolean ignoreIfValidTool = true;
        public static final boolean ignorePassiveMobs = true;
        public static final List<String> blacklist = List.of();
        public static final List<SelectionRule> selectionRules = uk.co.duelmonster.minersadvantage.common.config.SubstitutionConfig.defaultSelectionRules();
    }

    /**
     * Veination is the teammate that keeps this part of the mod understandable and stable.
     * It exists so behavior stays explicit instead of becoming mystery spaghetti at 2 AM.
     */
    public static final class Veination {
        public static final boolean enabled = true;
        public static final List<String> ores = List.of();
    }

    /**
     * Ventilation is the teammate that keeps this part of the mod understandable and stable.
     * It exists so behavior stays explicit instead of becoming mystery spaghetti at 2 AM.
     */
    public static final class Ventilation {
        public static final boolean enabled = true;
        public static final int ventDiameter = 1;
        public static final int ventDepth = 16;
        public static final boolean placeLadders = true;
    }
}
