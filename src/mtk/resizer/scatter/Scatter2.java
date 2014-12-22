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

			if (line.contains(PLATFORM)) {
				platform = line.substring(line.indexOf(PLATFORM) + PLATFORM.length()).trim();

			} else if (line.contains(PROJECT)) {
				project = line.substring(line.indexOf(PROJECT) + PROJECT.length()).trim();

			} else if (line.contains(STORAGE)) {
				storage = line.substring(line.indexOf(STORAGE) + STORAGE.length()).trim();

			} else if (line.contains(BLOCK_SIZE)) {
				block_size = line.substring(line.indexOf(BLOCK_SIZE) + BLOCK_SIZE.length()).trim();

			} else if (line.contains(Util.SYS)) {
				name = Util.SYS;
				info.file_name = name;
				info.physical_start_addr = line.substring(line.indexOf(Util.SYS) + Util.SYS.length()).trim();

			} else if (line.contains(Util.CACHE)) {
				name = Util.CACHE;
				info.file_name = name;
				info.physical_start_addr = line.substring(line.indexOf(Util.CACHE) + Util.CACHE.length()).trim();

			} else if (line.contains(Util.DATA)) {
				name = Util.DATA;
				info.file_name = name;
				info.physical_start_addr = line.substring(line.indexOf(Util.DATA) + Util.DATA.length()).trim();

			} else if (line.contains(Util.FAT)) {
				name = Util.FAT;
				info.file_name = name;
				info.physical_start_addr = line.substring(line.indexOf(Util.FAT) + Util.FAT.length()).trim();

			} else if (line.contains(Util.MBR)) {
				name = Util.MBR;
				info.file_name = name;
				info.physical_start_addr = line.substring(line.indexOf(Util.MBR) + Util.MBR.length()).trim();

			} else if (line.contains(Util.EBR1)) {
				name = Util.EBR1;
				info.file_name = name;
				info.physical_start_addr = line.substring(line.indexOf(Util.EBR1) + Util.EBR1.length()).trim();

			} else if (line.contains(Util.EBR2)) {
				name = Util.EBR2;
				info.file_name = name;
				info.physical_start_addr = line.substring(line.indexOf(Util.EBR2) + Util.EBR2.length()).trim();
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
		offset1 = Long.valueOf(info1.physical_start_addr.substring(2), 16);
		offset2 = Long.valueOf(info2.physical_start_addr.substring(2), 16);
		info1.partition_size = "0x" + Long.toHexString(offset2 - offset1);
		info2.partition_size = "0x0";
	}
}
