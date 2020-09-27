package de.erdbeerbaerlp.guilib.components;

import java.util.ArrayList;
import java.util.Arrays;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.gui.GuiUtils;

@OnlyIn(Dist.CLIENT)
public class StringList extends GuiComponent {
	
	protected final int                   offsetTop           = 18;
	protected final int                   offsetLeft          = 4;
	private final int                     barWidth            = 6;
	private final ArrayList<GuiComponent> components          = new ArrayList<>();
	private final ArrayList<Button>       removeButtons       = new ArrayList<>();
	private final ArrayList<TextFieldExt> textFields          = new ArrayList<>();
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
	private ArrayList<String>             listValues;
	private final Button                  btnAdd              = new Button(0, 0, 20, "+");
	private final Label                   lblTitle;
	private Runnable                      valuesUpdated;
	
	/**
	 * Creates a new String List component
	 *
	 * @param x X position
	 * @param y Y position
	 * @param width Text Field width
	 * @param height Text Field height
	 * @param values List of array values
	 */
	public StringList(int x, int y, int width, int height, String title, ArrayList<String> values) {
		super(x, y, width, height);
		
		setContentHeight(height);
		
		title    += (title.endsWith(":") ? "" : ":");
		lblTitle  = new Label(title, 6, 10);
		addComponent(lblTitle);
		
		setListValues(values != null ? values : new ArrayList<String>());
		
		btnAdd.setClickListener(() -> {
			generateRow((25 * listValues.size()), null);
		});
	}
	
	private void generateListControls() {
		int yPos = offsetTop;
		
		if (listValues != null && listValues.size() > 0) {
			clearRows();
			
			for (String value : listValues) {
				generateRow(yPos, value);
				
				yPos += 25;
			}
			
		}
		
		addComponent(btnAdd);
		
		fineTuneControls();
	}
	
	private void generateRow(int yPos, String value) {
		
		Button       btnRemove = new Button(offsetLeft, yPos, 20, "-");
		TextFieldExt txtValue  = new TextFieldExt(60, yPos, width - barWidth - offsetLeft);
		
		if (value != null) {
			txtValue.setText(value);
			txtValue.setCursorPositionZero();
		}
		
		if (value == null || value.isEmpty()) btnRemove.disable();
		
		addComponent(btnRemove);
		addComponent(txtValue);
		
		removeButtons.add(btnRemove);
		textFields.add(txtValue);
		
	}
	
	private void clearRows() {
		clearComponents();
		removeButtons.clear();
		textFields.clear();
	}
	
	public void adjustLayout(int _x, int _y, int _width, int _height) {
		this.setPosition(_x, _y);
		this.setWidth(_width);
		this.setHeight(_height);
		this.fineTuneControls();
	}
	
	private void fineTuneControls() {
		
		if (removeButtons.isEmpty()) {
			
			btnAdd.setPosition(offsetLeft, offsetTop);
			
		} else {
			int yPos = offsetTop;
			
			for (int indx1 = 0; indx1 < removeButtons.size(); indx1++) {
				Button       btnRemove = removeButtons.get(indx1);
				TextFieldExt txtValue  = textFields.get(indx1);
				
				btnRemove.setPosition(offsetLeft, yPos);
				
				txtValue.setPosition(30, yPos);
				txtValue.setWidth(width - barWidth - 60);
				
				btnRemove.setClickListener(() -> {
					String value = txtValue.getText();
					if (value != null && !value.isEmpty() && listValues.contains(value)) {
						
						// System.out.println("Removed: " + value);
						
						listValues.remove(value);
						if (valuesUpdated != null) valuesUpdated.run();
						
						if (listValues.isEmpty()) {
							txtValue.setText("");
							btnRemove.disable();
						} else {
							generateListControls();
						}
					}
				});
				
				txtValue.setIdleCallback(() -> {
					// System.out.println("IdleCallback: " + txtValue.getText());
					
					listValues.clear();
					for (int indx2 = 0; indx2 < textFields.size(); indx2++) {
						String value = textFields.get(indx2).getText();
						if (!value.isEmpty() && !listValues.contains(value)) {
							listValues.add(value);
						}
					}
					
					if (valuesUpdated != null) valuesUpdated.run();
					
					generateListControls();
				});
				
				final int lastY = (25 * removeButtons.size()) + 5;
				txtValue.setReturnAction(() -> {
					generateRow(lastY, null);
				});
				
				yPos += 25;
			}
			
			btnAdd.setPosition(width - barWidth - 25, yPos - 25);
		}
		
		fitContent();
	}
	
