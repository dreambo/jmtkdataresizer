package mtk.resizer.flash;

import java.io.IOException;
import java.util.ArrayList;

import mtk.resizer.util.Util;

public class Flash extends ArrayList<Partition> {

	private static final long serialVersionUID = 1L;

	public boolean isComplete() {
		if (size() < 2) {
			return false;
		}

		Partition prev = null;
		for (Partition part: this) {

			if (part == null) {
				return false;
			}

			if (!part.isComplete() && (part.name != Util.FAT || Util.FAT_PRESENT)) {
				return false;
			}

			if (prev != null && part.start != (prev.start + prev.size) && (part.name != Util.FAT || Util.FAT_PRESENT)) {
				return false;
			}

			prev = part;
		}

		return true;
	}

	/**
	 * write all partitions
	 * @throws IOException
	 */
	public void write() throws IOException {
		if (isComplete()) {
			for (Partition part: this) {
				part.write();
			}
		}
	}

	public long getTotalSize() {
		long totalSize = 0;
		for (Partition part: this) {
			totalSize += part.size;
		}

		return totalSize;
	}
}
