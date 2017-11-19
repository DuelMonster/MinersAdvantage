package co.uk.duelmonster.minersadvantage.common;

public class RankAndLevel {
	public Ranking	rank;
	public int		Level_1	= 0;
	public int		Level_2	= 0;
	
	public RankAndLevel(Ranking rank, int Level) {
		this(rank, Level, -1);
	}
	
	public RankAndLevel(Ranking rank, int Level_1, int Level_2) {
		this.rank = rank;
		this.Level_1 = Level_1;
		this.Level_2 = Level_2;
	}
}
