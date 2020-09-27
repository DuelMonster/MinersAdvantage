package de.erdbeerbaerlp.guilib;

import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.connect.IMixinConnector;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiLibMixinConnector implements IMixinConnector {
	/**
	 * Connect to Mixin
	 */
	@Override
	public void connect() {
		Mixins.addConfiguration("mixins.eguilib.json");
	}
}