package uk.co.duelmonster.minersadvantage.common;

import java.util.List;
import java.util.UUID;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.slf4j.Logger;
import uk.co.duelmonster.minersadvantage.common.log.LogUtils;

/**
 * Constants is the teammate that keeps this part of the mod understandable and stable.
 * It exists so behavior stays explicit instead of becoming mystery spaghetti at 2 AM.
 */
public class Constants {
    public static final Logger LOGGER = LogUtils.LOG;

    public static final String MOD_ID = "minersadvantage";
    public static final String MOD_NAME = "MinersAdvantage";
    public static final String MOD_NAME_MSG = "[MinersAdvantage] ";
    public static final String PROTOCOL_VERSION = "1.5";
    public static final UUID instanceUID = UUID.randomUUID();

    public static final int MIN_HUNGER = 1;
    public static final int DEFAULT_BLOCKRADIUS = 5;
    public static final int MIN_BLOCKRADIUS = 2;
    public static final int MAX_BLOCKRADIUS = 128;
    public static final int DEFAULT_BLOCKLIMIT = (DEFAULT_BLOCKRADIUS * DEFAULT_BLOCKRADIUS) * DEFAULT_BLOCKRADIUS;
    public static final int MIN_BLOCKLIMIT = (MIN_BLOCKRADIUS * MIN_BLOCKRADIUS) * MIN_BLOCKRADIUS;
    public static final int MAX_BLOCKLIMIT = (MAX_BLOCKRADIUS * MAX_BLOCKRADIUS) * MAX_BLOCKRADIUS;
    public static final int MIN_BREAKSPEED = 1;
    public static final int MAX_BREAKSPEED = 32;
    public static final int MAX_TREE_WIDTH = 128;

    public static final String FVC_URL = "http://duelmonster.000webhostapp.com/Minecraft/MinersAdvantage/forge_update.json";
    public static final String NAMESPACE = "uk.co.duelmonster.minersadvantage.";
    public static final String PROXY = NAMESPACE + "proxies.";
    public static final String GUI_FACTORY = NAMESPACE + "client.config.ConfigGuiFactory";

    public static final String COMMON_ID = MOD_ID + ".common";
    public static final String SERVER_ID = MOD_ID + ".server";
    public static final String CAPTIVATION_ID = MOD_ID + ".captivation";
    public static final String CROPINATION_ID = MOD_ID + ".cropination";
    public static final String EXCAVATION_ID = MOD_ID + ".excavation";
    public static final String PATHANATION_ID = MOD_ID + ".pathanation";
    public static final String ILLUMINATION_ID = MOD_ID + ".illumination";
    public static final String LUMBINATION_ID = MOD_ID + ".lumbination";
    public static final String SHAFTANATION_ID = MOD_ID + ".shaftanation";
    public static final String SUBSTITUTION_ID = MOD_ID + ".substitution";
    public static final String VEINATION_ID = MOD_ID + ".veination";

    public static final List<Ranking> RANKING_SILK_TOUCH = List.of(
        Ranking.EFFICIENCY_SILK_TOUCH,
        Ranking.SILK_TOUCH,
        Ranking.EFFICIENCY_FORTUNE,
        Ranking.FORTUNE,
        Ranking.EFFICIENCY,
        Ranking.NONE
    );
    public static final List<Ranking> RANKING_FORTUNE = List.of(
        Ranking.EFFICIENCY_FORTUNE,
        Ranking.FORTUNE,
        Ranking.EFFICIENCY_SILK_TOUCH,
        Ranking.SILK_TOUCH,
        Ranking.EFFICIENCY,
        Ranking.NONE
    );
    public static final List<Ranking> RANKING_DEFAULT = List.of(
        Ranking.EFFICIENCY,
        Ranking.EFFICIENCY_FORTUNE,
        Ranking.EFFICIENCY_SILK_TOUCH,
        Ranking.FORTUNE,
        Ranking.SILK_TOUCH,
        Ranking.NONE
    );

    public static final List<Block> DIRT_BLOCKS = List.of(
        Blocks.DIRT,
        Blocks.GRASS_BLOCK,
        Blocks.PODZOL,
        Blocks.COARSE_DIRT,
        Blocks.MYCELIUM
    );
}
