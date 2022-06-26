package uk.co.duelmonster.minersadvantage.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import uk.co.duelmonster.minersadvantage.config.defaults.MAConfig_Defaults;

public class MAConfig_Server extends MAConfig_Base {

  // ====================================================================================================
  // = Non-config transient variables
  // = non-serialised because we don't want these being transfered during a settings sync.
  // ====================================================================================================

  // ====================================================================================================
  // = Config variables
  // ====================================================================================================
  public final BooleanValue overrideFeatureEnablement;
  public final BooleanValue enforceCommonSettings;
  public final BooleanValue enforceCaptivationSettings;
  public final BooleanValue enforceCropinationSettings;
  public final BooleanValue enforceExcavationSettings;
  public final BooleanValue enforcePathanationSettings;
  public final BooleanValue enforceIlluminationSettings;
  public final BooleanValue enforceLumbinationSettings;
  public final BooleanValue enforceShaftanationSettings;
  public final BooleanValue enforceSubstitutionSettings;
  public final BooleanValue enforceVeinationSettings;

  // ====================================================================================================
  // = Initialisation
  // ====================================================================================================
  MAConfig_Server(ForgeConfigSpec.Builder builder) {
    super(builder);

    builder.comment("Server configuration for MinersAdvantage")
        // .translation("minersadvantage.server.comment")
        .push("server");

    overrideFeatureEnablement = builder
        .comment("When enabled, the Server side enabled features will take precedence over the Client config.",
            "Meaning the MinersAdvantage features that are disabled within the Server config will be disabled for all Players regardless of their Client side configs.")
        .translation("minersadvantage.server.override_client")
        .define("override_client", MAConfig_Defaults.Server.overrideFeatureEnablement);

    enforceCommonSettings = builder
        .comment("When enabled, the Server side Common feature settings will be enforced upon all Players connected to the server.")
        .translation("minersadvantage.server.enforce_common_settings")
        .define("enforce_common_settings", MAConfig_Defaults.Server.enforceCommonSettings);

    enforceCaptivationSettings = builder
        .comment("When enabled, the Server side Captivation feature settings will be enforced upon all Players connected to the server.")
        .translation("minersadvantage.server.enforce_captivation_settings")
        .define("enforce_captivation_settings", MAConfig_Defaults.Server.enforceCaptivationSettings);

    enforceCropinationSettings = builder
        .comment("When enabled, the Server side Cropination feature settings will be enforced upon all Players connected to the server.")
        .translation("minersadvantage.server.enforce_cropination_settings")
        .define("enforce_cropination_settings", MAConfig_Defaults.Server.enforceCropinationSettings);

    enforceExcavationSettings = builder
        .comment("When enabled, the Server side Excavation feature settings will be enforced upon all Players connected to the server.")
        .translation("minersadvantage.server.enforce_excavation_settings")
        .define("enforce_excavation_settings", MAConfig_Defaults.Server.enforceExcavationSettings);

    enforcePathanationSettings = builder
        .comment("When enabled, the Server side Pathanation feature settings will be enforced upon all Players connected to the server.")
        .translation("minersadvantage.server.enforce_pathanation_settings")
        .define("enforce_pathanation_settings", MAConfig_Defaults.Server.enforcePathanationSettings);

    enforceIlluminationSettings = builder
        .comment("When enabled, the Server side Illumination feature settings will be enforced upon all Players connected to the server.")
        .translation("minersadvantage.server.enforce_illumination_settings")
        .define("enforce_illumination_settings", MAConfig_Defaults.Server.enforceIlluminationSettings);

    enforceLumbinationSettings = builder
        .comment("When enabled, the Server side Lumbination feature settings will be enforced upon all Players connected to the server.")
        .translation("minersadvantage.server.enforce_lumbination_settings")
        .define("enforce_lumbination_settings", MAConfig_Defaults.Server.enforceLumbinationSettings);

    enforceShaftanationSettings = builder
        .comment("When enabled, the Server side Shaftanation feature settings will be enforced upon all Players connected to the server.")
        .translation("minersadvantage.server.enforce_shaftanation_settings")
        .define("enforce_shaftanation_settings", MAConfig_Defaults.Server.enforceShaftanationSettings);

    enforceSubstitutionSettings = builder
        .comment("When enabled, the Server side Substitution feature settings will be enforced upon all Players connected to the server.")
        .translation("minersadvantage.server.enforce_substitution_settings")
        .define("enforce_substitution_settings", MAConfig_Defaults.Server.enforceSubstitutionSettings);

    enforceVeinationSettings = builder
        .comment("When enabled, the Server side Veination feature settings will be enforced upon all Players connected to the server.")
        .translation("minersadvantage.server.enforce_veination_settings")
        .define("enforce_veination_settings", MAConfig_Defaults.Server.enforceVeinationSettings);

    builder.pop();
  }

}
