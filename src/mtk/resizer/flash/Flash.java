package mtk.resizer.flash;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Flash {

	public List<Partition> parts = new ArrayList<Partition>();

	public boolean isComplete() {
		if (parts.size() < 2) {
			return false;
		}

		Partition prev = null;
		for (int i = 0; i < parts.size(); i++) {
			if (parts.get(i) == null) {
				return false;
			}

			if (prev != parts.get(i).previous) {
				return false;
			}

			if (prev != null && parts.get(i).start != (prev.start + prev.size)) {
				return false;
			}

			prev = parts.get(i);
		}

		return true;
	}

	/**
	 * write all partitions
	 * @throws IOException
	 */
	public void write() throws IOException {
		if (isComplete()) for (Partition part: parts) {
			part.write();
		}
	}

	public long getTotalSize() {
		long totalSize = 0;
		if (isComplete()) for (Partition part: parts) {
			totalSize += part.size;
		}
	
		return totalSize;
	}

	@Override
	public String toString() {
		return parts.toString();
	}
}
