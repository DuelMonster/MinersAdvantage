package co.uk.duelmonster.minersadvantage.client;

import co.uk.duelmonster.minersadvantage.settings.Settings;
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
		if (Settings.get().bDisableParticleEffects() && !(effect instanceof ParticleDigging))
			super.addEffect(effect);
	}
	
	// @Override
	// public Particle spawnEffectParticle(int particleId, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... parameters) {
	// if (Settings.get().bDisableParticleEffects()
	// && (particleId == EnumParticleTypes.FOOTSTEP.getParticleID()
	// || particleId == EnumParticleTypes.BLOCK_CRACK.getParticleID()
	// || particleId == EnumParticleTypes.ITEM_CRACK.getParticleID()
	// || particleId == EnumParticleTypes.BLOCK_DUST.getParticleID()))
	// return null;
	// return super.spawnEffectParticle(particleId, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed, parameters);
	// }
	
}
