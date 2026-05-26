package uk.co.duelmonster.minersadvantage.common.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class RegistryPredicates {
    private RegistryPredicates() {
    }

    public static boolean isPickaxeTool(ItemStack stack) {
        try {
            return BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().endsWith("_pickaxe");
        } catch (RuntimeException | LinkageError ignored) {
            return false;
        }
    }

    public static boolean isAxeTool(ItemStack stack) {
        return stack.getItem() instanceof AxeItem;
    }

    public static boolean isShovelTool(ItemStack stack) {
        return stack.getItem() instanceof ShovelItem;
    }

    public static boolean isHoeTool(ItemStack stack) {
        return stack.getItem() instanceof HoeItem;
    }

    public static boolean isSwordToolId(String toolId) {
        return normalizedPath(toolId).endsWith("_sword") || normalizedPath(toolId).equals("sword");
    }

    public static boolean isCombatToolId(String toolId) {
        return normalizedPath(toolId).contains("combat");
    }

    public static boolean isOreLike(BlockState state) {
        return state.is(BlockTags.COAL_ORES)
            || state.is(BlockTags.IRON_ORES)
            || state.is(BlockTags.COPPER_ORES)
            || state.is(BlockTags.GOLD_ORES)
            || state.is(BlockTags.REDSTONE_ORES)
            || state.is(BlockTags.EMERALD_ORES)
            || state.is(BlockTags.LAPIS_ORES)
            || state.is(BlockTags.DIAMOND_ORES)
            || state.is(Blocks.ANCIENT_DEBRIS);
    }

    public static boolean isOreLikeBlockId(String blockId) {
        BlockState state = resolveBlockState(blockId);
        if (state != null) {
            return isOreLike(state);
        }

        String path = normalizedPath(blockId);
        return path.endsWith("_ore") || path.equals("ancient_debris");
    }

    public static boolean isStoneLike(BlockState state) {
        return state.is(BlockTags.BASE_STONE_OVERWORLD)
            || state.is(BlockTags.BASE_STONE_NETHER)
            || state.is(Blocks.DEEPSLATE)
            || state.is(Blocks.TUFF)
            || state.is(Blocks.CALCITE);
    }

    public static boolean isStoneLikeBlockId(String blockId) {
        BlockState state = resolveBlockState(blockId);
        if (state != null) {
            return isStoneLike(state);
        }

        String path = normalizedPath(blockId);
        return path.contains("stone")
            || path.contains("deepslate")
            || path.contains("tuff")
            || path.contains("calcite")
            || path.contains("netherrack")
            || path.contains("blackstone")
            || path.contains("basalt");
    }

    public static boolean isDirtLike(BlockState state) {
        return state.is(BlockTags.DIRT) || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT_PATH);
    }

    public static boolean isDirtLikeBlockId(String blockId) {
        BlockState state = resolveBlockState(blockId);
        if (state != null) {
            return isDirtLike(state);
        }

        String path = normalizedPath(blockId);
        return path.contains("dirt")
            || path.contains("grass_block")
            || path.contains("podzol")
            || path.contains("mycelium")
            || path.contains("farmland");
    }

    public static boolean isCropBlock(BlockState state) {
        return state.getBlock() instanceof CropBlock || state.getBlock() instanceof NetherWartBlock;
    }

    public static boolean isCropBlockId(String blockId) {
        BlockState state = resolveBlockState(blockId);
        if (state != null) {
            return isCropBlock(state);
        }

        String path = normalizedPath(blockId);
        return path.contains("crop")
            || path.contains("wheat")
            || path.contains("carrot")
            || path.contains("potato")
            || path.contains("beetroot")
            || path.contains("nether_wart");
    }

    public static boolean isLogLike(BlockState state) {
        return state.is(BlockTags.LOGS);
    }

    public static boolean isLogLikeBlockId(String blockId) {
        BlockState state = resolveBlockState(blockId);
        if (state != null) {
            return isLogLike(state);
        }

        String path = normalizedPath(blockId);
        return path.endsWith("_log") || path.endsWith("_stem") || path.endsWith("_hyphae");
    }

    public static boolean isLeafLike(BlockState state) {
        return state.is(BlockTags.LEAVES) || state.is(BlockTags.WART_BLOCKS);
    }

    public static boolean isLeafLikeBlockId(String blockId) {
        BlockState state = resolveBlockState(blockId);
        if (state != null) {
            return isLeafLike(state);
        }

        String path = normalizedPath(blockId);
        return path.endsWith("_leaves") || path.endsWith("_wart_block");
    }

    public static BlockState resolveBlockState(String blockId) {
        if (blockId == null || blockId.isBlank() || blockId.equals("air")) {
            return null;
        }

        try {
            for (Block block : BuiltInRegistries.BLOCK) {
                if (BuiltInRegistries.BLOCK.getKey(block).toString().equals(blockId)) {
                    return block.defaultBlockState();
                }
            }
        } catch (RuntimeException | LinkageError ignored) {
            // Some test runtimes do not provide a fully bootstrapped Minecraft registry.
        }
        return null;
    }

    public static boolean isContextEntity(String blockId) {
        return blockId != null && blockId.startsWith("entity:");
    }

    public static boolean isContextItem(String blockId) {
        return blockId != null && blockId.startsWith("item:");
    }

    public static boolean isGUIContext(String toolId) {
        return toolId != null && toolId.contains("gui");
    }

    public static boolean isShovelToolId(String toolId) {
        return "shovel".equals(toolId);
    }

    public static boolean isSaplingBlock(String blockId) {
        return blockId != null && blockId.contains("sapling");
    }

    public static boolean isManualToolMode(String toolHint) {
        return toolHint != null && toolHint.contains("manual");
    }

    public static boolean isManualLeftMode(String toolHint) {
        return toolHint != null && toolHint.contains("manual_left");
    }

    public static boolean isManualRightMode(String toolHint) {
        return toolHint != null && toolHint.contains("manual_right");
    }

    private static String normalizedPath(String id) {
        if (id == null || id.isBlank()) {
            return "";
        }
        int separator = id.indexOf(':');
        if (separator >= 0 && separator + 1 < id.length()) {
            return id.substring(separator + 1);
        }
        return id;
    }
}
