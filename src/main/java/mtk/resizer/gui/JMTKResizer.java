package mtk.resizer.gui;


import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.EtchedBorder;

import mtk.resizer.ctrl.ActionListener;
import mtk.resizer.util.Util;
import static mtk.resizer.util.Util.CENT;

public class JMTKResizer extends JFrame {

	private static final long serialVersionUID = 2L;

	public int look = -1;

	public final static String ABOUT = "JMTKResizer v2.32 by boudhaim@gmail.com";

	public int[]	iniPercents = new int[] {5000, 6000, 8000};
	public int[]	   percents = new int[] {5000, 6000, 8000};

	public JLabel  jlSys;
	public JLabel  jlCache;
	public JLabel  jlData;
	public JLabel  jlFat;
	public JButton jbSys;
	public JButton jbCache;
	public JButton jbData;
	public JButton jbFat;
	public JButton jbApply;
	public JButton jbReset;
	public JTextArea  jtLog;
	public JTextField jtScatFile;

    // main panels
    public ResizerPanel jpResizer;
    public JPanel jpResizer2;
    public JPanel jpScatter;

    // panels of tabs
    private JPanel jpTabResizer;
    public  JPanel jpTabScatter;
    private JPanel jpTabHelp;

	public boolean scatterOK = false;

    public void init() throws Exception {

    	jpTabResizer = new JPanel(new GridLayout(2,1));

    	// the resizer panel
    	jpResizer = new ResizerPanel(this);

    	jpResizer2 = new JPanel(new GridLayout(2, 1));
    	//jpResizer.setBackground(Color.GRAY);
    	jpResizer2.setBorder(new EtchedBorder());
    	jpResizer2.add(jpResizer);

    	ActionListener buttonListener = new ActionListener(this);

    	jtScatFile = new JTextField("put here your scatter file (MT65XX_Android_scatter.txt)");
		jtScatFile.setColumns(40);
		jtScatFile.setEditable(false);
    	jlSys	= new JLabel();
    	jlCache = new JLabel();
    	jlData	= new JLabel();
    	jlFat	= new JLabel();
    	jbSys = new JButton(); jbSys.setToolTipText("Click here to enable/disable resize");
    	jbSys.addActionListener(buttonListener); jbSys.setBackground(Util.SYSCOLOR);
    	jbCache = new JButton(); jbCache.setToolTipText("Click here to enable/disable resize");
    	jbCache.addActionListener(buttonListener); jbCache.setBackground(Util.CACHECOLOR);
    	jbData = new JButton(); jbData.setToolTipText("Click here to enable/disable resize");
    	jbData.addActionListener(buttonListener); jbData.setBackground(Util.DATACOLOR);
    	jbFat = new JButton(); jbFat.setBackground(Util.FATCOLOR);
		jbApply = new JButton("Apply"); jbApply.setEnabled(false);
		jbReset = new JButton("Reset"); jbReset.setEnabled(false);
		jtLog = new JTextArea(ABOUT);
		jtLog.setEditable(false);

		// jpPlus panel
    	JPanel jpPlus = new JPanel(new FlowLayout(FlowLayout.CENTER));
		// + ANDROID
		jpPlus.add(jbSys);
		jpPlus.add(jlSys);
		// + CACHE
		jpPlus.add(jbCache);
		jpPlus.add(jlCache);
		// + DATA
		jpPlus.add(jbData);
		jpPlus.add(jlData);
		// + FAT
		jpPlus.add(jbFat);
		jpPlus.add(jlFat);
		// add the panel plus
		jpResizer2.add(jpPlus);
		jpTabResizer.add(jpResizer2);

		// scatter file panel
		jpScatter = new JPanel(new GridLayout(2, 1));
		jpScatter.setBorder(new EtchedBorder());
		JPanel jpScatFile = new JPanel(new FlowLayout());
		JLabel jlScatFile = new JLabel("The scatter file:");
		jpScatFile.add(jlScatFile);
		jpScatFile.add(jtScatFile);
		JButton jbBrowse = new JButton("...");
		jpScatFile.add(jbBrowse);
		jbBrowse.addActionListener(buttonListener);
		jpScatter.add(jpScatFile);

		// buttons panel
		JPanel jpButtons = new JPanel(new FlowLayout());
		JButton exit = new JButton("Exit");
		exit.setToolTipText("Exit the program");
		exit.addActionListener(buttonListener);
		jpButtons.add(exit);

		//JButton apply = new JButton("Apply");
		jbApply.setToolTipText("Apply the current change");
		jbApply.addActionListener(buttonListener);
		jpButtons.add(jbApply);

		//JButton reset = new JButton("Reset");
		jbReset.setToolTipText("Rest the initial values");
		jbReset.addActionListener(buttonListener);
		jpButtons.add(jbReset);

		JButton jbLook = new JButton("Theme");
		jbLook.setToolTipText("Change the LookAndFeel");
		jbLook.addActionListener(buttonListener);
		jpButtons.add(jbLook);

		jpScatter.add(jpButtons);
		jpTabResizer.add(jpScatter);

		// add jpTabs as tabs
		// Resizer Tab
		JTabbedPane jtabPan = new JTabbedPane();
		jtabPan.addTab("Resizer", jpTabResizer);
		// Scatter Tab
		jpTabScatter = new JPanel();
		jpTabScatter.setLayout(new BorderLayout());
		jpTabScatter.setBorder(new EtchedBorder());
		jtabPan.addTab("Scatter", jpTabScatter);
		// Log Tab
		//JTextArea jtLog = new JTextArea("toto");
		JScrollPane jsLog = new JScrollPane(jtLog);
		jtabPan.addTab("Log", jsLog);
		// Help Tab
		jtabPan.addTab("Help", jpTabHelp = new JPanel(new GridLayout()));
		JTextArea help = new JTextArea(ABOUT + "\n\n");
		help.setText(help.getText() + "To modify the sizes of your partitions :\n\n");
		help.setText(help.getText() + "0- Enter recovery mode and do a full backup\n");
		help.setText(help.getText() + "1- In MtkDroidTools, do a backup for your phone and create SPFlashTool files\n");
		help.setText(help.getText() + "2- Open the scatter created in 1 with this program\n");
		help.setText(help.getText() + "3- First, disable the partitions you do not want to resize, with the small colored buttons\n");
		help.setText(help.getText() + "4- Adjust the sizes you want with the mouse and apply changes\n");
		help.setText(help.getText() + "5- the new scatter is in the same dir: MT65XX_Android_scatter_MOD.txt\n");
		help.setText(help.getText() + "6- Do a \"firmware upgrade\" in SPFlashTool and this modded scatter\n");
		help.setText(help.getText() + "7- Enter the recovery mode and do a full restore\n\n");
		help.setText(help.getText() + "If the Apply button is disabled, see the log tab:\n");
		help.setText(help.getText() + "In general your scatter is incompatible or you do not have the size of FAT in the scatter\n");
		help.setText(help.getText() + "Please use MTKdroidTools to have this information and add it manualy to your scatter\n");
		help.setText(help.getText() + "You can use firmware.info (created by MtkDroidTools) : only copy it to the same folder as the scatter");
		help.setEditable(false);
		jpTabHelp.add(help);
		add(jtabPan);
    }

