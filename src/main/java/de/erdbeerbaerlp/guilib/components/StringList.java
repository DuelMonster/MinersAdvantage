package de.erdbeerbaerlp.guilib.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.gui.GuiUtils;

public class StringList extends GuiComponent {
	
	protected final int                   border              = 4;
	private final int                     barWidth            = 6;
	private final ArrayList<GuiComponent> components          = new ArrayList<>();
	private final ArrayList<Button>       removeButtons       = new ArrayList<>();
	private final ArrayList<TextField>    textFields          = new ArrayList<>();
	protected boolean                     captureMouse        = true;
	private ResourceLocation              BACKGROUND_LOCATION = new ResourceLocation("minecraft", "textures/gui/options_background.png");
	protected int                         top;
	protected int                         bottom;
	protected int                         right;
	protected int                         left;
	protected float                       scrollDistance;
	private int                           barLeft;
	private boolean                       scrolling;
	private int                           contentHeight;
	private List<String>                  listValues;
	private final Button                  btnAdd              = new Button(0, 0, 20, "+");
	
	/**
	 * Creates a new String List component
	 *
	 * @param x X position
	 * @param y Y position
	 * @param width Text Field width
	 * @param height Text Field height
	 * @param values List of array values
	 */
	public StringList(int x, int y, int width, int height, List<String> values) {
		super(x, y, width, height);
		
		this.contentHeight = height;
		
		recalc();
		
		listValues = values != null ? values : new ArrayList<String>();
		
		btnAdd.setClickListener(() -> {
			generateRow((25 * listValues.size()), null);
		});
		
		generateListControls();
	}
	
	private void generateListControls() {
		int yPos = 5;
		
		if (listValues != null && listValues.size() > 0) {
			clearRows();
			
			for (String value : listValues) {
				generateRow(yPos, value);
				
				yPos += 25;
			}
		} else {
			generateRow(yPos, "");
		}
		
		this.addComponent(btnAdd);
	}
	
	private void generateRow(int yPos, String value) {
		
		Button    btnRemove = new Button(5, yPos, 20, "-");
		TextField txtValue  = new TextField(60, yPos, width - barWidth - 5);
		
		btnAdd.setPosition(30, yPos);
		
		txtValue.setText(value);
		txtValue.setCursorPositionZero();
		
		if (value == null || value.isEmpty()) btnRemove.disable();
		
		this.addComponent(btnRemove);
		this.addComponent(txtValue);
		
		removeButtons.add(btnRemove);
		textFields.add(txtValue);
		
		contentHeight += 25;
	}
	
	private void clearRows() {
		clearComponents();
		removeButtons.clear();
		textFields.clear();
	}
	
	public void setPositions() {
		int yPos = 5;
		
		for (int indx = 0; indx < removeButtons.size(); indx++) {
			Button    btnRemove = removeButtons.get(indx);
			TextField txtValue  = textFields.get(indx);
			
			btnRemove.setPosition(5, yPos);
			
			txtValue.setPosition(30, yPos);
			txtValue.setWidth(width - barWidth - 60);
			
			btnRemove.setClickListener(() -> {
				if (!txtValue.getText().isEmpty() && listValues.contains(txtValue.getText())) {
					listValues.remove(txtValue.getText());
					generateListControls();
				}
			});
			
			final int txtY = yPos + 25;
			txtValue.setReturnAction(() -> {
				if (!txtValue.getText().isEmpty() && listValues.contains(txtValue.getText())) {
					listValues.add(txtValue.getText());
					generateRow(txtY, null);
				}
			});
			
			yPos += 25;
		}
		
		btnAdd.setPosition(width - barWidth - 25, yPos - 25);
		
	}
	
	/**
	 * @return the listValues
	 */
	public List<String> getListValues() {
		return listValues;
	}
	
	/**
	 * @param listValues the listValues to set
	 */
	public void setListValues(List<String> listValues) {
		this.listValues = listValues;
		generateListControls();
	}
	
	private void recalc() {
		this.top     = getY();
		this.left    = getX();
		this.bottom  = height + this.top;
		this.right   = width + this.left;
		this.barLeft = this.left + this.getWidth() - barWidth;
	}
	
	@Override
	public void setX(int x) {
		super.setX(x);
		recalc();
	}
	
	@Override
	public void setY(int y) {
		super.setY(y);
		recalc();
	}
	
	public void setBackgroundTexture(ResourceLocation bg) {
		BACKGROUND_LOCATION = bg;
	}
	
	@Override
	public void unload() {
		for (GuiComponent c : components)
			c.unload();
	}
	
	@Override
	public void setWidth(int width) {
		super.setWidth(width);
		recalc();
	}
	
	@Override
	public void setHeight(int height) {
		super.setHeight(height);
		recalc();
	}
	
	protected int getContentHeight() {
		return contentHeight;
	}
	
	public void setContentHeight(int contentHeight) {
		this.contentHeight = contentHeight;
		recalc();
	}
	
