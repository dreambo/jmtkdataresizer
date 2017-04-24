package mtk.resizer.util;

import java.awt.Color;

public class Util {

	public static final long MB = 0x100000;		// 1MB =    1048576 Byte;
	public static final long GB = 0x40000000;	// 1GB = 1073741824 Byte;

	public final static long BPS = 0x200;
	public final static int CENT = 0x200 * 100;
	public final static int  ONE = Math.round(CENT/100f);
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
	public final static Color   SYSCOLOR = Color.CYAN;
	public final static Color CACHECOLOR = Color.YELLOW;
	public final static Color  DATACOLOR = Color.BLUE;
	public final static Color   FATCOLOR = Color.GREEN;
	public final static Color       DARK = Color.DARK_GRAY;

	public static boolean    FAT_PRESENT = true;

	public static int getPercent(int percent) {
		return Math.round(percent * 100f/CENT);
	}
}
