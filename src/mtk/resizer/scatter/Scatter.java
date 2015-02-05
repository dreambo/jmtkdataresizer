package mtk.resizer.scatter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import mtk.resizer.flash.Flash;
import mtk.resizer.flash.Partition;
import mtk.resizer.util.Util;

public abstract class Scatter implements IScatter {

	protected long dataStart;
	protected long dataSize;

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

	public void writeMod(Map<String, String[]> vals) throws IOException {

		modify(vals);
		File modFile = new File(file.getAbsolutePath().replaceAll("(?i)\\.txt", "_MOD.txt"));

		if (modFile.getName().equals(file.getName()) || (modFile.exists() && !modFile.delete())) {
			System.out.println("Error: can not write file: " + modFile);

		} else {
			BufferedWriter bw = new BufferedWriter(new FileWriter(modFile));
			bw.write(modScatter.toString());
			bw.close();
		}
	}

	public String getFile(String type) {

		Info info = getInfo(type);

		return (info != null ? info.file_name : null);
	}

	public Info getInfo(String type) {

		return infos.get(type);
	}

	public Map<String, Info> getInfos() {
		return infos;
	}

	public Flash getFlash() {
		Info info;
		Partition part, prev = null;
		Flash flash = new Flash();

		// ANDROID
		part = new Partition(Util.SYS);
		info = getInfo(part.name);
		part.start = Long.valueOf(info.physical_start_addr.substring(2), 16);
		part.size  = Long.valueOf(info.partition_size.substring(2), 16);
		part.previous = prev;
		flash.add(part);
		prev = part;
		// CACHE
		part = new Partition(Util.CACHE);
		info = getInfo(part.name);
		part.start = Long.valueOf(info.physical_start_addr.substring(2), 16);
		part.size  = Long.valueOf(info.partition_size.substring(2), 16);
		part.previous = prev;
		flash.add(part);
		prev = part;
		// DATA
		part = new Partition(Util.DATA);
		info = getInfo(part.name);
		part.start = Long.valueOf(info.physical_start_addr.substring(2), 16);
		part.size  = Long.valueOf(info.partition_size.substring(2), 16);
		part.previous = prev;
		flash.add(part);
		prev = part;
		// FAT
		part = new Partition(Util.FAT);
		info = getInfo(part.name);
		part.start = Long.valueOf(info.physical_start_addr.substring(2), 16);
		part.size  = Long.valueOf(info.partition_size.substring(2), 16);
		part.previous = prev;
		flash.add(part);

		return flash;
	}

	public long getStart(String type) {

		Info info = getInfo(type);
		String start;
		return (info == null || (start = info.physical_start_addr) == null || !start.startsWith("0x") ? -1 : Long.valueOf(start.substring(2), 16));
	}

	public long getSize(String type) {

		Info info = getInfo(type);
		String size;
		return (info == null || (size = info.partition_size) == null || !size.startsWith("0x") ? -1 : Long.valueOf(size.substring(2), 16));
	}

	/**
	 * make changes in the scatter, with this values
	 * @param vals : ex {"0x123", "0x123", "0x80000", "EBR1_MOD"} linear start, physical start, size and file name
	 * @throws IOException
	 */
	abstract protected void modify(Map<String, String[]> vals);
}
