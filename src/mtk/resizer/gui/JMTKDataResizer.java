package mtk.resizer.gui;


import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.ImageIcon;
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

public class JMTKDataResizer extends JFrame {

	private static final long serialVersionUID = 2L;

	public final static String ABOUT = "JMTKDataResizer v1.0";

	public JTextField jtScatFile;
	public JLabel jlData;
	public JLabel jlStor;
	public JButton jbPlusData;
	public JButton jbPlusStor;
	public JButton apply;
	public JButton reset;
	public JTextArea jtLog;

    // main panels
    public JPanel jpResizer2;
    public JPanel jpScatter;
    public ResizerPanel jpResizer;

    // panels of tabs
    private JPanel jpTabResizer;
    public  JPanel jpTabScatter;
    private JPanel jpTabHelp;

    public void init() throws Exception {

    	jpTabResizer = new JPanel(new GridLayout(2,1));

    	// the resizer panel
    	jpResizer = new ResizerPanel();

    	jpResizer2 = new JPanel(new GridLayout(2, 1));
    	//jpResizer.setBackground(Color.GRAY);
    	jpResizer2.setBorder(new EtchedBorder());
    	jpResizer2.add(jpResizer);

    	jtScatFile = new JTextField("put here your scatter file (MT65XX_Android_scatter.txt)");
		jtScatFile.setColumns(35);
		jtScatFile.setEditable(false);
    	jlData = new JLabel("Data: 4 GB");
    	jlStor = new JLabel("Storage: 12 GB");
    	jbPlusData = new JButton(new ImageIcon(JMTKDataResizer.class.getClassLoader().getResource("images/plus_blue.png")));
    	jbPlusData.setEnabled(false);
    	jbPlusStor = new JButton(new ImageIcon(JMTKDataResizer.class.getClassLoader().getResource("images/plus_green.png")));
    	jbPlusStor.setEnabled(false);
		apply = new JButton("Apply"); apply.setEnabled(false);
		reset = new JButton("Reset"); reset.setEnabled(false);
		jtLog = new JTextArea(ABOUT);

    	ActionListener buttonListener = new ActionListener(this);

		// jpPlus panel
		JPanel jpPlus = new JPanel(new GridLayout(1, 2));
		//jpPlus.setBorder(new EtchedBorder());
		JPanel jpPlusLeft  = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel jpPlusRight = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		// + Data
		jpPlusLeft.add(jbPlusData);
		jbPlusData.addActionListener(buttonListener);
		jpPlusLeft.add(jlData);
		// + Storage
		jpPlusRight.add(jlStor);
		jpPlusRight.add(jbPlusStor);
		jbPlusStor.addActionListener(buttonListener);
		// add the panel plus
		//jpPlusLeft.setBorder(new EtchedBorder());
		//jpPlusRight.setBorder(new EtchedBorder());
		jpPlus.add(jpPlusLeft);
		jpPlus.add(jpPlusRight);
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
		apply.setToolTipText("Apply the current change");
		apply.addActionListener(buttonListener);
		jpButtons.add(apply);

		//JButton reset = new JButton("Reset");
		reset.setToolTipText("Rest the initial values");
		reset.addActionListener(buttonListener);
		jpButtons.add(reset);

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
		JTextArea help = new JTextArea("\nTo increase/decrease your DATA partition :\n\n");
		help.setText(help.getText() + "1- In MtkDroidTools, do a backup for your phone and create SPFlashTool files\n");
		help.setText(help.getText() + "2- Open the scatter created in 1 in this program\n");
		help.setText(help.getText() + "3- Adjust the sizes you want with the PLUS buttons\n");
		help.setText(help.getText() + "4- Apply changes and flash the modded scatter in SPFlashTool\n");
		help.setText(help.getText() + "5- the new scatter is in the same dir: MT65XX_Android_scatter_MOD.txt\n\n");
		help.setText(help.getText() + "If the Apply button is disabled, see the log tab:\n");
		help.setText(help.getText() + "In general your scatter is incompatible or you do not have the size of FAT in the scatter\n");
		help.setText(help.getText() + "Please use MTKdroidTools to have this information and add it manualy to your scatter\n");
		help.setEditable(false);
		jpTabHelp.add(help);
		add(jtabPan);
    }

	public int applyLookAndFeel(int look) {
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
    		setTitle(JMTKDataResizer.ABOUT + " - " + lookName);
        	SwingUtilities.updateComponentTreeUI(this);
    		pack();

    	} catch (Exception e) {
    		e.printStackTrace();
    	}

		return look;
	}
}
