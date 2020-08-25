package de.erdbeerbaerlp.guilib.components;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;

public class EnumSliderExt extends Slider {
	private final Runnable  action;
	private final Enum<?>[] enumValues;
	private Enum<?>         enumValue;
	private int             prevIndex;
	
	/**
	 * Creates an new Enum slider
	 *
	 * @param xPos X position
	 * @param yPos Y position
	 * @param prefix String to display before the enum value
	 * @param enumClass Class of the enum
	 * @param currentVal Current value
	 * @param changeAction Runnable being called on value change
	 * @param <T> Your enum
	 */
	public <T extends Enum<T>> EnumSliderExt(int xPos, int yPos, String prefix, Class<T> enumClass, T currentVal, Runnable changeAction) {
		this(xPos, yPos, 150, 20, prefix, "", enumClass, currentVal, true, changeAction);
	}
	
	/**
	 * Creates an new Enum slider
	 *
	 * @param xPos X position
	 * @param yPos Y position
	 * @param prefix String to display before the enum value
	 * @param suf String to display behind the enum value
	 * @param enumClass Class of the enum
	 * @param width Width of the slider
	 * @param height Height of the slider
	 * @param drawStr Should the slider draw an string?
	 * @param currentVal Current value
	 * @param <T> Your enum
	 */
	public <T extends Enum<T>> EnumSliderExt(int xPos, int yPos, int width, int height, String prefix, String suf, Class<T> enumClass, T currentVal, boolean drawStr) {
		this(xPos, yPos, width, height, prefix, suf, enumClass, currentVal, drawStr, null);
	}
	
	/**
	 * Creates an new Enum slider
	 *
	 * @param xPos X position
	 * @param yPos Y position
	 * @param prefix String to display before the enum value
	 * @param suf String to display behind the enum value
	 * @param enumClass Class of the enum
	 * @param width Width of the slider
	 * @param height Height of the slider
	 * @param drawStr Should the slider draw an string?
	 * @param currentVal Current value
	 * @param <T> Your enum
	 * @param changeAction Runnable being called on value change
	 */
	public <T extends Enum<T>> EnumSliderExt(int xPos, int yPos, int width, int height, String prefix, String suf, Class<T> enumClass, T currentVal, boolean drawStr, Runnable changeAction) {
		super(xPos, yPos, width, height, prefix, suf, -1, -1, -1, false, drawStr, null);
		
		this.action      = changeAction;
		this.enumValue   = currentVal;
		this.enumValues  = enumClass.getEnumConstants();
		this.maxValue    = enumValues.length;
		this.showDecimal = false;
		this.minValue    = 0;
		this.sliderValue = (getCurrentIndex() - minValue) / (maxValue - minValue);
		prevIndex        = getCurrentIndex();
		// Try to get custom name
		String val = "";
		for (Method m : this.enumValue.getClass().getMethods()) {
			if (m.getName().equals("getName") && m.getParameterTypes().length == 0 && m.getReturnType() == String.class) {
				if (!m.isAccessible()) m.setAccessible(true);
				try {
					val = (String) m.invoke(enumValue);
				}
				catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
		displayString = dispString + (val.isEmpty() ? this.enumValue.name() : val) + suffix;
		
		drawString = drawStr;
		if (!drawString) {
			displayString = "";
		}
		
	}
	
	/**
	 * Gets the enum value
	 *
	 * @return Value
	 */
	public Enum<?> getEnum() {
		return enumValue;
	}
	
	/**
	 * Sets the enum value
	 *
	 * @return void
	 */
	public void setEnum(Enum<?> value) {
		enumValue   = value;
		sliderValue = (getCurrentIndex() - minValue) / (maxValue - minValue);
		prevIndex   = getCurrentIndex();
	}
	
	@Override
	public void updateSlider() {
		if (this.sliderValue < 0.0F) {
			this.sliderValue = 0.0F;
		}
		
		if (this.sliderValue > 1.0F) {
			this.sliderValue = 1.0F;
		}
		String val = this.enumValue.name();
		// Try to get custom name
		for (Method m : this.enumValue.getClass().getMethods()) {
			if (m.getName().equals("getName") && m.getParameterTypes().length == 0 && m.getReturnType() == String.class) {
				if (!m.isAccessible()) m.setAccessible(true);
				try {
					val = (String) m.invoke(enumValue);
				}
				catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
		
		if (drawString) {
			displayString = dispString + val + suffix;
		}
		if (prevIndex != getCurrentIndex()) this.onValueChanged();
		this.prevIndex = getCurrentIndex();
	}
	
	/**
	 * Gets called when the value changes
	 */
	@Override
	public void onValueChanged() {
		if (this.action != null) action.run();
	}
	
	private int getCurrentIndex() {
		int out = 0;
		for (Enum<?> e : this.enumValues) {
			if (e.name().equals(this.enumValue.name())) return out;
			out++;
		}
		return -1;
	}
	
	@Override
	public void mouseClick(double mouseX, double mouseY, int mouseButton) {
		playPressSound();
		if (isEnabled()) {
			updateSlider();
			this.dragging = true;
		}
	}
	
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partial) {
		if (this.dragging) {
			final int index = getCurrentIndex();
			this.sliderValue = (float) (mouseX - (this.getX() + 4)) / (float) (this.width - 8);
			int sliderValue = (int) Math.round(this.sliderValue * (maxValue - minValue) + this.minValue);
			if (sliderValue < 0) sliderValue = 0;
			if (index == -1) {
				this.enumValue = enumValues[0];
			} else {
				if (sliderValue < this.enumValues.length) {
					this.enumValue = enumValues[sliderValue];
				}
			}
		}
		
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		
		super.render(matrixStack, mouseX, mouseY, partial);
	}
}
