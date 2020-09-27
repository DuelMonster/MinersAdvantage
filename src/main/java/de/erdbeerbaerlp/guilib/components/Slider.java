package de.erdbeerbaerlp.guilib.components;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Slider extends GuiComponent {
	protected final String dispString;
	protected final String suffix;
	protected final int    precision;
	protected Runnable     action;
	protected double       prevValue;
	protected double       minValue;
	protected double       maxValue;
	protected double       sliderValue;
	protected boolean      showDecimal;
	protected String       displayString;
	protected boolean      drawString;
	protected boolean      dragging = false;
	
	/**
	 * Creates an Slider
	 *
	 * @param xPos X position
	 * @param yPos Y position
	 * @param prefix String to display before the value
	 * @param minVal minimum value
	 * @param maxVal maximum value
	 * @param currentVal current value
	 * @param changeAction Runnable being called on value change
	 */
	public Slider(int xPos, int yPos, String prefix, double minVal, double maxVal, double currentVal, Runnable changeAction) {
		this(xPos, yPos, 150, 20, prefix, "", minVal, maxVal, currentVal, true, true, changeAction);
	}
	
	/**
	 * Creates an Slider
	 *
	 * @param xPos X position
	 * @param yPos Y position
	 * @param prefix String to display before the value
	 * @param minVal minimum value
	 * @param maxVal maximum value
	 * @param currentVal current value
	 * @param drawStr should the slide draw an string?
	 * @param height Slider height
	 * @param width Slider width
	 * @param suf String to display behind the value
	 * @param showDec Should the slider show decimal values?
	 */
	public Slider(int xPos, int yPos, int width, int height, String prefix, String suf, double minVal, double maxVal, double currentVal, boolean showDec, boolean drawStr) {
		this(xPos, yPos, width, height, prefix, suf, minVal, maxVal, currentVal, showDec, drawStr, null);
	}
	
	/**
	 * Creates an Slider
	 *
	 * @param xPos X position
	 * @param yPos Y position
	 * @param prefix String to display before the value
	 * @param minVal minimum value
	 * @param maxVal maximum value
	 * @param currentVal current value
	 * @param changeAction Runnable being called on value change
	 * @param drawStr should the slide draw an string?
	 * @param height Slider height
	 * @param width Slider width
	 * @param suf String to display behind the value
	 * @param showDec Should the slider show decimal values?
	 */
	public Slider(int xPos, int yPos, int width, int height, String prefix, String suf, double minVal, double maxVal, double currentVal, boolean showDec, boolean drawStr, Runnable changeAction) {
		super(xPos, yPos, width, height);
		this.action = changeAction;
		minValue    = minVal;
		maxValue    = maxVal;
		sliderValue = prevValue = (currentVal - minValue) / (maxValue - minValue);
		dispString  = prefix;
		suffix      = suf;
		showDecimal = showDec;
		String val;
		
		if (showDecimal) {
			val       = Double.toString(sliderValue * (maxValue - minValue) + minValue);
			precision = Math.min(val.substring(val.indexOf(".") + 1).length(), 4);
		} else {
			val       = Integer.toString((int) Math.round(sliderValue * (maxValue - minValue) + minValue));
			precision = 0;
		}
		
		displayString = dispString + val + suffix;
		
		drawString = drawStr;
		if (!drawString) {
			displayString = "";
		}
		
	}
	
	/**
	 * Sets the value change action
	 *
	 * @param action runnable being run on value change
	 */
	public void setAction(Runnable action) {
		this.action = action;
	}
	
	public void setShowDecimal(boolean showDecimal) {
		this.showDecimal = showDecimal;
	}
	
	public void updateSlider() {
		if (this.sliderValue < 0.0F) {
			this.sliderValue = 0.0F;
		}
		
		if (this.sliderValue > 1.0F) {
			this.sliderValue = 1.0F;
		}
		
		String val;
		
		if (showDecimal) {
			val = Double.toString(sliderValue * (maxValue - minValue) + minValue);
			
			if (val.substring(val.indexOf(".") + 1).length() > precision) {
				val = val.substring(0, val.indexOf(".") + precision + 1);
				
				if (val.endsWith(".")) {
					val = val.substring(0, val.indexOf(".") + precision);
				}
			} else {
				while (val.substring(val.indexOf(".") + 1).length() < precision) {
					val = val + "0";
				}
			}
		} else {
			val = Integer.toString((int) Math.round(sliderValue * (maxValue - minValue) + minValue));
		}
		
		if (drawString) {
			displayString = dispString + val + suffix;
			setMessage(new StringTextComponent(displayString));
		}
		if (prevValue != getValue()) onValueChanged();
		prevValue = getValue();
	}
	
	public int getValueInt() {
		return (int) Math.round(sliderValue * (maxValue - minValue) + minValue);
	}
	
	public double getValue() {
		return sliderValue * (maxValue - minValue) + minValue;
	}
	
	public void setValue(double d) {
		this.sliderValue = (d - minValue) / (maxValue - minValue);
		updateSlider();
	}
	
	/**
	 * Gets called on value change
	 */
	public void onValueChanged() {
		if (this.action != null) action.run();
	}
	
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partial) {
		
		int color = 14737632;
		this.isHovered = mouseX >= getX() && mouseY >= getY() && mouseX < this.getX() + this.width && mouseY < getY() + this.height;
		if (packedFGColor != 0) {
			color = packedFGColor;
		} else if (!this.enabled) {
			color = 10526880;
		} else if (this.hovered) {
			color = 16777120;
		}
		mc.getTextureManager().bindTexture(WIDGETS_LOCATION);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		this.blit(matrixStack, getX(), getY(), 0, 46, this.width / 2, this.height);
		this.blit(matrixStack, getX() + this.width / 2, getY(), 200 - this.width / 2, 46, this.width / 2, this.height);
		if (this.dragging) {
			this.sliderValue = (mouseX - (this.getX() + 4)) / (float) (this.width - 8);
			updateSlider();
		}
		
		if (isEnabled()) {
			int i = (this.isHovered() ? 2 : 1) * 20;
			this.blit(matrixStack, this.getX() + (int) (this.sliderValue * (this.width - 8)), this.getY(), 0, 46 + i, 4, 20);
			this.blit(matrixStack, this.getX() + (int) (this.sliderValue * (this.width - 8)) + 4, this.getY(), 196, 46 + i, 4, 20);
		}
		int    bx            = this.getX();
		int    mwidth        = this.width;
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
		if (isEnabled()) {
			this.sliderValue = (mouseX - (this.getX() + 4)) / (this.width - 8);
			updateSlider();
			this.dragging = true;
		}
	}
	
	@Override
	public void mouseRelease(double mouseX, double mouseY, int state) {
		this.dragging = false;
	}
	
	@Override
	public boolean charTyped(char typedChar, int keyCode) {
		return false;
	}
	
	public void setMaxValue(double maxValue) {
		this.maxValue = maxValue;
		if (sliderValue > maxValue) sliderValue = maxValue;
		updateSlider();
	}
	
	public void setMinValue(double minValue) {
		this.minValue = minValue;
		if (sliderValue < minValue) sliderValue = minValue;
		updateSlider();
	}
}
