package mtk.resizer.ctrl;

import static mtk.resizer.util.Util.BS;
import static mtk.resizer.util.Util.BPS;
import static mtk.resizer.util.Util.CENT;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileFilter;

import mtk.resizer.br.BootRecord;
import mtk.resizer.flash.Flash;
import mtk.resizer.flash.Partition;
import mtk.resizer.gui.JMTKResizer;
import mtk.resizer.scatter.IScatter;
import mtk.resizer.scatter.IScatter.Info;
import mtk.resizer.scatter.Scatter;
import mtk.resizer.scatter.ScatterFactory;
import mtk.resizer.util.Util;

public class ActionListener implements java.awt.event.ActionListener {

	private String DEFAULT_DIR = "/media/dreambox/WIN10/Users/dreambox/Desktop/Tools/MTK/MtkDroidTools/backups";

	private JMTKResizer display;

	public static long totalSize = 8 * Util.GB;	// initial total size

	private static long sysSize;
	private static long cacheSize;
	private static long dataSize;
	private static long fatSize;

    private static Scatter scatter;
    private static Flash flash;

	public ActionListener(JMTKResizer display) {
		this.display = display;
	}

	public void actionPerformed(ActionEvent e) {

		JButton source = (JButton) e.getSource();

		if (source.getText().equals("Exit")) {
			//e.display.dispose();
			System.exit(0);
		}

		if (source.getText().equals("...")) {
			browseForScatter();

		} else if (source.getText().equals("Apply")) {
			applyChanges();
			
		} else if (source.getText().equals("Reset")) {
			initValues();
			display.refreshSize();
			display.jpResizer2.repaint();
			display.jbReset.setEnabled(false);
			display.jbApply.setEnabled(false);

		} else if (source.getText().equals("Theme")) {
			display.look++;
			display.applyLookAndFeel();

		} else {
			handleResizeButtons(source);
		}
	}

