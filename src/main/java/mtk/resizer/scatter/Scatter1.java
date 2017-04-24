package mtk.resizer.scatter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import mtk.resizer.util.Util;

public class Scatter1 extends Scatter {

	public Scatter1(File file) throws Exception {
		super(file);
	}

	public void load() throws IOException {

		BufferedReader reader = new BufferedReader(new FileReader(file));
		Info info = new Info();
		String line;
		String name = null, partName = null;

		modScatter = new StringBuffer();
		infos.clear();

		while ((line = reader.readLine()) != null) {

			if (line.contains(PLATFORM)) {
				platform = line.substring(line.indexOf(PLATFORM) + PLATFORM.length()).trim();

			} else if (line.contains(PROJECT)) {
				project = line.substring(line.indexOf(PROJECT) + PROJECT.length()).trim();

			} else if (line.contains(STORAGE)) {
				storage = line.substring(line.indexOf(STORAGE) + STORAGE.length()).trim();

			} else if (line.contains(BLOCK_SIZE)) {
				block_size = line.substring(line.indexOf(BLOCK_SIZE) + BLOCK_SIZE.length()).trim();

			} else if (line.contains(PARTITION_NAME)) {
				name = line.substring(line.indexOf(PARTITION_NAME) + PARTITION_NAME.length()).trim();
				if (partName != null && !partName.equals(name) && Util.ALL.contains(("." + partName).toUpperCase())) {
					infos.put(partName, info);
					info = new Info();
				}

				partName = name;

			} else if (line.contains(FILE_NAME)) {
				info.file_name = line.substring(line.indexOf(FILE_NAME) + FILE_NAME.length()).trim();

			} else if (line.contains(LINEAR_START_ADDR)) {
				info.linear_start_addr = line.substring(line.indexOf(LINEAR_START_ADDR) + LINEAR_START_ADDR.length()).trim();

			} else if (line.contains(PHYSICAL_START_ADDR)) {
				info.physical_start_addr = line.substring(line.indexOf(PHYSICAL_START_ADDR) + PHYSICAL_START_ADDR.length()).trim();

			} else if (line.contains(PARTITION_SIZE)) {
				info.partition_size = line.substring(line.indexOf(PARTITION_SIZE) + PARTITION_SIZE.length()).trim();
			}

			modScatter.append(line + NL);
		}

		if (Util.ALL.contains(("." + name).toUpperCase())) {
			infos.put(name, info);
		}

		reader.close();
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

				if (line.contains(PARTITION_NAME)) {
					name = line.substring(line.indexOf(PARTITION_NAME) + PARTITION_NAME.length()).trim();
	
				} else if (line.contains(LINEAR_START_ADDR)) {
					if (type.equalsIgnoreCase(name) && vals.get(type)[0] != null) {
						String linear_start_addr = line.substring(line.indexOf(LINEAR_START_ADDR) + LINEAR_START_ADDR.length()).trim();
						line = line.replace(linear_start_addr, vals.get(type)[0]);
					}
	
				} else if (line.contains(PHYSICAL_START_ADDR)) {
					if (type.equalsIgnoreCase(name) && vals.get(type)[1] != null) {
						String physical_start_addr = line.substring(line.indexOf(PHYSICAL_START_ADDR) + PHYSICAL_START_ADDR.length()).trim();
						line = line.replace(physical_start_addr, vals.get(type)[1]);
					}
	
				} else if (line.contains(PARTITION_SIZE)) {
					if (type.equalsIgnoreCase(name) && vals.get(type)[2] != null) {
						String partition_size = line.substring(line.indexOf(PARTITION_SIZE) + PARTITION_SIZE.length()).trim();
						line = line.replace(partition_size, vals.get(type)[2]);
					}
	
				} else if (line.contains(FILE_NAME)) {
					if (type.equalsIgnoreCase(name) && vals.get(type)[3] != null) {
						String file_name = line.substring(line.indexOf(FILE_NAME) + FILE_NAME.length()).trim();
						line = line.replace(file_name, vals.get(type)[3]);
					}
				} 
			}

			newScatter.append(line + NL);
		}

		modScatter = newScatter;
	}
}
