package mtk.resizer.scatter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import mtk.resizer.br.BootRecord;
import mtk.resizer.util.Util;

public abstract class Scatter implements IScatter {

	protected long dataStart;
	protected long dataSize;
	protected long totalSize;

	protected StringBuffer modScatter;
	protected File file; 

	protected String project;
	protected String storage;
	public String block_size;
	protected String platform;

	protected Map<String, Info> infos = new LinkedHashMap<String, Info>();

	public boolean isComplete() {

		boolean sys		= (getInfo(Util.SYS) 	!= null);
		boolean cache	= (getInfo(Util.CACHE)	!= null);
		boolean data	= (getInfo(Util.DATA) 	!= null);
		boolean fat		= (getInfo(Util.FAT) 	!= null);
		boolean mbr		= (getInfo(Util.MBR)  	!= null);

		return sys && cache && data && fat && mbr;
	}

	@Override
	public String toString() {

		String str = file.getAbsolutePath()		+ NL;
		str += PLATFORM		+ " " + platform	+ NL +
			   PROJECT		+ " " + project		+ NL +
			   STORAGE		+ " " + storage		+ NL +
			   BLOCK_SIZE	+ " " + block_size	+ NL;

		str += NL;

		for (String type: infos.keySet()) {
			Info nfo = infos.get(type);
			str += PARTITION_NAME + type + NL;
			str += nfo.toString() + NL;
		}

		return str;
	}

	protected Scatter(File file) throws Exception {

		if (file == null || !file.canRead()) {
			String msg = "Unable to open " + file + ": it exists and readable?";
			System.out.println(msg);

			file = null;

			throw new FileNotFoundException(msg);
		}

		this.file = file;
		load();
	}

	/**
	 * load the scatter from file
	 */
	public abstract void load() throws IOException;

	public void writeMod() throws IOException {

		File modFile = new File(file.getAbsolutePath().replaceAll("(?i)\\.txt", "_MOD.txt"));

		if (modFile.exists() && !modFile.delete()) {
			System.out.println("Error: can not write file: " + modFile);

		} else {
			BufferedWriter bw = new BufferedWriter(new FileWriter(modFile));
			bw.write(modScatter.toString());
			bw.close();
		}
	}

	public void setNewSizes(long newDataSize) {

		set(DATA_SIZE, newDataSize, true);
		set(FAT_START, dataStart + newDataSize, true);
		set(FAT_SIZE,  totalSize - newDataSize, true);
	}

	public void setNewBR(Map<String, Map<Integer, BootRecord>> parts) {

		String name;
		boolean changeMBR = false, changeEBR1 = false, changeEBR2 = false;

		for (String type: parts.keySet()) {
			// get the part of type
			Map<Integer, BootRecord> part = parts.get(type);
			File file = ((BootRecord) part.values().toArray()[0]).BR;
			name = file.getName().toUpperCase();
			if (name.contains(Util.MBR)) {
				changeMBR = true;
			} else if (name.contains(Util.EBR1)) {
				changeEBR1 = true;
			} else if (name.contains(Util.EBR2)) {
				changeEBR2 = true;
			}
		}

		set(MBR_NEW,  getMBR()  + (changeMBR  ? "_MOD" : ""), false);
		set(EBR1_NEW, getEBR1() + (changeEBR1 ? "_MOD" : ""), false);
		set(EBR2_NEW, getEBR2() + (changeEBR2 ? "_MOD" : ""), false);
	}

	private void set(String type, Object value, boolean hex) {

		int i = modScatter.indexOf(type);
		modScatter.replace(i, i + type.length(), (hex ? "0x" + Long.toHexString((Long) value) : value.toString()));
	}

	/* (non-Javadoc)
	 * @see com.ebr.swing.IScatter#getMBRStart()
	 */
	public long getMBRStart() {

		Info info = getInfo(Util.MBR);
		String hex;

		return (info != null && (hex = info.physical_start_addr) != null ? Long.valueOf(hex.substring(2), 16) : 0);
	}

	public long getDataStart() {

		Info info = getInfo(Util.DATA);
		String hex;

		return (info != null && (hex = info.physical_start_addr) != null ? Long.valueOf(hex.substring(2), 16) : 0);
	}

	public long getFatStart() {

		Info info = getInfo(Util.FAT);
		String hex;

		return (info != null && (hex = info.physical_start_addr) != null ? Long.valueOf(hex.substring(2), 16) : 0);
	}

	public long getDataSize() {

		if (dataSize > 0) {
			return dataSize;
		}

		Info info = getInfo(Util.DATA);
		String hex;

		dataSize = ((hex = info.partition_size) != null ? Long.valueOf(hex.substring(2), 16) : 0);
		if (dataSize == 0) {
			dataSize = (getFatStart() - getDataStart());
		}

		return dataSize;
	}

	public long getFatSize() {

		Info info = getInfo(Util.FAT);
		String hex;

		return (info != null && (hex = info.partition_size) != null ? Long.valueOf(hex.substring(2), 16) : 0);
	}

	public long getTotalSize() {

		return (totalSize = (getDataSize() + getFatSize()));
	}

	public String getMBR() {

		Info info = getInfo(Util.MBR);

		return (info != null ? info.file_name : null);
	}

	public String getEBR1() {

		Info info = getInfo(Util.EBR1);

		return (info != null ? info.file_name : null);
	}

	public String getEBR2() {

		Info info = getInfo(Util.EBR2);

		return (info != null ? info.file_name : null);
	}

	private Info getInfo(String type) {

		return infos.get(type);
	}

	public Map<String, Info> getInfos() {
		return infos;
	}


	/**
	 * make changes in the scatter, with this values
	 * @param vals : ex {"0x123", "0x123", "0x80000", "EBR1_MOD"} linear start, physical start, size and file name
	 * @throws IOException
	 */
	abstract public void modify(Map<String, String[]> vals);
}
