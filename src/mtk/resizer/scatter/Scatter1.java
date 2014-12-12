package mtk.resizer.scatter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Scatter1 extends Scatter {

	public Scatter1(File file) throws Exception {
		super(file);
	}

	public void load() throws IOException {

		BufferedReader reader = new BufferedReader(new FileReader(file));
		Info info = new Info();
		String line;
		boolean changeData = false, changeFat = false;
		boolean changeMbr = false, changeEbr1 = false, changeEbr2 = false;
		String partName = null;

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
				String name = line.substring(line.indexOf(PARTITION_NAME) + PARTITION_NAME.length()).trim();
				if (partName != null && !partName.equals(name)) {
					if (info.partition_name.contains("BR") || info.partition_name.contains(FAT) || info.partition_name.contains(DATA)) {
						infos.add(info);
					}

					info = new Info();
				}

				info.partition_name = name;
				partName = name;
				changeData = info.partition_name.toUpperCase().contains(DATA);
				changeFat  = info.partition_name.toUpperCase().contains(FAT);
				changeMbr  = info.partition_name.toUpperCase().contains(MBR);
				changeEbr1 = info.partition_name.toUpperCase().contains(EBR1);
				changeEbr2 = info.partition_name.toUpperCase().contains(EBR2);

			} else if (line.contains(FILE_NAME)) {
				info.file_name = line.substring(line.indexOf(FILE_NAME) + FILE_NAME.length()).trim();
				if (changeMbr) {
					line = line.replace(info.file_name, MBR_NEW);
				}
				if (changeEbr1) {
					line = line.replace(info.file_name, EBR1_NEW);
				}
				if (changeEbr2) {
					line = line.replace(info.file_name, EBR2_NEW);
				}

			} else if (line.contains(PHYSICAL_START_ADDR)) {
				info.physical_start_addr = line.substring(line.indexOf(PHYSICAL_START_ADDR) + PHYSICAL_START_ADDR.length()).trim();
				if (changeData) {
					dataStart = Long.valueOf(info.physical_start_addr.substring(2), 16);
				}
				if (changeFat) {
					line = line.replace(info.physical_start_addr, FAT_START);
				}

			} else if (line.contains(PARTITION_SIZE)) {
				info.partition_size = line.substring(line.indexOf(PARTITION_SIZE) + PARTITION_SIZE.length()).trim();
				if (changeFat) {
					totalSize += Long.valueOf(info.partition_size.substring(2), 16);
					line = line.replace(info.partition_size, FAT_SIZE);
				}
				if (changeData) {
					dataSize  = Long.valueOf(info.partition_size.substring(2), 16);
					totalSize += dataSize;
					line = line.replace(info.partition_size, DATA_SIZE);
				}
			}

			modScatter.append(line + NL);
		}

		infos.add(info);

		reader.close();
	}
}
