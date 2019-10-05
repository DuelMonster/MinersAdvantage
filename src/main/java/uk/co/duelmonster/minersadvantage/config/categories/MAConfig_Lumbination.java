package uk.co.duelmonster.minersadvantage.config.categories;

import java.util.List;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import uk.co.duelmonster.minersadvantage.config.defaults.MAConfig_Defaults;

public class MAConfig_Lumbination extends MAConfig_BaseCategory {

	// ====================================================================================================
	// = Config variables - !! NOT TO BE USED DIRECTLY !!
	// = Use the retrieval and modification functions below at all times!
	// ====================================================================================================
	private final BooleanValue chopTreeBelow;
	private final BooleanValue destroyLeaves;
	private final BooleanValue leavesAffectDurability;
	private final BooleanValue replantSaplings;
	private final BooleanValue useShearsOnLeaves;
	private final IntValue leafRange;
	private final IntValue trunkRange;
	private final ConfigValue<List<String>> logs;
	private final ConfigValue<List<String>> leaves;
	private final ConfigValue<List<String>> axes;

	// ====================================================================================================
	// = Initialisation
	// ====================================================================================================
	public MAConfig_Lumbination(ForgeConfigSpec.Builder builder) {

		enabled = builder
				.comment("Enable/Disable Lumbination")
				.translation("minersadvantage.lumbination.enabled")
				.define("enabled", MAConfig_Defaults.Lumbination.enabled);

		chopTreeBelow = builder
				.comment("Chops any part of the Tree below the block being destroyed.")
				.translation("minersadvantage.lumbination.chop_below")
				.define("chop_below", MAConfig_Defaults.Lumbination.chopTreeBelow);

		destroyLeaves = builder
				.comment("Destroy the leaves when chopping down a tree?")
				.translation("minersadvantage.lumbination.destroy_leaves")
				.define("destroy_leaves", MAConfig_Defaults.Lumbination.destroyLeaves);

		leavesAffectDurability = builder
				.comment("If 'true' leaves will use your tools durability. If 'false' leaves with be destroyed without using a tool.")
				.translation("minersadvantage.lumbination.leaves_affect_durability")
				.define("leaves_affect_durability", MAConfig_Defaults.Lumbination.leavesAffectDurability);

		replantSaplings = builder
				.comment("If 'true' and saplings for the tree being harvested are avalible in the players inventory, they tree will be replanted.")
				.translation("minersadvantage.lumbination.replant_saplings")
				.define("replant_saplings", MAConfig_Defaults.Lumbination.replantSaplings);

		useShearsOnLeaves = builder
				.comment("If the player has shears on their hot bar, they will be used to harvest the leaves of the tree being harvested.")
				.translation("minersadvantage.lumbination.use_shears")
				.define("use_shears", MAConfig_Defaults.Lumbination.useShearsOnLeaves);

		leafRange = builder
				.comment("The block range to check for leaves.")
				.translation("minersadvantage.lumbination.leaf_range")
				.defineInRange("leaf_range", MAConfig_Defaults.Lumbination.leafRange, 0, 16);

		trunkRange = builder
				.comment("The block range to check for the tree Trunk width.")
				.translation("minersadvantage.lumbination.trunk_range")
				.defineInRange("trunk_range", MAConfig_Defaults.Lumbination.trunkRange, 8, 128);

		logs = builder
				.comment("List of Wood IDs.")
				.translation("minersadvantage.lumbination.logs")
				.define("logs", MAConfig_Defaults.Lumbination.logs);

		leaves = builder
				.comment("List of Leaf block IDs.")
				.translation("minersadvantage.lumbination.leaves")
				.define("leaves", MAConfig_Defaults.Lumbination.leaves);

		axes = builder
				.comment("List of vaild Axe IDs.")
				.translation("minersadvantage.lumbination.axes")
				.define("axes", MAConfig_Defaults.Lumbination.axes);
	}

