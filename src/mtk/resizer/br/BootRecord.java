package mtk.resizer.br;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import mtk.resizer.scatter.Scatter;
import mtk.resizer.scatter.Scatter1;
import mtk.resizer.util.Util;

public class BootRecord implements IBootRecord {

	public File BR;
	private IBootRecord parent;
	private byte[] bytes;

	public static long offsetMBR;
	public final static Map<String, Map<Integer, BootRecord>> parts = new HashMap<String, Map<Integer, BootRecord>>();

	public BootRecord(File BR, BootRecord parent) throws Exception {
		this.BR = BR;
		this.parent = parent;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		String DEFAULT_DIR = "C:/Users/SONY/Desktop/Tools/MTK/MtkDroidTools/backups/709v92_jbla828_141010_backup_141208-194710";
		Scatter scatter = new Scatter1(new File(DEFAULT_DIR, "MT6592_Android_scatter.txt"));
		//long dataSize = scatter.dataSize;
    	//long totalSize = scatter.totalSize;

    	BootRecord MBR  = new BootRecord(new File(DEFAULT_DIR, "MBR"), null);
    	BootRecord EBR1 = new BootRecord(new File(DEFAULT_DIR, "EBR1"), MBR);
    	BootRecord EBR2 = new BootRecord(new File(DEFAULT_DIR, "EBR2"), MBR);
    	BootRecord.offsetMBR = scatter.getMBRStart();

    	long offsetEBR1 = EBR1.getOffset(true);
    	long offsetEBR2 = EBR2.getOffset(false);
    	if (offsetEBR2 == 0) {
    		EBR2.setParent(EBR1);
    		offsetEBR2 = EBR2.getOffset(true);
    	}

    	System.out.println("offsetMBR="  + offsetMBR);
    	System.out.println("offsetEBR1=" + offsetEBR1);
    	System.out.println("offsetEBR2=" + offsetEBR2);
	}

	/**
	 * Assuming that the first extended partition is the EBR partition, get its start
	 * @param bytes
	 * @return
	 */
	public long getOffset(boolean first)  throws IOException {

		if (parent == null) {
			return -1;
		}

		// scan all part searching the extended partition
		for (int i = 0; i < 4; i++) {
			if (parent.getPartType(i) == 5) {
				if (first) {
					return parent.getPartStart(i);
				}

				first = true;
			}
		}

		return 0;
	}

	/**
	 * little endian read from an array of bytes
	 * @param bytes
	 * @param partNb
	 * @return
	 */
	public int getPartType(int partNb)  throws IOException {

		int offset = PARTS_OFFSET + 16*partNb + PART_TYPE;
		return (int) getSectors(offset, 1);
	}


	/**
	 * little endian read from an array of bytes
	 * @param bytes
	 * @param partNb
	 * @return
	 */
	public long getPartStart(int partNb)  throws IOException {

		int offset = PARTS_OFFSET + 16*partNb + PART_START;
		return getSectors(offset, 4);
	}


	/**
	 * little endian read from an array of bytes
	 * @param bytes
	 * @param partNb
	 * @return
	 */
	public void setPartSize(int partNb, long size)  throws IOException {

		writeSectors(partNb, false, size);
	}


	/**
	 * little endian read from an array of bytes
	 * @param bytes
	 * @param partNb
	 * @return
	 */
	public void setPartStart(int partNb, long start) {

		writeSectors(partNb, true, start);
	}


	/**
	 * little endian read from an array of bytes
	 * @param bytes
	 * @param partNb
	 * @return
	 */
	public long getPartSize(int partNb)  throws IOException {

		int offset = PARTS_OFFSET + 16*partNb + PART_SIZE;
		return getSectors(offset, 4);
	}

	/**
	 * little endian read from an array of bytes
	 * @param bytes
	 * @param offset
	 * @param size
	 * @return
	 */
	private long getSectors(int offset, int size) throws IOException {

		String hex = "";
		readBytes();

		if (bytes == null) {
			return -1;
		}

		for (int i = size; i > 0; i--) {
			hex += String.format("%02x", bytes[offset + i - 1] & 0xFF);
		}

		//System.out.println("Hex of offset " + offset + ": " + hex);
		return Long.valueOf(hex, 16);
	}

	/**
	 * little endian write in bytes array, the integer
	 * @param bytes
	 * @param partNb
	 * @param start
	 * @param data
	 * @return
	 */
	private void writeSectors(int partNb, boolean start, long value) {

		int first = PARTS_OFFSET + 16*partNb + (start ? PART_START : PART_SIZE);
		byte[] data = toBytes(value);

		if (bytes == null || bytes.length < BPS || data.length != 4) {
			System.out.println("Array " + bytes + " or " + value + " not OK : array must be " + BPS + " and int 4 bytes");
			return;
		}

		for (int i = 0; i < data.length; i++) {
			bytes[first + i] = data[data.length -1 - i];
		}
	}

