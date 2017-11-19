package co.uk.duelmonster.minersadvantage.handlers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.uk.duelmonster.minersadvantage.client.ClientFunctions;
import co.uk.duelmonster.minersadvantage.common.Constants;
import co.uk.duelmonster.minersadvantage.common.RankAndLevel;
import co.uk.duelmonster.minersadvantage.common.Ranking;
import co.uk.duelmonster.minersadvantage.settings.Settings;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

public class SubstitutionHandler {
	
	public static SubstitutionHandler instance = new SubstitutionHandler();
	
	private World			world		= null;
	private EntityPlayer	player		= null;
	private Settings		settings	= null;
	
	// private boolean wasAttacking = false;
	public int				iPrevSlot			= -1;
	public int				iOptimalSlot		= -1;
	private int				iOptimalRankIndx	= -1;
	private RankAndLevel	oOptimalRank		= null;
	
	private List<Ranking>	rankings	= Constants.RANKING_DEFAULT;
	private BlockPos		oPos		= null;
	private IBlockState		state		= null;
	private Block			block		= null;
	
	public void reset() {
		world = null;
		player = null;
		settings = null;
		// wasAttacking = false;
		iPrevSlot = -1;
		iOptimalSlot = -1;
		iOptimalRankIndx = -1;
		oOptimalRank = null;
		rankings = Constants.RANKING_DEFAULT;
		oPos = null;
		state = null;
		block = null;
	}
	
	public void processToolSubtitution(World _world, EntityPlayerSP _player, BlockPos _pos) {
		reset();
		
		this.world = _world;
		this.player = _player;
		this.settings = Settings.get();
		this.oPos = _pos;
		this.state = world.getBlockState(oPos);
		this.block = state.getBlock();
		
		InventoryPlayer inventory = player.inventory;
		
		boolean silkTouchable = block.canSilkHarvest(world, oPos, state, player);
		
		// Rank the valid inventory slots that contain effective tools
		HashMap<Integer, RankAndLevel> rankingMap = new HashMap<Integer, RankAndLevel>();
		for (int iSlot = 0; iSlot < 9; iSlot++) {
			ItemStack itemStack = inventory.getStackInSlot(iSlot);
			if (itemStack != null && ForgeHooks.isToolEffective(world, oPos, itemStack)) {
				Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(itemStack);
				
				if (enchantments.containsKey(Enchantments.EFFICIENCY) && enchantments.containsKey(Enchantments.SILK_TOUCH))
					rankingMap.put(iSlot, new RankAndLevel(Ranking.EFFICIENCY_SILK_TOUCH, enchantments.get(Enchantments.EFFICIENCY)));
				else if (enchantments.containsKey(Enchantments.EFFICIENCY) && enchantments.containsKey(Enchantments.FORTUNE))
					rankingMap.put(iSlot, new RankAndLevel(Ranking.EFFICIENCY_FORTUNE, enchantments.get(Enchantments.EFFICIENCY), enchantments.get(Enchantments.FORTUNE)));
				else if (enchantments.containsKey(Enchantments.EFFICIENCY))
					rankingMap.put(iSlot, new RankAndLevel(Ranking.EFFICIENCY, enchantments.get(Enchantments.EFFICIENCY)));
				else if (enchantments.containsKey(Enchantments.FORTUNE))
					rankingMap.put(iSlot, new RankAndLevel(Ranking.FORTUNE, enchantments.get(Enchantments.FORTUNE)));
				else if (enchantments.containsKey(Enchantments.SILK_TOUCH))
					rankingMap.put(iSlot, new RankAndLevel(Ranking.SILK_TOUCH, 1));
				else
					rankingMap.put(iSlot, new RankAndLevel(Ranking.NONE, 0));
			}
		}
		
		// If there were no valid tools found then we jump out
		if (rankingMap.isEmpty())
			return;
		
		// Decide which is the required ranking list based on user settings
		if (silkTouchable && settings.bFavourSilkTouch)
			rankings = Constants.RANKING_SILK_TOUCH;
		else if (settings.bFavourFortune)
			rankings = Constants.RANKING_FORTUNE;
		else
			rankings = Constants.RANKING_DEFAULT;
		
		// Identify the most optimal tool for the job
		rankings.forEach(rank -> {
			rankingMap.entrySet().forEach(rankedSlot -> {
				
				RankAndLevel rankAndLevel = rankedSlot.getValue();
				int iCurrentRankIndx = rankings.size() - rankings.indexOf(rank);
				
				if (rankAndLevel.rank == rank && iOptimalRankIndx < iCurrentRankIndx &&
						(oOptimalRank == null ||
								(rankAndLevel.rank == oOptimalRank.rank &&
										rankAndLevel.Level_1 > oOptimalRank.Level_1 &&
										(rankAndLevel.Level_2 == -1 || rankAndLevel.Level_2 > oOptimalRank.Level_2)))) {
					iOptimalSlot = rankedSlot.getKey();
					iOptimalRankIndx = iCurrentRankIndx;
					oOptimalRank = rankAndLevel;
				}
			});
		});
		
		// Substitute the Players current item with the most optimal tool for the job
		if (iOptimalSlot >= 0 && iOptimalSlot != iPrevSlot) {
			iPrevSlot = inventory.currentItem;
			inventory.currentItem = iOptimalSlot;
			ClientFunctions.syncCurrentPlayItem(iOptimalSlot);
			try {
				Thread.sleep(100);
			}
			catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
