package de.erdbeerbaerlp.guilib.components;

import com.mojang.blaze3d.matrix.MatrixStack;

import de.erdbeerbaerlp.guilib.McMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@SuppressWarnings("unused")
public abstract class GuiComponent extends Widget {
	protected static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/widgets.png");
	protected static final ResourceLocation errorIcon        = new ResourceLocation(McMod.MODID, "textures/gui/imgerror.png");
	protected final FontRenderer            fontRenderer;
	protected Minecraft                     mc               = Minecraft.getInstance();
	protected int                           width;
	protected int                           height;
	protected int                           id;
	protected boolean                       hovered;                                                                          // Sometimes used by components
	protected boolean                       visible          = true, enabled = true;
	int                                     scrollOffsetX    = 0, scrollOffsetY = 0;
	private int                             x;
	private int                             y;
	private String[]                        tooltips         = new String[0];
	private int                             assignedPage     = -1;
	
	public GuiComponent(int xIn, int yIn, int widthIn, int heightIn) {
		super(xIn, yIn, widthIn, heightIn, new StringTextComponent(""));
		this.fontRenderer = mc.fontRenderer;
		this.setX(xIn);
		this.setY(yIn);
		this.width  = widthIn;
		this.height = heightIn;
	}
	
	/**
	 * @return can the component have a tooltip?
	 */
	public boolean canHaveTooltip() {
		return true;
	}
	
	/**
	 * Gets tooltips assigned to the component
	 *
	 * @return tooltips
	 */
	public String[] getTooltips() {
		return this.tooltips;
	}
	
	/**
	 * Sets the tooltip(s) of this component
	 *
	 * @param strings tooltip(s)
	 */
	public final void setTooltips(String... strings) {
		this.tooltips = strings;
	}
	
	@Override
	protected int getYImage(boolean isHovered) {
		int i = 1;
		if (!this.isEnabled()) {
			i = 0;
		} else if (isHovered) {
			i = 2;
		}
		
		return i;
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		return true;
	}
	
	/**
	 * @return Is this component visible?
	 */
	public final boolean isVisible() {
		return this.visible;
	}
	
	/**
	 * Sets the components visibility state
	 *
	 * @param visible visible?
	 */
	public final void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	/**
	 * @return is component enabled?
	 */
	public final boolean isEnabled() {
		return this.enabled;
	}
	
	/**
	 * @param enable should it be enabled?
	 */
	public final void setEnabled(boolean enable) {
		this.enabled = enable;
	}
	
	/**
	 * @return X position
	 */
	public final int getX() {
		return x + scrollOffsetX;
	}
	
	/**
	 * Sets the component´s X position
	 *
	 * @param x X position
	 */
	public void setX(int x) {
		this.x = x;
	}
	
	/**
	 * @return Y position
	 */
	public final int getY() {
		return y + scrollOffsetY;
	}
	
	/**
	 * Sets the component´s Y position
	 *
	 * @param y Y position
	 */
	public void setY(int y) {
		this.y = y;
	}
	
	/**
	 * Gets the component width
	 *
	 * @return width
	 */
	@Override
	public final int getWidth() {
		return width;
	}
	
	/**
	 * Sets the component width
	 *
	 * @param width Width
	 */
	@Override
	public void setWidth(int width) {
		this.width = width;
	}
	
	/**
	 * Gets the component height
	 *
	 * @return height
	 */
	public final int getHeight() {
		return height;
	}
	
	/**
	 * Sets the component height
	 *
	 * @param height Height
	 */
	@Override
	public void setHeight(int height) {
		this.height = height;
	}
	
	/**
	 * Plays a press sound
	 */
	public void playPressSound() {
		mc.getSoundHandler().play(SimpleSound.master(SoundEvents.UI_BUTTON_CLICK, enabled ? 1f : 0.5f));
	}
	
	/**
	 * Draws the component
	 */
	@Override
	public abstract void render(MatrixStack matrixStack, int mouseX, int mouseY, float partial);
	
	/**
	 * Called on mouse click
	 */
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		mouseClick(mouseX, mouseY, mouseButton);
		return true;
	}
	
	public abstract void mouseClick(double mouseX, double mouseY, int mouseButton);
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int state) {
		this.mouseRelease(mouseX, mouseY, state);
		return true;
	}
	
	/**
	 * Called on mouse release
	 */
	public abstract void mouseRelease(double mouseX, double mouseY, int state);
	
	/**
	 * Fired when a key is typed (except F11 which toggles full screen)
	 */
	@Override
	public abstract boolean charTyped(char typedChar, int keyCode);
	
	/**
	 * Disables this component
	 */
	public final void disable() {
		setEnabled(false);
	}
	
	/**
	 * Enables this component
	 */
	public final void enable() {
		setEnabled(true);
	}
	
	/**
	 * Hides this component
	 */
	public final void hide() {
		setVisible(false);
	}
	
	/**
	 * Shows this component
	 */
	public final void show() {
		setVisible(true);
	}
	
	/**
	 * Assigns this component to an page
	 * -1 means global
	 * Default: -1
	 *
	 * @param page Page to assign to
	 */
	public final void assignToPage(int page) {
		assignedPage = page;
	}
	
	/**
	 * Gets the page this component is assigned to
	 *
	 * @return Assigned page
	 */
	public final int getAssignedPage() {
		return assignedPage;
	}
	
	/**
	 * Sets the ID of this component
	 * (almost unused!)
	 *
	 * @param id ID
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * Sets the position of this component
	 *
	 * @param x X position
	 * @param y Y position
	 */
	public void setPosition(int x, int y) {
		setX(x);
		setY(y);
	}
	
	/**
	 * Gets called on GUI Close, used to stop threads and/or free memory
	 */
	public void unload() {
		
	}
}
