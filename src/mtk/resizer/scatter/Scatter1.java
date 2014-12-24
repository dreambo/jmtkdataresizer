package mtk.resizer.scatter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

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
		totalSize  = 0;
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
}