	// ====================================================================================================
	// = Config retrieval functions
	// ====================================================================================================

	/**
	 * @return chopTreeBelow
	 */
	public boolean chopTreeBelow() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.enforceLumbinationSettings.get())
			return parentConfig.serverOverrides.lumbination.chopTreeBelow();

		return chopTreeBelow.get();
	}

	/**
	 * @param chopTreeBelow Sets chopTreeBelow
	 */
	public void setChopTreeBelow(boolean value) { chopTreeBelow.set(value); }

	/**
	 * @return destroyLeaves
	 */
	public boolean destroyLeaves() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.enforceLumbinationSettings.get())
			return parentConfig.serverOverrides.lumbination.destroyLeaves();

		return destroyLeaves.get();
	}

	/**
	 * @param destroyLeaves Sets destroyLeaves
	 */
	public void setDestroyLeaves(boolean value) { destroyLeaves.set(value); }

	/**
	 * @return leavesAffectDurability
	 */
	public boolean leavesAffectDurability() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.enforceLumbinationSettings.get())
			return parentConfig.serverOverrides.lumbination.leavesAffectDurability();

		return leavesAffectDurability.get();
	}

	/**
	 * @param _bLeavesAffectDurability Sets bLeavesAffectDurability
	 */
	public void setLeavesAffectDurability(boolean value) { leavesAffectDurability.set(value); }

	/**
	 * @return replantSaplings
	 */
	public boolean replantSaplings() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.enforceLumbinationSettings.get())
			return parentConfig.serverOverrides.lumbination.replantSaplings();

		return replantSaplings.get();
	}

	/**
	 * @param replantSaplings Sets replantSaplings
	 */
	public void setReplantSaplings(boolean value) { replantSaplings.set(value); }

	/**
	 * @return useShearsOnLeaves
	 */
	public boolean useShearsOnLeaves() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.enforceLumbinationSettings.get())
			return parentConfig.serverOverrides.lumbination.useShearsOnLeaves();

		return useShearsOnLeaves.get();
	}

	/**
	 * @param useShearsOnLeaves Sets useShearsOnLeaves
	 */
	public void setUseShearsOnLeaves(boolean value) { useShearsOnLeaves.set(value); }

	/**
	 * @return leafRange
	 */
	public int leafRange() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.enforceLumbinationSettings.get())
			return parentConfig.serverOverrides.lumbination.leafRange();

		return leafRange.get();
	}

	/**
	 * @param leafRange Sets leafRange
	 */
	public void setLeafRange(int value) { leafRange.set(value); }

	/**
	 * @return trunkRange
	 */
	public int trunkRange() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.enforceLumbinationSettings.get())
			return parentConfig.serverOverrides.lumbination.trunkRange();

		return trunkRange.get();
	}

	/**
	 * @param trunkRange Sets trunkRange
	 */
	public void setTrunkRange(int value) { trunkRange.set(value); }

	/**
	 * @return Lumbination Logs
	 */
	public List<String> logs() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.enforceLumbinationSettings.get())
			return parentConfig.serverOverrides.lumbination.logs();

		return logs.get();
	}

	/**
	 * @param _Logs Sets Lumbination Logs
	 */
	public void setLogs(List<String> value) { logs.set(value); }

	/**
	 * @return Lumbination Leaves
	 */
	public List<String> leaves() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.enforceLumbinationSettings.get())
			return parentConfig.serverOverrides.lumbination.leaves();

		return leaves.get();
	}

	/**
	 * @param leaves Sets Lumbination Leaves
	 */
	public void setLeaves(List<String> value) { leaves.set(value); }

	/**
	 * @return Lumbination Axes
	 */
	public List<String> axes() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.enforceLumbinationSettings.get())
			return parentConfig.serverOverrides.lumbination.axes();

		return axes.get();
	}

	/**
	 * @param axes Sets Lumbination Axes
	 */
	public void setAxes(List<String> value) { axes.set(value); }

}
