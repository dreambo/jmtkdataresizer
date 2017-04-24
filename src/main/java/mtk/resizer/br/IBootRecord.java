package mtk.resizer.br;

import java.io.IOException;

public interface IBootRecord {

	public final static String BOOT_SIGN = "55AA";
	public final static int PARTS_OFFSET = 0x1BE;
	public final static int   PART_START = 0x008;
	public final static int    PART_SIZE = 0x00C;
	public final static int    PART_TYPE = 0x004;
	public final static int			 BPS = 0x200;


	public abstract int getPartType  (int partNb) throws IOException;
	public abstract long getPartStart(int partNb) throws IOException;
	public abstract long getPartSize (int partNb) throws IOException;
	public abstract void setPartStart(int partNb, long start) throws IOException;
	public abstract void setPartSize (int partNb, long size)  throws IOException;
}