    private void applyChanges() {

    	boolean sysChange	= (Util.getPercent(display.percents[0]) != Util.getPercent(display.iniPercents[0]));
    	boolean cacheChange = (Util.getPercent(display.percents[1]) != Util.getPercent(display.iniPercents[1]));
    	boolean dataChange	= (Util.getPercent(display.percents[2]) != Util.getPercent(display.iniPercents[2]) || !Util.FAT_PRESENT);

		long newSysSize		= (sysChange   ? BS * Math.round((totalSize * display.percents[0]/((double) CENT))/BS) : sysSize);
		long newCacheSize	= (cacheChange ? BS * Math.round((totalSize * display.percents[1]/((double) CENT))/BS) : cacheSize);
		long newDataSize	= (Util.FAT_PRESENT ? (dataChange ? BS * Math.round((totalSize * display.percents[2]/((double) CENT))/BS) : dataSize) : totalSize - (newSysSize + newCacheSize));
		long newFatSize		= (Util.FAT_PRESENT ? totalSize - (newSysSize + newCacheSize + newDataSize) : 0);

		addLog("After:");
		addLog("newSysSize="	+ newSysSize	+ " byte (" + Util.getPercent(display.percents[0]) + "%)");
    	addLog("newCacheSize="  + newCacheSize	+ " byte (" + Util.getPercent(display.percents[1]) + "%)");
    	addLog("newDataSize="	+ newDataSize	+ " byte (" + Util.getPercent(Util.FAT_PRESENT ? display.percents[2] : CENT - (display.percents[0] + display.percents[1])) + "%)");
    	if (Util.FAT_PRESENT) {
    		addLog("newFatSize="	+ newFatSize	+ " byte (" + (100 - Util.getPercent(display.percents[0] + display.percents[1] + display.percents[2])) + "%)");
    	}

    	Map<String, String[]> vals = new LinkedHashMap<String, String[]>();
    	Map<String, Long> diffs    = new LinkedHashMap<String, Long>();
    	String nfos[], tmp0, tmp1;

    	// SYS
    	Info sysInfo = scatter.getInfos().get(Util.SYS);
		nfos = new String[4];
		nfos[0] = tmp0 = sysInfo.linear_start_addr;
		nfos[1] = tmp1 = sysInfo.physical_start_addr;
		nfos[2] = "0x" + Long.toHexString(newSysSize);
		vals.put(Util.SYS, nfos);
		diffs.put(Util.SYS, newSysSize - sysSize);

    	// CACHE
		nfos = new String[4];
		nfos[0] = tmp0 = "0x" + Long.toHexString((Long.valueOf(tmp0.substring(2), 16) + newSysSize));
		nfos[1] = tmp1 = "0x" + Long.toHexString((Long.valueOf(tmp1.substring(2), 16) + newSysSize));
		nfos[2] = "0x" + Long.toHexString(newCacheSize);
		vals.put(Util.CACHE, nfos);
		diffs.put(Util.CACHE, newCacheSize - cacheSize);

    	// DATA
		nfos = new String[4];
		nfos[0] = tmp0 = "0x" + Long.toHexString((Long.valueOf(tmp0.substring(2), 16) + newCacheSize));
		nfos[1] = tmp1 = "0x" + Long.toHexString((Long.valueOf(tmp1.substring(2), 16) + newCacheSize));
		nfos[2] = "0x" + Long.toHexString(newDataSize);
		vals.put(Util.DATA, nfos);
		diffs.put(Util.DATA, newDataSize - dataSize);

    	// FAT
		if (Util.FAT_PRESENT) {
			nfos = new String[4];
			nfos[0] = "0x" + Long.toHexString((Long.valueOf(tmp0.substring(2), 16) + newDataSize));
			nfos[1] = "0x" + Long.toHexString((Long.valueOf(tmp1.substring(2), 16) + newDataSize));
			nfos[2] = "0x" + Long.toHexString(newFatSize);
			vals.put(Util.FAT, nfos);
			diffs.put(Util.FAT, newFatSize - fatSize);
		}

		// MBR, EBR1 and EBR2
		List<BootRecord> bootRecords = new ArrayList<BootRecord>();
    	for (String type: BootRecord.parts.keySet()) {

    		Map<Integer, BootRecord> part = BootRecord.parts.get(type);

    		for (BootRecord br: part.values()) {
    			if (!bootRecords.contains(br)) {
    				bootRecords.add(br);
	    			nfos = new String[4];
	    			nfos[3] = br.BR.getName() + "_MOD";
	    			if (br.BR.getName().toUpperCase().contains(Util.MBR)) {
	    				vals.put(Util.MBR, nfos);
	    			} else if (br.BR.getName().toUpperCase().contains(Util.EBR1)) {
	    				vals.put(Util.EBR1, nfos);
	    			} else if (br.BR.getName().toUpperCase().contains(Util.EBR2)) {
	    				vals.put(Util.EBR2, nfos);
	    			}
    			}
    		}
    	}

    	try {
    		// modify the scatter with this new values
    		scatter.modify(vals);
    		scatter.writeMod();

    		BootRecord.writeParts(diffs);

			display.jbReset.setEnabled(false);
			display.jbApply.setEnabled(false);
			display.iniPercents[0] = display.percents[0];
			display.iniPercents[1] = display.percents[1];
			display.iniPercents[2] = display.percents[2];
    	} catch(Exception ex) {
    		ex.printStackTrace();
    		addLog("Error: " + ex);
			JOptionPane.showMessageDialog(display, "some thing was wrong, please see logs!");
    	}
	}

