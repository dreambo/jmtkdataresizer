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
		String line, name = null, vals[] = new String[2];

		modScatter = new StringBuffer();

		while ((line = reader.readLine()) != null) {

			if (line.startsWith(Util.SYS)) {
				name = Util.SYS;
				vals = line.split(" ");
				info.file_name = vals[0];
				info.physical_start_addr = vals[1];
				info.linear_start_addr   = info.physical_start_addr; 

			} else if (line.startsWith(Util.CACHE)) {
				name = Util.CACHE;
				vals = line.split(" ");
				info.file_name = vals[0];
				info.physical_start_addr = vals[1];
				info.linear_start_addr   = info.physical_start_addr; 

			} else if (line.startsWith(Util.DATA)) {
				name = Util.DATA;
				vals = line.split(" ");
				info.file_name = vals[0];
				info.physical_start_addr = vals[1];
				info.linear_start_addr   = info.physical_start_addr; 

			} else if (line.contains(Util.FAT)) {
				vals = line.split(" ");
				name = Util.FAT;
				info.file_name = vals[0];
				info.physical_start_addr = vals[1];
				info.linear_start_addr   = info.physical_start_addr; 

			} else if (line.startsWith(Util.MBR)) {
				name = Util.MBR;
				vals = line.split(" ");
				info.file_name = vals[0];
				info.physical_start_addr = vals[1];
				info.linear_start_addr   = info.physical_start_addr; 

			} else if (line.startsWith(Util.EBR1)) {
				name = Util.EBR1;
				vals = line.split(" ");
				info.file_name = vals[0];
				info.physical_start_addr = vals[1];
				info.linear_start_addr   = info.physical_start_addr; 

			} else if (line.startsWith(Util.EBR2)) {
				name = Util.EBR2;
				vals = line.split(" ");
				info.file_name = vals[0];
				info.physical_start_addr = vals[1];
				info.linear_start_addr   = info.physical_start_addr; 
			}

			modScatter.append(line + NL);

			if (info.isComplete()) {

				if (Util.ALL.contains("." + name)) {
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
			info1.partition_size = "0x" + Long.toHexString(offset2 - offset1);
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
			infos.put(Util.FAT, info2);
		}
	}

	/**
	 * make changes in the scatter, with this values
	 * @param vals : ex {"0x123", "0x123", "0x80000", "EBR1_MOD"} linear start, physical start, size and file name
	 * @throws IOException
	 */
	public void modify(Map<String, String[]> vals) {

		String[] lines = modScatter.toString().split(NL);
		String[] oldVals = new String[2];
		StringBuffer newScatter = new StringBuffer();
		String offset, name;

		for (String line: lines) {
			for (String type: vals.keySet()) {
				if (line.toUpperCase().contains(type)) {
					oldVals = line.split(" ");
					offset = vals.get(type)[1];
					name = vals.get(type)[3];
					line = (name == null ? oldVals[0] : name) + " " + (offset == null ? oldVals[1] : offset);
					break;
				}
			}

			newScatter.append(line + NL);
		}

		modScatter = newScatter;
	}

	public Info getInfo(String type) {

		for (String typ: infos.keySet()) {
			if (typ.startsWith(type)) {
				return infos.get(typ);
			}
		}

		return null;
	}
}
