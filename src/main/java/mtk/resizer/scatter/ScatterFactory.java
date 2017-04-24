package mtk.resizer.scatter;

import java.io.File;

public class ScatterFactory {

	private static Scatter scatter;

	public static Scatter createScatter(File file) throws Exception {

		scatter = new Scatter1(file);
		scatter = (scatter.isComplete() ? scatter : new Scatter2(file));

		return scatter;
	}
}
