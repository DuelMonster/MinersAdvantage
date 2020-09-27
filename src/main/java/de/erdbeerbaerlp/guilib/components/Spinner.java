package de.erdbeerbaerlp.guilib.components;

import java.io.IOException;
import java.util.UUID;

import com.mojang.blaze3d.matrix.MatrixStack;

import de.erdbeerbaerlp.guilib.McMod;
import de.erdbeerbaerlp.guilib.util.ImageUtil;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Spinner extends GuiComponent {
	private final DynamicTexture   spinnerTexture = new DynamicTexture(32, 32, true);
	private final UUID             spinnerUUID    = UUID.randomUUID();
	private final ResourceLocation spinnerRes;
	private GifThread              loadingGif;
	
	/**
	 * Creates an spinner with default spinner texture
	 */
	public Spinner(int x, int y) {
		this(x, y, new ResourceLocation(McMod.MODID, "textures/gui/loading.gif"));
	}
	
	/**
	 * Creates an spinner
	 *
	 * @param spinnerTexture ResourceLocation to custom spinner texture, HAS TO BE 32x32
	 */
	public Spinner(int x, int y, final ResourceLocation spinnerTexture) {
		super(x, y, 32, 32);
		spinnerRes = spinnerTexture;
	}
	
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partial) {
		if (loadingGif == null || !loadingGif.isAlive()) {
			try {
				loadingGif = new GifThread(ImageUtil.convertToByteArrayIS(mc.getResourceManager().getResource(this.spinnerRes).getInputStream()), spinnerTexture, true, true, true);
			}
			catch (IOException ignored) {}
		}
		if (!loadingGif.isAlive()) loadingGif.start();
		mc.getTextureManager().bindTexture(mc.getTextureManager().getDynamicTextureLocation("spinner_" + spinnerUUID.toString().toLowerCase(), spinnerTexture));
		blit(matrixStack, getX() + getWidth() / 2 - 16, getY() + getHeight() / 2 - 16, 0, 0, 32, 32, 32, 32);
	}
	
	/**
	 * Has no effect
	 */
	@Override
	@Deprecated
	public void setWidth(int width) {}
	
	/**
	 * Has no effect
	 */
	@Override
	@Deprecated
	public void setHeight(int height) {}
	
	@Override
	public void mouseClick(double mouseX, double mouseY, int mouseButton) {
		
	}
	
	@Override
	public void mouseRelease(double mouseX, double mouseY, int state) {
		
	}
	
	@Override
	public boolean charTyped(char typedChar, int keyCode) {
		return false;
	}
	
	/**
	 * Pauses this spinner
	 */
	public void stop() {
		loadingGif.pause();
	}
	
	/**
	 * Resumes this spinner
	 */
	public void resume() {
		loadingGif.play();
	}
	
	/**
	 * @return true, if the spinner is currently not paused
	 */
	public boolean isSpinning() {
		return !loadingGif.isPaused();
	}
}
