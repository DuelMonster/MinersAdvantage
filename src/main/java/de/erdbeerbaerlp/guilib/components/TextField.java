package de.erdbeerbaerlp.guilib.components;

import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TextField extends GuiComponent {
	/**
	 * if true the textbox can lose focus by clicking elsewhere on the screen
	 */
	protected final boolean                     canLoseFocus            = true;
	final char                                  colorCodePlaceholder    = '\u0378';
	private final ArrayList<String>             suggestions             = new ArrayList<>();
	private final String[]                      colorCodeSuggestions    = new String[] { "\u00A711", "\u00A722", "\u00A733", "\u00A744", "\u00A755", "\u00A766", "\u00A777", "\u00A788", "\u00A799", "\u00A7AA", "\u00A7BB", "\u00A7CC", "\u00A7DD", "\u00A7EE", "\u00A7FF", "\u00A7LL", "\u00A7MM", "\u00A7NN", "\u00A7OO", "K\u00A7kK" };
	protected String                            text                    = "";
	protected int                               maxStringLength         = 100;
	protected int                               cursorCounter;
	protected boolean                           enableBackgroundDrawing = true;
	/**
	 * If this value is true along with isEnabled, keyTyped will process the keys.
	 */
	protected boolean                           isFocused;
	/**
	 * The current character index that should be used as start of the rendered text.
	 */
	protected int                               lineScrollOffset;
	protected int                               cursorPosition;
	/**
	 * other selection position, maybe the same as the cursor
	 */
	protected int                               selectionEnd;
	protected int                               enabledColor            = 14737632;
	protected int                               disabledColor           = 7368816;
	/**
	 * Called to check if the text is valid
	 */
	protected Predicate<String>                 validator               = s -> true;
	private Consumer<String>                    guiResponder;
	private BiFunction<String, Integer, String> textFormatter           = (p_195610_0_, p_195610_1_) -> p_195610_0_;
	private boolean                             acceptsColors           = false;
	private Runnable                            callback;
	private boolean                             isShiftDown;
	private boolean                             showSuggestions         = true;
	private int                                 selectedSuggestion      = 0;
	private String                              label                   = "";
	
	/**
	 * Creates an new text field
	 *
	 * @param x X position
	 * @param y Y position
	 * @param width Text Field width
	 * @param height Text Field height
	 */
	public TextField(int x, int y, int width, int height) {
		super(x, y, width, height);
	}
	
	/**
	 * Creates an new text field
	 *
	 * @param x X position
	 * @param y Y position
	 * @param width Text Field width
	 */
	public TextField(int x, int y, int width) {
		this(x, y, width, 20);
	}
	
	public void addSuggestion(String suggestion) {
		if (!suggestions.contains(suggestion)) suggestions.add(suggestion);
	}
	
	public void addSuggestions(String... suggestions) {
		for (final String s : suggestions) {
			if (!this.suggestions.contains(s)) this.suggestions.add(s);
		}
	}
	
	public void removeSuggestion(String suggestion) {
		suggestions.remove(suggestion);
	}
	
	public void enableSuggestions() {
		this.showSuggestions = true;
	}
	
	public void disableSuggestions() {
		this.showSuggestions = false;
	}
	
	public boolean isShowingSuggestions() {
		return showSuggestions;
	}
	
	public ArrayList<String> getSuggestions() {
		return suggestions;
	}
	
	public String getCurrentSuggestion() {
		if (acceptsColors && text.endsWith("\u00A7")) {
			if (selectedSuggestion < 0) selectedSuggestion = colorCodeSuggestions.length - 1;
			if (selectedSuggestion >= colorCodeSuggestions.length) selectedSuggestion = 0;
			return colorCodeSuggestions[selectedSuggestion];
		}
		if (!suggestions.isEmpty()) {
			final ArrayList<String> fittingSuggestions = new ArrayList<>();
			for (String s : suggestions)
				if (s.startsWith(text)) fittingSuggestions.add(s);
			if (selectedSuggestion < 0) selectedSuggestion = fittingSuggestions.size();
			if (fittingSuggestions.size() < selectedSuggestion) selectedSuggestion = 0;
			if (fittingSuggestions.isEmpty()) return "";
			return fittingSuggestions.get(selectedSuggestion);
		}
		return "";
	}
	
	/**
	 * Sets wether or not the text box should accept color codes by using a §
	 *
	 * @param acceptsColors accept color code char?
	 */
	public void setAcceptsColors(boolean acceptsColors) {
		this.acceptsColors = acceptsColors;
	}
	
	public void setResponder(Consumer<String> rssponderIn) {
		this.guiResponder = rssponderIn;
	}
	
	public void setTextFormatter(BiFunction<String, Integer, String> textFormatterIn) {
		this.textFormatter = textFormatterIn;
	}
	
	@Override
	protected IFormattableTextComponent getNarrationMessage() {
		String s = this.getMessage().getString();
		return new StringTextComponent(s.isEmpty() ? "" : I18n.format("gui.narrate.editBox", s, this.text));
	}
	
	/**
	 * Returns the contents of the textbox
	 */
	public String getText() {
		return this.text;
	}
	
	/**
	 * Sets the text of the textbox, and moves the cursor to the end.
	 */
	public void setText(String textIn) {
		if (this.validator.test(textIn)) {
			if (textIn.length() > this.maxStringLength) {
				this.text = textIn.substring(0, this.maxStringLength);
			} else {
				this.text = textIn;
			}
			
			this.setCursorPositionEnd();
			this.setSelectionPos(this.cursorPosition);
			this.onTextChanged(textIn);
		}
	}
	
	/**
	 * returns the text between the cursor and selectionEnd
	 */
	public String getSelectedText() {
		int i = Math.min(this.cursorPosition, this.selectionEnd);
		int j = Math.max(this.cursorPosition, this.selectionEnd);
		return this.text.substring(i, j);
	}
	
	public void setValidator(Predicate<String> validatorIn) {
		this.validator = validatorIn;
	}
	
	/**
	 * Adds the given text after the cursor, or replaces the currently selected text if there is a selection.
	 */
	public void writeText(String textToWrite) {
		String s = "";
		if (acceptsColors) {
			textToWrite = textToWrite.replace('\u00A7', colorCodePlaceholder);
		}
		String s1 = SharedConstants.filterAllowedCharacters(textToWrite);
		if (acceptsColors) {
			s1 = s1.replace(colorCodePlaceholder, '\u00A7');
		}
		int i = Math.min(this.cursorPosition, this.selectionEnd);
		int j = Math.max(this.cursorPosition, this.selectionEnd);
		int k = this.maxStringLength - this.text.length() - (i - j);
		if (!this.text.isEmpty()) {
			s = s + this.text.substring(0, i);
		}
		
		int l;
		if (k < s1.length()) {
			s = s + s1.substring(0, k);
			l = k;
		} else {
			s = s + s1;
			l = s1.length();
		}
		
		if (!this.text.isEmpty() && j < this.text.length()) {
			s = s + this.text.substring(j);
		}
		
		if (this.validator.test(s)) {
			this.text = s;
			this.clampCursorPosition(i + l);
			this.setSelectionPos(this.cursorPosition);
			this.onTextChanged(this.text);
		}
	}
	
	private void onTextChanged(String newText) {
		if (this.guiResponder != null) {
			this.guiResponder.accept(newText);
		}
		
		this.nextNarration = Util.milliTime() + 500L;
	}
	
	private void delete(int p_212950_1_) {
		if (Screen.hasControlDown()) {
			this.deleteWords(p_212950_1_);
		} else {
			this.deleteFromCursor(p_212950_1_);
		}
		
	}
	
	/**
	 * Deletes the given number of words from the current cursor's position, unless there is currently a selection, in
	 * which case the selection is deleted instead.
	 */
	public void deleteWords(int num) {
		if (!this.text.isEmpty()) {
			if (this.selectionEnd != this.cursorPosition) {
				this.writeText("");
			} else {
				this.deleteFromCursor(this.getNthWordFromCursor(num) - this.cursorPosition);
			}
		}
	}
	
	/**
	 * Deletes the given number of characters from the current cursor's position, unless there is currently a selection,
	 * in which case the selection is deleted instead.
	 */
	public void deleteFromCursor(int num) {
		if (!this.text.isEmpty()) {
			if (this.selectionEnd != this.cursorPosition) {
				this.writeText("");
			} else {
				boolean flag = num < 0;
				int     i    = flag ? this.cursorPosition + num : this.cursorPosition;
				int     j    = flag ? this.cursorPosition : this.cursorPosition + num;
				String  s    = "";
				if (i >= 0) {
					s = this.text.substring(0, i);
				}
				
				if (j < this.text.length()) {
					s = s + this.text.substring(j);
				}
				
				if (this.validator.test(s)) {
					this.text = s;
					if (flag) {
						this.moveCursorBy(num);
					}
					
					this.onTextChanged(this.text);
				}
			}
		}
	}
	
	/**
	 * Gets the starting index of the word at the specified number of words away from the cursor position.
	 */
	public int getNthWordFromCursor(int numWords) {
		return this.getNthWordFromPos(numWords, this.getCursorPosition());
	}
	
	/**
	 * Gets the starting index of the word at a distance of the specified number of words away from the given position.
	 */
	private int getNthWordFromPos(int n, int pos) {
		return this.getNthWordFromPosWS(n, pos, true);
	}
	
	/**
	 * Like getNthWordFromPos (which wraps this), but adds option for skipping consecutive spaces
	 */
	private int getNthWordFromPosWS(int n, int pos, boolean skipWs) {
		int     i    = pos;
		boolean flag = n < 0;
		int     j    = Math.abs(n);
		
		for (int k = 0; k < j; ++k) {
			if (!flag) {
				int l = this.text.length();
				i = this.text.indexOf(32, i);
				if (i == -1) {
					i = l;
				} else {
					while (skipWs && i < l && this.text.charAt(i) == ' ') {
						++i;
					}
				}
			} else {
				while (skipWs && i > 0 && this.text.charAt(i - 1) == ' ') {
					--i;
				}
				
				while (i > 0 && this.text.charAt(i - 1) != ' ') {
					--i;
				}
			}
		}
		
		return i;
	}
	
	/**
	 * Moves the text cursor by a specified number of characters and clears the selection
	 */
	public void moveCursorBy(int num) {
		this.setCursorPosition(this.cursorPosition + num);
	}
	
	public void clampCursorPosition(int pos) {
		this.cursorPosition = MathHelper.clamp(pos, 0, this.text.length());
	}
	
	/**
	 * Moves the cursor to the very start of this text box.
	 */
	public void setCursorPositionZero() {
		this.setCursorPosition(0);
	}
	
	/**
	 * Moves the cursor to the very end of this text box.
	 */
	public void setCursorPositionEnd() {
		this.setCursorPosition(this.text.length());
	}
	
	@Override
	public boolean charTyped(char character, int keyCode) {
		if (!this.canWrite()) {
			return false;
		} else {
			if (acceptsColors && character == '\u00A7') {
				character = colorCodePlaceholder;
			}
			if (SharedConstants.isAllowedCharacter(character)) {
				if (this.isEnabled()) {
					if (acceptsColors && character == colorCodePlaceholder) character = '\u00A7';
					this.writeText(Character.toString(character));
				}
				return true;
			} else {
				return false;
			}
		}
	}
	
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partial) {
		if (this.getEnableBackgroundDrawing()) {
			fill(matrixStack, this.getX() - 1, this.getY() - 1, this.getX() + this.width + 1, this.getY() + this.height + 1, -6250336);
			fill(matrixStack, this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, -16777216);
		}
		
		int     currentColor = this.isEnabled() ? this.enabledColor : this.disabledColor;
		int     j            = this.cursorPosition - this.lineScrollOffset;
		int     k            = this.selectionEnd - this.lineScrollOffset;
		String  s            = this.fontRenderer.func_238412_a_(this.text.substring(this.lineScrollOffset), this.getAdjustedWidth());
		boolean flag         = j >= 0 && j <= s.length();
		boolean flag1        = this.isFocused() && this.cursorCounter / 6 % 2 == 0 && flag;
		int     l            = this.enableBackgroundDrawing ? this.getX() + 4 : this.getX();
		int     i1           = this.enableBackgroundDrawing ? this.getY() + (this.height - 8) / 2 : this.getY();
		int     j1           = l;
		if (k > s.length()) {
			k = s.length();
		}
		
		if (!s.isEmpty()) {
			String s1 = flag ? s.substring(0, j) : s;
			j1 = this.fontRenderer.drawStringWithShadow(matrixStack, this.textFormatter.apply(s1, this.lineScrollOffset), l, i1, currentColor);
		}
		
		boolean flag2 = this.cursorPosition < this.text.length() || this.text.length() >= this.getMaxStringLength();
		int     k1    = j1;
		if (!flag) {
			k1 = j > 0 ? l + this.width : l;
		} else if (flag2) {
			k1 = j1 - 1;
			--j1;
		}
		
		if (!s.isEmpty() && flag && j < s.length()) {
			this.fontRenderer.drawStringWithShadow(matrixStack, this.textFormatter.apply(s.substring(j), this.cursorPosition), j1, i1, currentColor);
		}
		
		if (!flag2 && !getCurrentSuggestion().isEmpty()) {
			this.fontRenderer.drawStringWithShadow(matrixStack, getCurrentSuggestion(), k1 - 1, i1, -8355712);
		}
		if (!label.isEmpty())
			fontRenderer.drawString(matrixStack, label, getX(), getY() - 8, currentColor);
		if (flag1) {
			if (flag2) {
				AbstractGui.fill(matrixStack, k1, i1 - 1, k1 + 1, i1 + 1 + 9, -3092272);
			} else {
				this.fontRenderer.drawStringWithShadow(matrixStack, "_", k1, i1, currentColor);
			}
		}
		
		if (k != j) {
			int l1 = l + this.fontRenderer.getStringWidth(s.substring(0, k));
			this.drawSelectionBox(k1, i1 - 1, l1 - 1, i1 + 1 + 9);
		}
		
	}
	
	/**
	 * returns the maximum number of character that can be contained in this textbox
	 */
	private int getMaxStringLength() {
		return this.maxStringLength;
	}
	
	public void setMaxStringLength(int maxStringLength) {
		this.maxStringLength = maxStringLength;
	}
	
	/**
	 * Draws the blue selection box.
	 */
	private void drawSelectionBox(int startX, int startY, int endX, int endY) {
		if (startX < endX) {
			int i = startX;
			startX = endX;
			endX   = i;
		}
		
		if (startY < endY) {
			int j = startY;
			startY = endY;
			endY   = j;
		}
		
		if (endX > this.getX() + this.width) {
			endX = this.getX() + this.width;
		}
		
		if (startX > this.getX() + this.width) {
			startX = this.getX() + this.width;
		}
		
		Tessellator   tessellator   = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		RenderSystem.color4f(0.0F, 0.0F, 255.0F, 255.0F);
		RenderSystem.disableTexture();
		RenderSystem.enableColorLogicOp();
		RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
		bufferbuilder.pos(startX, endY, 0.0D).endVertex();
		bufferbuilder.pos(endX, endY, 0.0D).endVertex();
		bufferbuilder.pos(endX, startY, 0.0D).endVertex();
		bufferbuilder.pos(startX, startY, 0.0D).endVertex();
		tessellator.draw();
		RenderSystem.disableColorLogicOp();
		RenderSystem.enableTexture();
	}
	
	@Override
	public void mouseClick(double mouseX, double mouseY, int mouseButton) {
		if (this.isVisible()) {
			boolean isHovered = mouseX >= this.getX() && mouseX < this.getX() + this.width && mouseY >= this.getY() && mouseY < this.getY() + this.height;
			if (this.canLoseFocus) {
				super.setFocused(isHovered);
			}
			
			if (this.isFocused() && isHovered && mouseButton == 0) {
				int i = MathHelper.floor(mouseX) - this.getX();
				if (this.enableBackgroundDrawing) {
					i -= 4;
				}
				String s = this.fontRenderer.func_238412_a_(this.text.substring(this.lineScrollOffset), this.getAdjustedWidth());
				this.setCursorPosition(this.fontRenderer.func_238412_a_(s, i).length() + this.lineScrollOffset);
			}
		}
	}
	
	@Override
	public void mouseRelease(double mouseX, double mouseY, int state) {
		
	}
	
	@Override
	public boolean changeFocus(boolean p_changeFocus_1_) {
		return (this.visible && this.isEnabled()) && super.changeFocus(p_changeFocus_1_);
	}
	
	/**
	 * returns the current position of the cursor
	 */
	public int getCursorPosition() {
		return this.cursorPosition;
	}
	
	/**
	 * Sets the current position of the cursor.
	 */
	public void setCursorPosition(int pos) {
		this.clampCursorPosition(pos);
		if (!this.isShiftDown) {
			this.setSelectionPos(this.cursorPosition);
		}
		
		this.onTextChanged(this.text);
	}
	
	/**
	 * Sets the color to use when drawing this text box's text. A different color is used if this text box is disabled.
	 */
	public void setTextColor(int color) {
		this.enabledColor = color;
	}
	
	/**
	 * Sets the color to use for text in this text box when this text box is disabled.
	 */
	public void setDisabledTextColour(int color) {
		this.disabledColor = color;
	}
	
	/**
	 * returns the width of the textbox depending on if background drawing is enabled
	 */
	public int getAdjustedWidth() {
		return this.getEnableBackgroundDrawing() ? this.width - 8 : this.width;
	}
	
	/**
	 * Gets whether the background and outline of this text box should be drawn (true if so).
	 */
	private boolean getEnableBackgroundDrawing() {
		return this.enableBackgroundDrawing;
	}
	
	/**
	 * Sets whether or not the background and outline of this text box should be drawn.
	 */
	public void setEnableBackgroundDrawing(boolean enableBackgroundDrawingIn) {
		this.enableBackgroundDrawing = enableBackgroundDrawingIn;
	}
	
	/**
	 * Sets the position of the selection anchor (the selection anchor and the cursor position mark the edges of the
	 * selection). If the anchor is set beyond the bounds of the current text, it will be put back inside.
	 */
	public void setSelectionPos(int position) {
		int i = this.text.length();
		this.selectionEnd = MathHelper.clamp(position, 0, i);
		if (this.fontRenderer != null) {
			if (this.lineScrollOffset > i) {
				this.lineScrollOffset = i;
			}
			
			int    j = this.getAdjustedWidth();
			String s = this.fontRenderer.func_238412_a_(this.text.substring(this.lineScrollOffset), j);
			int    k = s.length() + this.lineScrollOffset;
			if (this.selectionEnd == this.lineScrollOffset) {
				this.lineScrollOffset -= this.fontRenderer.func_238413_a_(this.text, j, true).length();
			}
			
			if (this.selectionEnd > k) {
				this.lineScrollOffset += this.selectionEnd - k;
			} else if (this.selectionEnd <= this.lineScrollOffset) {
				this.lineScrollOffset -= this.lineScrollOffset - this.selectionEnd;
			}
			
			this.lineScrollOffset = MathHelper.clamp(this.lineScrollOffset, 0, i);
		}
		
	}
	
	@SuppressWarnings("resource")
	@Override
	public boolean keyPressed(int keyCode, int p_keyPressed_2_, int p_keyPressed_3_) {
		if (!this.canWrite()) {
			return false;
		} else {
			this.isShiftDown = Screen.hasShiftDown();
			if (Screen.isSelectAll(keyCode)) {
				this.setCursorPositionEnd();
				this.setSelectionPos(0);
				return true;
			} else if (Screen.isCopy(keyCode)) {
				Minecraft.getInstance().keyboardListener.setClipboardString(this.getSelectedText());
				return true;
			} else if (Screen.isPaste(keyCode)) {
				if (this.isEnabled()) {
					this.writeText(Minecraft.getInstance().keyboardListener.getClipboardString());
				}
				
				return true;
			} else if (Screen.isCut(keyCode)) {
				Minecraft.getInstance().keyboardListener.setClipboardString(this.getSelectedText());
				if (this.isEnabled()) {
					this.writeText("");
				}
				
				return true;
			} else {
				switch (keyCode) {
				// Return keys
				case 257:
				case 335:
					this.onReturn();
					return true;
				case 259:
					if (this.isEnabled()) {
						this.isShiftDown = false;
						this.delete(-1);
						this.isShiftDown = Screen.hasShiftDown();
					}
					
					return true;
				case 265:
					selectedSuggestion++;
					return false;
				case 264:
					selectedSuggestion--;
					return false;
				case 260:
				case 266:
				case 267:
				default:
					return false;
				case 261:
					if (this.isEnabled()) {
						this.isShiftDown = false;
						this.delete(1);
						this.isShiftDown = Screen.hasShiftDown();
					}
					
					return true;
				case 262:
					if (Screen.hasControlDown()) {
						this.setCursorPosition(this.getNthWordFromCursor(1));
					} else {
						this.moveCursorBy(1);
					}
					
					return true;
				case 263:
					if (Screen.hasControlDown()) {
						this.setCursorPosition(this.getNthWordFromCursor(-1));
					} else {
						this.moveCursorBy(-1);
					}
					
					return true;
				case 268:
					this.setCursorPositionZero();
					return true;
				case 269:
					this.setCursorPositionEnd();
					return true;
				}
			}
		}
	}
	
	/**
	 * Gets called when pressing return
	 */
	public void onReturn() {
		if (this.callback != null) this.callback.run();
	}
	
	/**
	 * Sets an callback for pressing return
	 *
	 * @param r Runnable to run
	 */
	public final void setReturnAction(Runnable r) {
		this.callback = r;
	}
	
	public boolean canWrite() {
		return this.isVisible() && this.isFocused() && this.isEnabled();
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
}
