package mtk.resizer.main;

import javax.swing.JFrame;

import mtk.resizer.gui.JMTKResizer;

public class Main {

	public static void main(String[] args) throws Exception {

		/*
		private static final String[] looks = {//"javax.swing.plaf.metal.MetalLookAndFeel",
											   "com.jgoodies.looks.plastic.PlasticXPLookAndFeel",
											   "com.jgoodies.looks.plastic.PlasticLookAndFeel",
											   "com.jgoodies.looks.plastic.Plastic3DLookAndFeel",
											   "javax.swing.plaf.nimbus.NimbusLookAndFeel"};
		*/

    	final JMTKResizer display = new JMTKResizer();
		display.applyLookAndFeel();
    	display.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    	display.init();
    	display.validate();
    	//display.pack();
    	display.setSize(706, 320);
    	display.setVisible(true);
	}
}