	/**
	 * Use this to add your components
	 *
	 * @param component The component to add
	 */
	public final void addComponent(GuiComponent component) {
		if (!this.components.contains(component)) this.components.add(component);
	}
	
	public final void removeComponent(GuiComponent component) {
		if (this.components.contains(component)) this.components.remove(component);
	}
	
	protected void drawBackground() {}
	
	private ArrayList<String> tooltips = new ArrayList<>();
	
	private int getMaxScroll() {
		return this.getContentHeight() - (this.height - this.border);
	}
	
	private void applyScrollLimits() {
		int max = getMaxScroll();
		
		if (max < 0) {
			max /= 2;
		}
		
		if (this.scrollDistance < 0.0F) {
			this.scrollDistance = 0.0F;
		}
		
		if (this.scrollDistance > max) {
			this.scrollDistance = max;
		}
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
		if (scroll != 0) {
			this.scrollDistance += -scroll * getScrollAmount();
			applyScrollLimits();
			return true;
		}
		return false;
	}
	
	protected int getScrollAmount() {
		return 20;
	}
	
	protected void drawPanel(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		for (final GuiComponent c : components) {
			c.scrollOffsetY = this.getY() - (int) scrollDistance;
			c.scrollOffsetX = this.getX();
			if (c.isVisible()) c.render(matrixStack, mouseX, mouseY, partialTicks);
		}
	}
	
	/**
	 * Resizes the content height to just perfectly fit all components
	 */
	public void fitContent() {
		for (final GuiComponent c : components) {
			if (((c.getY() - c.scrollOffsetY) + c.getHeight() + 10) > contentHeight) {
				contentHeight = (c.getY() - c.scrollOffsetY) + c.getHeight() + 10;
			}
		}
	}
	
	@Override
	public boolean canHaveTooltip() {
		return true;
	}
	
	@Override
	public void mouseRelease(double mouseX, double mouseY, int state) {
		this.scrolling = false;
		if (mouseX >= left && mouseX <= right) {
			for (GuiComponent comp : components) {
				if (comp.isVisible())
					comp.mouseReleased(mouseX, mouseY, state);
			}
		}
	}
	
	/**
	 * Use this to add multiple components at once
	 *
	 * @param components The components to add
	 */
	public final void addAllComponents(GuiComponent... components) {
		for (GuiComponent c : components) {
			this.addComponent(c);
		}
	}
	
	public final void clearComponents() {
		components.clear();
	}
	
	@Override
	// public boolean charTyped(char typedChar, int keyCode) {
	public boolean func_231042_a_(char typedChar, int keyCode) {
		
		if (isEnabled())
			for (GuiComponent comp : components) {
				comp.charTyped(typedChar, keyCode);
			}
		return true;
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (isEnabled())
			for (GuiComponent comp : components) {
				comp.keyPressed(keyCode, scanCode, modifiers);
			}
		return true;
	}
	