    private void applyChanges3() {

    	boolean sysChange	= (Util.getPercent(display.percents[0]) != Util.getPercent(display.iniPercents[0]));
    	boolean cacheChange = (Util.getPercent(display.percents[1]) != Util.getPercent(display.iniPercents[1]));
    	boolean dataChange	= (Util.getPercent(display.percents[2]) != Util.getPercent(display.iniPercents[2]) || !Util.FAT_PRESENT);

		long newSysSize		= (sysChange   ? BS * Math.round((totalSize * display.percents[0]/((double) CENT))/BS) : sysSize);
		long newCacheSize	= (cacheChange ? BS * Math.round((totalSize * display.percents[1]/((double) CENT))/BS) : cacheSize);
		long newDataSize	= (Util.FAT_PRESENT ? (dataChange ? BS * Math.round((totalSize * display.percents[2]/((double) CENT))/BS) : dataSize) : totalSize - (newSysSize + newCacheSize));
		long newFatSize		= (Util.FAT_PRESENT ? totalSize - (newSysSize + newCacheSize + newDataSize) : 0);

		addLog("After:");
		addLog("newSysSize="	+ newSysSize	+ " byte (" + Util.getPercent(display.percents[0]) + "%)");
    	addLog("newCacheSize="  + newCacheSize	+ " byte (" + Util.getPercent(display.percents[1]) + "%)");
    	addLog("newDataSize="	+ newDataSize	+ " byte (" + Util.getPercent(Util.FAT_PRESENT ? display.percents[2] : CENT - (display.percents[0] + display.percents[1])) + "%)");
    	if (Util.FAT_PRESENT) {
    		addLog("newFatSize="	+ newFatSize	+ " byte (" + (100 - Util.getPercent(display.percents[0] + display.percents[1] + display.percents[2])) + "%)");
    	}

    	Map<String, String[]> vals = new LinkedHashMap<String, String[]>();
    	Map<String, Long> diffs    = new LinkedHashMap<String, Long>();
    	String nfos[], tmp0, tmp1;

    	// SYS
    	flash.parts.get(0).size = 0;
    	Info sysInfo = scatter.getInfos().get(Util.SYS);
		nfos = new String[4];
		nfos[0] = tmp0 = sysInfo.linear_start_addr;
		nfos[1] = tmp1 = sysInfo.physical_start_addr;
		nfos[2] = "0x" + Long.toHexString(newSysSize);
		vals.put(Util.SYS, nfos);
		diffs.put(Util.SYS, newSysSize - sysSize);
    	flash.parts.get(0).size = newSysSize;

    	// CACHE
		nfos = new String[4];
		nfos[0] = tmp0 = "0x" + Long.toHexString((Long.valueOf(tmp0.substring(2), 16) + newSysSize));
		nfos[1] = tmp1 = "0x" + Long.toHexString((Long.valueOf(tmp1.substring(2), 16) + newSysSize));
		nfos[2] = "0x" + Long.toHexString(newCacheSize);
		vals.put(Util.CACHE, nfos);
		diffs.put(Util.CACHE, newCacheSize - cacheSize);
    	flash.parts.get(1).size = newCacheSize;

    	// DATA
		nfos = new String[4];
		nfos[0] = tmp0 = "0x" + Long.toHexString((Long.valueOf(tmp0.substring(2), 16) + newCacheSize));
		nfos[1] = tmp1 = "0x" + Long.toHexString((Long.valueOf(tmp1.substring(2), 16) + newCacheSize));
		nfos[2] = "0x" + Long.toHexString(newDataSize);
		vals.put(Util.DATA, nfos);
		diffs.put(Util.DATA, newDataSize - dataSize);
    	flash.parts.get(2).size = newDataSize;

    	// FAT
		if (Util.FAT_PRESENT) {
			nfos = new String[4];
			nfos[0] = "0x" + Long.toHexString((Long.valueOf(tmp0.substring(2), 16) + newDataSize));
			nfos[1] = "0x" + Long.toHexString((Long.valueOf(tmp1.substring(2), 16) + newDataSize));
			nfos[2] = "0x" + Long.toHexString(newFatSize);
			vals.put(Util.FAT, nfos);
			diffs.put(Util.FAT, newFatSize - fatSize);
	    	flash.parts.get(3).size = newFatSize;
		}

		// MBR, EBR1 and EBR2
		List<BootRecord> bootRecords = new ArrayList<BootRecord>();
    	for (String type: BootRecord.parts.keySet()) {

    		Map<Integer, BootRecord> part = BootRecord.parts.get(type);

    		for (BootRecord br: part.values()) {
    			if (!bootRecords.contains(br)) {
    				bootRecords.add(br);
	    			nfos = new String[4];
	    			nfos[3] = br.BR.getName() + "_MOD";
	    			if (br.BR.getName().toUpperCase().contains(Util.MBR)) {
	    				vals.put(Util.MBR, nfos);
	    			} else if (br.BR.getName().toUpperCase().contains(Util.EBR1)) {
	    				vals.put(Util.EBR1, nfos);
	    			} else if (br.BR.getName().toUpperCase().contains(Util.EBR2)) {
	    				vals.put(Util.EBR2, nfos);
	    			}
    			}
    		}
    	}

    	try {
    		// modify the scatter with this new values
    		scatter.modify(vals);
    		scatter.writeMod();

    		//BootRecord.writeParts(diffs);
    		flash.write();

			display.jbReset.setEnabled(false);
			display.jbApply.setEnabled(false);
			display.iniPercents[0] = display.percents[0];
			display.iniPercents[1] = display.percents[1];
			display.iniPercents[2] = display.percents[2];
    	} catch(Exception ex) {
    		ex.printStackTrace();
    		addLog("Error: " + ex);
			JOptionPane.showMessageDialog(display, "some thing was wrong, please see logs!");
    	}
	}

