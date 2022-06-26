package uk.co.duelmonster.minersadvantage.common;

import java.util.Locale;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

import net.minecraft.util.StringRepresentable;

// ====================================================================================================
// = TorchPlacement enum
// ====================================================================================================
public enum TorchPlacement implements StringRepresentable {
  INACTIVE(0, "INACTIVE"), FLOOR(1, "FLOOR"), LEFT_WALL(2, "LEFT_WALL"), RIGHT_WALL(3, "RIGHT_WALL"), BOTH_WALLS(4, "BOTH_WALLS");

  private final int    index;
  private final String name;

  public static final TorchPlacement[]             VALUES      = new TorchPlacement[5];
  private static final Map<String, TorchPlacement> NAME_LOOKUP = Maps.<String, TorchPlacement>newHashMap();

  private TorchPlacement(int indexIn, String nameIn) {
    this.index = indexIn;
    this.name  = nameIn;
  }

  @Override
  public String getSerializedName() {
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
      NAME_LOOKUP.put(enumTP.getSerializedName().toLowerCase(Locale.ROOT), enumTP);
    }
  }

  public static String[] getValidValues() {
    String[] saRtrn = new String[VALUES.length];
    for (TorchPlacement enumTP : VALUES)
      saRtrn[enumTP.index] = enumTP.getSerializedName();
    return saRtrn;
  }
}
