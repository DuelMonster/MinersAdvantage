package co.uk.duelmonster.minersadvantage.common;

public class Debugger {
	private static boolean IsEnabled = true;
	
	public static void log(Object o) {
		if (IsEnabled)
			System.out.println(o.toString());
	}
}
