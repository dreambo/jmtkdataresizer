package mtk.resizer.scatter;

import java.io.IOException;
import java.util.List;

public interface IScatter {

	public final static String DATA = "USRDATA";
	public final static String FAT  = "FAT";

	public static final String PARTITION_NAME		= "partition_name:";
	public static final String PHYSICAL_START_ADDR	= "physical_start_addr:";
	public static final String PARTITION_SIZE		= "partition_size:";
	public static final String FILE_NAME			= "file_name:";

	public static final String PLATFORM				= "platform:";
	public static final String PROJECT				= "project:";
	public static final String STORAGE				= "storage:";
	public static final String BLOCK_SIZE			= "block_size:";

	public final static String DATA_SIZE = "DATA_SIZE";
	public final static String FAT_SIZE  = "FAT_SIZE";
	public final static String FAT_START = "FAT_START";
	public final static String MBR_NEW   = "MBR_NEW";
	public final static String EBR1_NEW  = "EBR1_NEW";
	public final static String EBR2_NEW  = "EBR2_NEW";

	public static final String NL 					= System.getProperty("line.separator");

	public final static String MBR = "MBR";
	public final static String EBR1 = "EBR1";
	public final static String EBR2 = "EBR2";

	public static class Info {
		public String partition_name;
		public String file_name;
		public String physical_start_addr;
		public String partition_size;

		@Override
		public String toString() {

			return  PARTITION_NAME		+ " " + partition_name		+ NL +
					FILE_NAME			+ " " + file_name			+ NL +
					PHYSICAL_START_ADDR + " " + physical_start_addr + NL +
					PARTITION_SIZE		+ " " + partition_size		+ NL;
		}

		public boolean isComplete() {
			return (partition_name != null && physical_start_addr != null);
		}
	}

	public abstract void load() throws IOException;
	public abstract long getMBRStart();
	public abstract long getDataStart();
	public abstract long getFatStart();
	public abstract long getDataSize();
	public abstract long getFatSize();
	public abstract List<Info> getInfos();
}