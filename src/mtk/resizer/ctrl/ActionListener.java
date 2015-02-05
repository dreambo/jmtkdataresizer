package mtk.resizer.ctrl;

import static mtk.resizer.util.Util.BPS;
import static mtk.resizer.util.Util.BS;
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

	private long sysSize;
	private long cacheSize;
	private long dataSize;
	private long fatSize;

    private Scatter scatter;
    private Flash flash;

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

    	Map<String, String[]> vals = new LinkedHashMap<String, String[]>();
    	String nfos[], tmp0 = "", tmp1 = "";
    	Info info;
    	long prevStart = 0, prevSize = 0;
		addLog("After:");

		for (int i = 0; i < flash.size(); i++) {

			Partition part = flash.get(i);

			if (part.name != Util.FAT || Util.FAT_PRESENT) {
	
				long[] result = getNewSize(part);
				long newSize = result[0];
	    		addLog(part.name + " newSize="	+ newSize	+ " byte (" + result[1] + "%)");

	    		info = scatter.getInfos().get(part.name);
	    		nfos = new String[4];
	    		part.start = prevStart = (i == 0 ? part.start : prevStart + prevSize);
	    		nfos[0] = tmp0 = (i == 0 ? info.linear_start_addr   : "0x" + Long.toHexString(Long.valueOf(tmp0.substring(2), 16) + prevSize));
	    		nfos[1] = tmp1 = (i == 0 ? info.physical_start_addr : "0x" + Long.toHexString(Long.valueOf(tmp1.substring(2), 16) + prevSize));
	    		nfos[2] = "0x" + Long.toHexString(newSize);
	    		part.size  = prevSize  = newSize;
	    		vals.put(part.name, nfos);
			}
    	}

		// MBR, EBR1 and EBR2
		List<BootRecord> bootRecords = new ArrayList<BootRecord>();
		for (Partition part: flash) {
			BootRecord br = part.BR;
			if (br != null && !bootRecords.contains(br)) {
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

    	try {
    		// modify the scatter with this new values
    		scatter.writeMod(vals);
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

	private long[] getNewSize(Partition part) {

		long[] result = new long[2];
		int i = flash.indexOf(part);
		boolean last = (part.name == Util.FAT) || (part.name == Util.DATA && !Util.FAT_PRESENT);
		long percent, size;
		if (last) {
			long sumPercent = 0;
			long sumSize    = 0;
			for (int j = 0; j < i; j++) {
				sumPercent += display.percents[j];
				sumSize    += flash.get(j).size;
			}

			percent = CENT - sumPercent;
			size = totalSize - sumSize;
		} else {
			percent = display.percents[i];
			boolean change = (Util.getPercent(display.percents[i]) != Util.getPercent(display.iniPercents[i]));
			size = (change ? BS * Math.round((totalSize * percent/((double) CENT))/BS) : part.size);
		}

		result[0] = size;
		result[1] = Util.getPercent((int) percent);

		return result;
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
			scatter = ScatterFactory.createScatter(selectedFile);
			display.jpTabScatter.removeAll();

			if (scatter.isComplete()) {

				addLog("");
				addLog(scatter.toString());

				// initialise the block size, if exists
				if (scatter.block_size != null && scatter.block_size.startsWith("0x")) {
					int blockSize = Integer.valueOf(scatter.block_size.substring(2), 16);
					BS = (blockSize > 0 && blockSize % 512 == 0 ? blockSize : BS);
				}

				display.jtScatFile.setText(name);
				createScatterTable(scatter);
				display.jbReset.setEnabled(false);
				display.jbApply.setEnabled(false);

				sysSize		= Long.valueOf(scatter.getInfos().get(Util.SYS  ).partition_size.substring(2), 16);
				cacheSize	= Long.valueOf(scatter.getInfos().get(Util.CACHE).partition_size.substring(2), 16);
				dataSize	= Long.valueOf(scatter.getInfos().get(Util.DATA ).partition_size.substring(2), 16);
				fatSize		= Long.valueOf(scatter.getInfos().get(Util.FAT  ).partition_size.substring(2), 16);

		    	if (fatSize == 0) {
					Info info = scatter.getInfos().get(Util.FAT);
					if (!info.physical_start_addr.equals("0x0")) {
						String size = JOptionPane.showInputDialog(display, "Please provide the FAT size (start with 0x if hex):");
						if (size != null) {
							boolean hex = size.toLowerCase().startsWith("0x");
							try {
								fatSize = Long.valueOf(hex ? size.substring(2) : size, hex ? 16 : 10);
								info.partition_size = "0x" + Long.toHexString(fatSize);
							} catch(Exception e) {
								addLog("Wrong size given: " + size);
							}
						}
					} else {
						addLog("FAT partition not detected, it will not be resized!");						
					}
		    	}

		    	totalSize	= sysSize + cacheSize + dataSize + fatSize;

		    	display.percents[0] = display.iniPercents[0] = Math.round(CENT * (  sysSize /((float) totalSize)));
		    	display.percents[1] = display.iniPercents[1] = Math.round(CENT * (cacheSize /((float) totalSize)));
		    	display.percents[2] = display.iniPercents[2] = Math.round(CENT * ( dataSize /((float) totalSize)));


		    	if (fatSize > 0) {
		    		Util.FAT_PRESENT = true;
		    	} else {
		    		Util.FAT_PRESENT = false;
		    		display.percents[2] = display.iniPercents[2] = 0;
		    	}

		    	flash = scatter.getFlash();
		    	detectParts();
		    	addLog("Flash=" + flash + " --> " + (flash.isComplete() ? "OK" : "BAD") + " --> total size: " + (totalSize = flash.getTotalSize()) + ")");

		    	boolean sizesOk = (sysSize > 0 && cacheSize > 0 && dataSize > 0);// && fatSize > 0);
		    	boolean partsOk = flash.isComplete();

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

		    	//addLog("Partitions: "	+ BootRecord);
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

    	BootRecord MBR  = (scatter.getFile(Util.MBR)  == null ? null : new BootRecord(new File(DEFAULT_DIR, scatter.getFile(Util.MBR)),  scatter.getStart(Util.MBR )/BPS));
    	BootRecord EBR1 = (scatter.getFile(Util.EBR1) == null ? null : new BootRecord(new File(DEFAULT_DIR, scatter.getFile(Util.EBR1)), scatter.getStart(Util.EBR1)/BPS));
    	BootRecord EBR2 = (scatter.getFile(Util.EBR2) == null ? null : new BootRecord(new File(DEFAULT_DIR, scatter.getFile(Util.EBR2)), scatter.getStart(Util.EBR2)/BPS));

    	Map<String, Long> offsets = new LinkedHashMap<String, Long>();

    	for (Partition part: flash) {

    		offsets.put(part.name, part.start);

    		MBR.detectParts(part);
    		if (part.BR == null && EBR1 != null) {
        		EBR1.detectParts(part);
        		if (part.BR == null && EBR2 != null) {
            		EBR2.detectParts(part);
            		if (part.BR == null) {
            			EBR2.detectParts(part);
            		}
        		}
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
		table.setEnabled(true);
		table.setEditingColumn(2);
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
