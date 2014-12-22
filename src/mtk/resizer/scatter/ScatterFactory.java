package mtk.resizer.scatter;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import mtk.resizer.br.BootRecord;
import mtk.resizer.util.Util;

public class ScatterFactory {

	private static Scatter scatter = null;

	public static Scatter createScatter(File file) throws Exception {

		return createScatter(file, false);
	}

	public static Scatter createScatter(File file, boolean force) throws Exception {

		if (scatter == null || force) {
			scatter = new Scatter1(file);
			scatter = (scatter.isComplete() ? scatter : new Scatter2(file));
		}

		return scatter;
	}

	public static Scatter currentScatter() throws Exception {

		return createScatter(null);
	}

	public static void main(String[] args) throws Exception {

		//String dir = "/media/dreambox/WIN8/Users/SONY/Desktop/Tools/MTK/December 2014 zaloha";
		//String dir = "/media/dreambox/WIN8/Users/SONY/Desktop/Tools/MTK/MtkDroidTools/backups/JY-G5S+";
		String dir = "/media/dreambox/WIN8/Users/SONY/Desktop/Tools/MTK/MtkDroidTools/backups/709v92_jbla828_141010_backup_141208-194710/!Files_to_FlashTool";
		Scatter scatter = createScatter(new File(dir, "MT6592_Android_scatter.txt"));

		// test 1
		System.out.println(scatter);

		// test2
    	BootRecord.offsetMBR = scatter.getMBRStart();
    	BootRecord.parts.clear();

    	BootRecord MBR  = (scatter.getMBR()  == null ? null : new BootRecord(new File(dir, scatter.getMBR()),  null));
    	BootRecord EBR1 = (scatter.getEBR1() == null ? null : new BootRecord(new File(dir, scatter.getEBR1()), MBR));
    	BootRecord EBR2 = (scatter.getEBR2() == null ? null : new BootRecord(new File(dir, scatter.getEBR2()), MBR));

    	Map<String, Long> offsets = new LinkedHashMap<String, Long>();
    	for (String type: scatter.getInfos().keySet()) {
    		if (!type.contains("BR")) {
    			long offset = Long.valueOf(scatter.getInfos().get(type).physical_start_addr.substring(2), 16);
    			offsets.put(type, offset);
    		}
    	}

    	long offsetEBR1 = 0;
    	if (MBR != null) MBR.detectParts(offsets, 0, 0);
    	if (BootRecord.parts.size() < 4 && EBR1 != null) {
	    	offsetEBR1 = EBR1.getOffset(true);
	    	EBR1.detectParts(offsets, offsetEBR1, 0);
    	}
    	if (BootRecord.parts.size() < 4 && EBR2 != null) {
	    	long offsetEBR2 = EBR2.getOffset(false);
	    	if (offsetEBR2 == 0) {
	    		EBR2.setParent(EBR1);
	    		offsetEBR2 = EBR2.getOffset(true);
	    	}
    		EBR2.detectParts(offsets, offsetEBR1, offsetEBR2);
    	}

    	System.out.println(BootRecord.parts);

    	Map<String, String[]> vals = new LinkedHashMap<String, String[]>();
    	vals.put(Util.DATA, new String[] {"0x123", "0x124", "0x5678", "data.toto.img"});
    	vals.put(Util.EBR1, new String[] {null, null, null, "data.toto.img"});
    	scatter.modify(vals);
    	System.out.println(scatter.modScatter);
	}
}
