package uk.co.duelmonster.minersadvantage.config;

import java.util.List;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import uk.co.duelmonster.minersadvantage.common.Functions;
import uk.co.duelmonster.minersadvantage.common.JsonHelper;
import uk.co.duelmonster.minersadvantage.common.TorchPlacement;

public class SyncedClientConfig {

  public Client       client;
  public Common       common;
  public Captivation  captivation;
  public Cropination  cropination;
  public Excavation   excavation;
  public Pathanation  pathanation;
  public Illumination illumination;
  public Lumbination  lumbination;
  public Shaftanation shaftanation;
  public Substitution substitution;
  public Veination    veination;

  private transient String history = null;

  public boolean hasChanged() {
    String current = JsonHelper.toJson(SyncedClientConfig.create());
    if (current.equals(current) || history == null || history.isEmpty()) {
      history = current;
      return true;
    }

    return false;
  }

  public static SyncedClientConfig create() {
    SyncedClientConfig config = new SyncedClientConfig();

    config.client = config.new Client();
    {
      config.client.disableParticleEffects = MAConfig.CLIENT.disableParticleEffects();
    }
    config.common = config.new Common();
    {
      config.common.tpsGuard        = MAConfig.CLIENT.common.tpsGuard();
      config.common.gatherDrops     = MAConfig.CLIENT.common.gatherDrops();
      config.common.autoIlluminate  = MAConfig.CLIENT.common.autoIlluminate();
      config.common.mineVeins       = MAConfig.CLIENT.common.mineVeins();
      config.common.blocksPerTick   = MAConfig.CLIENT.common.blocksPerTick();
      config.common.enableTickDelay = MAConfig.CLIENT.common.enableTickDelay();
      config.common.tickDelay       = MAConfig.CLIENT.common.tickDelay();
      config.common.blockRadius     = MAConfig.CLIENT.common.blockRadius();
      config.common.blockLimit      = MAConfig.CLIENT.common.blockLimit();
    }
    config.captivation = config.new Captivation();
    {
      config.captivation.enabled                = MAConfig.CLIENT.captivation.enabled();
      config.captivation.allowInGUI             = MAConfig.CLIENT.captivation.allowInGUI();
      config.captivation.radiusHorizontal       = MAConfig.CLIENT.captivation.radiusHorizontal();
      config.captivation.radiusVertical         = MAConfig.CLIENT.captivation.radiusVertical();
      config.captivation.isWhitelist            = MAConfig.CLIENT.captivation.isWhitelist();
      config.captivation.unconditionalBlacklist = MAConfig.CLIENT.captivation.unconditionalBlacklist();
      config.captivation.blacklist              = MAConfig.CLIENT.captivation.blacklist();
    }
    config.cropination = config.new Cropination();
    {
      config.cropination.enabled      = MAConfig.CLIENT.cropination.enabled();
      config.cropination.harvestSeeds = MAConfig.CLIENT.cropination.harvestSeeds();
    }
    config.excavation = config.new Excavation();
    {
      config.excavation.enabled             = MAConfig.CLIENT.excavation.enabled();
      config.excavation.toggleMode          = MAConfig.CLIENT.excavation.toggleMode();
      config.excavation.ignoreBlockVariants = MAConfig.CLIENT.excavation.ignoreBlockVariants();
      config.excavation.isBlockWhitelist    = MAConfig.CLIENT.excavation.isBlockWhitelist();
      config.excavation.blockBlacklist      = MAConfig.CLIENT.excavation.blockBlacklist();
    }
    config.pathanation = config.new Pathanation();
    {
      config.pathanation.enabled    = MAConfig.CLIENT.pathanation.enabled();
      config.pathanation.pathWidth  = MAConfig.CLIENT.pathanation.pathWidth();
      config.pathanation.pathLength = MAConfig.CLIENT.pathanation.pathLength();
    }
    config.illumination = config.new Illumination();
    {
      config.illumination.enabled          = MAConfig.CLIENT.illumination.enabled();
      config.illumination.lowestLightLevel = MAConfig.CLIENT.illumination.lowestLightLevel();
    }
    config.lumbination = config.new Lumbination();
    {
      config.lumbination.enabled                = MAConfig.CLIENT.lumbination.enabled();
      config.lumbination.chopTreeBelow          = MAConfig.CLIENT.lumbination.chopTreeBelow();
      config.lumbination.destroyLeaves          = MAConfig.CLIENT.lumbination.destroyLeaves();
      config.lumbination.leavesAffectDurability = MAConfig.CLIENT.lumbination.leavesAffectDurability();
      config.lumbination.replantSaplings        = MAConfig.CLIENT.lumbination.replantSaplings();
      config.lumbination.useShearsOnLeaves      = MAConfig.CLIENT.lumbination.useShearsOnLeaves();
      config.lumbination.leafRange              = MAConfig.CLIENT.lumbination.leafRange();
      config.lumbination.trunkRange             = MAConfig.CLIENT.lumbination.trunkRange();
      config.lumbination.logs                   = MAConfig.CLIENT.lumbination.logs();
      config.lumbination.leaves                 = MAConfig.CLIENT.lumbination.leaves();
      config.lumbination.axes                   = MAConfig.CLIENT.lumbination.axes();
    }
    config.shaftanation = config.new Shaftanation();
    {
      config.shaftanation.enabled        = MAConfig.CLIENT.shaftanation.enabled();
      config.shaftanation.shaftLength    = MAConfig.CLIENT.shaftanation.shaftLength();
      config.shaftanation.shaftHeight    = MAConfig.CLIENT.shaftanation.shaftHeight();
      config.shaftanation.shaftWidth     = MAConfig.CLIENT.shaftanation.shaftWidth();
      config.shaftanation.torchPlacement = MAConfig.CLIENT.shaftanation.torchPlacement();
    }
    config.substitution = config.new Substitution();
    {
      config.substitution.enabled           = MAConfig.CLIENT.substitution.enabled();
      config.substitution.switchBack        = MAConfig.CLIENT.substitution.switchBack();
      config.substitution.favourSilkTouch   = MAConfig.CLIENT.substitution.favourSilkTouch();
      config.substitution.favourFortune     = MAConfig.CLIENT.substitution.favourFortune();
      config.substitution.ignoreIfValidTool = MAConfig.CLIENT.substitution.ignoreIfValidTool();
      config.substitution.ignorePassiveMobs = MAConfig.CLIENT.substitution.ignorePassiveMobs();
      config.substitution.blacklist         = MAConfig.CLIENT.substitution.blacklist();
    }
    config.veination = config.new Veination();
    {
      config.veination.enabled = MAConfig.CLIENT.veination.enabled();
      config.veination.ores    = MAConfig.CLIENT.veination.ores();
    }

    return config;
  }

