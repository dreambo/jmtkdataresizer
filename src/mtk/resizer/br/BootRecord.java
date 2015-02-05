package mtk.resizer.br;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import mtk.resizer.flash.Partition;

public class BootRecord implements IBootRecord {

	public File BR;
	private byte[] bytes;
	public long offset;

	public BootRecord(File BR, long offset) throws Exception {
		this.BR = BR;
		this.offset = offset;
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
	 * @throws IOException
	 */
	public void writeToFile() throws IOException {

		File file = new File(BR.getAbsolutePath() + "_MOD");
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

	public void detectParts(Partition part) throws IOException {

		//long totalOffset = getTotalOffset();

		// browse the 4 partitions in the partition table
		for (int i = 0; i < 4; i++) {
			if (getPartType(i) > 5 && BPS*(offset + getPartStart(i)) == part.start) {
				part.partNb = i;
				part.BR = this;
				return;
			}
		}
	}

	public String toString() {
		return (BR == null ? "null" : BR.getName());
	}
}
