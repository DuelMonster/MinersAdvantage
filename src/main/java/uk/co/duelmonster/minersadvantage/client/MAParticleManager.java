package uk.co.duelmonster.minersadvantage.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import uk.co.duelmonster.minersadvantage.config.MAConfig;

@OnlyIn(Dist.CLIENT)
public class MAParticleManager extends ParticleEngine {

  private static ParticleEngine MA_ParticleManager;
  private static ParticleEngine MC_ParticleManager;

  public MAParticleManager(ClientLevel worldIn, TextureManager rendererIn) {
    super(worldIn, rendererIn);
  }

  public static ParticleEngine get() {
    return MA_ParticleManager;
  }

  public static ParticleEngine set(MAParticleManager value) {
    MA_ParticleManager = value;
    return value;
  }

  public static ParticleEngine getOriginal() {
    return MC_ParticleManager;
  }

  public static void setOriginal(ParticleEngine value) {
    MC_ParticleManager = value;
  }

  @Override
  public void add(Particle effect) {
    if (!MAConfig.CLIENT.disableParticleEffects() || !(effect instanceof TerrainParticle))
      super.add(effect);
  }
}