  public class Client {

    public boolean disableParticleEffects;

  }

  public class Common {

    public boolean tpsGuard;
    public boolean gatherDrops;
    public boolean autoIlluminate;
    public boolean mineVeins;
    public int     blocksPerTick;
    public boolean enableTickDelay;
    public int     tickDelay;
    public int     blockRadius;
    public int     blockLimit;

  }

  public class Captivation {

    public boolean                enabled;
    public boolean                allowInGUI;
    public double                 radiusHorizontal;
    public double                 radiusVertical;
    public boolean                isWhitelist;
    public boolean                unconditionalBlacklist;
    public List<? extends String> blacklist;

  }

  public class Cropination {

    public boolean enabled;
    public boolean harvestSeeds;

  }

  public class Excavation {

    public boolean                enabled;
    public boolean                toggleMode;
    public boolean                ignoreBlockVariants;
    public boolean                isBlockWhitelist;
    public List<? extends String> blockBlacklist;

    // ====================================================================================================
    // = Config utility functions
    // ====================================================================================================

    public boolean isBlacklisted(Block block) {
      if (block == null || block == Blocks.AIR)
        return isBlockWhitelist;

      if (blockBlacklist.contains(Functions.getName(block)))
        return !isBlockWhitelist;

      return isBlockWhitelist;
    }
  }

  public class Pathanation {

    public boolean enabled;
    public int     pathWidth;
    public int     pathLength;

  }

  public class Illumination {

    public boolean enabled;
    public int     lowestLightLevel;
    public boolean useBlockLight;

  }

  public class Lumbination {

    public boolean                enabled;
    public boolean                chopTreeBelow;
    public boolean                destroyLeaves;
    public boolean                leavesAffectDurability;
    public boolean                replantSaplings;
    public boolean                useShearsOnLeaves;
    public int                    leafRange;
    public int                    trunkRange;
    public List<? extends String> logs;
    public List<? extends String> leaves;
    public List<? extends String> axes;

  }

  public class Shaftanation {

    public boolean        enabled;
    public int            shaftLength;
    public int            shaftHeight;
    public int            shaftWidth;
    public TorchPlacement torchPlacement;

  }

  public class Substitution {

    public boolean                enabled;
    public boolean                switchBack;
    public boolean                favourSilkTouch;
    public boolean                favourFortune;
    public boolean                ignoreIfValidTool;
    public boolean                ignorePassiveMobs;
    public List<? extends String> blacklist;

  }

  public class Veination {

    public boolean                enabled;
    public List<? extends String> ores;

  }
}
