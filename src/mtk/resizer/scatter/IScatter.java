package mtk.resizer.scatter;

import java.io.IOException;
import java.util.Map;

public interface IScatter {

	public static final String PARTITION_NAME		= "partition_name:";
	public static final String FILE_NAME			= "file_name:";
	public static final String LINEAR_START_ADDR	= "linear_start_addr:";
	public static final String PHYSICAL_START_ADDR	= "physical_start_addr:";
	public static final String PARTITION_SIZE		= "partition_size:";

	public static final String PLATFORM				= "platform:";
	public static final String PROJECT				= "project:";
	public static final String STORAGE				= "storage:";
	public static final String BLOCK_SIZE			= "block_size:";

	public final static String SYS_START	= "SYS_START";
	public final static String SYS_SIZE		= "SYS_SIZE";
	public final static String CACHE_START	= "CACHE_START";
	public final static String CACHE_SIZE	= "CACHE_SIZE";
	public final static String DATA_START	= "DATA_START";
	public final static String DATA_SIZE	= "DATA_SIZE";
	public final static String FAT_START	= "FAT_START";
	public final static String FAT_SIZE 	= "FAT_SIZE";

	public final static String MBR_NEW   = "MBR_NEW";
	public final static String EBR1_NEW  = "EBR1_NEW";
	public final static String EBR2_NEW  = "EBR2_NEW";

	public static final String NL 		 = System.getProperty("line.separator");

	public static class Info {
		public String file_name;
		public String linear_start_addr;
		public String physical_start_addr;
		public String partition_size;

		@Override
		public String toString() {

			return  FILE_NAME			+ " " + file_name			+ NL +
					LINEAR_START_ADDR	+ " " + linear_start_addr	+ NL +
					PHYSICAL_START_ADDR	+ " " + physical_start_addr	+ NL +
					PARTITION_SIZE		+ " " + partition_size		+ NL;
		}

		public boolean isComplete() {
			return (physical_start_addr != null);
		}
	}

	public abstract void load() throws IOException;
	public abstract long getStart(String type);
	public abstract long getSize(String type);
	public abstract Map<String, Info> getInfos();
	public void writeMod(Map<String, String[]> vals) throws IOException;
}