    private void applyChanges2() {

    	Map<String, String[]> vals = new LinkedHashMap<String, String[]>();
    	String nfos[], tmp0 = null, tmp1 = null, tmp2 = null;
    	Info info;
		addLog("After:");

		for (int i = 0; i < flash.parts.size(); i++) {
			Partition part = flash.parts.get(i);
        	boolean change = (i < display.percents.length ? Util.getPercent(display.percents[i]) != Util.getPercent(display.iniPercents[i]) : true);
    		long newSize   = (change ? (i < display.percents.length ? BS * Math.round((totalSize * display.percents[i]/((double) CENT))/BS) : (totalSize - (flash.parts.get(0).size + flash.parts.get(1).size + flash.parts.get(2).size))) : part.size);
    		addLog(part.name + " newSize="	+ newSize	+ " byte (" + (i < display.percents.length ? Util.getPercent(display.percents[i]) : (100 - Util.getPercent(display.percents[0] + display.percents[1] + display.percents[2]))) + "%)");

    		info = scatter.getInfos().get(part.name);
    		nfos = new String[4];
    		part.start = (i == 0 ? part.start : Long.valueOf(tmp1.substring(2), 16) + Long.valueOf(tmp2.substring(2), 16));
    		nfos[0] = tmp0 = (i == 0 ? info.linear_start_addr   : "0x" + Long.toHexString((Long.valueOf(tmp0.substring(2), 16) + newSize)));
    		nfos[1] = tmp1 = (i == 0 ? info.physical_start_addr : "0x" + Long.toHexString((Long.valueOf(tmp1.substring(2), 16) + newSize)));
    		nfos[2] = tmp2 = "0x" + Long.toHexString(newSize);
    		vals.put(part.name, nfos);
    		part.size  = newSize;
    	}

		// MBR, EBR1 and EBR2
		List<BootRecord> bootRecords = new ArrayList<BootRecord>();
    	for (String type: BootRecord.parts.keySet()) {

    		Map<Integer, BootRecord> part = BootRecord.parts.get(type);

    		for (BootRecord br: part.values()) {
    			if (!bootRecords.contains(br)) {
    				bootRecords.add(br);
	    			nfos = new String[4];
	    			nfos[3] = br.BR.getName() + "_MOD";
	    			if (br.BR.getName().toUpperCase().contains(Util.MBR)) {
	    				vals.put(Util.MBR, nfos);
	    			} else if (br.BR.getName().toUpperCase().contains(Util.EBR1)) {
	    				vals.put(Util.EBR1, nfos);
	    			} else if (br.BR.getName().toUpperCase().contains(Util.EBR2)) {
	    				vals.put(Util.EBR2, nfos);
	    			}
    			}
    		}
    	}

    	try {
    		// modify the scatter with this new values
    		scatter.modify(vals);
    		scatter.writeMod();

    		flash.write();

			display.jbReset.setEnabled(false);
			display.jbApply.setEnabled(false);
			display.iniPercents[0] = display.percents[0];
			display.iniPercents[1] = display.percents[1];
			display.iniPercents[2] = display.percents[2];
    	} catch(Exception ex) {
    		ex.printStackTrace();
    		addLog("Error: " + ex);
			JOptionPane.showMessageDialog(display, "some thing was wrong, please see logs!");
    	}
	}

