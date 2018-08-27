package co.uk.duelmonster.minersadvantage.handlers;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang3.ArrayUtils;

import co.uk.duelmonster.minersadvantage.common.Constants;
import co.uk.duelmonster.minersadvantage.config.MAConfig;
import co.uk.duelmonster.minersadvantage.settings.ConfigHandler;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.oredict.OreDictionary;

public class VeinationHandler {
	
	private static boolean		bOresGot		= false;
	protected static String[]	veinationOres	= null;
	
	public static void getOreList() {
		if (!bOresGot) {
			// Check the Forge OreDictionary for any extra Ores not included in the config file.
			Collection<String> saOreNames = Arrays.asList(OreDictionary.getOreNames());
			
			veinationOres = MAConfig.get().veination.ores();
			
			saOreNames.stream()
					.filter(ore -> ore.startsWith("ore"))
					.forEach(ore -> {
						OreDictionary.getOres(ore).stream()
								.filter(item -> item.getItem() instanceof ItemBlock).forEach(item -> {
									String sID = item.getItem().getRegistryName().toString().trim();
									if (!ArrayUtils.contains(veinationOres, sID))
										if (sID.toLowerCase().contains("redstone")) {
											veinationOres = ArrayUtils.add(veinationOres, Blocks.REDSTONE_ORE.getRegistryName().toString().trim());
											veinationOres = ArrayUtils.add(veinationOres, Blocks.LIT_REDSTONE_ORE.getRegistryName().toString().trim());
										} else
											veinationOres = ArrayUtils.add(veinationOres, sID);
								});
					});
			
			ConfigHandler.setValue(Constants.VEINATION_ID, "ores", veinationOres.clone());
			
			ConfigHandler.save();
			
			bOresGot = true;
		}
	}
	
}
