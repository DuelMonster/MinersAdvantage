package de.erdbeerbaerlp.guilib.components;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.BossInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ProgressBar extends GuiComponent {
	private static final ResourceLocation GUI_BARS_TEXTURES = new ResourceLocation("textures/gui/bars.png");
	
	private boolean        showPercentText = true;
	private String         text            = "";
	private BossInfo.Color color           = BossInfo.Color.BLUE;
	
	private int maxValue  = 100;
	private int value     = 0;
	private int precision = 1;
	
	public ProgressBar(int x, int y, int widthIn) {
		super(x, y, widthIn, 15);
	}
	
	public void setMaxValue(int maxValue) {
		if (maxValue <= 0) throw new IllegalArgumentException("Max value can not be smaller than 1");
		this.maxValue = maxValue;
		if (value > maxValue) value = maxValue;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public void setColor(BossInfo.Color color) {
		this.color = color;
	}
	
	public void setShowPercentText(boolean showPercentText) {
		this.showPercentText = showPercentText;
	}
	
	@Override
	public void setHeight(int height) {}
	
	public void setPrecision(int precision) {
		this.precision = precision;
	}
	
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partial) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(GUI_BARS_TEXTURES);
		int progress = (int) (getPercent() * getWidth());
		
		for (int w = 0; w <= getWidth(); w++) {
			if (w == 0) {
				this.blit(matrixStack, getX(), getY() + 10, 0, color.ordinal() * 5 * 2, 1, 5);
				if (progress > 0) {
					this.blit(matrixStack, getX(), getY() + 10, 0, color.ordinal() * 5 * 2 + 5, 1, 5);
				}
			} else if (w == getWidth()) {
				this.blit(matrixStack, getX() + w, getY() + 10, 182, color.ordinal() * 5 * 2, 1, 5);
				if (progress == 1.0f) {
					this.blit(matrixStack, getX() + w, getY() + 10, 182, color.ordinal() * 5 * 2 + 5, 1, 5);
				}
			} else {
				this.blit(matrixStack, getX() + w, getY() + 10, 5, color.ordinal() * 5 * 2, 1, 5);
				if (progress >= w) {
					this.blit(matrixStack, getX() + w, getY() + 10, 5, color.ordinal() * 5 * 2 + 5, 1, 5);
				}
			}
		}
		
		String val = String.valueOf(getPercent() * 100);
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
		
		String s = text + (showPercentText ? (" " + val + "%") : "");
		int    l = mc.fontRenderer.getStringWidth(s);
		mc.fontRenderer.drawStringWithShadow(matrixStack, s, getX() + (getWidth() / 2 - l / 2), getY(), 16777215);
	}
	
	private float getPercent() {
		return ((float) value) / (float) maxValue;
	}
	
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
	
	public void increment(int value) {
		this.value = Math.min(maxValue, this.value + value);
	}
	
	public void decrement(int value) {
		this.value = Math.max(0, this.value - value);
	}
	
	public double getValue() {
		return this.value;
	}
	
	public void setValue(int value) {
		this.value = Math.min(value, maxValue);
	}
}
