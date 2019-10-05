package uk.co.duelmonster.minersadvantage.common;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

public class RegistryHelper {

	private static IForgeRegistry<Block> blockRegistry = null;
	private static IForgeRegistry<Item> itemRegistry = null;
	private static IForgeRegistry<Enchantment> enchantmentRegistry = null;

	public static IForgeRegistry<Block> getBlockRegistry() {
		if (blockRegistry == null)
			blockRegistry = GameRegistry.findRegistry(Block.class);

		return blockRegistry;
	}

	public static IForgeRegistry<Item> getItemRegistry() {
		if (itemRegistry == null)
			itemRegistry = GameRegistry.findRegistry(Item.class);

		return itemRegistry;
	}

	public static IForgeRegistry<Enchantment> getEnchantmentRegistry() {
		if (enchantmentRegistry == null)
			enchantmentRegistry = GameRegistry.findRegistry(Enchantment.class);

		return enchantmentRegistry;
	}

}
