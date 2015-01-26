package mtk.resizer.scatter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import mtk.resizer.util.Util;

public class Scatter2 extends Scatter {

	public Scatter2(File file) throws Exception {
		super(file);
	}

	public void load() throws IOException {

		BufferedReader reader = new BufferedReader(new FileReader(file));
		Info info = new Info();
		String line, name = null;

		modScatter = new StringBuffer();

		while ((line = reader.readLine()) != null) {

			if (line.contains(Util.SYS)) {
				name = Util.SYS;
				info.file_name = name;
				info.physical_start_addr = line.substring(line.indexOf(Util.SYS) + Util.SYS.length()).trim();
				info.linear_start_addr   = info.physical_start_addr; 

			} else if (line.contains(Util.CACHE)) {
				name = Util.CACHE;
				info.file_name = name;
				info.physical_start_addr = line.substring(line.indexOf(Util.CACHE) + Util.CACHE.length()).trim();
				info.linear_start_addr   = info.physical_start_addr; 

			} else if (line.contains(Util.DATA)) {
				name = Util.DATA;
				info.file_name = name;
				info.physical_start_addr = line.substring(line.indexOf(Util.DATA) + Util.DATA.length()).trim();
				info.linear_start_addr   = info.physical_start_addr; 

			} else if (line.contains(Util.FAT)) {
				name = Util.FAT;
				info.file_name = name;
				info.physical_start_addr = line.substring(line.indexOf(Util.FAT) + Util.FAT.length()).trim();
				info.linear_start_addr   = info.physical_start_addr; 

			} else if (line.contains(Util.MBR)) {
				name = Util.MBR;
				info.file_name = name;
				info.physical_start_addr = line.substring(line.indexOf(Util.MBR) + Util.MBR.length()).trim();
				info.linear_start_addr   = info.physical_start_addr; 

			} else if (line.contains(Util.EBR1)) {
				name = Util.EBR1;
				info.file_name = name;
				info.physical_start_addr = line.substring(line.indexOf(Util.EBR1) + Util.EBR1.length()).trim();
				info.linear_start_addr   = info.physical_start_addr; 

			} else if (line.contains(Util.EBR2)) {
				name = Util.EBR2;
				info.file_name = name;
				info.physical_start_addr = line.substring(line.indexOf(Util.EBR2) + Util.EBR2.length()).trim();
				info.linear_start_addr   = info.physical_start_addr; 
			}

			modScatter.append(line + NL);

			if (info.isComplete()) {

				if (Util.ALL.contains("." + name.toUpperCase())) {
					infos.put(name.toUpperCase(), info);
				}

				info = new Info();
			}
		}

		correctSizes(infos);

		reader.close();
	}

	private void correctSizes(Map<String, Info> infos) {

		Info info1 = infos.get(Util.SYS);
		Info info2 = infos.get(Util.CACHE);
		long offset1 = Long.valueOf(info1.physical_start_addr.substring(2), 16);
		long offset2 = Long.valueOf(info2.physical_start_addr.substring(2), 16);
		info1.partition_size = "0x" + Long.toHexString(offset2 - offset1);

		info1 = infos.get(Util.CACHE);
		info2 = infos.get(Util.DATA);
		offset1 = Long.valueOf(info1.physical_start_addr.substring(2), 16);
		offset2 = Long.valueOf(info2.physical_start_addr.substring(2), 16);
		info1.partition_size = "0x" + Long.toHexString(offset2 - offset1);

		info1 = infos.get(Util.DATA);
		info2 = infos.get(Util.FAT);
		offset1 = (info1 == null ? 0 : Long.valueOf(info1.physical_start_addr.substring(2), 16));
		offset2 = (info2 == null ? 0 : Long.valueOf(info2.physical_start_addr.substring(2), 16));
		if (info1 != null) {
			info1.partition_size = "0x" + (offset1 > offset2 ? 0 : Long.toHexString(offset2 - offset1));
		} else {
			info1 = new Info();
			info1.partition_size = "0x0";
			infos.put(Util.DATA, info1);
		}
		if (info2 != null) {
			info2.partition_size = "0x0";
		} else {
			info2 = new Info();
			info2.partition_size = "0x0";
			infos.put(Util.FAT, info1);
		}
	}

	/**
	 * make changes in the scatter, with this values
	 * @param vals : ex {"0x123", "0x123", "0x80000", "EBR1_MOD"} linear start, physical start, size and file name
	 * @throws IOException
	 */
	public void modify(Map<String, String[]> vals) {

		String[] lines = modScatter.toString().split(NL);
		StringBuffer newScatter = new StringBuffer();
		String name = null;

		for (String line: lines) {
			for (String type: vals.keySet()) {

				if (line.toUpperCase().startsWith(type)) {
					name = vals.get(type)[3];
					line = (name == null ? type : name) + " " + vals.get(type)[1];
					break;
				}
			}

			newScatter.append(line + NL);
		}

		modScatter = newScatter;
	}
}
