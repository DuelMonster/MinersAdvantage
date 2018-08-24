package co.uk.duelmonster.minersadvantage.handlers;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.commons.lang3.ArrayUtils;

import co.uk.duelmonster.minersadvantage.common.Constants;
import co.uk.duelmonster.minersadvantage.config.MAConfig;
import co.uk.duelmonster.minersadvantage.settings.ConfigHandler;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class VeinationHandler {
	
	private static boolean bOresGot = false;
	
	public static void getOreList() {
		if (!bOresGot) {
			// Check the Forge OreDictionary for any extra Ores not included in the config file.
			Collection<String> saOreNames = Arrays.asList(OreDictionary.getOreNames());
			
			final String[] veinationOres = MAConfig.get().veination.ores();
			
			saOreNames.stream()
					.filter(new Predicate<String>() {
						@Override
						public boolean test(String ore) {
							return ore.startsWith("ore");
						}
					})
					.forEach(new Consumer<String>() {
						@Override
						public void accept(String ore) {
							OreDictionary.getOres(ore).stream()
									.filter(new Predicate<ItemStack>() {
										@Override
										public boolean test(ItemStack item) {
											return item.getItem() instanceof ItemBlock;
										}
									}).forEach(new Consumer<ItemStack>() {
										@Override
										public void accept(ItemStack item) {
											String sID = item.getItem().getRegistryName().toString().trim();
											if (!ArrayUtils.contains(veinationOres, sID))
												if (sID.toLowerCase().contains("redstone")) {
													ArrayUtils.add(veinationOres, Blocks.REDSTONE_ORE.getRegistryName().toString().trim());
													ArrayUtils.add(veinationOres, Blocks.LIT_REDSTONE_ORE.getRegistryName().toString().trim());
												} else
													ArrayUtils.add(veinationOres, sID);
										}
									});
						}
					});
			
			ConfigHandler.setValue(Constants.VEINATION_ID, "ores", veinationOres.toString());
			
			ConfigHandler.save();
			
			bOresGot = true;
		}
	}
	
}
