package uk.co.duelmonster.minersadvantage.config;

import java.util.HashMap;
import java.util.UUID;

import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec;
import uk.co.duelmonster.minersadvantage.MA;
import uk.co.duelmonster.minersadvantage.client.ClientFunctions;
import uk.co.duelmonster.minersadvantage.common.Constants;
import uk.co.duelmonster.minersadvantage.common.JsonHelper;
import uk.co.duelmonster.minersadvantage.common.SyncType;
import uk.co.duelmonster.minersadvantage.config.categories.MAConfig_Captivation;
import uk.co.duelmonster.minersadvantage.config.categories.MAConfig_Common;
import uk.co.duelmonster.minersadvantage.config.categories.MAConfig_Cropination;
import uk.co.duelmonster.minersadvantage.config.categories.MAConfig_Excavation;
import uk.co.duelmonster.minersadvantage.config.categories.MAConfig_Illumination;
import uk.co.duelmonster.minersadvantage.config.categories.MAConfig_Lumbination;
import uk.co.duelmonster.minersadvantage.config.categories.MAConfig_Pathanation;
import uk.co.duelmonster.minersadvantage.config.categories.MAConfig_Shaftanation;
import uk.co.duelmonster.minersadvantage.config.categories.MAConfig_Substitution;
import uk.co.duelmonster.minersadvantage.config.categories.MAConfig_Veination;
import uk.co.duelmonster.minersadvantage.config.categories.MAConfig_Ventilation;
import uk.co.duelmonster.minersadvantage.network.packets.PacketSynchronization;

public class MAConfig_Base {

  // ====================================================================================================
  // = Non-config transient variables
  // = non-serialised because we don't want these being transfered during a settings sync.
  // ====================================================================================================
  public transient MAConfig_Server serverOverrides = null;

  private static transient HashMap<UUID, SyncedClientConfig> playerConfigs = new HashMap<UUID, SyncedClientConfig>();

  public static SyncedClientConfig getCurrentPlayerConfig() {
    return getPlayerConfig(Constants.instanceUID);
  }

  public static SyncedClientConfig getPlayerConfig(UUID uid) {
    if (playerConfigs.isEmpty() || playerConfigs.get(uid) == null)
      setPlayerConfig(uid, SyncedClientConfig.create());

    return playerConfigs.get(uid);
  }

  public static SyncedClientConfig setPlayerConfig(UUID uid, String payload) {
    return setPlayerConfig(uid, JsonHelper.fromJson(payload, SyncedClientConfig.class));
  }

  public static SyncedClientConfig setPlayerConfig(UUID uid, SyncedClientConfig config) {
    return playerConfigs.put(uid, config);
  }

  @OnlyIn(Dist.CLIENT)
  public void syncPlayerConfigToServer() {

    LocalPlayer player = ClientFunctions.getPlayer();
    if (player != null) {
      SyncedClientConfig config = getCurrentPlayerConfig();
      if (config.hasChanged()) {
        MA.NETWORK.sendToServer(new PacketSynchronization(player.getUUID(), SyncType.ClientConfig, JsonHelper.toJson(SyncedClientConfig.create())));
      }
    }

  }

  // ====================================================================================================
  // = Initialisation
  // ====================================================================================================
  MAConfig_Base(ForgeConfigSpec.Builder builder) {
    ForgeConfigSpec.Builder subBuilder = null;

    subBuilder = builder
        .comment("Common configuration for MinersAdvantage")
        // .translation("minersadvantage.common")
        .push("common");
    common     = new MAConfig_Common(subBuilder);
    builder.pop();

    subBuilder  = builder
        .comment("Captivation configuration for MinersAdvantage")
        // .translation("minersadvantage.captivation")
        .push("captivation");
    captivation = new MAConfig_Captivation(subBuilder);
    builder.pop();

    subBuilder  = builder
        .comment("Cropination configuration for MinersAdvantage")
        // .translation("minersadvantage.cropination")
        .push("cropination");
    cropination = new MAConfig_Cropination(subBuilder);
    builder.pop();

    subBuilder = builder
        .comment("Excavation configuration for MinersAdvantage")
        // .translation("minersadvantage.excavation")
        .push("excavation");
    excavation = new MAConfig_Excavation(subBuilder);
    builder.pop();

    subBuilder  = builder
        .comment("Pathanation configuration for MinersAdvantage")
        // .translation("minersadvantage.pathanation")
        .push("pathanation");
    pathanation = new MAConfig_Pathanation(subBuilder);
    builder.pop();

    subBuilder   = builder
        .comment("Illumination configuration for MinersAdvantage")
        // .translation("minersadvantage.illumination")
        .push("illumination");
    illumination = new MAConfig_Illumination(subBuilder);
    builder.pop();

    subBuilder  = builder
        .comment("Lumbination configuration for MinersAdvantage")
        // .translation("minersadvantage.lumbination")
        .push("lumbination");
    lumbination = new MAConfig_Lumbination(subBuilder);
    builder.pop();

    subBuilder   = builder
        .comment("Shaftanation configuration for MinersAdvantage")
        // .translation("minersadvantage.shaftanation")
        .push("shaftanation");
    shaftanation = new MAConfig_Shaftanation(subBuilder);
    builder.pop();

    subBuilder   = builder
        .comment("Substitution configuration for MinersAdvantage")
        // .translation("minersadvantage.substitution")
        .push("substitution");
    substitution = new MAConfig_Substitution(subBuilder);
    builder.pop();

    subBuilder = builder
        .comment("Veination configuration for MinersAdvantage")
        // .translation("minersadvantage.veination")
        .push("veination");
    veination  = new MAConfig_Veination(subBuilder);
    builder.pop();

    subBuilder = builder
        .comment("Ventilation configuration for MinersAdvantage")
        // .translation("minersadvantage.veination")
        .push("ventilation");
    ventilation = new MAConfig_Ventilation(subBuilder);
    builder.pop();

    setSubCategoryParents();
  }

  // ====================================================================================================
  // = Sub Categories
  // ====================================================================================================
  public final MAConfig_Common       common;
  public final MAConfig_Captivation  captivation;
  public final MAConfig_Cropination  cropination;
  public final MAConfig_Excavation   excavation;
  public final MAConfig_Pathanation  pathanation;
  public final MAConfig_Illumination illumination;
  public final MAConfig_Lumbination  lumbination;
  public final MAConfig_Shaftanation shaftanation;
  public final MAConfig_Substitution substitution;
  public final MAConfig_Veination    veination;
  public final MAConfig_Ventilation   ventilation;

  public void setSubCategoryParents() {
    common.SetParentConfig(this);
    captivation.SetParentConfig(this);
    cropination.SetParentConfig(this);
    excavation.SetParentConfig(this);
    pathanation.SetParentConfig(this);
    illumination.SetParentConfig(this);
    lumbination.SetParentConfig(this);
    shaftanation.SetParentConfig(this);
    substitution.SetParentConfig(this);
    veination.SetParentConfig(this);
    ventilation.SetParentConfig(this);
  }

  // ====================================================================================================
  // = Config retrieval functions
  // ====================================================================================================

  public boolean allEnabled() {
    return captivation.enabled() &&
        cropination.enabled() &&
        excavation.enabled() &&
        lumbination.enabled() &&
        illumination.enabled() &&
        pathanation.enabled() &&
        shaftanation.enabled() &&
        substitution.enabled() &&
        veination.enabled() &&
        ventilation.enabled();
  }

}