	@Override
	public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
		for (GuiComponent comp : components) {
			comp.keyReleased(keyCode, scanCode, modifiers);
		}
		return true;
	}
	
	private boolean isMouseInComponent(double mouseX, double mouseY, GuiComponent comp) {
		return (mouseX >= comp.getX() && mouseY >= comp.getY() && mouseX < comp.getX() + comp.getWidth() && mouseY < comp.getY() + comp.getHeight());
	}
	
	private int getBarHeight() {
		int barHeight = (height * height) / this.getContentHeight();
		
		if (barHeight < 32) barHeight = 32;
		
		if (barHeight > height - border * 2)
			barHeight = height - border * 2;
		
		return barHeight;
	}
	
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (isEnabled())
			if (this.scrolling) {
				int maxScroll = height - getBarHeight();
				double moved = deltaY / maxScroll;
				this.scrollDistance += getMaxScroll() * moved;
				applyScrollLimits();
				return true;
			}
		return false;
	}
	
	/**
	 * @return Resource Location of Scroll Panel Background, defaults to {@link AbstractGui#BACKGROUND_LOCATION}
	 */
	protected ResourceLocation getBackground() {
		return BACKGROUND_LOCATION;
	}
	
	@Override
	public final void mouseClick(double mouseX, double mouseY, int mouseButton) {
		if (isEnabled()) {
			this.scrolling = mouseButton == 0 && mouseX >= barLeft && mouseX < barLeft + barWidth;
			if (this.scrolling) {
				return;
			}
			if (mouseX >= left && mouseX <= right) {
				for (GuiComponent comp : components) {
					if (comp.isVisible() && (isMouseInComponent(mouseX, mouseY, comp)) || comp instanceof TextField) {
						comp.mouseClicked(mouseX, mouseY, mouseButton);
						break;
					}
				}
			}
		}
	}
	
	@Override
	// public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
	public void func_230430_a_(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		this.drawBackground();
		Tessellator   tess   = Tessellator.getInstance();
		BufferBuilder worldr = tess.getBuffer();
		double        scale  = mc.getMainWindow().getGuiScaleFactor();
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glScissor((int) (left * scale), (int) (mc.getMainWindow().getFramebufferHeight() - (bottom * scale)),
				(int) (width * scale), (int) (height * scale));
		if (this.mc.world != null) {
			this.drawGradientRect(matrixStack, this.left, this.top, this.right, this.bottom, 0xC0101010, 0xD0101010);
		} else // Draw dark dirt background
		{
			RenderSystem.disableLighting();
			RenderSystem.disableFog();
			this.mc.getTextureManager().bindTexture(getBackground());
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			final float texScale = 32.0F;
			worldr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
			worldr.pos(this.left, this.bottom, 0.0D).tex(this.left / texScale, (this.bottom + (int) this.scrollDistance) / texScale).color(0x20, 0x20, 0x20, 0xFF).endVertex();
			worldr.pos(this.right, this.bottom, 0.0D).tex(this.right / texScale, (this.bottom + (int) this.scrollDistance) / texScale).color(0x20, 0x20, 0x20, 0xFF).endVertex();
			worldr.pos(this.right, this.top, 0.0D).tex(this.right / texScale, (this.top + (int) this.scrollDistance) / texScale).color(0x20, 0x20, 0x20, 0xFF).endVertex();
			worldr.pos(this.left, this.top, 0.0D).tex(this.left / texScale, (this.top + (int) this.scrollDistance) / texScale).color(0x20, 0x20, 0x20, 0xFF).endVertex();
			tess.draw();
		}
		
		this.drawPanel(matrixStack, mouseX, mouseY, partialTicks);
		
		RenderSystem.disableDepthTest();
		
		int extraHeight = (this.getContentHeight() + border) - height;
		if (extraHeight > 0) {
			int barHeight = getBarHeight();
			
			int barTop = (int) this.scrollDistance * (height - barHeight) / extraHeight + this.top;
			if (barTop < this.top) {
				barTop = this.top;
			}
			
			RenderSystem.disableTexture();
			worldr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
			worldr.pos(barLeft, this.bottom, 0.0D).tex(0.0F, 1.0F).color(0x00, 0x00, 0x00, 0xFF).endVertex();
			worldr.pos(barLeft + barWidth, this.bottom, 0.0D).tex(1.0F, 1.0F).color(0x00, 0x00, 0x00, 0xFF).endVertex();
			worldr.pos(barLeft + barWidth, this.top, 0.0D).tex(1.0F, 0.0F).color(0x00, 0x00, 0x00, 0xFF).endVertex();
			worldr.pos(barLeft, this.top, 0.0D).tex(0.0F, 0.0F).color(0x00, 0x00, 0x00, 0xFF).endVertex();
			tess.draw();
			worldr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
			worldr.pos(barLeft, barTop + barHeight, 0.0D).tex(0.0F, 1.0F).color(0x80, 0x80, 0x80, 0xFF).endVertex();
			worldr.pos(barLeft + barWidth, barTop + barHeight, 0.0D).tex(1.0F, 1.0F).color(0x80, 0x80, 0x80, 0xFF).endVertex();
			worldr.pos(barLeft + barWidth, barTop, 0.0D).tex(1.0F, 0.0F).color(0x80, 0x80, 0x80, 0xFF).endVertex();
			worldr.pos(barLeft, barTop, 0.0D).tex(0.0F, 0.0F).color(0x80, 0x80, 0x80, 0xFF).endVertex();
			tess.draw();
			worldr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
			worldr.pos(barLeft, barTop + barHeight - 1, 0.0D).tex(0.0F, 1.0F).color(0xC0, 0xC0, 0xC0, 0xFF).endVertex();
			worldr.pos(barLeft + barWidth - 1, barTop + barHeight - 1, 0.0D).tex(1.0F, 1.0F).color(0xC0, 0xC0, 0xC0, 0xFF).endVertex();
			worldr.pos(barLeft + barWidth - 1, barTop, 0.0D).tex(1.0F, 0.0F).color(0xC0, 0xC0, 0xC0, 0xFF).endVertex();
			worldr.pos(barLeft, barTop, 0.0D).tex(0.0F, 0.0F).color(0xC0, 0xC0, 0xC0, 0xFF).endVertex();
			tess.draw();
		}
		
		RenderSystem.enableTexture();
		RenderSystem.shadeModel(GL11.GL_FLAT);
		RenderSystem.enableAlphaTest();
		RenderSystem.disableBlend();
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
		for (final GuiComponent comp : Lists.reverse(components)) { // Reversing to call front component
			tooltips.clear();
			if (!comp.isVisible()) continue;
			if (comp.canHaveTooltip() && isMouseInComponent(mouseX, mouseY, comp)) {
				
				if (comp.getTooltips() != null) {
					tooltips.addAll(Arrays.asList(comp.getTooltips()));
					break;
				}
			}
		}
		
	}
	
	@Override
	public String[] getTooltips() {
		return tooltips.toArray(new String[0]);
	}
	
	protected void drawGradientRect(MatrixStack matrixStack, int left, int top, int right, int bottom, int color1, int color2) {
		GuiUtils.drawGradientRect(matrixStack.getLast().getMatrix(), 0, left, top, right, bottom, color1, color2);
	}
	
}
