package mtk.resizer.scatter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Scatter2 extends Scatter {

	public Scatter2(File file) throws Exception {
		super(file);
	}

	public void load() throws IOException {

		BufferedReader reader = new BufferedReader(new FileReader(file));
		Info info = new Info();
		String line;

		modScatter = new StringBuffer();
		totalSize  = 0;

		while ((line = reader.readLine()) != null) {

			if (line.contains(PLATFORM)) {
				platform = line.substring(line.indexOf(PLATFORM) + PLATFORM.length()).trim();

			} else if (line.contains(PROJECT)) {
				project = line.substring(line.indexOf(PROJECT) + PROJECT.length()).trim();

			} else if (line.contains(STORAGE)) {
				storage = line.substring(line.indexOf(STORAGE) + STORAGE.length()).trim();

			} else if (line.contains(BLOCK_SIZE)) {
				block_size = line.substring(line.indexOf(BLOCK_SIZE) + BLOCK_SIZE.length()).trim();

			} else if (line.contains(DATA)) {
				info.partition_name = DATA;
				info.file_name = info.partition_name;
				info.physical_start_addr = line.substring(line.indexOf(DATA) + DATA.length()).trim();

			} else if (line.contains(FAT)) {
				info.partition_name = FAT;
				info.file_name = info.partition_name;
				info.physical_start_addr = line.substring(line.indexOf(FAT) + FAT.length()).trim();

			} else if (line.contains(MBR)) {
				info.partition_name = MBR;
				info.file_name = info.partition_name;
				info.physical_start_addr = line.substring(line.indexOf(MBR) + MBR.length()).trim();

			} else if (line.contains(EBR1)) {
				info.partition_name = EBR1;
				info.file_name = info.partition_name;
				info.physical_start_addr = line.substring(line.indexOf(EBR1) + EBR1.length()).trim();

			} else if (line.contains(EBR2)) {
				info.partition_name = EBR2;
				info.file_name = info.partition_name;
				info.physical_start_addr = line.substring(line.indexOf(EBR2) + EBR2.length()).trim();
			}

			modScatter.append(line + NL);

			if (info.isComplete()) {
				String name = info.partition_name.toUpperCase();
				if (name.contains("BR") || name.contains(FAT) || name.contains(DATA)) {
					infos.add(info);
				}

				info = new Info();
			}
		}

		reader.close();
	}
}
