package uk.co.duelmonster.minersadvantage.client;

import net.minecraft.client.particle.DiggingParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import uk.co.duelmonster.minersadvantage.config.MAConfig;

@OnlyIn(Dist.CLIENT)
public class MAParticleManager extends ParticleManager {
	
	private static ParticleManager	MA_ParticleManager;
	private static ParticleManager	MC_ParticleManager;
	
	public MAParticleManager(World worldIn, TextureManager rendererIn) {
		super(worldIn, rendererIn);
	}
	
	public static ParticleManager get() {
		return MA_ParticleManager;
	}
	
	public static ParticleManager set(MAParticleManager value) {
		MA_ParticleManager = value;
		return value;
	}
	
	public static ParticleManager getOriginal() {
		return MC_ParticleManager;
	}
	
	public static void setOriginal(ParticleManager value) {
		MC_ParticleManager = value;
	}
	
	@Override
	public void addEffect(Particle effect) {
		if (!MAConfig.CLIENT.disableParticleEffects() || !(effect instanceof DiggingParticle))
			super.addEffect(effect);
	}
}
