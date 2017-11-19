package co.uk.duelmonster.minersadvantage.client.config;

import java.util.Set;

import co.uk.duelmonster.minersadvantage.client.gui.GuiConfigMA;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ConfigGuiFactory implements IModGuiFactory {
	@Override
	public void initialize(Minecraft minecraftInstance) {}
	
	@Override
	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
		return null;
	}
	
	@Override
	public GuiScreen createConfigGui(GuiScreen pGUI) {
		return new GuiConfigMA(pGUI);
	}
	
	@Override
	public boolean hasConfigGui() {
		return true;
	}
}