	/**
	 * @return the listValues
	 */
	public ArrayList<String> getListValues() {
		return listValues;
	}
	
	/**
	 * @param listValues the listValues to set
	 */
	public void setListValues(ArrayList<String> listValues) {
		this.listValues = listValues;
		if (valuesUpdated != null) valuesUpdated.run();
		generateListControls();
	}
	
	private void recalc() {
		top     = getY();
		left    = getX();
		bottom  = height + top;
		right   = width + left;
		barLeft = right - barWidth;
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
	 * @param valuesUpdated the valuesUpdated to set
	 */
	public void setUpdatedCallback(Runnable updatedCallback) {
		this.valuesUpdated = updatedCallback;
	}
	
	/**
	 * Use this to add your components
	 *
	 * @param component The component to add
	 */
	public final void addComponent(GuiComponent component) {
		if (!components.contains(component)) components.add(component);
	}
	
	public final void removeComponent(GuiComponent component) {
		if (components.contains(component)) components.remove(component);
	}
	
	protected void drawBackground() {}
	
	private ArrayList<String> tooltips = new ArrayList<>();
	
	private int getMaxScroll() {
		return getContentHeight() - (height - offsetTop);
	}
	
	private void applyScrollLimits() {
		int max = getMaxScroll();
		
		if (max < 0) {
			max /= 2;
		}
		
		if (scrollDistance < 0.0F) {
			scrollDistance = 0.0F;
		}
		
		if (scrollDistance > max) {
			scrollDistance = max;
		}
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
		if (scroll != 0) {
			scrollDistance += -scroll * getScrollAmount();
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
			c.scrollOffsetY = getY() - (int) scrollDistance;
			c.scrollOffsetX = getX();
			if (c.isVisible()) c.render(matrixStack, mouseX, mouseY, partialTicks);
		}
	}
	
	/**
	 * Resizes the content height to just perfectly fit all components
	 */
	public void fitContent() {
		for (final GuiComponent c : components) {
			if (((c.getY() - c.scrollOffsetY) + c.getHeight() + offsetTop + offsetLeft) > contentHeight) {
				contentHeight = (c.getY() - c.scrollOffsetY) + c.getHeight() + offsetTop + offsetLeft;
			}
		}
	}
	
	@Override
	public boolean canHaveTooltip() {
		return true;
	}
	
	/**
	 * Use this to add multiple components at once
	 *
	 * @param components The components to add
	 */
	public final void addAllComponents(GuiComponent... components) {
		for (GuiComponent c : components) {
			addComponent(c);
		}
	}
	
	public final void clearComponents() {
		components.clear();
	}
	
	@Override
	public boolean charTyped(char typedChar, int keyCode) {
		// public boolean func_231042_a_(char typedChar, int keyCode) {
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
	
	private int getBarHeight() {
		int barHeight = (height * height) / getContentHeight();
		
		if (barHeight < 32) barHeight = 32;
		
		if (barHeight > height - offsetLeft * 2)
			barHeight = height - offsetLeft * 2;
		
		return barHeight;
	}
	
	private boolean isMouseInComponent(double mouseX, double mouseY, GuiComponent comp) {
		return (mouseX >= comp.getX() && mouseX <= comp.getX() + comp.getWidth() && mouseY >= comp.getY() && mouseY < comp.getY() + comp.getHeight());
	}
	
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (isEnabled())
			if (scrolling) {
				int maxScroll = height - getBarHeight();
				double moved = deltaY / maxScroll;
				scrollDistance += getMaxScroll() * moved;
				applyScrollLimits();
				return true;
			}
		return false;
	}
	
	@Override
	public void mouseRelease(double mouseX, double mouseY, int state) {
		scrolling = false;
		if (mouseX >= left && mouseX <= right && mouseY >= top && mouseY <= bottom) {
			
			// System.out.println("Released inside StringList: " + mouseX + ", " + mouseY + ", " + components);
			
			GuiComponent compReleased = null;
			for (GuiComponent comp : components) {
				if (comp.isVisible() && isMouseInComponent(mouseX, mouseY, comp)) {
					compReleased = comp;
				} else if (comp.getClass() == TextFieldExt.class && comp.isFocused()) {
					((TextFieldExt) comp).setFocused(false);
				}
			}
			
			// fire click event outside of the loop due to high probability of Concurrent Modification Exceptions
			if (compReleased != null) {
				compReleased.mouseReleased(mouseX, mouseY, state);
			}
		}
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
			scrolling = mouseButton == 0 && mouseX >= barLeft && mouseX < barLeft + barWidth;
			if (scrolling) {
				return;
			}
			
			// System.out.println("Click inside StringList: " + mouseX + ", " + mouseY + ", " + components);
			
			GuiComponent compClicked = null;
			for (GuiComponent comp : components) {
				if (comp.isVisible() && isMouseInComponent(mouseX, mouseY, comp)) {
					compClicked = comp;
				} else if (comp.getClass() == TextFieldExt.class && comp.isFocused()) {
					((TextFieldExt) comp).setFocused(false);
				}
			}
			
			// fire click event outside of the loop due to high probability of Concurrent Modification Exceptions
			if (compClicked != null) {
				compClicked.mouseClicked(mouseX, mouseY, mouseButton);
			}
		}
	}
	
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		// public void func_230430_a_(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		drawBackground();
		Tessellator   tess   = Tessellator.getInstance();
		BufferBuilder worldr = tess.getBuffer();
		double        scale  = mc.getMainWindow().getGuiScaleFactor();
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glScissor((int) (left * scale), (int) (mc.getMainWindow().getFramebufferHeight() - (bottom * scale)),
				(int) (width * scale), (int) (height * scale));
		if (mc.world != null) {
			drawGradientRect(matrixStack, left, top, right, bottom, 0xC0101010, 0xD0101010);
		} else // Draw dark dirt background
		{
			RenderSystem.disableLighting();
			RenderSystem.disableFog();
			mc.getTextureManager().bindTexture(getBackground());
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			final float texScale = 32.0F;
			worldr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
			worldr.pos(left, bottom, 0.0D).tex(left / texScale, (bottom + (int) scrollDistance) / texScale).color(0x20, 0x20, 0x20, 0xFF).endVertex();
			worldr.pos(right, bottom, 0.0D).tex(right / texScale, (bottom + (int) scrollDistance) / texScale).color(0x20, 0x20, 0x20, 0xFF).endVertex();
			worldr.pos(right, top, 0.0D).tex(right / texScale, (top + (int) scrollDistance) / texScale).color(0x20, 0x20, 0x20, 0xFF).endVertex();
			worldr.pos(left, top, 0.0D).tex(left / texScale, (top + (int) scrollDistance) / texScale).color(0x20, 0x20, 0x20, 0xFF).endVertex();
			tess.draw();
		}
		
		drawPanel(matrixStack, mouseX, mouseY, partialTicks);
		
		RenderSystem.disableDepthTest();
		
		int extraHeight = (getContentHeight() + offsetTop) - height;
		if (extraHeight > 0) {
			int barHeight = getBarHeight();
			
			int barTop = (int) scrollDistance * (height - barHeight) / extraHeight + top;
			if (barTop < top) {
				barTop = top;
			}
			
			RenderSystem.disableTexture();
			worldr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
			worldr.pos(barLeft, bottom, 0.0D).tex(0.0F, 1.0F).color(0x00, 0x00, 0x00, 0xFF).endVertex();
			worldr.pos(barLeft + barWidth, bottom, 0.0D).tex(1.0F, 1.0F).color(0x00, 0x00, 0x00, 0xFF).endVertex();
			worldr.pos(barLeft + barWidth, top, 0.0D).tex(1.0F, 0.0F).color(0x00, 0x00, 0x00, 0xFF).endVertex();
			worldr.pos(barLeft, top, 0.0D).tex(0.0F, 0.0F).color(0x00, 0x00, 0x00, 0xFF).endVertex();
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
