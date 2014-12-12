package mtk.resizer.scatter;

import java.io.File;

public class ScatterFactory {

	public static Scatter createScatter(File file) throws Exception {

		Scatter scatter = new Scatter1(file);
		scatter = (scatter.isComplete() ? scatter : new Scatter2(file));

		return scatter;
	}
}
