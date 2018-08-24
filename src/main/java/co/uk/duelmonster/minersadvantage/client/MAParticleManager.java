package co.uk.duelmonster.minersadvantage.client;

import co.uk.duelmonster.minersadvantage.config.MAConfig;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleDigging;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MAParticleManager extends ParticleManager {
	
	public MAParticleManager(World worldIn, TextureManager rendererIn) {
		super(worldIn, rendererIn);
	}
	
	@Override
	public void addEffect(Particle effect) {
		if (!MAConfig.get().common.bDisableParticleEffects() || !(effect instanceof ParticleDigging))
			super.addEffect(effect);
	}
}
