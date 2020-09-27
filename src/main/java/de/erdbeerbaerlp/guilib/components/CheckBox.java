package de.erdbeerbaerlp.guilib.components;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.gui.GuiUtils;

@OnlyIn(Dist.CLIENT)
public class CheckBox extends GuiComponent {
	// private static final ResourceLocation CHECKBOX_LOCATION = new ResourceLocation("minecraft", "textures/gui/checkbox.png");
	private final int    boxWidth;
	private final String displayString;
	private Runnable     callback;
	private boolean      isChecked;
	
	/**
	 * Creates an new checkbox
	 *
	 * @param xPos X position
	 * @param yPos Y position
	 * @param displayString Text to display next to the box
	 * @param isChecked Default state
	 */
	@SuppressWarnings("resource")
	public CheckBox(int xPos, int yPos, String displayString, boolean isChecked) {
		super(xPos, yPos, 0, 0);
		this.displayString = displayString;
		this.isChecked     = isChecked;
		this.boxWidth      = 11;
		this.height        = 11;
		this.width         = this.boxWidth + 2 + Minecraft.getInstance().fontRenderer.getStringWidth(displayString);
	}
	
	/**
	 * Creates an new checkbox
	 *
	 * @param xPos X position
	 * @param yPos Y position
	 * @param displayString Text to display next to the box
	 */
	public CheckBox(int xPos, int yPos, String displayString) {
		this(xPos, yPos, displayString, false);
	}
	
	/**
	 * Creates an new textless checkbox
	 *
	 * @param xPos X position
	 * @param yPos Y position
	 */
	public CheckBox(int xPos, int yPos) {
		this(xPos, yPos, "", false);
	}
	
	/**
	 * Creates an new checkbox
	 *
	 * @param xPos X position
	 * @param yPos Y position
	 * @param height Height of the checkbox
	 * @param width Width of the checkbox
	 */
	public CheckBox(int xPos, int yPos, int width, int height) {
		this(xPos, yPos, "", false);
		this.width  = width;
		this.height = height;
	}
	
	/**
	 * Checks if the checkbox was checked
	 *
	 * @return checked?
	 */
	public boolean isChecked() {
		return this.isChecked;
	}
	
	/**
	 * Checks or unchecks the checkbox
	 *
	 * @param isChecked state
	 */
	public void setIsChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}
	
	/**
	 * Gets called on check/uncheck
	 */
	public void onChange() {
		if (this.callback != null) this.callback.run();
	}
	
	/**
	 * Adds an change listener to the checkbox
	 *
	 * @param r listener
	 */
	public final void setChangeListener(Runnable r) {
		this.callback = r;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partial) {
		this.isHovered = mouseX >= getX() && mouseY >= getY() && mouseX < this.getX() + this.width && mouseY < getY() + this.height;
		int color = 14737632;
		if (packedFGColor != 0) {
			color = packedFGColor;
		} else if (!this.enabled) {
			color = 10526880;
		} else if (this.hovered) {
			color = 16777120;
		}
		GuiUtils.drawContinuousTexturedBox(WIDGETS_LOCATION, this.getX(), this.getY(), 0, 46, this.boxWidth, this.height, 200, 20, 2, 3, 2, 2, this.getBlitOffset());
		if (this.isChecked)
			drawCenteredString(matrixStack, mc.fontRenderer, "x", this.getX() + this.boxWidth / 2 + 1, this.getY() + 1, 14737632);
		
		drawString(matrixStack, mc.fontRenderer, displayString, this.getX() + this.boxWidth + 2, this.getY() + 2, color);
	}
	
	@Override
	public void mouseClick(double mouseX, double mouseY, int mouseButton) {
		playPressSound();
		if (isEnabled()) {
			this.setIsChecked(!isChecked());
			onChange();
		}
		
	}
	
	@Override
	public void mouseRelease(double mouseX, double mouseY, int state) {
		
	}
	
	@Override
	public boolean charTyped(char typedChar, int keyCode) {
		return false;
	}
}