	private void browseForScatter() {

		JFileChooser jfc = new JFileChooser(DEFAULT_DIR);

		jfc.setFileFilter(new FileFilter() {
			
			@Override
			public String getDescription() {
				return "*scatter*txt";
			}

			@Override
			public boolean accept(File f) {
				return f.getName().toUpperCase().contains("SCATTER") ||
					   f.getName().toUpperCase().endsWith("TXT") ||
					   f.isDirectory();
			}
		});

		String name = null;
		if (JFileChooser.APPROVE_OPTION == jfc.showOpenDialog(display)) try {

			File selectedFile = jfc.getSelectedFile();
			DEFAULT_DIR = selectedFile.getParent();
			name = selectedFile.getParentFile().getName() + "/" + selectedFile.getName();
			scatter = ScatterFactory.createScatter(selectedFile, true);
			display.jpTabScatter.removeAll();

			if (scatter.isComplete()) {

				addLog("");
				addLog(scatter.toString());

				// initialise the block size, if exists
				if (scatter.block_size != null && scatter.block_size.startsWith("0x")) {
					int blockSize = Integer.valueOf(scatter.block_size.substring(2), 16);
					BS = (blockSize > 0 && blockSize % 512 == 0 ? blockSize : BS);
				}

		    	flash = scatter.getFlash();

				display.jtScatFile.setText(name);
				createScatterTable(scatter);
				display.jbReset.setEnabled(false);
				display.jbApply.setEnabled(false);

				sysSize		= Long.valueOf(scatter.getInfos().get(Util.SYS  ).partition_size.substring(2), 16);
				cacheSize	= Long.valueOf(scatter.getInfos().get(Util.CACHE).partition_size.substring(2), 16);
				dataSize	= Long.valueOf(scatter.getInfos().get(Util.DATA ).partition_size.substring(2), 16);
				fatSize		= Long.valueOf(scatter.getInfos().get(Util.FAT  ).partition_size.substring(2), 16);

				totalSize	= sysSize + cacheSize + dataSize + fatSize;

		    	display.percents[0] = display.iniPercents[0] = Math.round(CENT * (  sysSize /((float) totalSize)));
		    	display.percents[1] = display.iniPercents[1] = Math.round(CENT * (cacheSize /((float) totalSize)));
		    	display.percents[2] = display.iniPercents[2] = Math.round(CENT * ( dataSize /((float) totalSize)));

		    	detectParts();
		    	System.out.println("Flash=" + flash + " --> " + flash.isComplete() + " --> " + totalSize + " (" + flash.getTotalSize() + ")");

		    	if (fatSize > 0) {
		    		Util.FAT_PRESENT = true;
		    	} else {
		    		Util.FAT_PRESENT = false;
		    		display.percents[2] = display.iniPercents[2] = 0;
		    	}

		    	boolean sizesOk = (sysSize > 0 && cacheSize > 0 && dataSize > 0);// && fatSize > 0);
		    	boolean partsOk = (BootRecord.parts.size() > (Util.FAT_PRESENT ? 3 : 2));

		    	if (fatSize == 0) {
		    		addLog("FAT partition size is unknown, it can not be resized");
		    		addLog("You can manualy add FAT sizes in the scatter using MtkDroiTools\n");
		    	}

		    	if (!sizesOk) {
		    		addLog("DATA partition size must be present in the scatter file!");
		    		addLog("Please check your scatter (it is made by MtkDroiTools ?)");
		    	}

		    	if (!partsOk) {
		    		addLog("The SYS, CACHE and DATA partitions must be found in boot records!");
		    		addLog("Please check your scatter, MBR, EBR1 and EBR2 files");
		    	}

		    	display.scatterOK = (partsOk && sizesOk);
		    	display.jbApply.setEnabled(false);
		    	display.jbReset.setEnabled(false);

		    	addLog("Partitions: "	+ BootRecord.parts);
		    	addLog("totalSize="		+ totalSize	 + " byte (100%)");
		    	addLog("Before: ");
		    	addLog("sysSize="		+ sysSize	 + " byte (" + Util.getPercent(display.percents[0]) + "%)");
		    	addLog("cacheSize="		+ cacheSize	 + " byte (" + Util.getPercent(display.percents[1]) + "%)");
		    	if (Util.FAT_PRESENT) {
		    		addLog("dataSize="		+ dataSize	 + " byte (" + Util.getPercent(display.percents[2]) + "%)");
		    		addLog("fatSize="		+ (totalSize - (sysSize + cacheSize + dataSize)) + " byte (" + Util.getPercent((CENT - (display.percents[0] + display.percents[1] + display.percents[2]))) + "%)");
		    	} else {
		    		addLog("dataSize="		+ (totalSize - (sysSize + cacheSize)) + " byte (" + (100 - Util.getPercent(display.percents[0] + display.percents[1])) + "%)");
		    	}

				display.jbSys.setBackground(Util.SYSCOLOR);
				display.jbCache.setBackground(Util.CACHECOLOR);
				display.jbData.setBackground(Util.DATACOLOR);

		    	display.refreshSize();
				display.jpResizer2.repaint();

			} else {
				addLog("Not a valid scatter file: " + name);
				JOptionPane.showMessageDialog(display, name + " is not a valid scatter!");
				display.jtScatFile.setText("");
			}
		} catch(Exception ex) {
			ex.printStackTrace();
			addLog("Not a readeable file: " + name);
			JOptionPane.showMessageDialog(display, name + " is not readeable or not valid!");
			display.jtScatFile.setText("");
		};
	}

