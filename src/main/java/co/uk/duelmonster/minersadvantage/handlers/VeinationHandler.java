package co.uk.duelmonster.minersadvantage.handlers;

import java.util.Arrays;
import java.util.Collection;

import com.google.gson.JsonObject;

import co.uk.duelmonster.minersadvantage.common.Constants;
import co.uk.duelmonster.minersadvantage.settings.ConfigHandler;
import co.uk.duelmonster.minersadvantage.settings.Settings;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.oredict.OreDictionary;

public class VeinationHandler {
	
	private static boolean bOresGot = false;
	
	public static void getOreList() {
		if (!bOresGot) {
			// Check the Forge OreDictionary for any extra Ores not included in the config file.
			Collection<String> saOreNames = Arrays.asList(OreDictionary.getOreNames());
			
			JsonObject veinationOres = Settings.get().veinationOres();
			
			saOreNames.stream()
					.filter(ore -> ore.startsWith("ore"))
					.forEach(ore -> {
						OreDictionary.getOres(ore).stream()
								.filter(item -> item.getItem() instanceof ItemBlock).forEach(item -> {
									String sID = item.getItem().getRegistryName().toString().trim();
									if (!veinationOres.has(sID))
										if (sID.toLowerCase().contains("redstone")) {
											veinationOres.addProperty(Blocks.REDSTONE_ORE.getRegistryName().toString().trim(), "");
											veinationOres.addProperty(Blocks.LIT_REDSTONE_ORE.getRegistryName().toString().trim(), "");
										} else
											veinationOres.addProperty(sID, "");
								});
					});
			
			ConfigHandler.setValue(Constants.VEINATION_ID, "ores", veinationOres.toString());
			
			ConfigHandler.save();
			
			bOresGot = true;
		}
	}
	
}
