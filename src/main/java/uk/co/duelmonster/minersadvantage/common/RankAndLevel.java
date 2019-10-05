package uk.co.duelmonster.minersadvantage.common;

public class RankAndLevel {
	public int		SlotID	= -1;
	public Ranking	rank;
	public int		Level_1	= 0;
	public int		Level_2	= 0;
	
	public RankAndLevel(int iSlotID, Ranking rank, int Level) {
		this(iSlotID, rank, Level, -1);
	}
	
	public RankAndLevel(int iSlotID, Ranking rank, int Level_1, int Level_2) {
		this.SlotID = iSlotID;
		this.rank = rank;
		this.Level_1 = Level_1;
		this.Level_2 = Level_2;
	}
}
