package de.erdbeerbaerlp.guilib.components;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.resources.I18n;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@SuppressWarnings("unused")
public class Label extends GuiComponent {
	private final List<String> labels;
	private final int          textColor;
	private boolean            centered;
	private int                backColor;
	private int                ulColor;
	private int                brColor;
	private int                border;
	
	/**
	 * Creates a new Label
	 *
	 * @param x X position
	 * @param y Y position
	 * @param color Custom text color
	 */
	public Label(int x, int y, int color) {
		super(x, y, 0, 0);
		this.labels    = Lists.newArrayList();
		this.textColor = color;
		this.setX(x);
		this.setY(y);
		this.visible = true;
	}
	
	/**
	 * Creates a new Label
	 *
	 * @param x X position
	 * @param y Y position
	 */
	public Label(int x, int y) {
		this(x, y, -1);
	}
	
	/**
	 * Creates a new Label
	 *
	 * @param x X position
	 * @param y Y position
	 * @param text Label text (1. line)
	 */
	public Label(String text, int x, int y) {
		this(x, y);
		this.addLine(text);
	}
	
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partial) {
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		int i = this.getY() + this.height / 2 + this.border / 2;
		int j = i - this.labels.size() * 10 / 2;
		
		for (int k = 0; k < this.labels.size(); ++k) {
			if (this.centered) {
				drawCenteredString(matrixStack, this.fontRenderer, this.labels.get(k), this.getX(), j + k * 10, this.textColor);
			} else {
				drawString(matrixStack, this.fontRenderer, this.labels.get(k), this.getX(), j + k * 10, this.textColor);
			}
		}
	}
	
	/**
	 * Adds an line of text
	 *
	 * @param p_175202_1_ Text
	 */
	public void addLine(String p_175202_1_) {
		this.labels.add(I18n.format(p_175202_1_));
	}
	
	/**
	 * Sets the label text
	 * Use \n for an new line
	 *
	 * @param text Text
	 */
	public void setText(String text) {
		this.labels.clear();
		labels.addAll(Arrays.asList(text.split("\n")));
	}
	
	/**
	 * Sets the Label to be centered
	 */
	public void setCentered() {
		this.centered = true;
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
}
