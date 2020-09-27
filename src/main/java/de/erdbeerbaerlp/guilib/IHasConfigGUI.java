package de.erdbeerbaerlp.guilib;

import net.minecraft.client.gui.screen.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface IHasConfigGUI {
	Screen getConfigGUI(final Screen modList);
}
