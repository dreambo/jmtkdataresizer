package mtk.resizer.ctrl;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileFilter;

import mtk.resizer.br.BootRecord;
import mtk.resizer.gui.JMTKDataResizer;
import mtk.resizer.scatter.IScatter;
import mtk.resizer.scatter.Scatter;
import mtk.resizer.scatter.ScatterFactory;


public class ActionListener implements java.awt.event.ActionListener {

	private String DEFAULT_DIR = "C:/Users/SONY/Desktop/Tools/MTK/MtkDroidTools/backups";

	private static final long MB = 0x100000;	// 1MB = 1048576 Byte;
	private static final long GB = 0x40000000;	// 1GB = 1073741824 Byte;

	private int look;
	private int initPercent;
	private int dataPercent;
	private JMTKDataResizer display;

	private long totalSize;

    private static Scatter scatter;
    private static BootRecord MBR;
    private static BootRecord EBR1;
    private static BootRecord EBR2;

	private boolean enableApply = false;
	
	
	
	public void setDataPercent(int dataPercent) {
		this.dataPercent = dataPercent;
	}

	public ActionListener(JMTKDataResizer display) {
		this.display = display;
	}

	public void actionPerformed(ActionEvent e) {

		JButton source = (JButton) e.getSource();

		if (source.getText().equals("Exit")) {
			//e.display.dispose();
			System.exit(0);
		}

		if (source.getText().equals("Theme")) {
			look = display.applyLookAndFeel(++look);

		} else if (source.getText().equals("Reset")) {
			initValues();
			display.jpResizer.setDataPercent(dataPercent);
			refreshSize();
			display.jpResizer2.repaint();
			display.reset.setEnabled(false);
			display.apply.setEnabled(false);

		} else if (source.getText().equals("Apply")) {
			display.reset.setEnabled(false);
			display.apply.setEnabled(false);
			initPercent = dataPercent;
			long newDataSize = MB * Math.round((totalSize * dataPercent/100d)/MB);
			long newFatSize  = totalSize - newDataSize;
	    	addLog("newDataSize=" + newDataSize);
	    	addLog("newFatSize="  + newFatSize);

	    	scatter.setNewSizes(newDataSize);
	    	scatter.setNewBR(BootRecord.parts);

	    	try {
	    		scatter.writeMod();
	    		scatter.load();
	    		BootRecord.writeParts((newDataSize - scatter.getDataSize())/BootRecord.BPS);
	    	} catch(Exception ex) {
	    		ex.printStackTrace();
	    	}

		} else if (source == display.jbPlusData) {
			setPercent(dataPercent + 1);
			display.apply.setEnabled(enableApply && dataPercent != initPercent);
			display.reset.setEnabled(dataPercent != initPercent);
			display.jpResizer.setDataPercent(dataPercent);
			refreshSize();
			display.jpResizer2.repaint();

		} else if (source == display.jbPlusStor) {
			setPercent(dataPercent - 1);
			display.apply.setEnabled(enableApply && dataPercent != initPercent);
			display.reset.setEnabled(dataPercent != initPercent);
			display.jpResizer.setDataPercent(dataPercent);
			refreshSize();
			display.jpResizer2.repaint();

		} else if (source.getText().equals("...")) {

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

					display.jtScatFile.setText(name);
					createScatterTable(scatter);
					display.jbPlusData.setEnabled(true);
					display.jbPlusStor.setEnabled(true);
					display.reset.setEnabled(false);
					display.apply.setEnabled(false);

			    	long dataSize = scatter.getDataSize();
			    	totalSize = scatter.getTotalSize();

			    	// init
			    	BootRecord.offsetMBR = scatter.getMBRStart();
			    	BootRecord.parts.clear();

			    	long dataStart = scatter.getDataStart()/BootRecord.BPS;
			    	long fatStart  =  scatter.getFatStart()/BootRecord.BPS;

			    	MBR  = (scatter.getMBR()  == null ? null : new BootRecord(new File(DEFAULT_DIR, scatter.getMBR()),  null));
			    	EBR1 = (scatter.getEBR1() == null ? null : new BootRecord(new File(DEFAULT_DIR, scatter.getEBR1()), MBR));
			    	EBR2 = (scatter.getEBR2() == null ? null : new BootRecord(new File(DEFAULT_DIR, scatter.getEBR2()), MBR));

			    	long offsetEBR1 = 0;
			    	if (MBR != null) MBR.detectParts(dataStart, fatStart, -1, -1);
			    	if (BootRecord.parts.size() != 2 && EBR1 != null) {
				    	offsetEBR1 = EBR1.getOffset(true);
				    	EBR1.detectParts(dataStart, fatStart, offsetEBR1, -1);
			    	}
			    	if (BootRecord.parts.size() != 2 && EBR2 != null) {
				    	long offsetEBR2 = EBR2.getOffset(false);
				    	if (offsetEBR2 == 0) {
				    		EBR2.setParent(EBR1);
				    		offsetEBR2 = EBR2.getOffset(true);
				    	}
			    		EBR2.detectParts(dataStart, fatStart, offsetEBR1, offsetEBR2);
			    	}

			    	boolean sizesOk = (totalSize > dataSize);
			    	boolean partsOk = (BootRecord.parts.size() == 2);

			    	if (!sizesOk) {
			    		addLog("FAT and DATA sizes must be present in the scatter file!");
			    		addLog("Please check your scatter (it is made by MtkDroiTools ?)");
			    		addLog("You can manualy add DATA and FAT sizes in the scatter using MtkDroiTools");
			    	}

			    	if (!partsOk) {
			    		addLog("The DATA and FAT partitions must be found!");
			    		addLog("Please check your scatter, MBR, ENR1 and EBR2 files");
			    	}

			    	enableApply = (partsOk && sizesOk);

			    	addLog("Partitions: " + BootRecord.parts);
			    	addLog("dataSize=" +  dataSize);
			    	addLog("fatSize="  + (totalSize - dataSize));

			    	setPercent((int) Math.round(100f * (dataSize /((float) totalSize))));
			    	initPercent = dataPercent;
					display.jpResizer.setDataPercent(dataPercent);
			    	refreshSize();
					display.jpResizer2.repaint();

				} else {
					addLog("Not a valid scatter file: " + name);
					JOptionPane.showMessageDialog(display, name + " is not a valid scatter!");
					display.jbPlusData.setEnabled(false);
					display.jbPlusStor.setEnabled(false);
					display.jtScatFile.setText("");
				}
			} catch(Exception ex) {
				ex.printStackTrace();
				addLog("Not a readeable file: " + name);
				JOptionPane.showMessageDialog(display, name + " is not readeable or not valid!");
				display.jbPlusData.setEnabled(false);
				display.jbPlusStor.setEnabled(false);
				display.jtScatFile.setText("");
			};
		}
	}

    private void initValues() {
    	dataPercent = initPercent;
    }

    private void setPercent(int percent) {
    	dataPercent = (percent < 5 ? 5 : (percent > 95 ? 95 : percent));
    }

    private String getSize(int percent) {
    	double size = (totalSize * percent/100f)/GB;

    	return Math.round(size * 100f)/100f + " GB";
    }

	private void addLog(String msg) {

		if ("".equals(msg)) {
			display.jtLog.setText(JMTKDataResizer.ABOUT + IScatter.NL + IScatter.NL);
		} else {
			System.out.println(msg);
			display.jtLog.setText(display.jtLog.getText() + IScatter.NL + msg);
		}
	}

	private void refreshSize() {
		display.jlData.setText(display.jlData.getText().replaceAll(":.*GB$", ": " + getSize(dataPercent)));
		display.jlStor.setText(display.jlStor.getText().replaceAll(":.*GB$", ": " + getSize(100 - dataPercent)));
	}

	private void createScatterTable(Scatter scatter) {

		List<Scatter.Info> infos = scatter.getInfos();
		String[][] cells = new String[infos.size()][4];
		for (int i = 0; i < infos.size(); i++) {
			cells[i][0] = infos.get(i).partition_name; 
			cells[i][1] = infos.get(i).file_name; 
			cells[i][2] = infos.get(i).physical_start_addr; 
			cells[i][3] = infos.get(i).partition_size;
		}

		JTable table = new JTable(cells, new String[] {IScatter.PARTITION_NAME, IScatter.FILE_NAME, IScatter.PHYSICAL_START_ADDR, IScatter.PARTITION_SIZE});
		table.setEnabled(false);
		display.jpTabScatter.add(table.getTableHeader(), BorderLayout.NORTH);
		display.jpTabScatter.add(table, BorderLayout.CENTER);
		display.jpTabScatter.validate();
	}
}
