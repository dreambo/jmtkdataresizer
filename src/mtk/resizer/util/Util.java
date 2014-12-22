package mtk.resizer.util;

import java.awt.Color;

public class Util {

	public final static long BPS = 0x200;
	public final static int CENT = 0x200 * 100;
	public static int		  BS = 0x20000;

	// partition names
	public final static String MBR		= "MBR";
	public final static String EBR1		= "EBR1";
	public final static String EBR2		= "EBR2";
	public final static String SYS		= "ANDROID";
	public final static String CACHE	= "CACHE";
	public final static String DATA		= "USRDATA";
	public final static String FAT		= "FAT";
	public final static String ALL		= "." + MBR + "." + EBR1 + "." + EBR2 + "." + SYS + "." + CACHE + "." + DATA + "." + FAT;

	// partition colors
	public final static Color SYSCOLOR	 = Color.CYAN;
	public final static Color CACHECOLOR = Color.YELLOW;
	public final static Color DATACOLOR	 = Color.BLUE;
	public final static Color FATCOLOR	 = Color.GREEN;

	public static int getPercent(int percent) {
		return Math.round(percent * 100f/CENT);
	}
}
