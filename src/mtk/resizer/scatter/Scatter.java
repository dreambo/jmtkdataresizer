package mtk.resizer.scatter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mtk.resizer.br.BootRecord;

public abstract class Scatter implements IScatter {

	protected long dataStart;
	protected long dataSize;
	protected long totalSize;

	protected StringBuffer modScatter;
	protected File file; 

	protected String platform;
	protected String project;
	protected String storage;
	protected String block_size;

	protected List<Info> infos = new ArrayList<Info>();

	public boolean isComplete() {

		return (getInfo(DATA) != null && getInfo(FAT) != null && getInfo(MBR) != null);
	}

	@Override
	public String toString() {

		String str = file.getAbsolutePath();
		str += PLATFORM		+ " " + platform	+ NL +
			   PROJECT		+ " " + project		+ NL +
			   STORAGE		+ " " + storage		+ NL +
			   BLOCK_SIZE	+ " " + block_size	+ NL;

		str += NL;

		for (Info nfo: this.infos) {
			str += nfo.toString();
			str += NL;
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

		File modFile = new File(file.getAbsolutePath().replaceAll("(?i)\\.txt", "_mod.txt"));

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
			if (name.contains(MBR)) {
				changeMBR = true;
			} else if (name.contains(EBR1)) {
				changeEBR1 = true;
			} else if (name.contains(EBR2)) {
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

		Info info = getInfo(MBR);
		String hex;

		return (info != null && (hex = info.physical_start_addr) != null ? Long.valueOf(hex.substring(2), 16) : 0);
	}

	public long getDataStart() {

		Info info = getInfo(DATA);
		String hex;

		return (info != null && (hex = info.physical_start_addr) != null ? Long.valueOf(hex.substring(2), 16) : 0);
	}

	public long getFatStart() {

		Info info = getInfo(FAT);
		String hex;

		return (info != null && (hex = info.physical_start_addr) != null ? Long.valueOf(hex.substring(2), 16) : 0);
	}

	public long getDataSize() {

		if (dataSize > 0) {
			return dataSize;
		}

		Info info = getInfo(DATA);
		String hex;

		dataSize = ((hex = info.partition_size) != null ? Long.valueOf(hex.substring(2), 16) : 0);
		if (dataSize == 0) {
			dataSize = (getFatStart() - getDataStart());
		}

		return dataSize;
	}

	public long getFatSize() {

		Info info = getInfo(FAT);
		String hex;

		return (info != null && (hex = info.partition_size) != null ? Long.valueOf(hex.substring(2), 16) : 0);
	}

	public long getTotalSize() {

		return (totalSize = (getDataSize() + getFatSize()));
	}

	public String getMBR() {

		Info info = getInfo(MBR);

		return (info != null ? info.file_name : null);
	}

	public String getEBR1() {

		Info info = getInfo(EBR1);

		return (info != null ? info.file_name : null);
	}

	public String getEBR2() {

		Info info = getInfo(EBR2);

		return (info != null ? info.file_name : null);
	}

	private Info getInfo(String type) {
		for (Info info: infos) {
			if (info.partition_name != null && info.partition_name.toUpperCase().contains(type)) {
				return info;
			}
		}

		return null;
	}

	public List<Info> getInfos() {
		return infos;
	}
}
