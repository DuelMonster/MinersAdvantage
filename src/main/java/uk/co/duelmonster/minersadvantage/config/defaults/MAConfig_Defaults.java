package uk.co.duelmonster.minersadvantage.config.defaults;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.minecraft.world.item.Items;
import uk.co.duelmonster.minersadvantage.common.Constants;
import uk.co.duelmonster.minersadvantage.common.Functions;
import uk.co.duelmonster.minersadvantage.common.TorchPlacement;

// ====================================================================================================
// = Default config variables
// ====================================================================================================
public class MAConfig_Defaults {

  public static class Client {

    public static final boolean disableParticleEffects = false;
  }

  public static class Server {

    public static final boolean overrideFeatureEnablement   = false;
    public static final boolean enforceCommonSettings       = false;
    public static final boolean enforceCaptivationSettings  = false;
    public static final boolean enforceCropinationSettings  = false;
    public static final boolean enforceExcavationSettings   = false;
    public static final boolean enforcePathanationSettings  = false;
    public static final boolean enforceIlluminationSettings = false;
    public static final boolean enforceLumbinationSettings  = false;
    public static final boolean enforceShaftanationSettings = false;
    public static final boolean enforceSubstitutionSettings = false;
    public static final boolean enforceVeinationSettings    = false;
    public static final boolean enforceVentilationSettings  = false;

  }

  public static class Common {

    public static final boolean tpsGuard        = true;
    public static final boolean gatherDrops     = false;
    public static final boolean autoIlluminate  = true;
    public static final boolean mineVeins       = true;
    public static final int     blocksPerTick   = 1;
    public static final boolean enableTickDelay = true;
    public static final int     tickDelay       = 5;
    public static final int     blockRadius     = Constants.DEFAULT_BLOCKRADIUS;
    public static final int     blockLimit      = Constants.DEFAULT_BLOCKLIMIT;

  }

  public static class Captivation {

    public static final boolean      enabled                = true;
    public static final boolean      allowInGUI             = false;
    public static final double       radiusHorizontal       = 16;
    public static final double       radiusVertical         = 16;
    public static final boolean      isWhitelist            = false;
    public static final boolean      unconditionalBlacklist = false;
    public static final List<String> blacklist              = Arrays.asList(
        Functions.getName(Items.ROTTEN_FLESH),
        Functions.getName(Items.EGG));

  }

  public static class Cropination {

    public static final boolean enabled      = true;
    public static final boolean harvestSeeds = true;

  }

  public static class Excavation {

    public static final boolean      enabled             = true;
    public static final boolean      toggleMode          = false;
    public static final boolean      ignoreBlockVariants = false;
    public static final boolean      isBlockWhitelist    = false;
    public static final List<String> blockBlacklist      = Collections.emptyList();

  }

  public static class Pathanation {

    public static final boolean enabled    = true;
    public static final int     pathWidth  = 3;
    public static final int     pathLength = 6;

  }

  public static class Illumination {

    public static final boolean enabled          = true;
    public static final int     lowestLightLevel = 7;
    public static final boolean useBlockLight    = true;

  }

  public static class Lumbination {

    public static final boolean      enabled                = true;
    public static final boolean      chopTreeBelow          = true;
    public static final boolean      destroyLeaves          = true;
    public static final boolean      leavesAffectDurability = false;
    public static final boolean      replantSaplings        = true;
    public static final boolean      useShearsOnLeaves      = true;
    public static final int          leafRange              = 6;
    public static final int          trunkRange             = 32;
    public static final List<String> logs                   = Collections.emptyList();
    public static final List<String> leaves                 = Collections.emptyList();
    public static final List<String> axes                   = Collections.emptyList();

  }

  public static class Shaftanation {

    public static final boolean        enabled        = true;
    public static final int            shaftLength    = 16;
    public static final int            shaftHeight    = 2;
    public static final int            shaftWidth     = 1;
    public static final TorchPlacement torchPlacement = TorchPlacement.FLOOR;

  }

  public static class Substitution {

    public static final boolean      enabled           = true;
    public static final boolean      switchBack        = true;
    public static final boolean      favourSilkTouch   = false;
    public static final boolean      favourFortune     = true;
    public static final boolean      ignoreIfValidTool = true;
    public static final boolean      ignorePassiveMobs = true;
    public static final List<String> blacklist         = Collections.emptyList();

  }

  public static class Veination {

    public static final boolean      enabled = true;
    public static final List<String> ores    = Collections.emptyList();

  }

  public static class Ventilation {

    public static final boolean enabled      = true;
    public static final int     ventDiameter = 1;
    public static final int     ventDepth    = 16;
    public static final boolean placeLadders = true;

  }

}