	private void detectParts() throws Exception {
    	// init
    	BootRecord.offsetMBR = scatter.getMBRStart();
    	BootRecord.parts.clear();

    	BootRecord MBR  = (scatter.getMBR()  == null ? null : new BootRecord(new File(DEFAULT_DIR, scatter.getMBR()), null, scatter.getStart(Util.MBR )/BPS));
    	BootRecord EBR1 = (scatter.getEBR1() == null ? null : new BootRecord(new File(DEFAULT_DIR, scatter.getEBR1()), MBR, scatter.getStart(Util.EBR1)/BPS));
    	BootRecord EBR2 = (scatter.getEBR2() == null ? null : new BootRecord(new File(DEFAULT_DIR, scatter.getEBR2()), MBR, scatter.getStart(Util.EBR2)/BPS));

    	Map<String, Long> offsets = new LinkedHashMap<String, Long>();

    	for (Partition part: flash.parts) {

    		offsets.put(part.name, part.start);

    		MBR.detectParts(part);
    		if (part.BR == null && EBR1 != null) {
        		EBR1.detectParts(part);
        		if (part.BR == null && EBR2 != null) {
            		EBR2.detectParts(part);
            		if (part.BR == null) {
            			EBR2.setParent(EBR1);
            			EBR2.detectParts(part);
            		}
        		}
    		}
    	}

    	System.out.println("Flash=" + flash + " --> " + flash.isComplete() + " --> " + totalSize + " (" + flash.getTotalSize() + ")");

    	/*
    	Map<String, Long> offsets = new LinkedHashMap<String, Long>();
    	for (Partition part: flash.parts) {
    		offsets.put(part.name, part.start);
    	}

    	Map<String, Long> offsets = new LinkedHashMap<String, Long>();
    	for (String type: scatter.getInfos().keySet()) {
    		if (!type.contains("BR")) {
    			long offset = Long.valueOf(scatter.getInfos().get(type).physical_start_addr.substring(2), 16);
    			offsets.put(type, offset);
    		}
    	}
		*/

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

    	// complete the flash partitions with her numbers and BootRecord
    	for (Partition partition: flash.parts) {
    		Map<Integer, BootRecord> part = BootRecord.parts.get(partition.name);
    		if (part != null) {
    			partition.partNb = (Integer) part.keySet().toArray()[0];
    			partition.BR = part.get(partition.partNb);
    		} else {
    			//TODO: flash.parts.remove(partition);
    		}
    	}
	}

	private void initValues() {
    	display.percents[0] = display.iniPercents[0];
    	display.percents[1] = display.iniPercents[1];
    	if (Util.FAT_PRESENT) {
    		display.percents[2] = display.iniPercents[2];
    	}
    }

	private void addLog(String msg) {

		if ("".equals(msg)) {
			display.jtLog.setText(JMTKResizer.ABOUT + IScatter.NL + IScatter.NL);
		} else {
			System.out.println(msg);
			display.jtLog.setText(display.jtLog.getText() + IScatter.NL + msg);
		}
	}

	private void createScatterTable(Scatter scatter) {

		Map<String, Scatter.Info> infos = scatter.getInfos();
		String[][] cells = new String[infos.size()][5];
		int i = 0;

		for (String type: infos.keySet()) {
			cells[i  ][0] = type; 
			cells[i  ][1] = infos.get(type).file_name; 
			cells[i  ][2] = infos.get(type).linear_start_addr; 
			cells[i  ][3] = infos.get(type).physical_start_addr; 
			cells[i++][4] = infos.get(type).partition_size;
		}

		JTable table = new JTable(cells, new String[] {IScatter.PARTITION_NAME, IScatter.FILE_NAME, IScatter.LINEAR_START_ADDR, IScatter.PHYSICAL_START_ADDR, IScatter.PARTITION_SIZE});
		table.setEnabled(false);
		display.jpTabScatter.add(table.getTableHeader(), BorderLayout.NORTH);
		display.jpTabScatter.add(table, BorderLayout.CENTER);
		display.jpTabScatter.validate();
	}

	private void handleResizeButtons(Object source) {

		if (source == display.jbSys) {
			display.jbSys.setBackground(display.jbSys.getBackground() == Util.DARK ? Util.SYSCOLOR : Util.DARK);

		} else if (source == display.jbCache) {
			display.jbCache.setBackground(display.jbCache.getBackground() == Util.DARK ? Util.CACHECOLOR : Util.DARK);

		} if (source == display.jbData && Util.FAT_PRESENT) {
			display.jbData.setBackground(display.jbData.getBackground() == Util.DARK ? Util.DATACOLOR : Util.DARK);
		}
	}
}