	private static byte[] toBytes(long value) {

		int MAX = 8;
		String hex = Long.toHexString(value);

		if (hex.length() > MAX) {
			return null;
		}

		String zeros = "";
		for (int i = 0; i < MAX; i++) {
			zeros += "0";
		}

		hex = (zeros + hex).substring(hex.length(), hex.length() + MAX);
		byte[] data = new byte[MAX / 2];

		for (int i = 0; i < hex.length(); i += 2) {
			data[i/2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4) | Character.digit(hex.charAt(i + 1), 16)); 
		}

		return data;
	}

	public void readBytes() throws IOException {

		if (bytes != null) {
			return;
		}

		String err = "File " + BR + " is not a valid boot sector!";

		if (BR == null || !BR.canRead() || BR.length() < BPS) {
			System.out.println(err);
			return;
		}

		bytes = new byte[BPS];
		InputStream is = null;
		try {
			is = new FileInputStream(BR);
			is.read(bytes);
		} finally {
			if (is != null) is.close();
		}

		// signature verification
		String sign = String.format("%02x", bytes[510] & 0xFF) + String.format("%02x", bytes[511] & 0xFF);
		if (!BOOT_SIGN.equalsIgnoreCase(sign)) {
			System.out.println(err);
			bytes = null;
		}
	}

	/**
	 * write bytes in file
	 * @param bytes
	 * @param file
	 * @throws IOException
	 */
	private void writeToFile(File file) throws IOException {

		String err  = "File " + file + " not writable or an invalid boot sector!";

		if (file == null || (file.exists() && !file.delete()) || bytes == null) {
			System.out.println(err);
			return;
		}

		OutputStream fos = null;

		try {
			fos = new FileOutputStream(file);
			fos.write(bytes);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fos != null) try {
				fos.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * write in the boot file the new size or start (data or fat)
	 * @param newDataSize : new data size in byte
	 * @param data : DATA or FAT
	 */
	public static void writeParts(Map<String, Long> diffs) throws IOException {

		long sysDiffSize	= diffs.get(Util.SYS  )/BPS;
		long cacheDiffSize	= diffs.get(Util.CACHE)/BPS;
		long dataDiffSize	= diffs.get(Util.DATA )/BPS;
		long fatDiffSize	= Util.FAT_PRESENT ? diffs.get(Util.FAT)/BPS : 0;

		int partNb;
		Map<Integer, BootRecord> part;
		BootRecord br;
		File file;
		long diffStart;

		// SYS
		part = parts.get(Util.SYS);
		partNb = (Integer) part.keySet().toArray()[0];
		br = part.get(partNb);
		file = br.BR;
		// patch the size
		br.writeSectors(partNb, false, br.getPartSize(partNb) + sysDiffSize);
		br.writeToFile(new File(file.getAbsolutePath() + "_MOD"));
		diffStart = 0;

		// CACHE
		part = parts.get(Util.CACHE);
		partNb = (Integer) part.keySet().toArray()[0];
		br = part.get(partNb);
		file = br.BR;
		// patch the start and size
		br.writeSectors(partNb, true, br.getPartStart(partNb) + sysDiffSize + diffStart);
		br.writeSectors(partNb, false, br.getPartSize(partNb) + cacheDiffSize);
		br.writeToFile(new File(file.getAbsolutePath() + "_MOD"));
		diffStart += sysDiffSize;

		// DATA
		part = parts.get(Util.DATA);
		partNb = (Integer) part.keySet().toArray()[0];
		br = part.get(partNb);
		file = br.BR;
		// patch the start and size
		br.writeSectors(partNb, true, br.getPartStart(partNb) + cacheDiffSize + diffStart);
		br.writeSectors(partNb, false, br.getPartSize(partNb) + dataDiffSize);
		br.writeToFile(new File(file.getAbsolutePath() + "_MOD"));
		diffStart += cacheDiffSize;

		// FAT
		part = parts.get(Util.FAT);
		partNb = (Integer) part.keySet().toArray()[0];
		br = part.get(partNb);
		file = br.BR;
		if (Util.FAT_PRESENT) {
			// patch the start and size
			br.writeSectors(partNb, true, br.getPartStart(partNb) + dataDiffSize + diffStart);
			br.writeSectors(partNb, false, br.getPartSize(partNb) + fatDiffSize);
		}
		br.writeToFile(new File(file.getAbsolutePath() + "_MOD"));
	}

	public void detectParts(Map<String, Long> offsets, long offsetEBR1, long offsetEBR2) throws IOException {

		long offset, offsetEBR = offsetMBR/BPS;

		offsetEBR += (offsetEBR1 + offsetEBR2);

		for (String type: offsets.keySet()) {

			long start = offsets.get(type);

			for (int i = 0; i < 4; i++) { // browse the 4 partitions in the partition table
				if (getPartType(i) > 5) {
					offset = offsetEBR + getPartStart(i);
					if (offset == start/BPS) {
						Map<Integer, BootRecord> part = new HashMap<Integer, BootRecord>();
						part.put(i, this);
						parts.put(type, part);
					}
				}
			}
		}
	}

	public void setParent(BootRecord parent) {
		this.parent = parent;
	}

	public String toString() {
		return (BR == null ? "null" : BR.getName());
	}
}
