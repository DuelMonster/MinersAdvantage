package de.erdbeerbaerlp.guilib.components;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.lang3.ArrayUtils;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import de.erdbeerbaerlp.guilib.util.ImageUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Button extends GuiComponent {
	protected DynamicTexture   BUTTON_ICON_IMAGE;
	protected ResourceLocation BUTTON_ICON;
	protected String           displayString;
	private Runnable           callback;
	private String             errorTooltip = "";
	
	/**
	 * Creates a Button
	 *
	 * @param xPos x position
	 * @param yPos y position
	 * @param displayString Button Text
	 */
	public Button(int xPos, int yPos, String displayString) {
		this(xPos, yPos, 100, displayString);
	}
	
	/**
	 * Creates a Button
	 *
	 * @param xPos x position
	 * @param yPos y position
	 * @param width Button width
	 * @param displayString Button Text
	 */
	public Button(int xPos, int yPos, int width, String displayString) {
		this(xPos, yPos, width, 20, displayString);
	}
	
	/**
	 * Creates a Button with icon
	 *
	 * @param xPos x position
	 * @param yPos y position
	 * @param width Button width
	 * @param displayString Button Text
	 * @param icon ResourceLocation to an Icon image (16x16!)
	 */
	public Button(int xPos, int yPos, int width, String displayString, ResourceLocation icon) {
		this(xPos, yPos, width, 20, displayString, icon);
	}
	
	/**
	 * Creates a Button
	 *
	 * @param xPos x position
	 * @param yPos y position
	 * @param width Button width
	 * @param height Button height
	 * @param displayString Button Text
	 */
	public Button(int xPos, int yPos, int width, int height, String displayString) {
		this(xPos, yPos, width, height, displayString, (ResourceLocation) null);
	}
	
	/**
	 * Creates a Button with icon
	 *
	 * @param xPos x position
	 * @param yPos y position
	 * @param width Button width
	 * @param height Button height
	 * @param displayString Button Text
	 * @param icon ResourceLocation to an Icon image (16x16!)
	 */
	public Button(int xPos, int yPos, int width, int height, String displayString, ResourceLocation icon) {
		super(xPos, yPos, width, height);
		this.BUTTON_ICON   = icon;
		this.displayString = displayString;
	}
	
	/**
	 * Creates a Button with icon
	 *
	 * @param xPos x position
	 * @param yPos y position
	 * @param width Button width
	 * @param height Button height
	 * @param displayString Button Text
	 * @param iconURL URL to an Icon image (16x16!)
	 */
	public Button(int xPos, int yPos, int width, int height, String displayString, String iconURL) {
		this(xPos, yPos, width, height, displayString);
		try {
			this.BUTTON_ICON_IMAGE = new DynamicTexture(ImageUtil.getImageFromIS(ImageUtil.getInputStreamFromImageURL(iconURL), false, 16, 16, true));
		}
		catch (IOException e) {
			errorTooltip = e.getCause().getLocalizedMessage();
		}
		this.displayString = displayString;
	}
	
	/**
	 * Creates a Button with icon
	 *
	 * @param xPos x position
	 * @param yPos y position
	 * @param width Button width
	 * @param height Button height
	 * @param displayString Button Text
	 * @param iconURL URL to an Icon image (16x16!)
	 */
	public Button(int xPos, int yPos, int width, int height, String displayString, URL iconURL) {
		this(xPos, yPos, width, height, displayString, iconURL.toString());
		
	}
	
	/**
	 * Creates a Button with icon
	 *
	 * @param xPos x position
	 * @param yPos y position
	 * @param width Button width
	 * @param height Button height
	 * @param displayString Button Text
	 * @param icon Icon as NativeImage (16x16!)
	 */
	public Button(int xPos, int yPos, int width, int height, String displayString, NativeImage icon) {
		super(xPos, yPos, width, height);
		this.BUTTON_ICON_IMAGE = new DynamicTexture(icon);
		this.displayString     = displayString;
	}
	
	/**
	 * Creates a Button with icon
	 *
	 * @param xPos x position
	 * @param yPos y position
	 * @param displayString Button text
	 * @param icon ResourceLocation to an Icon image (16x16!)
	 */
	public Button(int xPos, int yPos, String displayString, ResourceLocation icon) {
		this(xPos, yPos, 100, displayString, icon);
	}
	
	/**
	 * Creates a Button with icon
	 *
	 * @param xPos x position
	 * @param yPos y position
	 * @param icon ResourceLocation to an Icon image (16x16!)
	 */
	public Button(int xPos, int yPos, ResourceLocation icon) {
		this(xPos, yPos, 20, "", icon);
	}
	
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partial) {
		int color = 14737632;
		this.isHovered = mouseX >= getX() && mouseY >= getY() && mouseX < this.getX() + this.width && mouseY < getY() + this.height;
		if (packedFGColor != 0) {
			color = packedFGColor;
		} else if (!this.isEnabled()) {
			color = 10526880;
		} else if (this.hovered) {
			color = 16777120;
		}
		mc.getTextureManager().bindTexture(WIDGETS_LOCATION);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
		int i = this.getYImage(this.isHovered());
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		this.blit(matrixStack, getX(), getY(), 0, 46 + i * 20, this.width / 2, this.height);
		this.blit(matrixStack, getX() + this.width / 2, getY(), 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
		this.renderBg(matrixStack, mc, mouseX, mouseY);
		// int j = getFGColor();
		int bx     = this.getX();
		int mwidth = this.width;
		if (BUTTON_ICON != null && BUTTON_ICON_IMAGE == null) {
			Minecraft.getInstance().getTextureManager().bindTexture(BUTTON_ICON);
			blit(matrixStack, bx + 2, getY() + 2, 0, 0, 16, 16, 16, 16);
			
			// ! MODIFY X !
			bx     += 2 + 16;
			mwidth -= 16;
		} else if (BUTTON_ICON_IMAGE != null && BUTTON_ICON == null) {
			mc.getTextureManager().bindTexture(mc.getTextureManager().getDynamicTextureLocation("icon", BUTTON_ICON_IMAGE));
			blit(matrixStack, bx + 2, getY(), 0, 0, 16, 16, 16, 16);
			bx     += 2 + 16;
			mwidth -= 16;
		} else // noinspection ConstantConditions
			if (BUTTON_ICON_IMAGE == null && BUTTON_ICON == null && !errorTooltip.equals("")) {
				mc.getTextureManager().bindTexture(errorIcon);
				blit(matrixStack, bx + 2, getY(), 0, 0, 16, 16, 16, 16);
				bx += 2 + 16;
				mwidth -= 16;
			}
		String buttonText    = this.displayString;
		int    strWidth      = mc.fontRenderer.getStringWidth(buttonText);
		int    ellipsisWidth = mc.fontRenderer.getStringWidth("...");
		if (strWidth > mwidth - 6 && strWidth > ellipsisWidth)
			buttonText = mc.fontRenderer.func_238412_a_(buttonText, mwidth - 6 - ellipsisWidth).trim() + "...";
		
		drawCenteredString(matrixStack, mc.fontRenderer, buttonText, bx + mwidth / 2, this.getY() + (this.height - 8) / 2, color);
	}
	
	@Override
	public void mouseClick(double mouseX, double mouseY, int mouseButton) {
		playPressSound();
		if (enabled) onClick();
	}
	
	@Override
	public void mouseRelease(double mouseX, double mouseY, int state) {
		
	}
	
	@Override
	public boolean charTyped(char typedChar, int keyCode) {
		return false;
	}
	
	public final void setText(String text) {
		this.displayString = text;
	}
	
	/**
	 * Add a click listener / callback
	 *
	 * @param r Listener
	 */
	public final void setClickListener(Runnable r) {
		this.callback = r;
	}
	
	/**
	 * Gets called on button click
	 */
	public void onClick() {
		if (this.callback != null) this.callback.run();
	}
	
	@Override
	public String[] getTooltips() {
		if (BUTTON_ICON == null && BUTTON_ICON_IMAGE == null && !errorTooltip.isEmpty()) {
			return ArrayUtils.addAll(super.getTooltips(), "", "\u00A7cError loading image:", "\u00A7c" + errorTooltip);
		}
		return super.getTooltips();
	}
	
	public static class DefaultButtonIcons {
		public static final ResourceLocation ADD         = new ResourceLocation("eguilib:textures/gui/buttonicons/add.png");
		public static final ResourceLocation DELETE      = new ResourceLocation("eguilib:textures/gui/buttonicons/delete.png");
		public static final ResourceLocation PLAY        = new ResourceLocation("eguilib:textures/gui/buttonicons/play.png");
		public static final ResourceLocation PAUSE       = new ResourceLocation("eguilib:textures/gui/buttonicons/pause.png");
		public static final ResourceLocation STOP        = new ResourceLocation("eguilib:textures/gui/buttonicons/stop.png");
		public static final ResourceLocation SAVE        = new ResourceLocation("eguilib:textures/gui/buttonicons/save.png");
		public static final ResourceLocation NEW         = new ResourceLocation("eguilib:textures/gui/buttonicons/new.png");
		public static final ResourceLocation FILE        = new ResourceLocation("eguilib:textures/gui/buttonicons/file.png");
		public static final ResourceLocation FILE_TXT    = new ResourceLocation("eguilib:textures/gui/buttonicons/file_txt.png");
		public static final ResourceLocation FILE_NBT    = new ResourceLocation("eguilib:textures/gui/buttonicons/file_nbt.png");
		public static final ResourceLocation FILE_BIN    = new ResourceLocation("eguilib:textures/gui/buttonicons/file_bin.png");
		public static final ResourceLocation ARROW_RIGHT = new ResourceLocation("eguilib:textures/gui/buttonicons/arrow-right.png");
		public static final ResourceLocation ARROW_LEFT  = new ResourceLocation("eguilib:textures/gui/buttonicons/arrow-left.png");
	}
}
