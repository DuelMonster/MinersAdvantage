package co.uk.duelmonster.minersadvantage.client.gui;

import java.text.NumberFormat;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiNumberField extends GuiTextField {
	public static final String REGEX_NUMERICS = "(?:^|[^w.,-])(\\d[\\d,.-]+)(?=\\W|$)/)"; // "[^0-9.-]";
	
	public GuiNumberField(FontRenderer renderer, int posX, int posY, int sizeX, int sizeY) {
		super(0, renderer, posX, posY, sizeX, sizeY);
		this.setMaxStringLength(Integer.MAX_VALUE);
	}
	
	@Override
	public boolean mouseClicked(int mx, int my, int click) {
		boolean bRtrn = super.mouseClicked(mx, my, click);
		
		if (!isFocused())
			setText(getText().length() <= 0 ? "0" : getText());
		
		return bRtrn;
	}
	
	@Override
	public String getText() {
		return super.getText().replaceAll(REGEX_NUMERICS, "");
	}
	
	@Override
	public void writeText(String text) {
		super.writeText(text.replaceAll(REGEX_NUMERICS, ""));
	}
	
	@Override
	public void setText(String text) {
		super.setText(text.replaceAll(REGEX_NUMERICS, ""));
	}
	
	public Number getNumber() {
		try {
			return NumberFormat.getInstance().parse(getText());
		}
		catch (Exception ex) {
			return 0;
		}
	}
}
