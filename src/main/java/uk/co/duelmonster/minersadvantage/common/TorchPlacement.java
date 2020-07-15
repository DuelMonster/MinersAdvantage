package uk.co.duelmonster.minersadvantage.common;

import java.util.Locale;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

import net.minecraft.util.IStringSerializable;

// ====================================================================================================
// = TorchPlacement enum
// ====================================================================================================
public enum TorchPlacement implements IStringSerializable {
	INACTIVE(0, "INACTIVE"), FLOOR(1, "FLOOR"), LEFT_WALL(2, "LEFT_WALL"), RIGHT_WALL(3, "RIGHT_WALL");
	
	private final int		index;
	private final String	name;
	
	public static final TorchPlacement[]				VALUES		= new TorchPlacement[4];
	private static final Map<String, TorchPlacement>	NAME_LOOKUP	= Maps.<String, TorchPlacement>newHashMap();
	
	private TorchPlacement(int indexIn, String nameIn) {
		this.index = indexIn;
		this.name = nameIn;
	}
	
	@Override
	public String getString() {
		return this.name;
	}
	
	/**
	 * Get the Index of this Facing (0-5). The order is D-U-N-S-W-E
	 */
	public int getIndex() {
		return this.index;
	}
	
	/**
	 * Get the facing specified by the given name
	 */
	@Nullable
	public static TorchPlacement byName(String name) {
		return name == null ? null : (TorchPlacement) NAME_LOOKUP.get(name.toLowerCase(Locale.ROOT));
	}
	
	@Override
	public String toString() {
		return this.name;
	}
	
	static {
		for (TorchPlacement enumTP : values()) {
			VALUES[enumTP.index] = enumTP;
			NAME_LOOKUP.put(enumTP.getString().toLowerCase(Locale.ROOT), enumTP);
		}
	}
	
	public static String[] getValidValues() {
		String[] saRtrn = new String[VALUES.length];
		for (TorchPlacement enumTP : VALUES)
			saRtrn[enumTP.index] = enumTP.getString();
		return saRtrn;
	}
}
