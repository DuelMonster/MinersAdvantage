package uk.co.duelmonster.minersadvantage.config.categories;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import uk.co.duelmonster.minersadvantage.common.Functions;
import uk.co.duelmonster.minersadvantage.config.defaults.MAConfig_Defaults;

public class MAConfig_Excavation extends MAConfig_BaseCategory {
	
	// ====================================================================================================
	// = Config variables - !! NOT TO BE USED DIRECTLY !!
	// = Use the retrieval and modification functions below at all times!
	// ====================================================================================================
	private final BooleanValue			toggleMode;
	private final BooleanValue			ignoreBlockVariants;
	private final BooleanValue			isToolWhitelist;
	private final ConfigValue<List<String>>	toolBlacklist;
	private final BooleanValue			isBlockWhitelist;
	private final ConfigValue<List<String>>	blockBlacklist;
	
	// ====================================================================================================
	// = Initialisation
	// ====================================================================================================
	public MAConfig_Excavation(ForgeConfigSpec.Builder builder) {
		
		enabled = builder
				.comment("Enable/Disable Excavation")
				.translation("minersadvantage.excavation.enabled")
				.define("enabled", MAConfig_Defaults.Excavation.enabled);
		
		toggleMode = builder
				.comment("Enable/Disable Excavation Toggle Mode.  When Enabled, pressing the Excavation keybindings will act as ON/OFF toggle switchs.", 
						"A notification message will be shown in chat to let you know if the Toggle is ON or OFF.")
				.translation("minersadvantage.excavation.toggle_mode")
				.define("toggle_mode", MAConfig_Defaults.Excavation.toggleMode);
		
		ignoreBlockVariants = builder
				.comment("Ignore Block Variations, like stone/granite/andesite/diorite, when using Excavation.")
				.translation("minersadvantage.excavation.ignore_variants")
				.define("ignore_variants", MAConfig_Defaults.Excavation.ignoreBlockVariants);
		
		isToolWhitelist = builder
				.comment("If 'true' Excavation will only work when using tools within the Tool Blacklist.")
				.translation("minersadvantage.excavation.is_tool_whitelist")
				.define("is_tool_whitelist", MAConfig_Defaults.Excavation.isToolWhitelist);
		
		toolBlacklist = builder
				.comment("List of blacklisted Tool IDs. Default is empty to allow the use of all Items (including hand).")
				.translation("minersadvantage.excavation.tool_blacklist")
				.define("tool_blacklist", MAConfig_Defaults.Excavation.toolBlacklist);
		
		isBlockWhitelist = builder
				.comment("If 'true' Excavation will only work when harvesting Blocks within the Blacklist.")
				.translation("minersadvantage.excavation.is_block_whitelist")
				.define("is_block_whitelist", MAConfig_Defaults.Excavation.isBlockWhitelist);
		
		blockBlacklist = builder
				.comment("List of blacklisted Block IDs. Default is empty.")
				.translation("minersadvantage.excavation.block_blacklist")
				.define("block_blacklist", MAConfig_Defaults.Excavation.blockBlacklist);
	}
	
	// ====================================================================================================
	// = Config retrieval functions
	// ====================================================================================================
	
	/**
	 * @return ignoreBlockVariants
	 */
	public boolean ignoreBlockVariants() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.enforceExcavationSettings.get())
			return parentConfig.serverOverrides.excavation.ignoreBlockVariants();
		
		return ignoreBlockVariants.get();
	}
	
	/**
	 * @param ignoreBlockVariants
	 *            Sets ignoreBlockVariants
	 */
	public void setIgnoreBlockVariants(boolean value) { ignoreBlockVariants.set(value); }
	
	/**
	 * @return toggleMode
	 */
	public boolean toggleMode() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.enforceExcavationSettings.get())
			return parentConfig.serverOverrides.excavation.toggleMode();
		
		return toggleMode.get();
	}
	
	/**
	 * @param toggleMode
	 *            Sets toggleMode
	 */
	public void setToggleMode(boolean value) { toggleMode.set(value); }
	
	/**
	 * @return isToolWhitelist
	 */
	public boolean isToolWhitelist() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.enforceExcavationSettings.get())
			return parentConfig.serverOverrides.excavation.isToolWhitelist();
		
		return isToolWhitelist.get();
	}
	
	/**
	 * @param isToolWhitelist
	 *            Sets isToolWhitelist
	 */
	public void setIsToolWhitelist(boolean value) { isToolWhitelist.set(value); }
	
	/**
	 * @return toolBlacklist
	 */
	public List<String> toolBlacklist() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.enforceExcavationSettings.get())
			return parentConfig.serverOverrides.excavation.toolBlacklist();
		
		return toolBlacklist.get();
	}
	
	/**
	 * @param _toolBlacklist
	 *            Sets toolBlacklist
	 */
	public void setToolBlacklist(List<String> value) { toolBlacklist.set(value); }
	
	/**
	 * @return isBlockWhitelist
	 */
	public boolean isBlockWhitelist() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.enforceExcavationSettings.get())
			return parentConfig.serverOverrides.excavation.isBlockWhitelist();
		
		return isBlockWhitelist.get();
	}
	
	/**
	 * @param isBlockWhitelist
	 *            Sets isBlockWhitelist
	 */
	public void setIsBlockWhitelist(boolean value) { isBlockWhitelist.set(value); }
	
	/**
	 * @return blockBlacklist
	 */
	public List<String> blockBlacklist() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.enforceExcavationSettings.get())
			return parentConfig.serverOverrides.excavation.blockBlacklist();
		
		return blockBlacklist.get();
	}
	
	/**
	 * @param blockBlacklist
	 *            Sets blockBlacklist
	 */
	public void setBlockBlacklist(List<String> value) { blockBlacklist.set(value); }
	
	// ====================================================================================================
	// = Config utility functions
	// ====================================================================================================
	
	public boolean isBlacklisted(ItemStack item) {
		if (item == null || item.isEmpty())
			return isToolWhitelist();
		
		return isBlacklisted(item.getItem());
	}
	
	public boolean isBlacklisted(Item item) {
		if (item == null)
			return isToolWhitelist();

		if (toolBlacklist().contains(Functions.getName(item)))
			return !isToolWhitelist();
		
		return isToolWhitelist();
	}
	
	public boolean isBlacklisted(Block block) {
		if (block == null || block == Blocks.AIR)
			return isBlockWhitelist();
		
		if (blockBlacklist().contains(Functions.getName(block)))
			return !isBlockWhitelist();
		
		return isBlockWhitelist();
	}

}
