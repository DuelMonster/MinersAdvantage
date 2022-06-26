package uk.co.duelmonster.minersadvantage.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import uk.co.duelmonster.minersadvantage.config.defaults.MAConfig_Defaults;

public class MAConfig_Client extends MAConfig_Base {

  // ====================================================================================================
  // = Config variables
  // ====================================================================================================
  public final BooleanValue disableParticleEffects;

  // ====================================================================================================
  // = Initialisation
  // ====================================================================================================
  MAConfig_Client(ForgeConfigSpec.Builder builder) {
    super(builder);

    builder.comment("Client configuration for MinersAdvantage")
        // .translation("minersadvantage.client.comment")
        .push("client");

    disableParticleEffects = builder
        .comment("Disable Particle Effects generated during block harvesting.")
        .translation("minersadvantage.client.disable_particle_effects")
        .define("disable_particle_effects", MAConfig_Defaults.Client.disableParticleEffects);

    builder.pop();

  }

  /**
   * @return disableParticleEffects
   */
  public boolean disableParticleEffects() {
    return disableParticleEffects.get();
  }

  /**
   * @param disableParticleEffects Sets disableParticleEffects
   */
  public void setDisableParticleEffects(boolean value) {
    disableParticleEffects.set(value);

    // if (value && MAParticleManager.getOriginal() != null) { ClientFunctions.mc.particles =
    // MAParticleManager.getOriginal(); }
  }

}