	public void applyLookAndFeel() {
		// UIManager.setLookAndFeel("com.pagosoft.plaf.PgsLookAndFeel");
    	// PlafOptions and PgsLookAndFeel provide static methods to do both of it:
    	//PlafOptions.setAsLookAndFeel();
    	//PlafOptions.updateAllUIs();
    	//PgsLookAndFeel.setAsLookAndFeel();
    	//PgsLookAndFeel.updateAllUIs();

		LookAndFeelInfo[] infos = UIManager.getInstalledLookAndFeels();

		if (look < 0) {
			for (int i = 0; i < infos.length; i++) {
				if (infos[i].getName().toUpperCase().contains("NIMBUS")) {
					look = i;
					break;
				}
			}
		}

		if (look < 0) {
			for (int i = 0; i < infos.length; i++) {
				if (infos[i].getName().toUpperCase().contains("WINDOW")) {
					look = i;
					break;
				}
			}
		}

		look = (Math.abs(look) % infos.length);

		String lookName = infos[look].getName();

		try {
    		UIManager.setLookAndFeel(infos[look].getClassName());
    		setTitle(JMTKResizer.ABOUT + " - " + lookName);
        	SwingUtilities.updateComponentTreeUI(this);
    		//pack();

    	} catch (Exception e) {
    		e.printStackTrace();
    	}
	}

    private String getSize(int percent) {
    	double size = (ActionListener.totalSize * percent/((float) CENT))/Util.GB;
    	NumberFormat formatter = new DecimalFormat("00.00");

    	return formatter.format(Math.round(size * ((float) CENT))/((float) CENT)) + " GB";
    }

    public void refreshSize() {

    	jlSys  .setText("System: "  + getSize(percents[0]));
		jlCache.setText("Cache: "	+ getSize(percents[1]));
		if (Util.FAT_PRESENT) {
			jlData.setText("Data: "	+ getSize(percents[2]));
			jlFat .setText("Storage: "	+ getSize(CENT - (percents[0] + percents[1] + percents[2])));
		} else {
			jlData.setText("Data: "	+ getSize(CENT - (percents[0] + percents[1])));
			jlFat .setText("Storage: "	+ getSize(0));
		}
	}
}
