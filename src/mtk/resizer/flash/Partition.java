package mtk.resizer.flash;

import static mtk.resizer.util.Util.BPS;

import java.io.IOException;

import mtk.resizer.br.BootRecord;

public class Partition {

	public String name;
	public int partNb;
	public long start;
	public long size;
	public BootRecord BR;

	public Partition previous;

	public Partition(String name) {
		this.name = name;
	}

	public boolean isComplete() {
		return (start > 0 && size > 0 && BR != null);
	}

	public void write() throws IOException {
		if (isComplete()) {
			BR.setPartStart(partNb, (start/BPS) - BR.offset);
			BR.setPartSize( partNb, size/BPS);
			BR.writeToFile();
		}
	}

	@Override
	public String toString() {
		return name + "[" + start + "," + size + "," + partNb + "," + BR + "]";
	}
}
