package mtk.resizer.main;

import javax.swing.JFrame;

import mtk.resizer.gui.JMTKDataResizer;

public class Main {

	public static void main(String[] args) throws Exception {

		/*
		private static final String[] looks = {//"javax.swing.plaf.metal.MetalLookAndFeel",
											   "com.jgoodies.looks.plastic.PlasticXPLookAndFeel",
											   "com.jgoodies.looks.plastic.PlasticLookAndFeel",
											   "com.jgoodies.looks.plastic.Plastic3DLookAndFeel",
											   "javax.swing.plaf.nimbus.NimbusLookAndFeel"};
		*/

    	final JMTKDataResizer display = new JMTKDataResizer();
		display.applyLookAndFeel(-1);
    	display.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    	display.init();
    	display.validate();
    	display.pack();
    	display.setVisible(true);
	}
}
